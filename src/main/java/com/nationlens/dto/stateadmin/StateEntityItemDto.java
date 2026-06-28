package com.nationlens.dto.stateadmin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StateEntityItemDto {
    private Long id;
    private String nameEn;
    private String nameHi;
    private String entityTypeCode;
    private Boolean verified;
    private String status;
    private String slug;
}
