package com.nationlens.dto.news;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nationlens.domain.entity.RssNewsItem;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsItemDto {

    private final Long id;
    private final String title;
    private final String description;
    private final String articleUrl;
    private final String thumbnailUrl;
    private final String author;
    private final LocalDateTime publishedAt;
    private final String cityKey;
    private final String category;
    private final String sourceName;
    private final String sourceLanguage;
    private final LocalDateTime fetchedAt;

    public NewsItemDto(RssNewsItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.description = item.getDescription();
        this.articleUrl = item.getArticleUrl();
        this.thumbnailUrl = item.getThumbnailUrl();
        this.author = item.getAuthor();
        this.publishedAt = item.getPublishedAt();
        this.cityKey = item.getCityKey();
        this.category = item.getCategory();
        this.sourceName = item.getSourceName();
        this.sourceLanguage = item.getSourceLanguage();
        this.fetchedAt = item.getFetchedAt();
    }
}
