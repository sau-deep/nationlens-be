package com.nationlens.repository;

import com.nationlens.domain.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findBySlug(String slug);
    List<District> findByStateIdOrderByNameEnAsc(Long stateId);
}
