package com.nationlens.repository;

import com.nationlens.domain.entity.ContentTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentTranslationRepository extends JpaRepository<ContentTranslation, Long> {

    Optional<ContentTranslation> findByContentTypeAndContentIdAndLocale(
            String contentType, Long contentId, String locale);

    /** Bulk lookup for a set of source rows in one locale (used to localize list responses). */
    List<ContentTranslation> findByContentTypeAndLocaleAndContentIdIn(
            String contentType, String locale, List<Long> contentIds);
}
