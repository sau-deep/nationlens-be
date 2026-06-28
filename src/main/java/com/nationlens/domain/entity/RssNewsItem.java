package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rss_news_items")
@Getter @Setter
public class RssNewsItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_source_id", nullable = false)
    private RssFeedSource feedSource;

    /** Unique identifier from the RSS feed (uri/link). Used for dedup. */
    @Column(nullable = false, length = 1000)
    private String guid;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "article_url", nullable = false, columnDefinition = "TEXT")
    private String articleUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(length = 255)
    private String author;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "city_key", length = 60)
    private String cityKey;

    @Column(length = 60)
    private String category;

    @Column(name = "source_name", length = 200)
    private String sourceName;

    /** Denormalized from feed source — used for locale filtering (EN, HI, TA, …). */
    @Column(name = "source_language", length = 10)
    private String sourceLanguage = "EN";

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Column
    private Boolean active = true;

    /** Awareness topic assigned by curation: ELECTIONS | GOVERNANCE | LEGAL | CIVIC. */
    @Column(name = "awareness_topic", length = 40)
    private String awarenessTopic;

    /** Curation relevance score; higher = more civic-awareness value. Used to rank the feed. */
    @Column(name = "relevance_score")
    private Integer relevanceScore = 0;

    /** True when the item touches everyday middle/lower-class concerns (prices, jobs, schemes…). */
    @Column(name = "class_relevant")
    private Boolean classRelevant = false;
}
