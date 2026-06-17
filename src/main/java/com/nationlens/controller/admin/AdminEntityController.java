package com.nationlens.controller.admin;

import com.nationlens.domain.entity.EntityType;
import com.nationlens.domain.entity.NlEntity;
import com.nationlens.domain.entity.PoliticalProfile;
import com.nationlens.dto.admin.CreateCrawlerEntityRequest;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.entity.EntitySummaryDto;
import com.nationlens.repository.EntityTypeRepository;
import com.nationlens.repository.NlEntityRepository;
import com.nationlens.repository.PoliticalProfileRepository;
import com.nationlens.service.EntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/admin/entities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminEntityController {

    private final EntityService entityService;
    private final NlEntityRepository nlEntityRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final PoliticalProfileRepository politicalProfileRepository;
    private final com.nationlens.repository.MediaMappingRepository mediaMappingRepository;

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    /**
     * Bulk-import endpoint used by nationlens_crawler.py.
     * Creates an entity + optional political profile from crawled data.
     * Skips (returns 200) if slug already exists (idempotent).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFromCrawler(
            @Valid @RequestBody CreateCrawlerEntityRequest req) {

        // Resolve or build slug
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? req.getSlug()
                : NON_SLUG.matcher(req.getNameEn().toLowerCase()).replaceAll("-");
        if (slug.length() > 295) slug = slug.substring(0, 295);

        // Skip duplicates
        if (nlEntityRepository.findBySlug(slug).isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok("already_exists",
                    Map.of("slug", slug, "skipped", true)));
        }

        // Resolve entity type (default to MP if not found)
        EntityType entityType = entityTypeRepository.findByCode(req.getEntityTypeCode())
                .orElseGet(() -> entityTypeRepository.findByCode("MP")
                        .orElseThrow(() -> new IllegalStateException("Entity type MP not seeded")));

        NlEntity entity = new NlEntity();
        entity.setNameEn(req.getNameEn());
        entity.setNameHi(req.getNameHi());
        entity.setSlug(slug);
        entity.setEntityType(entityType);
        entity.setDescriptionEn(req.getDescriptionEn());
        entity.setDescriptionHi(req.getDescriptionHi());
        entity.setImageUrl(req.getImageUrl());
        entity.setVerified(false);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        nlEntityRepository.save(entity);

        // Create political profile if this is an MP/MLA
        if (Set.of("MP", "MLA", "RAJYA_SABHA_MP").contains(entityType.getCode())) {
            PoliticalProfile pp = new PoliticalProfile();
            pp.setEntity(entity);
            pp.setEducation(req.getEducation());
            pp.setDeclaredCriminalCases(
                    req.getCriminalCasesTotal() != null ? req.getCriminalCasesTotal() : 0);
            pp.setDeclaredAssetsInr(req.getTotalAssetsInr());
            pp.setDeclaredLiabilitiesInr(req.getTotalLiabilitiesInr());
            pp.setAffidavitSourceUrl(req.getAffidavitUrl());
            pp.setTermStartYear(req.getElectionYear());
            pp.setCreatedAt(LocalDateTime.now());
            pp.setUpdatedAt(LocalDateTime.now());
            politicalProfileRepository.save(pp);
        }

        return ResponseEntity.ok(ApiResponse.ok("created",
                Map.of("id", entity.getId(), "slug", entity.getSlug(), "skipped", false)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EntitySummaryDto>>> listEntities(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(entityService.listEntities(pageable)));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEntity(@PathVariable Long id) {
        NlEntity entity = nlEntityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        entity.setVerified(true);
        entity.setUpdatedAt(LocalDateTime.now());
        nlEntityRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.ok("Entity verified", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        }
        NlEntity entity = nlEntityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        entity.setStatus(status);
        entity.setUpdatedAt(LocalDateTime.now());
        nlEntityRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", null));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        NlEntity entity = nlEntityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        java.util.List<com.nationlens.domain.entity.MediaMapping> mappings = mediaMappingRepository.findByEntityId(id);
        if (!mappings.isEmpty()) {
            mediaMappingRepository.deleteAll(mappings);
        }
        nlEntityRepository.delete(entity);
        return ResponseEntity.noContent().build();
    }
}
