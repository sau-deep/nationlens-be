package com.nationlens.repository;

import com.nationlens.domain.entity.RssNewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RssNewsItemRepository extends JpaRepository<RssNewsItem, Long> {

    boolean existsByGuid(String guid);

    Page<RssNewsItem> findByActiveTrueOrderByPublishedAtDesc(Pageable pageable);

    Page<RssNewsItem> findByCityKeyInAndActiveTrueOrderByPublishedAtDesc(
            List<String> cityKeys, Pageable pageable);

    Page<RssNewsItem> findByCategoryAndActiveTrueOrderByPublishedAtDesc(
            String category, Pageable pageable);

    Page<RssNewsItem> findByCityKeyInAndCategoryAndActiveTrueOrderByPublishedAtDesc(
            List<String> cityKeys, String category, Pageable pageable);

    /** NATIONAL always visible; city items match language or EN fallback. */
    @Query("""
            SELECT n FROM RssNewsItem n
            WHERE n.active = true
              AND (
                n.cityKey = 'NATIONAL'
                OR (n.cityKey IN :cityKeys AND (UPPER(n.sourceLanguage) = :lang OR UPPER(n.sourceLanguage) = 'EN'))
              )
            ORDER BY n.publishedAt DESC, n.fetchedAt DESC
            """)
    Page<RssNewsItem> findLocalizedFeed(
            @Param("cityKeys") List<String> cityKeys,
            @Param("lang") String lang,
            Pageable pageable);

    @Query("""
            SELECT n FROM RssNewsItem n
            WHERE n.active = true
              AND n.category = :category
              AND (
                n.cityKey = 'NATIONAL'
                OR (n.cityKey IN :cityKeys AND (UPPER(n.sourceLanguage) = :lang OR UPPER(n.sourceLanguage) = 'EN'))
              )
            ORDER BY n.publishedAt DESC, n.fetchedAt DESC
            """)
    Page<RssNewsItem> findLocalizedFeed(
            @Param("cityKeys") List<String> cityKeys,
            @Param("category") String category,
            @Param("lang") String lang,
            Pageable pageable);

    @Query("""
            SELECT n FROM RssNewsItem n
            WHERE n.active = true
              AND n.category = :category
              AND (n.cityKey = 'NATIONAL' OR UPPER(n.sourceLanguage) = :lang OR UPPER(n.sourceLanguage) = 'EN')
            ORDER BY n.publishedAt DESC, n.fetchedAt DESC
            """)
    Page<RssNewsItem> findLocalizedFeedByCategory(
            @Param("category") String category,
            @Param("lang") String lang,
            Pageable pageable);

    @Query("""
            SELECT n FROM RssNewsItem n
            WHERE n.active = true
              AND (n.cityKey = 'NATIONAL' OR UPPER(n.sourceLanguage) = :lang OR UPPER(n.sourceLanguage) = 'EN')
            ORDER BY n.publishedAt DESC, n.fetchedAt DESC
            """)
    Page<RssNewsItem> findLocalizedFeedAll(@Param("lang") String lang, Pageable pageable);

    @Query("SELECT DISTINCT n.cityKey FROM RssNewsItem n WHERE n.active = true AND n.cityKey IS NOT NULL")
    List<String> findDistinctCityKeys();

    @Query("SELECT DISTINCT n.category FROM RssNewsItem n WHERE n.active = true AND n.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Modifying
    @Transactional
    @Query("DELETE FROM RssNewsItem n WHERE n.fetchedAt < :cutoff")
    int deleteByFetchedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
