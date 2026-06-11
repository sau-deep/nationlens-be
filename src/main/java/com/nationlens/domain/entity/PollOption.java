package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "poll_options")
@Getter @Setter
public class PollOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(name = "label_en", nullable = false, length = 255)
    private String labelEn;

    @Column(name = "label_hi", length = 255)
    private String labelHi;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
