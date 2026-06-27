package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.BrowseSectionDto;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.dto.media.MediaOwnerDto;
import com.nationlens.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public "browse by section" experience.
 *
 *   GET /public/browse/sections                 → the six sections + counts
 *   GET /public/browse/sections/{key}           → reels/videos in a section
 *   GET /public/browse/owners                   → media ownership tree
 *   GET /public/browse/owners/{slug}            → one owner + its media
 *
 * Sections: POLITICS | ENVIRONMENT | GOVERNMENT | MEDIA | JUDICIARY | CITIZEN
 */
@RestController
@RequestMapping("/public/browse")
@RequiredArgsConstructor
public class PublicBrowseController {

    private final MediaService mediaService;

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<BrowseSectionDto>>> sections() {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getBrowseSections()));
    }

    @GetMapping("/sections/{key}")
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> sectionMedia(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getBrowseSectionMedia(key)));
    }

    @GetMapping("/owners")
    public ResponseEntity<ApiResponse<List<MediaOwnerDto>>> owners() {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getOwnershipTree()));
    }

    @GetMapping("/owners/{slug}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> owner(@PathVariable String slug) {
        return mediaService.getOwnerWithMedia(slug)
            .map(data -> ResponseEntity.ok(ApiResponse.ok(data)))
            .orElse(ResponseEntity.notFound().build());
    }
}
