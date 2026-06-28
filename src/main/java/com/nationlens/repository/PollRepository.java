package com.nationlens.repository;

import com.nationlens.domain.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Poll> findByEntityIdAndIsActiveTrueOrderByCreatedAtDesc(Long entityId);
    List<Poll> findAllByOrderByCreatedAtDesc();
}
