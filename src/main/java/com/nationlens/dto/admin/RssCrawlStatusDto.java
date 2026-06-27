package com.nationlens.dto.admin;

import java.time.LocalDateTime;

public record RssCrawlStatusDto(
        long totalSources,
        long activeSources,
        long totalNewsItems,
        LocalDateTime lastFetchedAt
) {
}
