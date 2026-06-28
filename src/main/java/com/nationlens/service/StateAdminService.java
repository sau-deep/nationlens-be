package com.nationlens.service;

import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.entity.NlEntity;
import com.nationlens.domain.entity.State;
import com.nationlens.domain.entity.User;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.stateadmin.*;
import com.nationlens.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StateAdminService {

    private final UserScopeService userScopeService;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final NlEntityRepository entityRepository;
    private final MediaMappingRepository mediaMappingRepository;
    private final MediaLinkRepository mediaLinkRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;

    public StateAdminDashboardDto getDashboard(User user) {
        StateContext ctx = requireStateContext(user);
        return StateAdminDashboardDto.builder()
            .stateName(ctx.state().getNameEn())
            .stateCode(ctx.state().getCode())
            .entityCount(entityRepository.countByStateId(ctx.stateId()))
            .mediaCount(mediaMappingRepository.countByStateScope(ctx.stateCode(), ctx.districtIds()))
            .pendingMediaCount(mediaMappingRepository.countByStateScopeAndStatus(
                ctx.stateCode(), ctx.districtIds(), ApprovalStatus.PENDING_REVIEW))
            .districtCount(districtRepository.findByStateIdOrderByNameEnAsc(ctx.stateId()).size())
            .build();
    }

    public List<StateMediaItemDto> listMedia(User user, String statusFilter) {
        StateContext ctx = requireStateContext(user);
        ApprovalStatus status = mapStatusFilter(statusFilter);
        List<MediaLink> items = status != null
            ? mediaMappingRepository.findByStateScopeAndStatus(ctx.stateCode(), ctx.districtIds(), status)
            : mediaMappingRepository.findByStateScope(ctx.stateCode(), ctx.districtIds());
        return items.stream().limit(100).map(this::toMediaItem).toList();
    }

    @Transactional
    public StateMediaItemDto approveMedia(User user, Long mediaId) {
        verifyMediaInScope(user, mediaId);
        MediaLink updated = mediaLinkRepository.findById(mediaId).orElseThrow();
        mediaService.updateStatus(mediaId, ApprovalStatus.APPROVED, user.getId());
        updated.setApprovalStatus(ApprovalStatus.APPROVED);
        return toMediaItem(updated);
    }

    @Transactional
    public StateMediaItemDto rejectMedia(User user, Long mediaId) {
        verifyMediaInScope(user, mediaId);
        mediaService.updateStatus(mediaId, ApprovalStatus.REJECTED, user.getId());
        MediaLink updated = mediaLinkRepository.findById(mediaId).orElseThrow();
        return toMediaItem(updated);
    }

    public List<StateEntityItemDto> listEntities(User user) {
        StateContext ctx = requireStateContext(user);
        return entityRepository.findByStateId(ctx.stateId()).stream()
            .map(this::toEntityItem)
            .toList();
    }

    public List<StateUserItemDto> listUsers(User user) {
        StateContext ctx = requireStateContext(user);
        if (ctx.districtIds().isEmpty()) return List.of();
        return userRepository.findByDistrictIdIn(ctx.districtIds()).stream()
            .map(this::toUserItem)
            .toList();
    }

    private void verifyMediaInScope(User user, Long mediaId) {
        StateContext ctx = requireStateContext(user);
        boolean inScope = mediaMappingRepository.findByStateScope(ctx.stateCode(), ctx.districtIds())
            .stream().anyMatch(m -> m.getId().equals(mediaId));
        if (!inScope) {
            throw new IllegalArgumentException("Media not in your state scope");
        }
    }

    private StateContext requireStateContext(User user) {
        UserScopeService.UserScope scope = userScopeService.resolve(user);
        if (scope.stateId() == null) {
            throw new IllegalStateException("No state assigned to this account. Contact a national admin.");
        }
        State state = stateRepository.findById(scope.stateId())
            .orElseThrow(() -> new IllegalStateException("Assigned state not found"));
        List<Long> districtIds = districtRepository.findByStateIdOrderByNameEnAsc(scope.stateId())
            .stream().map(d -> d.getId()).toList();
        return new StateContext(scope.stateId(), state.getCode(), state, districtIds);
    }

    private ApprovalStatus mapStatusFilter(String filter) {
        if (filter == null || filter.isBlank()) return ApprovalStatus.PENDING_REVIEW;
        return switch (filter.toUpperCase()) {
            case "APPROVED" -> ApprovalStatus.APPROVED;
            case "REJECTED" -> ApprovalStatus.REJECTED;
            case "PENDING", "PENDING_REVIEW" -> ApprovalStatus.PENDING_REVIEW;
            default -> null;
        };
    }

    private StateMediaItemDto toMediaItem(MediaLink m) {
        return StateMediaItemDto.builder()
            .id(m.getId())
            .titleEn(m.getTitleEn())
            .platform(m.getPlatform() != null ? m.getPlatform().name() : null)
            .approvalStatus(m.getApprovalStatus() != null ? m.getApprovalStatus().name() : null)
            .sourceOwner(m.getSourceOwner())
            .createdAt(m.getCreatedAt())
            .build();
    }

    private StateEntityItemDto toEntityItem(NlEntity e) {
        return StateEntityItemDto.builder()
            .id(e.getId())
            .nameEn(e.getNameEn())
            .nameHi(e.getNameHi())
            .entityTypeCode(e.getEntityType() != null ? e.getEntityType().getCode() : null)
            .verified(e.getVerified())
            .status(e.getStatus())
            .slug(e.getSlug())
            .build();
    }

    private StateUserItemDto toUserItem(User u) {
        return StateUserItemDto.builder()
            .id(u.getId())
            .displayName(u.getDisplayName())
            .email(u.getEmail())
            .roles(u.getRoles().stream().map(r -> r.getCode()).toList())
            .isActive(u.getIsActive())
            .build();
    }

    private record StateContext(Long stateId, String stateCode, State state, List<Long> districtIds) {}
}
