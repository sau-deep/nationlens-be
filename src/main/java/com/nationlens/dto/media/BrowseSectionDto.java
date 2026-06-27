package com.nationlens.dto.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A top-level browse section (Politics, Environment, Government, Media,
 * Judiciary, Citizen) with its approved-media count.
 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrowseSectionDto {
    private String key;
    private String labelEn;
    private String labelHi;
    private String description;
    private String icon;
    private String accent;
    private long count;
}
