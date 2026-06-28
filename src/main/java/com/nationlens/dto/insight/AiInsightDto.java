package com.nationlens.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AiInsightDto {
    private Long id;
    private String insightType;
    private String titleEn;
    private String titleHi;
    private String bodyEn;
    private String bodyHi;
    private Long entityId;
    private String entitySlug;
    private Long districtId;
    private String districtSlug;
    private Boolean isPlaceholder;
    private LocalDateTime generatedAt;
}
