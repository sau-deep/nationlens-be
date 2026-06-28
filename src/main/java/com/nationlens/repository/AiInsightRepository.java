package com.nationlens.repository;

import com.nationlens.domain.entity.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {

    @Query("SELECT i FROM AiInsight i WHERE i.isPublished = true ORDER BY i.homeDisplayOrder ASC, i.generatedAt DESC")
    List<AiInsight> findPublishedForHome();
}
