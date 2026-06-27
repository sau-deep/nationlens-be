package com.nationlens.repository;

import com.nationlens.domain.entity.MediaOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaOwnerRepository extends JpaRepository<MediaOwner, Long> {
    Optional<MediaOwner> findBySlug(String slug);
    List<MediaOwner> findByParentIsNullOrderByNameEnAsc();
    List<MediaOwner> findByParentIdOrderByNameEnAsc(Long parentId);
}
