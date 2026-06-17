package com.nationlens.service;

import com.nationlens.domain.entity.RssFeedSource;
import com.nationlens.domain.entity.RssNewsItem;
import com.nationlens.repository.RssFeedSourceRepository;
import com.nationlens.repository.RssNewsItemRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssFeedIngestionService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final Pattern IMG_TAG = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

    private final RssFeedSourceRepository feedSourceRepository;
    private final RssNewsItemRepository newsItemRepository;

    /** Runs 30 seconds after startup, then every 2 hours. */
    @Scheduled(fixedDelayString = "${nationlens.rss.fetch-interval-ms:7200000}", initialDelay = 30000)
    @Transactional
    public void ingestAllFeeds() {
        List<RssFeedSource> sources = feedSourceRepository.findByActiveTrue();
        log.info("RSS ingestion starting — {} active sources", sources.size());

        int totalSaved = 0;
        for (RssFeedSource source : sources) {
            try {
                int saved = ingestFeed(source);
                totalSaved += saved;
                source.setLastFetchedAt(LocalDateTime.now());
                feedSourceRepository.save(source);
                if (saved > 0) {
                    log.debug("Ingested {} new items from '{}'", saved, source.getName());
                }
            } catch (Exception e) {
                log.warn("RSS fetch failed for '{}' ({}): {}", source.getName(), source.getFeedUrl(), e.getMessage());
            }
        }

        // Prune items older than 7 days to keep the table lean
        int pruned = newsItemRepository.deleteByFetchedAtBefore(LocalDateTime.now().minusDays(7));
        log.info("RSS ingestion complete — saved: {}, pruned: {} expired items", totalSaved, pruned);
    }

    private int ingestFeed(RssFeedSource source) throws Exception {
        URL url = new URL(source.getFeedUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "NationLens/1.0 RSS Reader (+https://nationlens.in)");
        conn.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);
        conn.setInstanceFollowRedirects(true);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(conn));

        int saved = 0;
        for (SyndEntry entry : feed.getEntries()) {
            String guid = entry.getUri() != null && !entry.getUri().isBlank()
                    ? entry.getUri() : entry.getLink();
            if (guid == null || guid.isBlank()) continue;

            // Truncate guid to DB column length
            if (guid.length() > 1000) guid = guid.substring(0, 1000);

            if (newsItemRepository.existsByGuid(guid)) continue;

            String rawTitle = entry.getTitle();
            if (rawTitle == null || rawTitle.isBlank()) continue;

            RssNewsItem item = new RssNewsItem();
            item.setFeedSource(source);
            item.setGuid(guid);
            item.setTitle(truncate(cleanHtml(rawTitle), 1000));
            item.setArticleUrl(entry.getLink());
            item.setCityKey(source.getCityKey());
            item.setCategory(source.getCategory());
            item.setSourceName(source.getName());
            item.setSourceLanguage(
                    source.getSourceLanguage() != null ? source.getSourceLanguage().toUpperCase() : "EN");
            item.setFetchedAt(LocalDateTime.now());
            item.setActive(true);

            if (entry.getAuthor() != null && !entry.getAuthor().isBlank()) {
                item.setAuthor(truncate(entry.getAuthor(), 255));
            }

            if (entry.getPublishedDate() != null) {
                item.setPublishedAt(entry.getPublishedDate().toInstant().atZone(IST).toLocalDateTime());
            } else if (entry.getUpdatedDate() != null) {
                item.setPublishedAt(entry.getUpdatedDate().toInstant().atZone(IST).toLocalDateTime());
            }

            if (entry.getDescription() != null) {
                String raw = entry.getDescription().getValue();
                String thumb = extractFirstImageUrl(raw);
                if (thumb != null) item.setThumbnailUrl(thumb);
                item.setDescription(truncate(cleanHtml(raw), 2000));
            }

            // Also check media:content (enclosures / media modules) for thumbnail
            if (item.getThumbnailUrl() == null) {
                entry.getEnclosures().stream()
                        .filter(e -> e.getType() != null && e.getType().startsWith("image/"))
                        .findFirst()
                        .ifPresent(e -> item.setThumbnailUrl(e.getUrl()));
            }

            newsItemRepository.save(item);
            saved++;
        }
        return saved;
    }

    private static String extractFirstImageUrl(String html) {
        if (html == null) return null;
        Matcher m = IMG_TAG.matcher(html);
        return m.find() ? m.group(1) : null;
    }

    private static String cleanHtml(String html) {
        if (html == null) return null;
        return HTML_TAG.matcher(html).replaceAll("").trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
