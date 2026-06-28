package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_mappings")
@Getter @Setter
public class MediaMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_link_id", nullable = false)
    private MediaLink mediaLink;

    @Column(name = "mapping_type", nullable = false, length = 40)
    private String mappingType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_type_code", length = 40)
    private String entityTypeCode;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "constituency_id")
    private Long constituencyId;

    @Column(name = "party_entity_id")
    private Long partyEntityId;

    @Column(name = "audience_scope", nullable = false, length = 20)
    private String audienceScope = "ENTITY";

    @Column(name = "state_code", length = 10)
    private String stateCode;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "issue_key", length = 100)
    private String issueKey;

    @Column(name = "section_key", nullable = false, length = 100)
    private String sectionKey;

    @Column(name = "sub_menu_key", length = 100)
    private String subMenuKey;

    @Column(name = "display_context", nullable = false, length = 100)
    private String displayContext;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
