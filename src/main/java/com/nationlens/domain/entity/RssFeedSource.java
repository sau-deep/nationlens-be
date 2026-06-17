package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rss_feed_sources")
@Getter @Setter
public class RssFeedSource {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "feed_url", nullable = false, columnDefinition = "TEXT")
    private String feedUrl;

    /** ADR, LEGAL, ELECTIONS, CIVIC, NATIONAL_NEWS, CITY_NEWS */
    @Column(nullable = false, length = 60)
    private String category;

    /** NATIONAL, DELHI, MUMBAI, BANGALORE, CHENNAI, KOLKATA, HYDERABAD, AHMEDABAD, PUNE,
     *  JAIPUR, LUCKNOW, CHANDIGARH, BHOPAL, PATNA, THIRUVANANTHAPURAM, KOCHI, NAGPUR,
     *  SURAT, VARANASI */
    @Column(name = "city_key", length = 60)
    private String cityKey;

    @Column(name = "state_code", length = 10)
    private String stateCode;

    @Column(name = "source_language", length = 10)
    private String sourceLanguage = "EN";

    @Column
    private Boolean active = true;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    /** How often to re-fetch (minutes). Default 120 = 2 hours. */
    @Column(name = "fetch_interval_minutes")
    private Integer fetchIntervalMinutes = 120;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
