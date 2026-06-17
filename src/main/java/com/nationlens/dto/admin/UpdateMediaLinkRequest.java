package com.nationlens.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.domain.enums.MediaPlatform;
import com.nationlens.domain.enums.MediaSentiment;
import com.nationlens.domain.enums.SourceConfidence;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMediaLinkRequest {

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
    private Boolean sourceVerified;
    private Boolean isEmbeddable;
    private ApprovalStatus approvalStatus;
    private SourceConfidence sourceConfidence;
    private String visibility;
    private Integer displayOrder;
}
