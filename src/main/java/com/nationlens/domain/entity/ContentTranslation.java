package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Cached translation of a single content field into a target locale.
 * One row per (contentType, contentId, locale). See changelog 027.
 */
@Entity
@Table(name = "content_translations")
@Getter @Setter
public class ContentTranslation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false, length = 60)
    private String contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "source_hash", length = 64)
    private String sourceHash;

    /** MACHINE (Claude) | REVIEWED (human-approved). */
    @Column(nullable = false, length = 20)
    private String status = "MACHINE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
