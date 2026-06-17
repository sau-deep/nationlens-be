package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public media feed endpoint.
 *
 * Query params (all optional, combined with AND logic):
 *   scope   = NATIONAL | STATE | DISTRICT | ENTITY | GENERAL
 *   state   = 2-letter state code, e.g. MH, DL, KA (used when scope=STATE or for state feed)
 *   hashtag = single tag to filter by, e.g. parliament
 *   section = section key, e.g. SPEECHES_INTERVIEWS
 *
 * Common use-cases:
 *   /public/media/feed?scope=NATIONAL                         → pan-India content (home feed)
 *   /public/media/feed?state=MH                              → Maharashtra feed (NATIONAL + STATE:MH)
 *   /public/media/feed?hashtag=parliament                    → parliament-tagged content
 *   /public/media/feed?section=CRITICAL_REPORTS              → critical reports section
 */
@RestController
@RequestMapping("/public/media/feed")
@RequiredArgsConstructor
public class MediaFeedController {

    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> getFeed(
        @RequestParam(required = false) String scope,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String hashtag,
        @RequestParam(required = false) String section
    ) {
        List<MediaLinkDto> result;

        // Priority: state feed > hashtag > section > scope
        if (state != null && !state.isBlank()) {
            // Returns NATIONAL + STATE:<code> combined feed
            result = mediaService.getMediaForStateFeed(state.toUpperCase());
        } else if (hashtag != null && !hashtag.isBlank()) {
            result = mediaService.getMediaByTag(hashtag.toLowerCase());
        } else if (section != null && !section.isBlank()) {
            result = mediaService.getMediaBySection(section.toUpperCase());
        } else if (scope != null && !scope.isBlank()) {
            result = mediaService.getMediaByScope(scope.toUpperCase());
        } else {
            // Default: return NATIONAL-scoped content (home feed)
            result = mediaService.getMediaByScope("NATIONAL");
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
