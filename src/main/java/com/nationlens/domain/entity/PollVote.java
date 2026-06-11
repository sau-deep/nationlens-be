package com.nationlens.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "poll_votes")
@Getter @Setter
public class PollVote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private Long pollId;

    @Column(name = "poll_option_id", nullable = false)
    private Long pollOptionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;
}
