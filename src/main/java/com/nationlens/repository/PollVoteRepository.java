package com.nationlens.repository;

import com.nationlens.domain.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByPollIdAndUserId(Long pollId, Long userId);
    Optional<PollVote> findByPollIdAndUserId(Long pollId, Long userId);
    long countByPollOptionId(Long pollOptionId);
}
