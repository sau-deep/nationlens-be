package com.nationlens.controller.admin;

import com.nationlens.domain.entity.NlEntity;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.entity.EntitySummaryDto;
import com.nationlens.repository.NlEntityRepository;
import com.nationlens.service.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/entities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminEntityController {

    private final EntityService entityService;
    private final NlEntityRepository nlEntityRepository;

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
}
