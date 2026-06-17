package com.nationlens.repository;

import com.nationlens.domain.entity.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
    Optional<EntityType> findByCode(String code);
}
