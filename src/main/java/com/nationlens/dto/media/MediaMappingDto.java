package com.nationlens.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class MediaMappingDto {
    private Long id;
    private String mappingType;
    private Long entityId;
    private Long districtId;
    private String entityTypeCode;
    private String sectionKey;
    private String subMenuKey;
    private String displayContext;
    private Integer displayOrder;
    private Boolean isPrimary;
    private String audienceScope;
    private String stateCode;
    private String tags;
}
