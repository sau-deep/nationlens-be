package com.nationlens.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body used by the nationlens_crawler.py script to bulk-import
 * politician/entity data crawled from myneta.info and similar civic sites.
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCrawlerEntityRequest {

    @NotBlank
    private String nameEn;
    private String nameHi;

    /** e.g. MP, MLA, PARTY, JOURNALIST — must match an entity_types.code row. */
    @NotBlank
    private String entityTypeCode;

    /** URL-friendly unique identifier. Auto-generated from nameEn if blank. */
    private String slug;

    private String descriptionEn;
    private String descriptionHi;

    private String imageUrl;

    /** Loose string fields — resolved to FK ids by the service. */
    private String partyName;
    private String constituencyName;
    private String stateName;
    private String districtName;

    // ── Political profile fields (optional) ──────────────────────────────────

    private String education;
    private Integer age;
    private String gender;

    private Integer criminalCasesTotal;
    private Integer criminalCasesSerious;

    /** Total declared assets in INR (long). */
    private Long totalAssetsInr;

    /** Total declared liabilities in INR (long). */
    private Long totalLiabilitiesInr;

    private String affidavitUrl;

    /** e.g. 2024 */
    private Integer electionYear;

    /** e.g. LS, RS, RAJASTHAN, MAHARASHTRA */
    private String electionType;

    /** Source page used for this record. */
    private String sourceUrl;
}
