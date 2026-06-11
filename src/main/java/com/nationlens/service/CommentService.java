package com.nationlens.service;

import com.nationlens.domain.entity.Comment;
import com.nationlens.domain.entity.User;
import com.nationlens.dto.media.CommentDto;
import com.nationlens.dto.media.CommentRequest;
import com.nationlens.repository.CommentRepository;
import com.nationlens.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public List<CommentDto> getCommentsForMedia(Long mediaLinkId) {
        return commentRepository.findByMediaLinkIdAndIsDeletedFalseOrderByCreatedAtDesc(mediaLinkId)
            .stream().map(this::toDto).toList();
    }

    @Transactional
    public CommentDto addComment(Long mediaLinkId, CommentRequest req, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setMediaLinkId(mediaLinkId);
        comment.setBody(req.getBody());
        comment.setParentId(req.getParentId());
        comment.setUser(user);
        comment.setModerationStatus("APPROVED"); // auto-approve for now
        comment.setCreatedAt(LocalDateTime.now());
        return toDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to delete this comment");
        }
        comment.setIsDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private CommentDto toDto(Comment c) {
        return CommentDto.builder()
            .id(c.getId())
            .body(c.getBody())
            .parentId(c.getParentId())
            .userId(c.getUser().getId())
            .userDisplayName(c.getUser().getDisplayName())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
