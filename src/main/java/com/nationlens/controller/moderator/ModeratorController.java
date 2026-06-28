package com.nationlens.controller.moderator;

import com.nationlens.domain.entity.User;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.PendingMediaDto;
import com.nationlens.dto.admin.ReelCrawlStatusDto;
import com.nationlens.dto.admin.RssCrawlStatusDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.dto.moderator.ModerationQueueDto;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.RssFeedSourceRepository;
import com.nationlens.repository.RssNewsItemRepository;
import com.nationlens.service.ModerationService;
import com.nationlens.service.ReelCrawlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR','DISTRICT_MODERATOR','FACT_CHECKER')")
public class ModeratorController {

    private final ModerationService moderationService;

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<ModerationQueueDto>> getQueue(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getQueue(user)));
    }

    @PatchMapping("/media/{id}/approve")
    public ResponseEntity<ApiResponse<MediaLinkDto>> approveMedia(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long moderatorId = userDetails instanceof User u ? u.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(moderationService.approveMedia(id, moderatorId)));
    }

    @PatchMapping("/media/{id}/reject")
    public ResponseEntity<ApiResponse<MediaLinkDto>> rejectMedia(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long moderatorId = userDetails instanceof User u ? u.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(moderationService.rejectMedia(id, moderatorId)));
    }

    @PatchMapping("/media/{id}/hide")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR')")
    public ResponseEntity<ApiResponse<MediaLinkDto>> hideMedia(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long moderatorId = userDetails instanceof User u ? u.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(moderationService.hideMedia(id, moderatorId)));
    }

    @PatchMapping("/media/{id}/fact-check")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','FACT_CHECKER')")
    public ResponseEntity<ApiResponse<MediaLinkDto>> factCheckNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long moderatorId = userDetails instanceof User u ? u.getId() : null;
        String note = body.getOrDefault("note", "");
        return ResponseEntity.ok(ApiResponse.ok(moderationService.addFactCheckNote(id, note, moderatorId)));
    }

    @PatchMapping("/comments/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR','DISTRICT_MODERATOR')")
    public ResponseEntity<ApiResponse<Void>> approveComment(@PathVariable Long id) {
        moderationService.approveComment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/comments/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR','DISTRICT_MODERATOR')")
    public ResponseEntity<ApiResponse<Void>> rejectComment(@PathVariable Long id) {
        moderationService.rejectComment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private User requireUser(UserDetails userDetails) {
        if (userDetails instanceof User u) return u;
        throw new IllegalStateException("Authenticated user required");
    }
}
