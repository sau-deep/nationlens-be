package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.CommentDto;
import com.nationlens.dto.media.CommentRequest;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.service.CommentService;
import com.nationlens.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicMediaController {

    private final MediaService mediaService;
    private final CommentService commentService;

    @GetMapping("/media")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listMedia(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<MediaLinkDto> result = mediaService.listApproved(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "content", result.getContent(),
            "totalElements", result.getTotalElements(),
            "totalPages", result.getTotalPages(),
            "number", result.getNumber()
        )));
    }

    @GetMapping("/entities/{entityId}/media")
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> entityMedia(
        @PathVariable Long entityId,
        @RequestParam(required = false) String sectionKey
    ) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getMediaForEntity(entityId, sectionKey)));
    }

    @GetMapping("/districts/{districtId}/media")
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> districtMedia(@PathVariable Long districtId) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getMediaForDistrict(districtId)));
    }

    @GetMapping("/media/{mediaId}/comments")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable Long mediaId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getCommentsForMedia(mediaId)));
    }

    @PostMapping("/media/{mediaId}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
        @PathVariable Long mediaId,
        @Valid @RequestBody CommentRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(commentService.addComment(mediaId, request, userId)));
    }

    @DeleteMapping("/media/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Comment deleted", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.nationlens.domain.entity.User u) {
            return u.getId();
        }
        throw new IllegalStateException("Cannot extract user id");
    }
}
