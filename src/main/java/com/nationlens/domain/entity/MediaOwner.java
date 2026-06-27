package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Who owns a piece of media. Self-referencing tree so we can model
 * "top companies owning big companies":
 *   CONGLOMERATE -> MEDIA_HOUSE -> CHANNEL / DIGITAL outlet.
 */
@Entity
@Table(name = "media_owners")
@Getter @Setter
public class MediaOwner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_hi")
    private String nameHi;

    /** CONGLOMERATE | MEDIA_HOUSE | CHANNEL | DIGITAL | GOVERNMENT | INDEPENDENT */
    @Column(name = "owner_type", nullable = false, length = 40)
    private String ownerType;

    /** The bigger company that controls this one (null = top of the tree). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_owner_id")
    private MediaOwner parent;

    @Column(name = "controlled_by")
    private String controlledBy;

    @Column(name = "ownership_note_en", columnDefinition = "TEXT")
    private String ownershipNoteEn;

    @Column(name = "ownership_note_hi", columnDefinition = "TEXT")
    private String ownershipNoteHi;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String website;

    @Column(name = "hq_location", length = 160)
    private String hqLocation;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
