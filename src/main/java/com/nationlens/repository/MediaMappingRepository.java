package com.nationlens.repository;

import com.nationlens.domain.entity.MediaMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaMappingRepository extends JpaRepository<MediaMapping, Long> {

    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml " +
           "WHERE mm.entityId = :entityId AND ml.approvalStatus = 'APPROVED' " +
           "ORDER BY mm.displayOrder ASC, ml.createdAt DESC")
    List<MediaMapping> findApprovedByEntityId(@Param("entityId") Long entityId);

    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml " +
           "WHERE mm.entityId = :entityId AND mm.sectionKey = :sectionKey AND ml.approvalStatus = 'APPROVED' " +
           "ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedByEntityIdAndSectionKey(
        @Param("entityId") Long entityId,
        @Param("sectionKey") String sectionKey
    );

    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml " +
           "WHERE mm.districtId = :districtId AND ml.approvalStatus = 'APPROVED' " +
           "ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedByDistrictId(@Param("districtId") Long districtId);

    List<MediaMapping> findByMediaLinkId(Long mediaLinkId);
}
