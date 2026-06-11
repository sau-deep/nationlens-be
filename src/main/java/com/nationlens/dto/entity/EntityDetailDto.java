package com.nationlens.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EntityDetailDto {
    private Long id;
    private String slug;
    private String nameEn;
    private String nameHi;
    private String descriptionEn;
    private String descriptionHi;
    private String imageUrl;
    private String entityTypeCode;
    private String entityTypeNameEn;
    private Boolean verified;
    private String status;

    private String districtNameEn;
    private String districtSlug;

    // PoliticalProfile fields
    private String education;
    private Integer declaredCriminalCases;
    private Long declaredAssetsInr;
    private Long declaredLiabilitiesInr;
    private BigDecimal parliamentAttendancePct;
    private Integer questionsRaised;
    private Integer billsIntroduced;
    private Integer termStartYear;
    private Integer accountabilityScore;
    private String affidavitSourceUrl;
}
