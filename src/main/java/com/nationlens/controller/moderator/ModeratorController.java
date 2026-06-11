package com.nationlens.controller.moderator;

import com.nationlens.domain.entity.User;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.dto.moderator.ModerationQueueDto;
import com.nationlens.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','CONTENT_MODERATOR','DISTRICT_MODERATOR','FACT_CHECKER')")
public class ModeratorController {

    private final ModerationService moderationService;

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<ModerationQueueDto>> getQueue() {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getQueue()));
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

    @PatchMapping("/comments/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveComment(@PathVariable Long id) {
        moderationService.approveComment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/comments/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectComment(@PathVariable Long id) {
        moderationService.rejectComment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
