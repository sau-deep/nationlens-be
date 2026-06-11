package com.nationlens.repository;

import com.nationlens.domain.entity.PoliticalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliticalProfileRepository extends JpaRepository<PoliticalProfile, Long> {
    Optional<PoliticalProfile> findByEntityId(Long entityId);
}
