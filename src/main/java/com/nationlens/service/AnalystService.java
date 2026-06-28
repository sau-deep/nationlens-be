package com.nationlens.service;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.analyst.AnalystDashboardDto;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalystService {

    private final MediaLinkRepository mediaLinkRepository;
    private final NlEntityRepository entityRepository;
    private final CommentRepository commentRepository;
    private final PollRepository pollRepository;
    private final UserRepository userRepository;
    private final RssNewsItemRepository rssNewsItemRepository;
    private final MediaService mediaService;

    public AnalystDashboardDto getDashboard() {
        return AnalystDashboardDto.builder()
            .totalMedia(mediaLinkRepository.count())
            .approvedMedia(mediaLinkRepository.countByApprovalStatus(ApprovalStatus.APPROVED))
            .pendingMedia(mediaLinkRepository.countByApprovalStatus(ApprovalStatus.PENDING_REVIEW))
            .rejectedMedia(mediaLinkRepository.countByApprovalStatus(ApprovalStatus.REJECTED))
            .totalEntities(entityRepository.count())
            .totalComments(commentRepository.count())
            .activePolls(pollRepository.findByIsActiveTrueOrderByCreatedAtDesc().size())
            .totalUsers(userRepository.count())
            .rssNewsItems(rssNewsItemRepository.count())
            .pendingComments(commentRepository.findByModerationStatusAndIsDeletedFalseOrderByCreatedAtDesc("PENDING").size())
            .build();
    }

    public List<MediaLinkDto> listApprovedMedia(int limit) {
        return mediaLinkRepository
            .findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.APPROVED, PageRequest.of(0, limit))
            .map(ml -> mediaService.findById(ml.getId()).orElse(null))
            .stream()
            .filter(dto -> dto != null)
            .toList();
    }

    public List<MediaLinkDto> listPendingMedia(int limit) {
        return mediaLinkRepository
            .findByApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(ApprovalStatus.PENDING_REVIEW)
            .stream()
            .limit(limit)
            .map(ml -> mediaService.findById(ml.getId()).orElse(null))
            .filter(dto -> dto != null)
            .toList();
    }
}
