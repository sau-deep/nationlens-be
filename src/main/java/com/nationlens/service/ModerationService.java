package com.nationlens.service;

import com.nationlens.domain.entity.Comment;
import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.dto.moderator.ModerationQueueDto;
import com.nationlens.dto.moderator.ModerationQueueDto.CommentQueueItem;
import com.nationlens.repository.CommentRepository;
import com.nationlens.repository.MediaLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationService {

    private final MediaLinkRepository mediaLinkRepository;
    private final CommentRepository commentRepository;
    private final MediaService mediaService;

    public ModerationQueueDto getQueue() {
        List<MediaLink> pendingMediaLinks = mediaLinkRepository
            .findByApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(ApprovalStatus.PENDING_REVIEW)
            .stream()
            .limit(50)
            .toList();

        List<Comment> pendingComments = commentRepository
            .findByModerationStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING")
            .stream()
            .limit(50)
            .toList();

        List<MediaLinkDto> pendingMediaDtos = pendingMediaLinks.stream()
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

    @Transactional
    public MediaLinkDto approveMedia(Long id, Long moderatorId) {
        return mediaService.updateStatus(id, ApprovalStatus.APPROVED, moderatorId);
    }

    @Transactional
    public MediaLinkDto rejectMedia(Long id, Long moderatorId) {
        return mediaService.updateStatus(id, ApprovalStatus.REJECTED, moderatorId);
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
