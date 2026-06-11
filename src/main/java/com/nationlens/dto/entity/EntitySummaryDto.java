package com.nationlens.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EntitySummaryDto {
    private Long id;
    private String slug;
    private String nameEn;
    private String nameHi;
    private String imageUrl;
    private String entityTypeCode;
    private String entityTypeNameEn;
    private Boolean verified;
    private String districtNameEn;
    private Integer accountabilityScore;
}
