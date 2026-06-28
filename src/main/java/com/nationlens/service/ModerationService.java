package com.nationlens.service;

import com.nationlens.domain.entity.Comment;
import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.entity.User;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.dto.moderator.ModerationQueueDto;
import com.nationlens.dto.moderator.ModerationQueueDto.CommentQueueItem;
import com.nationlens.repository.CommentRepository;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.MediaMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationService {

    private final MediaLinkRepository mediaLinkRepository;
    private final MediaMappingRepository mediaMappingRepository;
    private final CommentRepository commentRepository;
    private final MediaService mediaService;
    private final UserScopeService userScopeService;

    public ModerationQueueDto getQueue(User moderator) {
        UserScopeService.UserScope scope = userScopeService.resolve(moderator);
        String role = scope.primaryRole();

        List<MediaLink> pendingMedia = resolvePendingMedia(scope, role);
        List<Comment> pendingComments = resolvePendingComments(scope, role);

        List<MediaLinkDto> pendingMediaDtos = pendingMedia.stream()
            .map(this::toMediaDto)
            .toList();

        List<CommentQueueItem> commentItems = pendingComments.stream()
            .map(this::toCommentQueueItem)
            .toList();

        return ModerationQueueDto.builder()
            .pendingMedia(pendingMediaDtos)
            .pendingComments(commentItems)
            .totalPending(pendingMediaDtos.size() + commentItems.size())
            .build();
    }

    /** @deprecated use getQueue(User) */
    public ModerationQueueDto getQueue() {
        List<MediaLink> pendingMediaLinks = mediaLinkRepository
            .findByApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(ApprovalStatus.PENDING_REVIEW)
            .stream().limit(50).toList();
        List<Comment> pendingComments = commentRepository
            .findByModerationStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING")
            .stream().limit(50).toList();
        return ModerationQueueDto.builder()
            .pendingMedia(pendingMediaLinks.stream().map(this::toMediaDto).toList())
            .pendingComments(pendingComments.stream().map(this::toCommentQueueItem).toList())
            .totalPending(pendingMediaLinks.size() + pendingComments.size())
            .build();
    }

    private List<MediaLink> resolvePendingMedia(UserScopeService.UserScope scope, String role) {
        List<MediaLink> all = mediaLinkRepository
            .findByApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(ApprovalStatus.PENDING_REVIEW);

        if (Set.of("SUPER_ADMIN", "NATIONAL_ADMIN", "CONTENT_MODERATOR").contains(role)) {
            return all.stream().limit(50).toList();
        }
        if ("DISTRICT_MODERATOR".equals(role) && scope.districtId() != null) {
            return mediaMappingRepository
                .findByDistrictIdAndStatus(scope.districtId(), ApprovalStatus.PENDING_REVIEW)
                .stream().limit(50).toList();
        }
        if ("FACT_CHECKER".equals(role)) {
            // Fact-checkers see media items that need source review (all pending for now)
            return all.stream().limit(50).toList();
        }
        return all.stream().limit(50).toList();
    }

    private List<Comment> resolvePendingComments(UserScopeService.UserScope scope, String role) {
        if ("FACT_CHECKER".equals(role)) {
            return List.of(); // fact-checkers focus on media sources only
        }
        List<Comment> all = commentRepository
            .findByModerationStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING");

        if ("DISTRICT_MODERATOR".equals(role) && scope.districtId() != null) {
            var districtMediaIds = mediaMappingRepository
                .findByDistrictId(scope.districtId())
                .stream().map(MediaLink::getId).toList();
            return all.stream()
                .filter(c -> c.getMediaLinkId() != null && districtMediaIds.contains(c.getMediaLinkId()))
                .limit(50).toList();
        }
        return all.stream().limit(50).toList();
    }

    @Transactional
    public MediaLinkDto approveMedia(Long id, Long moderatorId) {
        return mediaService.updateStatus(id, ApprovalStatus.APPROVED, moderatorId);
    }

    @Transactional
    public MediaLinkDto rejectMedia(Long id, Long moderatorId) {
        return mediaService.updateStatus(id, ApprovalStatus.REJECTED, moderatorId);
    }

    @Transactional
    public MediaLinkDto hideMedia(Long id, Long moderatorId) {
        return mediaService.updateStatus(id, ApprovalStatus.HIDDEN, moderatorId);
    }

    @Transactional
    public MediaLinkDto addFactCheckNote(Long id, String note, Long moderatorId) {
        MediaLink ml = mediaLinkRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + id));
        ml.setModerationNotes(note);
        ml.setApprovedBy(moderatorId);
        ml.setUpdatedAt(LocalDateTime.now());
        mediaLinkRepository.save(ml);
        return mediaService.findById(id).orElseThrow();
    }

    @Transactional
    public void approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        comment.setModerationStatus("APPROVED");
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Transactional
    public void rejectComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        comment.setIsDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private MediaLinkDto toMediaDto(MediaLink ml) {
        return MediaLinkDto.builder()
            .id(ml.getId())
            .platform(ml.getPlatform())
            .contentType(ml.getContentType())
            .sentimentType(ml.getSentimentType())
            .titleEn(ml.getTitleEn())
            .titleHi(ml.getTitleHi())
            .summaryEn(ml.getSummaryEn())
            .summaryHi(ml.getSummaryHi())
            .sourceUrl(ml.getSourceUrl())
            .embedUrl(ml.getEmbedUrl())
            .thumbnailUrl(ml.getThumbnailUrl())
            .sourceOwner(ml.getSourceOwner())
            .sourcePublishedAt(ml.getSourcePublishedAt())
            .isEmbeddable(ml.getIsEmbeddable())
            .noAppSwitchRequired(ml.getNoAppSwitchRequired())
            .approvalStatus(ml.getApprovalStatus())
            .sourceConfidence(ml.getSourceConfidence())
            .displayOrder(ml.getDisplayOrder())
            .createdAt(ml.getCreatedAt())
            .mappings(List.of())
            .build();
    }

    private CommentQueueItem toCommentQueueItem(Comment comment) {
        return CommentQueueItem.builder()
            .id(comment.getId())
            .body(comment.getBody())
            .userDisplayName(comment.getUser() != null ? comment.getUser().getDisplayName() : null)
            .mediaLinkId(comment.getMediaLinkId())
            .moderationStatus(comment.getModerationStatus())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
