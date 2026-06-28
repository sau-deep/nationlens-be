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
    private final TranslationService translationService;

    /**
     * Localized feed rules:
     * - Only items whose {@code sourceLanguage} matches the selected UI locale are returned.
     * - Metro feeds include that city plus NATIONAL, both filtered to the same language.
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
        final String target = lang.toLowerCase(Locale.ROOT);
        return result.map(item -> toLocalizedDto(item, target));
    }

    public Optional<NewsItemDto> getById(Long id, String language) {
        final String lang = normalizeLanguage(language);
        final String target = lang.toLowerCase(Locale.ROOT);
        return newsItemRepository.findById(id)
                .filter(i -> Boolean.TRUE.equals(i.getActive()))
                .filter(i -> lang.equals(normalizeLanguage(i.getSourceLanguage())))
                .map(item -> toLocalizedDto(item, target));
    }

    /**
     * Resolve title + description into {@code targetLocale} (on-read + cache). When the
     * Claude key is unset the {@link TranslationService} returns the source text, so this
     * is a no-op until translation is switched on.
     */
    private NewsItemDto toLocalizedDto(RssNewsItem item, String targetLocale) {
        String source = item.getSourceLanguage() == null ? "en" : item.getSourceLanguage().toLowerCase(Locale.ROOT);
        String title = translationService.localize(
                TranslationService.NEWS_TITLE, item.getId(), item.getTitle(), source, targetLocale);
        String description = translationService.localize(
                TranslationService.NEWS_DESC, item.getId(), item.getDescription(), source, targetLocale);
        return new NewsItemDto(item, title, description);
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "EN";
        return language.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
