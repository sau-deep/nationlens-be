package com.nationlens.repository;

import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaLinkRepository extends JpaRepository<MediaLink, Long> {
    Page<MediaLink> findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus status, Pageable pageable);
    List<MediaLink> findByApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(ApprovalStatus status);
    long countByModerationStatus(String moderationStatus);
    List<MediaLink> findByOwnerIdAndApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(Long ownerId, ApprovalStatus status);
}
