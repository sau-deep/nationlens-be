package com.nationlens.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "districts")
@Getter @Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class District {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;

    @Column(name = "name_hi", length = 120)
    private String nameHi;

    @Column(unique = true, nullable = false, length = 160)
    private String slug;

    @Column(name = "population")
    private Long population;

    @Column(name = "literacy_rate", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal literacyRate;

    @Column(name = "area_sq_km", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal areaSqKm;

    @Column(name = "headquarters", length = 120)
    private String headquarters;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "home_display_order", nullable = false)
    private Integer homeDisplayOrder = 0;
}
