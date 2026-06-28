package com.nationlens.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReelCrawlStatusDto {
    private final boolean enabled;
    private final boolean firecrawlConfigured;
    private final int presetCount;
    private final long pendingReviewCount;
    private final LocalDateTime lastRunAt;
    private final int lastRunSaved;
    private final int lastRunSkipped;
    private final String lastRunStatus;
}
