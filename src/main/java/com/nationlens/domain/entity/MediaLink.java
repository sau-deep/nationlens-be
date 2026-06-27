package com.nationlens.domain.entity;

import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.domain.enums.MediaPlatform;
import com.nationlens.domain.enums.MediaSentiment;
import com.nationlens.domain.enums.SourceConfidence;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_links")
@Getter @Setter
public class MediaLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MediaPlatform platform;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_type", nullable = false, length = 40)
    private MediaSentiment sentimentType;

    @Column(name = "title_en", nullable = false, length = 500)
    private String titleEn;

    @Column(name = "title_hi", length = 500)
    private String titleHi;

    @Column(name = "summary_en", columnDefinition = "TEXT")
    private String summaryEn;

    @Column(name = "summary_hi", columnDefinition = "TEXT")
    private String summaryHi;

    @Column(name = "source_url", nullable = false, columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "embed_url", columnDefinition = "TEXT")
    private String embedUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "source_owner")
    private String sourceOwner;

    /** Structured owner (who owns this media) — see {@link MediaOwner}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private MediaOwner owner;

    @Column(name = "source_published_at")
    private LocalDateTime sourcePublishedAt;

    @Column(name = "source_verified")
    private Boolean sourceVerified = false;

    @Column(name = "is_embeddable")
    private Boolean isEmbeddable = false;

    @Column(name = "no_app_switch_required")
    private Boolean noAppSwitchRequired = true;

    @Column(name = "external_open_allowed")
    private Boolean externalOpenAllowed = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 40)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;

    @Column(length = 40)
    private String visibility = "PUBLIC";

    @Column(name = "moderation_status", length = 40)
    private String moderationStatus = "PENDING";

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_confidence", length = 40)
    private SourceConfidence sourceConfidence = SourceConfidence.MEDIUM;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "language_visibility", length = 20)
    private String languageVisibility = "BOTH";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
