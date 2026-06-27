package com.nationlens.repository;

import com.nationlens.domain.entity.RssFeedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RssFeedSourceRepository extends JpaRepository<RssFeedSource, Long> {
    List<RssFeedSource> findByActiveTrue();
    List<RssFeedSource> findByCityKeyAndActiveTrue(String cityKey);

    @Query("SELECT MAX(s.lastFetchedAt) FROM RssFeedSource s")
    Optional<LocalDateTime> findMaxLastFetchedAt();
}
