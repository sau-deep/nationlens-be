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
 * Query params (optional):
 *   context     = HOME (admin-curated home slots)
 *   scope       = NATIONAL | STATE | DISTRICT | ENTITY | GENERAL | ENTITY_TYPE
 *   state       = 2-letter state code (MH, DL, …)
 *   hashtag     = topic tag, e.g. parliament
 *   section     = profile or browse section key
 *   entityType  = MP | MLA | … (type-wide reel placement)
 *   entityId    = specific entity profile
 *   districtId  = specific district page
 *   displayContext = HOME | BROWSE | ENTITY_PROFILE | DISTRICT_FEED | STATE_FEED
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
        @RequestParam(required = false) String section,
        @RequestParam(required = false) String context,
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) Long entityId,
        @RequestParam(required = false) Long districtId,
        @RequestParam(required = false) String displayContext
    ) {
        List<MediaLinkDto> result = mediaService.queryPublicFeed(
            context, scope, state, hashtag, section, entityType, entityId, districtId, displayContext
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
