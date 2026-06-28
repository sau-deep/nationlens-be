package com.nationlens.service;

import com.nationlens.domain.entity.AiInsight;
import com.nationlens.dto.insight.AiInsightDto;
import com.nationlens.repository.AiInsightRepository;
import com.nationlens.repository.DistrictRepository;
import com.nationlens.repository.NlEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiInsightService {

    private final AiInsightRepository aiInsightRepository;
    private final NlEntityRepository entityRepository;
    private final DistrictRepository districtRepository;

    public List<AiInsightDto> listPublishedForHome(int limit) {
        return aiInsightRepository.findPublishedForHome().stream()
            .limit(limit)
            .map(this::toDto)
            .toList();
    }

    private AiInsightDto toDto(AiInsight i) {
        AiInsightDto.AiInsightDtoBuilder b = AiInsightDto.builder()
            .id(i.getId())
            .insightType(i.getInsightType())
            .titleEn(i.getTitleEn())
            .titleHi(i.getTitleHi())
            .bodyEn(i.getBodyEn())
            .bodyHi(i.getBodyHi())
            .entityId(i.getEntityId())
            .districtId(i.getDistrictId())
            .isPlaceholder(i.getIsPlaceholder())
            .generatedAt(i.getGeneratedAt() != null ? i.getGeneratedAt() : i.getCreatedAt());

        if (i.getEntityId() != null) {
            entityRepository.findById(i.getEntityId())
                .ifPresent(e -> b.entitySlug(e.getSlug()));
        }
        if (i.getDistrictId() != null) {
            districtRepository.findById(i.getDistrictId())
                .ifPresent(d -> b.districtSlug(d.getSlug()));
        }

        return b.build();
    }
}
