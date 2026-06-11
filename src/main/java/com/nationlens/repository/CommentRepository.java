package com.nationlens.repository;

import com.nationlens.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMediaLinkIdAndIsDeletedFalseAndModerationStatusOrderByCreatedAtDesc(
        Long mediaLinkId, String moderationStatus
    );
    List<Comment> findByMediaLinkIdAndIsDeletedFalseOrderByCreatedAtDesc(Long mediaLinkId);
    List<Comment> findByModerationStatusAndIsDeletedFalseOrderByCreatedAtDesc(String moderationStatus);
}
