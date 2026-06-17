package com.nationlens.repository;

import com.nationlens.domain.entity.RssFeedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RssFeedSourceRepository extends JpaRepository<RssFeedSource, Long> {
    List<RssFeedSource> findByActiveTrue();
    List<RssFeedSource> findByCityKeyAndActiveTrue(String cityKey);
}
