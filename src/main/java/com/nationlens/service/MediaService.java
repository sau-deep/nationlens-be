package com.nationlens.service;

import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.entity.MediaMapping;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.UpdateMediaLinkRequest;
import com.nationlens.dto.media.*;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.MediaMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private final MediaLinkRepository mediaLinkRepository;
    private final MediaMappingRepository mediaMappingRepository;

    public List<MediaLinkDto> getMediaForEntity(Long entityId, String sectionKey) {
        List<MediaMapping> mappings = sectionKey != null
            ? mediaMappingRepository.findApprovedByEntityIdAndSectionKey(entityId, sectionKey)
            : mediaMappingRepository.findApprovedByEntityId(entityId);
        return mappings.stream().map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    public List<MediaLinkDto> getMediaForDistrict(Long districtId) {
        return mediaMappingRepository.findApprovedByDistrictId(districtId).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    public Page<MediaLinkDto> listApproved(Pageable pageable) {
        return mediaLinkRepository.findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.APPROVED, pageable)
            .map(ml -> {
                List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(ml.getId());
                return toDto(ml, mappings.stream().map(this::toMappingDto).toList());
            });
    }

    public Page<MediaLinkDto> listAllForAdmin(Pageable pageable) {
        return mediaLinkRepository.findAll(pageable).map(ml -> {
            List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(ml.getId());
            return toDto(ml, mappings.stream().map(this::toMappingDto).toList());
        });
    }

    @Transactional
    public MediaLinkDto create(CreateMediaLinkRequest req, Long createdBy) {
        MediaLink link = new MediaLink();
        link.setPlatform(req.getPlatform());
        link.setContentType(req.getContentType());
        link.setSentimentType(req.getSentimentType());
        link.setTitleEn(req.getTitleEn());
        link.setTitleHi(req.getTitleHi());
        link.setSummaryEn(req.getSummaryEn());
        link.setSummaryHi(req.getSummaryHi());
        link.setSourceUrl(req.getSourceUrl());
        link.setEmbedUrl(req.getEmbedUrl());
        link.setThumbnailUrl(req.getThumbnailUrl());
        link.setSourceOwner(req.getSourceOwner());
        link.setSourcePublishedAt(req.getSourcePublishedAt());
        link.setIsEmbeddable(req.getIsEmbeddable());
        link.setNoAppSwitchRequired(req.getNoAppSwitchRequired());
        link.setSourceConfidence(req.getSourceConfidence());
        link.setDisplayOrder(req.getDisplayOrder());
        link.setApprovalStatus(ApprovalStatus.PENDING_REVIEW);
        link.setCreatedBy(createdBy);
        link.setCreatedAt(LocalDateTime.now());
        link = mediaLinkRepository.save(link);

        List<MediaMapping> savedMappings = List.of();
        if (req.getMappings() != null) {
            final Long linkId = link.getId();
            savedMappings = req.getMappings().stream().map(mr -> {
                MediaMapping mm = new MediaMapping();
                mm.setMediaLink(mediaLinkRepository.getReferenceById(linkId));
                mm.setMappingType(mr.getMappingType());
                mm.setEntityId(mr.getEntityId());
                mm.setDistrictId(mr.getDistrictId());
                mm.setConstituencyId(mr.getConstituencyId());
                mm.setPartyEntityId(mr.getPartyEntityId());
                mm.setSectionKey(mr.getSectionKey());
                mm.setSubMenuKey(mr.getSubMenuKey());
                mm.setDisplayContext(mr.getDisplayContext());
                mm.setDisplayOrder(mr.getDisplayOrder());
                mm.setIsPrimary(mr.getIsPrimary());
                mm.setAudienceScope(mr.getAudienceScope() != null ? mr.getAudienceScope() : "ENTITY");
                mm.setStateCode(mr.getStateCode());
                mm.setTags(mr.getTags());
                mm.setCreatedAt(LocalDateTime.now());
                return mediaMappingRepository.save(mm);
            }).toList();
        }

        return toDto(link, savedMappings.stream().map(this::toMappingDto).toList());
    }

    @Transactional
    public MediaLinkDto updateStatus(Long id, ApprovalStatus status, Long approvedBy) {
        MediaLink link = mediaLinkRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Media link not found: " + id));
        link.setApprovalStatus(status);
        if (status == ApprovalStatus.APPROVED) {
            link.setApprovedBy(approvedBy);
            link.setApprovedAt(LocalDateTime.now());
        }
        link.setUpdatedAt(LocalDateTime.now());
        mediaLinkRepository.save(link);
        List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(id);
        return toDto(link, mappings.stream().map(this::toMappingDto).toList());
    }

    @Transactional
    public void delete(Long id) {
        MediaLink link = mediaLinkRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Media link not found: " + id));
        List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(id);
        if (!mappings.isEmpty()) {
            mediaMappingRepository.deleteAll(mappings);
        }
        mediaLinkRepository.delete(link);
    }

    @Transactional
    public MediaLinkDto update(Long id, UpdateMediaLinkRequest req) {
        MediaLink link = mediaLinkRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Media link not found: " + id));
        if (req.getPlatform() != null) link.setPlatform(req.getPlatform());
        if (req.getContentType() != null) link.setContentType(req.getContentType());
        if (req.getSentimentType() != null) link.setSentimentType(req.getSentimentType());
        if (req.getTitleEn() != null) link.setTitleEn(req.getTitleEn());
        if (req.getTitleHi() != null) link.setTitleHi(req.getTitleHi());
        if (req.getSummaryEn() != null) link.setSummaryEn(req.getSummaryEn());
        if (req.getSummaryHi() != null) link.setSummaryHi(req.getSummaryHi());
        if (req.getSourceUrl() != null) link.setSourceUrl(req.getSourceUrl());
        if (req.getEmbedUrl() != null) link.setEmbedUrl(req.getEmbedUrl());
        if (req.getThumbnailUrl() != null) link.setThumbnailUrl(req.getThumbnailUrl());
        if (req.getSourceOwner() != null) link.setSourceOwner(req.getSourceOwner());
        if (req.getSourceVerified() != null) link.setSourceVerified(req.getSourceVerified());
        if (req.getIsEmbeddable() != null) link.setIsEmbeddable(req.getIsEmbeddable());
        if (req.getApprovalStatus() != null) link.setApprovalStatus(req.getApprovalStatus());
        if (req.getSourceConfidence() != null) link.setSourceConfidence(req.getSourceConfidence());
        if (req.getVisibility() != null) link.setVisibility(req.getVisibility());
        if (req.getDisplayOrder() != null) link.setDisplayOrder(req.getDisplayOrder());
        link.setUpdatedAt(java.time.LocalDateTime.now());
        mediaLinkRepository.save(link);
        List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(id);
        return toDto(link, mappings.stream().map(this::toMappingDto).toList());
    }

    public Optional<MediaLinkDto> findById(Long id) {
        return mediaLinkRepository.findById(id).map(ml -> {
            List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(ml.getId());
            return toDto(ml, mappings.stream().map(this::toMappingDto).toList());
        });
    }

    private MediaLinkDto toDto(MediaLink ml, List<MediaMappingDto> mappings) {
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
            .mappings(mappings)
            .build();
    }

    private MediaMappingDto toMappingDto(MediaMapping mm) {
        return MediaMappingDto.builder()
            .id(mm.getId())
            .mappingType(mm.getMappingType())
            .entityId(mm.getEntityId())
            .districtId(mm.getDistrictId())
            .sectionKey(mm.getSectionKey())
            .subMenuKey(mm.getSubMenuKey())
            .displayContext(mm.getDisplayContext())
            .displayOrder(mm.getDisplayOrder())
            .isPrimary(mm.getIsPrimary())
            .audienceScope(mm.getAudienceScope())
            .stateCode(mm.getStateCode())
            .tags(mm.getTags())
            .build();
    }

    public List<MediaLinkDto> getMediaByScope(String audienceScope) {
        return mediaMappingRepository.findApprovedByScope(audienceScope).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    public List<MediaLinkDto> getMediaForStateFeed(String stateCode) {
        return mediaMappingRepository.findApprovedForStateFeed(stateCode).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    public List<MediaLinkDto> getMediaByTag(String tag) {
        return mediaMappingRepository.findApprovedByTag(tag).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    public List<MediaLinkDto> getMediaBySection(String sectionKey) {
        return mediaMappingRepository.findApprovedBySectionKey(sectionKey).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }
}
