package com.nationlens.service;

import com.nationlens.domain.entity.MediaLink;
import com.nationlens.domain.entity.MediaMapping;
import com.nationlens.domain.entity.MediaOwner;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.dto.admin.UpdateMediaLinkRequest;
import com.nationlens.dto.media.*;
import com.nationlens.repository.MediaLinkRepository;
import com.nationlens.repository.MediaMappingRepository;
import com.nationlens.repository.MediaOwnerRepository;
import com.nationlens.repository.NlEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private final MediaLinkRepository mediaLinkRepository;
    private final MediaMappingRepository mediaMappingRepository;
    private final MediaOwnerRepository mediaOwnerRepository;
    private final NlEntityRepository entityRepository;

    /** Top-level browse sections in display order. */
    public static final List<BrowseSectionDto> BROWSE_SECTIONS = List.of(
        BrowseSectionDto.builder().key("POLITICS").labelEn("Politics").labelHi("राजनीति")
            .description("Debates, campaigns, manifestos & accountability").icon("Landmark").accent("#FF8C00").build(),
        BrowseSectionDto.builder().key("ENVIRONMENT").labelEn("Environment").labelHi("पर्यावरण")
            .description("Air, water, climate & green action").icon("Leaf").accent("#00C853").build(),
        BrowseSectionDto.builder().key("GOVERNMENT").labelEn("Government").labelHi("सरकार")
            .description("Schemes, governance, budgets & service delivery").icon("Building2").accent("#007BFF").build(),
        BrowseSectionDto.builder().key("MEDIA").labelEn("Media").labelHi("मीडिया")
            .description("Who owns the news & how it's made").icon("Tv").accent("#E6683C").build(),
        BrowseSectionDto.builder().key("JUDICIARY").labelEn("Judiciary").labelHi("न्यायपालिका")
            .description("Courts, rights, verdicts & legal literacy").icon("Scale").accent("#A78BFA").build(),
        BrowseSectionDto.builder().key("CITIZEN").labelEn("Citizen").labelHi("नागरिक")
            .description("Civic wins, voter guides & local action").icon("Users").accent("#22D3EE").build()
    );

    public List<MediaLinkDto> getMediaForEntity(Long entityId, String sectionKey) {
        java.util.LinkedHashMap<Long, MediaMapping> unique = new java.util.LinkedHashMap<>();

        List<MediaMapping> direct = sectionKey != null
            ? mediaMappingRepository.findApprovedByEntityIdAndSectionKey(entityId, sectionKey)
            : mediaMappingRepository.findApprovedByEntityId(entityId);
        for (MediaMapping mm : direct) {
            unique.putIfAbsent(mm.getMediaLink().getId(), mm);
        }

        entityRepository.findById(entityId).ifPresent(entity -> {
            if (entity.getEntityType() == null) return;
            String typeCode = entity.getEntityType().getCode();
            List<MediaMapping> byType = sectionKey != null
                ? mediaMappingRepository.findApprovedByEntityTypeAndSectionKey(typeCode, sectionKey)
                : mediaMappingRepository.findApprovedByEntityType(typeCode);
            for (MediaMapping mm : byType) {
                unique.putIfAbsent(mm.getMediaLink().getId(), mm);
            }
        });

        return unique.values().stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm))))
            .toList();
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
                mm.setEntityTypeCode(mr.getEntityTypeCode());
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
        if (req.getMappings() != null) {
            replaceMappings(id, req.getMappings());
        }
        List<MediaMapping> mappings = mediaMappingRepository.findByMediaLinkId(id);
        return toDto(link, mappings.stream().map(this::toMappingDto).toList());
    }

    @Transactional
    public void replaceMappings(Long mediaLinkId, List<MediaMappingRequest> requests) {
        List<MediaMapping> existing = mediaMappingRepository.findByMediaLinkId(mediaLinkId);
        if (!existing.isEmpty()) {
            mediaMappingRepository.deleteAll(existing);
        }
        if (requests == null || requests.isEmpty()) {
            return;
        }
        MediaLink linkRef = mediaLinkRepository.getReferenceById(mediaLinkId);
        for (MediaMappingRequest mr : requests) {
            MediaMapping mm = new MediaMapping();
            mm.setMediaLink(linkRef);
            mm.setMappingType(mr.getMappingType());
            mm.setEntityId(mr.getEntityId());
            mm.setEntityTypeCode(mr.getEntityTypeCode());
            mm.setDistrictId(mr.getDistrictId());
            mm.setConstituencyId(mr.getConstituencyId());
            mm.setPartyEntityId(mr.getPartyEntityId());
            mm.setSectionKey(mr.getSectionKey());
            mm.setSubMenuKey(mr.getSubMenuKey());
            mm.setDisplayContext(mr.getDisplayContext());
            mm.setDisplayOrder(mr.getDisplayOrder() != null ? mr.getDisplayOrder() : 0);
            mm.setIsPrimary(mr.getIsPrimary() != null ? mr.getIsPrimary() : false);
            mm.setAudienceScope(mr.getAudienceScope() != null ? mr.getAudienceScope() : "ENTITY");
            mm.setStateCode(mr.getStateCode());
            mm.setTags(mr.getTags());
            mm.setCreatedAt(LocalDateTime.now());
            mediaMappingRepository.save(mm);
        }
    }

    @Transactional
    public void deleteMapping(Long mappingId) {
        MediaMapping mm = mediaMappingRepository.findById(mappingId)
            .orElseThrow(() -> new IllegalArgumentException("Media mapping not found: " + mappingId));
        mediaMappingRepository.delete(mm);
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
            .owner(toOwnerDto(ml.getOwner(), true, false))
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
            .entityTypeCode(mm.getEntityTypeCode())
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

    public List<MediaLinkDto> getMediaByEntityType(String entityType, String sectionKey) {
        List<MediaMapping> mappings = sectionKey != null && !sectionKey.isBlank()
            ? mediaMappingRepository.findApprovedByEntityTypeAndSectionKey(entityType.toUpperCase(), sectionKey.toUpperCase())
            : mediaMappingRepository.findApprovedByEntityType(entityType.toUpperCase());
        return mappings.stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm))))
            .toList();
    }

    public List<MediaLinkDto> getMediaByScope(String audienceScope) {
        return mediaMappingRepository.findApprovedByScope(audienceScope).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    /** Admin-curated home reel slots (display_context = HOME). */
    public List<MediaLinkDto> getMediaForHome() {
        return mediaMappingRepository.findApprovedForHome().stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    /** Trending: NATIONAL-scoped approved media (browse + home mappings). */
    public List<MediaLinkDto> getTrendingMedia() {
        return mediaMappingRepository.findApprovedByScope("NATIONAL").stream()
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

    // ── Browse sections ───────────────────────────────────────────────────────

    /** The six top-level browse sections, each with its approved-media count. */
    public List<BrowseSectionDto> getBrowseSections() {
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        for (Object[] row : mediaMappingRepository.countApprovedBrowseBySection()) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return BROWSE_SECTIONS.stream().map(s -> BrowseSectionDto.builder()
            .key(s.getKey()).labelEn(s.getLabelEn()).labelHi(s.getLabelHi())
            .description(s.getDescription()).icon(s.getIcon()).accent(s.getAccent())
            .count(counts.getOrDefault(s.getKey(), 0L))
            .build()).toList();
    }

    /** Reels / videos inside one browse section. */
    public List<MediaLinkDto> getBrowseSectionMedia(String sectionKey) {
        return mediaMappingRepository.findApprovedBrowseBySection(sectionKey.toUpperCase()).stream()
            .map(mm -> toDto(mm.getMediaLink(), List.of(toMappingDto(mm)))).toList();
    }

    /**
     * Unified public feed resolver. Entity/district IDs take priority, then context/type/tag/section/scope.
     */
    public List<MediaLinkDto> queryPublicFeed(
        String context,
        String scope,
        String state,
        String hashtag,
        String section,
        String entityType,
        Long entityId,
        Long districtId,
        String displayContext
    ) {
        if (entityId != null) {
            return getMediaForEntity(entityId, section);
        }
        if (districtId != null) {
            return getMediaForDistrict(districtId);
        }
        if (context != null && "HOME".equalsIgnoreCase(context.trim())) {
            return getMediaForHome();
        }
        if (entityType != null && !entityType.isBlank()) {
            return getMediaByEntityType(entityType, section);
        }
        if (displayContext != null && "BROWSE".equalsIgnoreCase(displayContext.trim())
                && section != null && !section.isBlank()) {
            return getBrowseSectionMedia(section);
        }
        if (state != null && !state.isBlank()) {
            return getMediaForStateFeed(state.toUpperCase());
        }
        if (hashtag != null && !hashtag.isBlank()) {
            return getMediaByTag(hashtag.toLowerCase());
        }
        if (section != null && !section.isBlank()) {
            return getMediaBySection(section.toUpperCase());
        }
        if (scope != null && !scope.isBlank()) {
            return getMediaByScope(scope.toUpperCase());
        }
        return getMediaByScope("NATIONAL");
    }

    // ── Ownership ───────────────────────────────────────────────────────────

    /** Full ownership tree (top-level owners with nested children). */
    public List<MediaOwnerDto> getOwnershipTree() {
        return mediaOwnerRepository.findByParentIsNullOrderByNameEnAsc().stream()
            .map(this::toOwnerTreeNode).toList();
    }

    private MediaOwnerDto toOwnerTreeNode(MediaOwner owner) {
        MediaOwnerDto dto = toOwnerDto(owner, false, false);
        List<MediaOwnerDto> children = mediaOwnerRepository.findByParentIdOrderByNameEnAsc(owner.getId())
            .stream().map(this::toOwnerTreeNode).toList();
        return MediaOwnerDto.builder()
            .id(dto.getId()).slug(dto.getSlug()).nameEn(dto.getNameEn()).nameHi(dto.getNameHi())
            .ownerType(dto.getOwnerType()).controlledBy(dto.getControlledBy())
            .ownershipNoteEn(dto.getOwnershipNoteEn()).ownershipNoteHi(dto.getOwnershipNoteHi())
            .logoUrl(dto.getLogoUrl()).website(dto.getWebsite()).hqLocation(dto.getHqLocation())
            .foundedYear(dto.getFoundedYear())
            .children(children.isEmpty() ? null : children)
            .build();
    }

    /** A single owner (with ownership chain) plus its approved media. */
    public Optional<java.util.Map<String, Object>> getOwnerWithMedia(String slug) {
        return mediaOwnerRepository.findBySlug(slug).map(owner -> {
            List<MediaLinkDto> media = mediaLinkRepository
                .findByOwnerIdAndApprovalStatusOrderByDisplayOrderAscCreatedAtDesc(owner.getId(), ApprovalStatus.APPROVED)
                .stream()
                .map(ml -> toDto(ml, mediaMappingRepository.findByMediaLinkId(ml.getId()).stream().map(this::toMappingDto).toList()))
                .toList();
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("owner", toOwnerDto(owner, true, false));
            result.put("media", media);
            return result;
        });
    }

    /**
     * Map a MediaOwner to a DTO.
     * @param withChain  include the parent chain (this -> ... -> ultimate parent)
     * @param withNote   (reserved) include long ownership notes
     */
    private MediaOwnerDto toOwnerDto(MediaOwner owner, boolean withChain, boolean withNote) {
        if (owner == null) return null;
        MediaOwner parent = owner.getParent();
        List<MediaOwnerDto> chain = null;
        if (withChain && parent != null) {
            chain = new ArrayList<>();
            MediaOwner cur = parent;
            int guard = 0;
            while (cur != null && guard++ < 10) {
                chain.add(MediaOwnerDto.builder()
                    .id(cur.getId()).slug(cur.getSlug()).nameEn(cur.getNameEn())
                    .nameHi(cur.getNameHi()).ownerType(cur.getOwnerType()).build());
                cur = cur.getParent();
            }
        }
        return MediaOwnerDto.builder()
            .id(owner.getId())
            .slug(owner.getSlug())
            .nameEn(owner.getNameEn())
            .nameHi(owner.getNameHi())
            .ownerType(owner.getOwnerType())
            .controlledBy(owner.getControlledBy())
            .ownershipNoteEn(owner.getOwnershipNoteEn())
            .ownershipNoteHi(owner.getOwnershipNoteHi())
            .logoUrl(owner.getLogoUrl())
            .website(owner.getWebsite())
            .hqLocation(owner.getHqLocation())
            .foundedYear(owner.getFoundedYear())
            .parentId(parent != null ? parent.getId() : null)
            .parentSlug(parent != null ? parent.getSlug() : null)
            .parentNameEn(parent != null ? parent.getNameEn() : null)
            .ownershipChain(chain)
            .build();
    }
}
