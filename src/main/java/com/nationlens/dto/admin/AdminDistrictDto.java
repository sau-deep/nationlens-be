package com.nationlens.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDistrictDto {
    private Long id;
    private String slug;
    private String nameEn;
    private String nameHi;
    private String stateCode;
    private String stateNameEn;
    private Boolean isFeatured;
    private Integer homeDisplayOrder;
    private Long population;
}
