package com.nationlens.service;

import com.nationlens.domain.entity.RssNewsItem;
import com.nationlens.dto.news.NewsItemDto;
import com.nationlens.repository.RssNewsItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsFeedService {

    private final RssNewsItemRepository newsItemRepository;

    /**
     * Localized feed rules:
     * - NATIONAL city items are always included (everyone sees national news).
     * - City items match user's metro + prefer source language; EN used as fallback.
     */
    public Page<NewsItemDto> getFeed(String city, String category, String language, int page, int size) {
        size = Math.min(size, 50);
        PageRequest pageable = PageRequest.of(page, size);
        String lang = normalizeLanguage(language);
        String cat = blankToNull(category);
        String cityKey = blankToNull(city);

        Page<RssNewsItem> result;
        if (cityKey != null) {
            List<String> cityKeys = "NATIONAL".equalsIgnoreCase(cityKey)
                    ? List.of("NATIONAL")
                    : List.of(cityKey.toUpperCase(), "NATIONAL");
            result = cat != null
                    ? newsItemRepository.findLocalizedFeed(cityKeys, cat, lang, pageable)
                    : newsItemRepository.findLocalizedFeed(cityKeys, lang, pageable);
        } else if (cat != null) {
            result = newsItemRepository.findLocalizedFeedByCategory(cat, lang, pageable);
        } else {
            result = newsItemRepository.findLocalizedFeedAll(lang, pageable);
        }
        return result.map(NewsItemDto::new);
    }

    public Optional<NewsItemDto> getById(Long id) {
        return newsItemRepository.findById(id)
                .filter(i -> Boolean.TRUE.equals(i.getActive()))
                .map(NewsItemDto::new);
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "EN";
        return language.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
