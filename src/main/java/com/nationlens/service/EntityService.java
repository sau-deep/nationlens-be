package com.nationlens.service;

import com.nationlens.domain.entity.NlEntity;
import com.nationlens.domain.entity.PoliticalProfile;
import com.nationlens.dto.entity.EntityDetailDto;
import com.nationlens.dto.entity.EntitySummaryDto;
import com.nationlens.repository.NlEntityRepository;
import com.nationlens.repository.PoliticalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntityService {

    private final NlEntityRepository entityRepository;
    private final PoliticalProfileRepository profileRepository;

    public Page<EntitySummaryDto> listEntities(Pageable pageable) {
        return entityRepository.findAllActive(pageable).map(this::toSummary);
    }

    public List<EntitySummaryDto> listByDistrict(Long districtId) {
        return entityRepository.findByDistrictIdAndStatusOrderByNameEnAsc(districtId, "ACTIVE")
            .stream().map(this::toSummary).toList();
    }

    public List<EntitySummaryDto> listByType(String typeCode) {
        return entityRepository.findByEntityTypeCode(typeCode)
            .stream().map(this::toSummary).toList();
    }

    public List<EntitySummaryDto> listFeaturedForHome(int limit) {
        return entityRepository.findFeaturedForHome().stream()
            .limit(limit)
            .map(this::toSummary)
            .toList();
    }

    public Optional<EntityDetailDto> findBySlug(String slug) {
        return entityRepository.findBySlug(slug).map(entity -> {
            Optional<PoliticalProfile> profile = profileRepository.findByEntityId(entity.getId());
            return toDetail(entity, profile.orElse(null));
        });
    }

    private EntitySummaryDto toSummary(NlEntity e) {
        EntitySummaryDto.EntitySummaryDtoBuilder b = EntitySummaryDto.builder()
            .id(e.getId())
            .slug(e.getSlug())
            .nameEn(e.getNameEn())
            .nameHi(e.getNameHi())
            .imageUrl(e.getImageUrl())
            .verified(e.getVerified());

        if (e.getEntityType() != null) {
            b.entityTypeCode(e.getEntityType().getCode())
             .entityTypeNameEn(e.getEntityType().getNameEn());
        }
        if (e.getDistrict() != null) {
            b.districtNameEn(e.getDistrict().getNameEn());
        }

        profileRepository.findByEntityId(e.getId())
            .ifPresent(p -> b.accountabilityScore(p.getAccountabilityScore()));

        b.isFeatured(e.getIsFeatured())
         .homeDisplayOrder(e.getHomeDisplayOrder());

        return b.build();
    }

    @Transactional
    public EntitySummaryDto updateHomeFeatured(Long id, Boolean isFeatured, Integer homeDisplayOrder) {
        NlEntity entity = entityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        if (isFeatured != null) entity.setIsFeatured(isFeatured);
        if (homeDisplayOrder != null) entity.setHomeDisplayOrder(homeDisplayOrder);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        entityRepository.save(entity);
        return toSummary(entity);
    }

    private EntityDetailDto toDetail(NlEntity e, PoliticalProfile p) {
        EntityDetailDto.EntityDetailDtoBuilder b = EntityDetailDto.builder()
            .id(e.getId())
            .slug(e.getSlug())
            .nameEn(e.getNameEn())
            .nameHi(e.getNameHi())
            .descriptionEn(e.getDescriptionEn())
            .descriptionHi(e.getDescriptionHi())
            .imageUrl(e.getImageUrl())
            .verified(e.getVerified())
            .status(e.getStatus());

        if (e.getEntityType() != null) {
            b.entityTypeCode(e.getEntityType().getCode())
             .entityTypeNameEn(e.getEntityType().getNameEn());
        }
        if (e.getDistrict() != null) {
            b.districtNameEn(e.getDistrict().getNameEn())
             .districtSlug(e.getDistrict().getSlug());
        }

        if (p != null) {
            b.education(p.getEducation())
             .declaredCriminalCases(p.getDeclaredCriminalCases())
             .declaredAssetsInr(p.getDeclaredAssetsInr())
             .declaredLiabilitiesInr(p.getDeclaredLiabilitiesInr())
             .parliamentAttendancePct(p.getParliamentAttendancePct())
             .questionsRaised(p.getQuestionsRaised())
             .billsIntroduced(p.getBillsIntroduced())
             .termStartYear(p.getTermStartYear())
             .accountabilityScore(p.getAccountabilityScore())
             .affidavitSourceUrl(p.getAffidavitSourceUrl());
        }

        return b.build();
    }
}
