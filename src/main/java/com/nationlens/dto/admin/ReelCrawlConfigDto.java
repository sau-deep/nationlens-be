package com.nationlens.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReelCrawlConfigDto {
    private final String hashtag;
    private final String label;
    private final String sectionKey;
    private final String displayContext;
    private final String audienceScope;
    private final String mappingType;
    private final String subMenuKey;
    private final String tags;
}
