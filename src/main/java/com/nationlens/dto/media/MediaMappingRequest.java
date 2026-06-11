package com.nationlens.dto.media;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MediaMappingRequest {
    @NotBlank
    private String mappingType;

    private Long entityId;
    private Long districtId;
    private Long constituencyId;
    private Long partyEntityId;

    @NotBlank
    private String sectionKey;

    private String subMenuKey;

    @NotBlank
    private String displayContext;

    private Integer displayOrder = 0;
    private Boolean isPrimary = false;
}
