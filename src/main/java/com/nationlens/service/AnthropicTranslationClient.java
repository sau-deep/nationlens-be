package com.nationlens.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin client over the Anthropic Messages API used for content translation.
 *
 * Dormant by default: until {@code anthropic.api-key} is configured (and
 * {@code anthropic.enabled=true}), {@link #translate} returns {@code null} so the
 * caller falls back to the source text. Drop the key into application.yml /
 * application-production.yml to switch it on — no code change required.
 */
@Component
public class AnthropicTranslationClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicTranslationClient.class);

    private final RestTemplate restTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${anthropic.enabled:false}")
    private boolean enabled;

    @Value("${anthropic.api-key:}")
    private String apiKey;

    @Value("${anthropic.base-url:https://api.anthropic.com/v1/messages}")
    private String baseUrl;

    @Value("${anthropic.model:claude-haiku-4-5}")
    private String model;

    @Value("${anthropic.version:2023-06-01}")
    private String anthropicVersion;

    @Value("${anthropic.max-tokens:1024}")
    private int maxTokens;

    public AnthropicTranslationClient(RestTemplate restTemplate,
                                      com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Translate {@code sourceText} into {@code targetLanguageName}.
     * Returns {@code null} on any failure or when disabled (caller should fall back).
     */
    public String translate(String sourceText, String targetLanguageName) {
        if (!isEnabled() || sourceText == null || sourceText.isBlank()) {
            return null;
        }

        String prompt = """
                Translate the text below into %s.
                Rules:
                - Return ONLY the translation, with no quotes, labels, or explanation.
                - Keep the brand name "NationLens" unchanged.
                - Keep numbers, URLs, and placeholders like {count} unchanged.
                - Use natural, concise wording suitable for a civic news app UI.

                Text:
                %s""".formatted(targetLanguageName, sourceText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", anthropicVersion);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String json = restTemplate.postForObject(baseUrl, new HttpEntity<>(body, headers), String.class);
            if (json == null) return null;
            JsonNode root = objectMapper.readTree(json);
            JsonNode content = root.path("content");
            if (content.isArray() && !content.isEmpty()) {
                String text = content.get(0).path("text").asText(null);
                return (text != null && !text.isBlank()) ? text.trim() : null;
            }
            log.warn("Anthropic translation: unexpected response shape: {}", json);
            return null;
        } catch (Exception e) {
            log.warn("Anthropic translation failed ({}): {}", targetLanguageName, e.getMessage());
            return null;
        }
    }
}
