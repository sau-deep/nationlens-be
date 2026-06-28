package com.nationlens.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReelCrawlResultDto {
    private final String hashtag;
    private final String sectionKey;
    private final int found;
    private final int saved;
    private final int skipped;
    private final int failed;
    private final List<String> errors;
    private final LocalDateTime completedAt;
}
