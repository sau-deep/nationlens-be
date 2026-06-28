package com.nationlens.controller.admin;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.UpdateMediaLinkRequest;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.CreateMediaLinkRequest;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MediaLinkDto>>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.listAllForAdmin(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaLinkDto>> getById(@PathVariable Long id) {
        return mediaService.findById(id)
            .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MediaLinkDto>> create(
        @Valid @RequestBody CreateMediaLinkRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Media link created", mediaService.create(request, userId)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<MediaLinkDto>> approve(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(mediaService.updateStatus(id, ApprovalStatus.APPROVED, userId)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<MediaLinkDto>> reject(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(mediaService.updateStatus(id, ApprovalStatus.REJECTED, userId)));
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<ApiResponse<MediaLinkDto>> hide(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(mediaService.updateStatus(id, ApprovalStatus.HIDDEN, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaLinkDto>> update(
        @PathVariable Long id,
        @RequestBody UpdateMediaLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Media link updated", mediaService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/mappings/{mappingId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long mappingId) {
        mediaService.deleteMapping(mappingId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.nationlens.domain.entity.User u) {
            return u.getId();
        }
        throw new IllegalStateException("Cannot extract user id");
    }
}
