package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "political_profiles")
@Getter @Setter
public class PoliticalProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", unique = true, nullable = false)
    private NlEntity entity;

    @Column(length = 500)
    private String education;

    @Column(name = "declared_criminal_cases")
    private Integer declaredCriminalCases = 0;

    @Column(name = "declared_assets_inr")
    private Long declaredAssetsInr;

    @Column(name = "declared_liabilities_inr")
    private Long declaredLiabilitiesInr;

    @Column(name = "parliament_attendance_pct", precision = 5, scale = 2)
    private BigDecimal parliamentAttendancePct;

    @Column(name = "questions_raised")
    private Integer questionsRaised = 0;

    @Column(name = "bills_introduced")
    private Integer billsIntroduced = 0;

    @Column(name = "term_start_year")
    private Integer termStartYear;

    @Column(name = "accountability_score")
    private Integer accountabilityScore;

    @Column(name = "affidavit_source_url", columnDefinition = "TEXT")
    private String affidavitSourceUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
