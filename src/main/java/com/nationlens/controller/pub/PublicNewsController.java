package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.news.NewsItemDto;
import com.nationlens.repository.RssNewsItemRepository;
import com.nationlens.service.NewsFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicNewsController {

    private final NewsFeedService newsFeedService;
    private final RssNewsItemRepository newsItemRepository;

    /**
     * Paginated localized news feed.
     *
     * @param city     metro key e.g. DELHI — returns that city PLUS NATIONAL (always).
     * @param category ADR, LEGAL, ELECTIONS, CIVIC, NATIONAL_NEWS, CITY_NEWS
     * @param language UI locale code e.g. hi, en, ta — maps to RSS source_language
     */
    @GetMapping("/news")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listNews(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "en") String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = newsFeedService.getFeed(city, category, language, page, size);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber()
        )));
    }

    @GetMapping("/news/{id}")
    public ResponseEntity<ApiResponse<NewsItemDto>> getNewsItem(@PathVariable Long id) {
        return newsFeedService.getById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/news/cities")
    public ResponseEntity<ApiResponse<List<String>>> listCities() {
        return ResponseEntity.ok(ApiResponse.ok(newsItemRepository.findDistinctCityKeys()));
    }

    @GetMapping("/news/categories")
    public ResponseEntity<ApiResponse<List<String>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.ok(newsItemRepository.findDistinctCategories()));
    }
}
