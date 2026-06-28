package com.nationlens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nationlens.domain.enums.ApprovalStatus;
import com.nationlens.domain.enums.MediaPlatform;
import com.nationlens.domain.enums.MediaSentiment;
import com.nationlens.domain.enums.SourceConfidence;
import com.nationlens.dto.admin.ReelCrawlConfigDto;
import com.nationlens.dto.admin.ReelCrawlResultDto;
import com.nationlens.dto.admin.ReelCrawlStatusDto;
import com.nationlens.dto.admin.ReelCrawlTriggerRequest;
import com.nationlens.dto.media.CreateMediaLinkRequest;
import com.nationlens.dto.media.MediaMappingRequest;
import com.nationlens.repository.MediaLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Discovers Instagram reels via Firecrawl hashtag pages and saves them to media_links
 * with section mappings so they appear in browse tabs, home feed, and tag-based feeds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReelCrawlService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String FIRECRAWL_SCRAPE_URL = "https://api.firecrawl.dev/v1/scrape";

    /** Civic hashtag presets mapped to NationLens browse sections. */
    public static final List<ReelCrawlPreset> PRESETS = List.of(
        preset("parliament",   "Parliament",       "POLITICS",   "parliament,loksabha",   "PARLIAMENT_REEL"),
        preset("LokSabha",     "Lok Sabha",        "GOVERNMENT", "loksabha,parliament", "PARLIAMENT_REEL"),
        preset("RajyaSabha",   "Rajya Sabha",      "GOVERNMENT", "rajyasabha",          "PARLIAMENT_REEL"),
        preset("sansad",       "Sansad",           "GOVERNMENT", "sansad,parliament",   "PARLIAMENT_REEL"),
        preset("IndianPolitics","Indian Politics", "POLITICS",   "politics,election",   null),
        preset("MP",           "MP",               "POLITICS",   "mp,parliament",       "MP_REEL"),
        preset("MLA",          "MLA",              "POLITICS",   "mla,vidhansabha",     "MLA_REEL"),
        preset("vidhansabha",  "Vidhan Sabha",     "GOVERNMENT", "vidhansabha,mla",     "MLA_REEL"),
        preset("RTI",          "RTI",              "CITIZEN",    "rti,transparency",    "CIVIC_REEL"),
        preset("civicaction",  "Civic Action",     "CITIZEN",    "civic,citizen",       "CIVIC_REEL")
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MediaService mediaService;
    private final MediaLinkRepository mediaLinkRepository;

    @Value("${nationlens.reels.enabled:false}")
    private boolean reelsCrawlEnabled;

    @Value("${nationlens.reels.firecrawl-api-key:}")
    private String firecrawlApiKey;

    @Value("${nationlens.reels.per-hashtag-limit:5}")
    private int defaultPerHashtagLimit;

    private final AtomicReference<LastRun> lastRun = new AtomicReference<>();

    public record ReelCrawlPreset(
        String hashtag,
        String label,
        String sectionKey,
        String displayContext,
        String audienceScope,
        String mappingType,
        String subMenuKey,
        String tags,
        Long entityId
    ) {}

    private record LastRun(
        LocalDateTime at,
        int saved,
        int skipped,
        String status
    ) {}

    private record ExtractedReel(
        String shortcode,
        String title,
        String authorHandle,
        String thumbnailUrl,
        Boolean verified
    ) {}

    private static ReelCrawlPreset preset(String hashtag, String label, String sectionKey, String tags, String subMenuKey) {
        return new ReelCrawlPreset(
            hashtag, label, sectionKey, "BROWSE", "GENERAL", "FEED", subMenuKey, tags, null
        );
    }

    public List<ReelCrawlConfigDto> listPresets() {
        return PRESETS.stream().map(this::toConfigDto).toList();
    }

    public ReelCrawlStatusDto getStatus() {
        LastRun run = lastRun.get();
        return ReelCrawlStatusDto.builder()
            .enabled(reelsCrawlEnabled)
            .firecrawlConfigured(firecrawlApiKey != null && !firecrawlApiKey.isBlank())
            .presetCount(PRESETS.size())
            .pendingReviewCount(mediaLinkRepository.countByApprovalStatus(ApprovalStatus.PENDING_REVIEW))
            .lastRunAt(run != null ? run.at() : null)
            .lastRunSaved(run != null ? run.saved() : 0)
            .lastRunSkipped(run != null ? run.skipped() : 0)
            .lastRunStatus(run != null ? run.status() : null)
            .build();
    }

    /** Manual trigger from admin UI or API. */
    public List<ReelCrawlResultDto> trigger(ReelCrawlTriggerRequest request, Long adminUserId) {
        ensureConfigured();
        int limit = request.getLimit() != null ? request.getLimit() : defaultPerHashtagLimit;

        List<ReelCrawlResultDto> results = new ArrayList<>();
        if (request.getHashtag() != null && !request.getHashtag().isBlank()) {
            ReelCrawlPreset preset = findPreset(request.getHashtag())
                .orElse(buildCustomPreset(request));
            results.add(crawlPreset(preset, limit, request.isDryRun(), adminUserId));
        } else {
            for (ReelCrawlPreset preset : PRESETS) {
                results.add(crawlPreset(preset, limit, request.isDryRun(), adminUserId));
            }
        }

        int totalSaved = results.stream().mapToInt(ReelCrawlResultDto::getSaved).sum();
        int totalSkipped = results.stream().mapToInt(ReelCrawlResultDto::getSkipped).sum();
        lastRun.set(new LastRun(LocalDateTime.now(IST), totalSaved, totalSkipped, "OK"));
        return results;
    }

    /** Daily scheduled crawl of all civic hashtag presets. */
    @Scheduled(cron = "${nationlens.reels.crawl-cron:0 0 6 * * *}", zone = "Asia/Kolkata")
    public void scheduledDailyCrawl() {
        if (!reelsCrawlEnabled) {
            log.debug("Reel crawl scheduled job skipped — nationlens.reels.enabled=false");
            return;
        }
        if (firecrawlApiKey == null || firecrawlApiKey.isBlank()) {
            log.warn("Reel crawl scheduled job skipped — FIRECRAWL_API_KEY not configured");
            return;
        }

        log.info("Starting scheduled daily reel crawl ({} presets)", PRESETS.size());
        try {
            ReelCrawlTriggerRequest req = new ReelCrawlTriggerRequest();
            req.setLimit(defaultPerHashtagLimit);
            req.setDryRun(false);
            List<ReelCrawlResultDto> results = trigger(req, null);
            int saved = results.stream().mapToInt(ReelCrawlResultDto::getSaved).sum();
            log.info("Scheduled reel crawl complete — saved {} new reels", saved);
        } catch (Exception e) {
            lastRun.set(new LastRun(LocalDateTime.now(IST), 0, 0, "FAILED: " + e.getMessage()));
            log.error("Scheduled reel crawl failed: {}", e.getMessage(), e);
        }
    }

    private ReelCrawlResultDto crawlPreset(ReelCrawlPreset preset, int limit, boolean dryRun, Long adminUserId) {
        List<String> errors = new ArrayList<>();
        int saved = 0;
        int skipped = 0;
        int failed = 0;

        List<ExtractedReel> reels;
        try {
            reels = scrapeHashtag(preset.hashtag());
        } catch (Exception e) {
            errors.add(e.getMessage());
            log.warn("Firecrawl failed for #{}: {}", preset.hashtag(), e.getMessage());
            return buildResult(preset, 0, 0, 0, 1, errors);
        }

        List<ExtractedReel> batch = reels.stream().limit(limit).toList();
        for (ExtractedReel reel : batch) {
            if (reel.shortcode() == null || reel.shortcode().isBlank()) {
                skipped++;
                continue;
            }

            String sourceUrl = "https://www.instagram.com/reel/" + reel.shortcode() + "/";
            if (mediaLinkRepository.findBySourceUrl(sourceUrl).isPresent()) {
                skipped++;
                continue;
            }

            if (dryRun) {
                log.info("[dry-run] would save {} → section {}", sourceUrl, preset.sectionKey());
                saved++;
                continue;
            }

            try {
                saveReel(reel, preset, adminUserId);
                saved++;
            } catch (Exception e) {
                failed++;
                errors.add(sourceUrl + ": " + e.getMessage());
                log.warn("Failed to save reel {}: {}", sourceUrl, e.getMessage());
            }
        }

        return buildResult(preset, batch.size(), saved, skipped, failed, errors);
    }

    protected void saveReel(ExtractedReel reel, ReelCrawlPreset preset, Long adminUserId) {
        String sourceUrl = "https://www.instagram.com/reel/" + reel.shortcode() + "/";
        String embedUrl = sourceUrl + "embed/";
        String author = reel.authorHandle() != null ? reel.authorHandle() : "";
        String title = reel.title() != null && !reel.title().isBlank()
            ? truncate(reel.title(), 499)
            : "Instagram Reel" + (author.isBlank() ? "" : " by " + author);

        CreateMediaLinkRequest req = new CreateMediaLinkRequest();
        req.setPlatform(MediaPlatform.INSTAGRAM);
        req.setContentType("REEL");
        req.setSentimentType(MediaSentiment.NEUTRAL);
        req.setTitleEn(title);
        req.setSummaryEn(title);
        req.setSourceUrl(sourceUrl);
        req.setEmbedUrl(embedUrl);
        req.setThumbnailUrl(reel.thumbnailUrl());
        req.setSourceOwner(author);
        req.setIsEmbeddable(true);
        req.setNoAppSwitchRequired(true);
        req.setSourceConfidence(SourceConfidence.MEDIUM);
        req.setDisplayOrder(0);
        req.setMappings(List.of(buildMapping(preset)));

        mediaService.create(req, adminUserId);
    }

    private MediaMappingRequest buildMapping(ReelCrawlPreset preset) {
        MediaMappingRequest mapping = new MediaMappingRequest();
        mapping.setMappingType(preset.mappingType());
        mapping.setSectionKey(preset.sectionKey());
        mapping.setSubMenuKey(preset.subMenuKey());
        mapping.setDisplayContext(preset.displayContext());
        mapping.setAudienceScope(preset.audienceScope());
        mapping.setTags(preset.tags());
        mapping.setDisplayOrder(0);
        mapping.setIsPrimary(true);
        if (preset.entityId() != null) {
            mapping.setEntityId(preset.entityId());
        }
        return mapping;
    }

    private List<ExtractedReel> scrapeHashtag(String hashtag) throws Exception {
        String url = "https://www.instagram.com/explore/tags/" + hashtag + "/";
        log.info("Firecrawl scraping #{} → {}", hashtag, url);

        Map<String, Object> extractSchema = buildExtractSchema();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("url", url);
        payload.put("formats", List.of("extract"));
        payload.put("extract", Map.of(
            "schema", extractSchema,
            "prompt", "Extract all Instagram reels shown on this hashtag page. "
                + "For each reel find the shortcode from the URL (e.g. /reel/ABC123/ → shortcode is ABC123), "
                + "the caption text, creator handle, thumbnail, and any metadata visible."
        ));
        payload.put("actions", List.of(
            Map.of("type", "wait", "milliseconds", 3000),
            Map.of("type", "scroll", "direction", "down", "selector", "body"),
            Map.of("type", "wait", "milliseconds", 2000)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(firecrawlApiKey);

        ResponseEntity<String> response = restTemplate.postForEntity(
            FIRECRAWL_SCRAPE_URL,
            new HttpEntity<>(payload, headers),
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Firecrawl HTTP " + response.getStatusCode());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode reelsNode = root.path("data").path("extract").path("reels");
        if (!reelsNode.isArray()) {
            return List.of();
        }

        List<ExtractedReel> reels = new ArrayList<>();
        for (JsonNode node : reelsNode) {
            reels.add(new ExtractedReel(
                textOrNull(node, "shortcode"),
                textOrNull(node, "title"),
                textOrNull(node, "author_handle"),
                textOrNull(node, "thumbnail_url"),
                node.path("verified").asBoolean(false)
            ));
        }
        log.info("Firecrawl found {} reels for #{}", reels.size(), hashtag);
        return reels;
    }

    private Map<String, Object> buildExtractSchema() {
        Map<String, Object> reelItem = new LinkedHashMap<>();
        reelItem.put("type", "object");
        reelItem.put("properties", Map.of(
            "shortcode", Map.of("type", "string"),
            "title", Map.of("type", "string"),
            "author_handle", Map.of("type", "string"),
            "thumbnail_url", Map.of("type", "string"),
            "verified", Map.of("type", "boolean")
        ));
        reelItem.put("required", List.of("shortcode"));

        Map<String, Object> reels = Map.of(
            "type", "array",
            "items", reelItem
        );

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "object");
        root.put("properties", Map.of("reels", reels));
        root.put("required", List.of("reels"));
        return root;
    }

    private Optional<ReelCrawlPreset> findPreset(String hashtag) {
        return PRESETS.stream()
            .filter(p -> p.hashtag().equalsIgnoreCase(hashtag.trim()))
            .findFirst();
    }

    private ReelCrawlPreset buildCustomPreset(ReelCrawlTriggerRequest request) {
        String section = request.getSectionKey() != null ? request.getSectionKey() : "POLITICS";
        String displayContext = request.getDisplayContext() != null ? request.getDisplayContext() : "BROWSE";
        String hashtag = request.getHashtag().trim();

        if ("ENTITY_PROFILE".equalsIgnoreCase(displayContext) && request.getEntityId() != null) {
            return new ReelCrawlPreset(
                hashtag, hashtag, section, displayContext, "ENTITY", "ENTITY",
                null, hashtag.toLowerCase(), request.getEntityId()
            );
        }

        return new ReelCrawlPreset(
            hashtag, hashtag, section, displayContext, "GENERAL", "FEED", null, hashtag.toLowerCase(), null
        );
    }

    private ReelCrawlConfigDto toConfigDto(ReelCrawlPreset p) {
        return ReelCrawlConfigDto.builder()
            .hashtag(p.hashtag())
            .label(p.label())
            .sectionKey(p.sectionKey())
            .displayContext(p.displayContext())
            .audienceScope(p.audienceScope())
            .mappingType(p.mappingType())
            .subMenuKey(p.subMenuKey())
            .tags(p.tags())
            .build();
    }

    private ReelCrawlResultDto buildResult(
        ReelCrawlPreset preset, int found, int saved, int skipped, int failed, List<String> errors
    ) {
        return ReelCrawlResultDto.builder()
            .hashtag(preset.hashtag())
            .sectionKey(preset.sectionKey())
            .found(found)
            .saved(saved)
            .skipped(skipped)
            .failed(failed)
            .errors(errors.isEmpty() ? null : errors)
            .completedAt(LocalDateTime.now(IST))
            .build();
    }

    private void ensureConfigured() {
        if (firecrawlApiKey == null || firecrawlApiKey.isBlank()) {
            throw new IllegalStateException("FIRECRAWL_API_KEY is not configured");
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode val = node.path(field);
        if (val.isMissingNode() || val.isNull()) return null;
        String s = val.asText(null);
        return s != null && !s.isBlank() ? s : null;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
