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

    List<MediaMapping> findByEntityId(Long entityId);

    // Find all APPROVED content for a given audience scope
    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND mm.audienceScope = :scope ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedByScope(@Param("scope") String scope);

    // Find APPROVED content for NATIONAL + specific state (home feed)
    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND (mm.audienceScope = 'NATIONAL' OR (mm.audienceScope = 'STATE' AND mm.stateCode = :stateCode)) ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedForStateFeed(@Param("stateCode") String stateCode);

    // Find APPROVED content by hashtag tag (comma-separated search)
    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND mm.tags IS NOT NULL AND mm.tags LIKE %:tag% ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedByTag(@Param("tag") String tag);

    // Find APPROVED content by section key (for section-specific feeds)
    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND mm.sectionKey = :sectionKey ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedBySectionKey(@Param("sectionKey") String sectionKey);

    // Browse sections: APPROVED content in a top-level browse section (display_context = 'BROWSE')
    @Query("SELECT mm FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND mm.displayContext = 'BROWSE' AND mm.sectionKey = :sectionKey ORDER BY mm.displayOrder ASC")
    List<MediaMapping> findApprovedBrowseBySection(@Param("sectionKey") String sectionKey);

    // Browse section counts: [sectionKey, count] for all browse sections
    @Query("SELECT mm.sectionKey, COUNT(mm) FROM MediaMapping mm JOIN mm.mediaLink ml WHERE ml.approvalStatus = 'APPROVED' AND mm.displayContext = 'BROWSE' GROUP BY mm.sectionKey")
    List<Object[]> countApprovedBrowseBySection();
}
