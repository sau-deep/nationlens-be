package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights")
@Getter @Setter
public class AiInsight {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insight_type", nullable = false, length = 80)
    private String insightType;

    @Column(name = "title_en", nullable = false, length = 500)
    private String titleEn;

    @Column(name = "title_hi", length = 500)
    private String titleHi;

    @Column(name = "body_en", nullable = false, columnDefinition = "TEXT")
    private String bodyEn;

    @Column(name = "body_hi", columnDefinition = "TEXT")
    private String bodyHi;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "is_placeholder", nullable = false)
    private Boolean isPlaceholder = true;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "home_display_order", nullable = false)
    private Integer homeDisplayOrder = 0;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
