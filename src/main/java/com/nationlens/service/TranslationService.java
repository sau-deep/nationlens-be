package com.nationlens.service;

import com.nationlens.domain.entity.ContentTranslation;
import com.nationlens.repository.ContentTranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hybrid content localization facade.
 *
 * <p>Resolution order for a (contentType, contentId, targetLocale, sourceText):
 * <ol>
 *   <li>target locale == source locale (or English source asked for English) → return source as-is</li>
 *   <li>a cached {@link ContentTranslation} whose source_hash still matches → return it</li>
 *   <li>Claude machine translation (if enabled) → cache it and return</li>
 *   <li>otherwise → return the source text (English fallback)</li>
 * </ol>
 *
 * Stored, human-REVIEWED translations always win over MACHINE ones for the same key,
 * and are never overwritten by the machine path.
 */
@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    // Content-type keys for the content_translations table.
    public static final String ENTITY_NAME = "ENTITY_NAME";
    public static final String ENTITY_DESC = "ENTITY_DESC";
    public static final String DISTRICT_NAME = "DISTRICT_NAME";
    public static final String STATE_NAME = "STATE_NAME";
    public static final String NEWS_TITLE = "NEWS_TITLE";
    public static final String NEWS_DESC = "NEWS_DESC";

    /** App locale code → human language name for the translation prompt. */
    private static final Map<String, String> LANGUAGE_NAMES = Map.ofEntries(
            Map.entry("en", "English"),
            Map.entry("hi", "Hindi"),
            Map.entry("bn", "Bengali"),
            Map.entry("te", "Telugu"),
            Map.entry("mr", "Marathi"),
            Map.entry("ta", "Tamil"),
            Map.entry("gu", "Gujarati"),
            Map.entry("kn", "Kannada"),
            Map.entry("ml", "Malayalam"),
            Map.entry("pa", "Punjabi"),
            Map.entry("or", "Odia"),
            Map.entry("as", "Assamese"),
            Map.entry("ur", "Urdu")
    );

    private final ContentTranslationRepository repository;
    private final AnthropicTranslationClient anthropic;

    public TranslationService(ContentTranslationRepository repository,
                              AnthropicTranslationClient anthropic) {
        this.repository = repository;
        this.anthropic = anthropic;
    }

    /**
     * Localize one field. {@code sourceText} is the English (or source-locale) value already
     * loaded from the row. Never returns null unless sourceText is null.
     */
    public String localize(String contentType, Long contentId, String sourceText,
                           String sourceLocale, String targetLocale) {
        if (sourceText == null || sourceText.isBlank()) return sourceText;
        String target = normalize(targetLocale);
        String source = normalize(sourceLocale);
        if (target.equals(source) || !LANGUAGE_NAMES.containsKey(target)) {
            return sourceText;
        }

        String hash = sha256(sourceText);
        Optional<ContentTranslation> existing =
                repository.findByContentTypeAndContentIdAndLocale(contentType, contentId, target);

        if (existing.isPresent()) {
            ContentTranslation row = existing.get();
            // Human-reviewed translations are authoritative; never auto-replace.
            if ("REVIEWED".equals(row.getStatus()) || hash.equals(row.getSourceHash())) {
                return row.getTranslatedText();
            }
            // Source text changed since the machine translation was cached → refresh.
            String fresh = anthropic.translate(sourceText, LANGUAGE_NAMES.get(target));
            if (fresh == null) return sourceText;
            row.setTranslatedText(fresh);
            row.setSourceHash(hash);
            row.setStatus("MACHINE");
            saveQuietly(row);
            return fresh;
        }

        String translated = anthropic.translate(sourceText, LANGUAGE_NAMES.get(target));
        if (translated == null) return sourceText; // English fallback

        ContentTranslation row = new ContentTranslation();
        row.setContentType(contentType);
        row.setContentId(contentId);
        row.setLocale(target);
        row.setTranslatedText(translated);
        row.setSourceHash(hash);
        row.setStatus("MACHINE");
        saveQuietly(row);
        return translated;
    }

    public boolean machineTranslationEnabled() {
        return anthropic.isEnabled();
    }

    public boolean isSupportedLocale(String locale) {
        return LANGUAGE_NAMES.containsKey(normalize(locale));
    }

    private void saveQuietly(ContentTranslation row) {
        try {
            repository.save(row);
        } catch (Exception e) {
            // A unique-key race or transient DB error must not break the read path.
            log.debug("content_translation save skipped: {}", e.getMessage());
        }
    }

    private static String normalize(String locale) {
        return locale == null ? "en" : locale.trim().toLowerCase();
    }

    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode());
        }
    }
}
