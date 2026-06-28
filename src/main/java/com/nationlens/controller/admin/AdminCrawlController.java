package com.nationlens.controller.admin;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.PendingMediaDto;
import com.nationlens.dto.admin.ReelCrawlConfigDto;
import com.nationlens.dto.admin.ReelCrawlResultDto;
import com.nationlens.dto.admin.ReelCrawlStatusDto;
import com.nationlens.dto.admin.ReelCrawlTriggerRequest;
import com.nationlens.dto.admin.RssCrawlStatusDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.RssFeedSourceRepository;
import com.nationlens.repository.RssNewsItemRepository;
import com.nationlens.service.ReelCrawlService;
import com.nationlens.service.RssFeedIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
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

    @PostMapping("/rss/trigger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerRss() {
        rssFeedIngestionService.ingestAllFeeds();
        return ResponseEntity.ok(ApiResponse.ok("RSS ingestion triggered",
                Map.of("triggered", true)));
    }

    @GetMapping("/reels/status")
    public ResponseEntity<ApiResponse<ReelCrawlStatusDto>> reelStatus() {
        return ResponseEntity.ok(ApiResponse.ok(reelCrawlService.getStatus()));
    }

    @GetMapping("/reels/configs")
    public ResponseEntity<ApiResponse<List<ReelCrawlConfigDto>>> reelConfigs() {
        return ResponseEntity.ok(ApiResponse.ok(reelCrawlService.listPresets()));
    }

    @PostMapping("/reels/trigger")
    public ResponseEntity<ApiResponse<List<ReelCrawlResultDto>>> triggerReels(
        @RequestBody(required = false) ReelCrawlTriggerRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReelCrawlTriggerRequest req = request != null ? request : new ReelCrawlTriggerRequest();
        Long userId = extractUserId(userDetails);
        List<ReelCrawlResultDto> results = reelCrawlService.trigger(req, userId);
        int saved = results.stream().mapToInt(ReelCrawlResultDto::getSaved).sum();
        return ResponseEntity.ok(ApiResponse.ok(
            "Reel crawl complete — " + saved + " new reel(s) queued for review",
            results
        ));
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.nationlens.domain.entity.User u) {
            return u.getId();
        }
        return null;
    }
}
