package com.nationlens.repository;

import com.nationlens.domain.entity.NlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NlEntityRepository extends JpaRepository<NlEntity, Long> {
    Optional<NlEntity> findBySlug(String slug);

    List<NlEntity> findByDistrictIdAndStatusOrderByNameEnAsc(Long districtId, String status);

    @Query("SELECT e FROM NlEntity e WHERE e.status = 'ACTIVE' ORDER BY e.nameEn ASC")
    Page<NlEntity> findAllActive(Pageable pageable);

    @Query("SELECT e FROM NlEntity e WHERE e.entityType.code = :typeCode AND e.status = 'ACTIVE'")
    List<NlEntity> findByEntityTypeCode(@Param("typeCode") String typeCode);
}
