package com.nationlens.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReelCrawlTriggerRequest {
    /** Instagram hashtag without #. If blank, all civic presets are crawled. */
    private String hashtag;

    /** Max reels to import per hashtag (default from config). */
    private Integer limit;

    /** Override NationLens section key (e.g. POLITICS, GOVERNMENT, CITIZEN). */
    private String sectionKey;

    /** Override display context: BROWSE, HOME, ENTITY_PROFILE, etc. */
    private String displayContext;

    /** Optional entity ID for ENTITY_PROFILE placement. */
    private Long entityId;

    /** When true, scrape only — no DB writes. */
    private boolean dryRun;
}
