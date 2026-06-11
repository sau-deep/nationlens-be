package com.nationlens.dto.media;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.domain.enums.MediaPlatform;
import com.nationlens.domain.enums.MediaSentiment;
import com.nationlens.domain.enums.SourceConfidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class MediaLinkDto {
    private Long id;
    private MediaPlatform platform;
    private String contentType;
    private MediaSentiment sentimentType;
    private String titleEn;
    private String titleHi;
    private String summaryEn;
    private String summaryHi;
    private String sourceUrl;
    private String embedUrl;
    private String thumbnailUrl;
    private String sourceOwner;
    private LocalDateTime sourcePublishedAt;
    private Boolean isEmbeddable;
    private Boolean noAppSwitchRequired;
    private ApprovalStatus approvalStatus;
    private SourceConfidence sourceConfidence;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private List<MediaMappingDto> mappings;
}
