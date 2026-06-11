package com.nationlens.dto.media;

import com.nationlens.domain.enums.MediaPlatform;
import com.nationlens.domain.enums.MediaSentiment;
import com.nationlens.domain.enums.SourceConfidence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class CreateMediaLinkRequest {
    @NotNull
    private MediaPlatform platform;

    @NotBlank
    private String contentType;

    @NotNull
    private MediaSentiment sentimentType;

    @NotBlank
    private String titleEn;

    private String titleHi;
    private String summaryEn;
    private String summaryHi;

    @NotBlank
    private String sourceUrl;

    private String embedUrl;
    private String thumbnailUrl;
    private String sourceOwner;
    private LocalDateTime sourcePublishedAt;
    private Boolean isEmbeddable = false;
    private Boolean noAppSwitchRequired = true;
    private SourceConfidence sourceConfidence = SourceConfidence.MEDIUM;
    private Integer displayOrder = 0;
    private List<MediaMappingRequest> mappings;
}
