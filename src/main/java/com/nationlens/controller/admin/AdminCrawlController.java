package com.nationlens.controller.admin;

import com.nationlens.dto.admin.PendingMediaDto;
import com.nationlens.dto.admin.RssCrawlStatusDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.RssFeedSourceRepository;
import com.nationlens.repository.RssNewsItemRepository;
import com.nationlens.service.RssFeedIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/crawl")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminCrawlController {

    private final RssFeedSourceRepository rssFeedSourceRepository;
    private final RssNewsItemRepository rssNewsItemRepository;
    private final MediaLinkRepository mediaLinkRepository;
    private final RssFeedIngestionService rssFeedIngestionService;

    @GetMapping("/rss/status")
    public ResponseEntity<ApiResponse<RssCrawlStatusDto>> rssStatus() {
        long totalSources = rssFeedSourceRepository.count();
        long activeSources = rssFeedSourceRepository.findByActiveTrue().size();
        long totalNewsItems = rssNewsItemRepository.count();
        LocalDateTime lastFetchedAt = rssFeedSourceRepository.findMaxLastFetchedAt().orElse(null);

        return ResponseEntity.ok(ApiResponse.ok(
                new RssCrawlStatusDto(totalSources, activeSources, totalNewsItems, lastFetchedAt)));
    }

    @GetMapping("/media/pending")
    public ResponseEntity<ApiResponse<PendingMediaDto>> pendingMedia() {
        long pendingCount = mediaLinkRepository.countByModerationStatus("PENDING");
        return ResponseEntity.ok(ApiResponse.ok(new PendingMediaDto(pendingCount)));
    }

    @PostMapping("/rss/trigger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerRss() {
        rssFeedIngestionService.ingestAllFeeds();
        return ResponseEntity.ok(ApiResponse.ok("RSS ingestion triggered",
                Map.of("triggered", true)));
    }
}
