package com.nationlens.controller.moderator;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.PendingMediaDto;
import com.nationlens.dto.admin.ReelCrawlStatusDto;
import com.nationlens.dto.admin.RssCrawlStatusDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.RssFeedSourceRepository;
import com.nationlens.repository.RssNewsItemRepository;
import com.nationlens.service.ReelCrawlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/moderator/crawl")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR')")
public class ModeratorCrawlController {

    private final RssFeedSourceRepository rssFeedSourceRepository;
    private final RssNewsItemRepository rssNewsItemRepository;
    private final MediaLinkRepository mediaLinkRepository;
    private final ReelCrawlService reelCrawlService;

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
        long pendingCount = mediaLinkRepository.countByApprovalStatus(ApprovalStatus.PENDING_REVIEW);
        return ResponseEntity.ok(ApiResponse.ok(new PendingMediaDto(pendingCount)));
    }

    @GetMapping("/reels/status")
    public ResponseEntity<ApiResponse<ReelCrawlStatusDto>> reelStatus() {
        return ResponseEntity.ok(ApiResponse.ok(reelCrawlService.getStatus()));
    }
}
