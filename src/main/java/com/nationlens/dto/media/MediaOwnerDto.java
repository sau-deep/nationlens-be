package com.nationlens.dto.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Media owner with its ownership chain. Used both nested inside a media item
 * (compact attribution) and as a standalone ownership-tree node.
 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaOwnerDto {
    private Long id;
    private String slug;
    private String nameEn;
    private String nameHi;
    private String ownerType;
    private String controlledBy;
    private String ownershipNoteEn;
    private String ownershipNoteHi;
    private String logoUrl;
    private String website;
    private String hqLocation;
    private Integer foundedYear;

    /** Immediate parent (the bigger company), if any. */
    private Long parentId;
    private String parentSlug;
    private String parentNameEn;

    /** Full ownership chain from this owner up to the ultimate parent. */
    private List<MediaOwnerDto> ownershipChain;

    /** Children (companies this one owns) — populated for tree responses. */
    private List<MediaOwnerDto> children;

    /** Count of approved media items attributed to this owner. */
    private Long mediaCount;
}
