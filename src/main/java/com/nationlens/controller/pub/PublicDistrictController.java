package com.nationlens.controller.pub;

import com.nationlens.domain.entity.District;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/districts")
@RequiredArgsConstructor
public class PublicDistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<District>>> list(
        @RequestParam(required = false) Boolean featured,
        @RequestParam(defaultValue = "6") int limit
    ) {
        if (Boolean.TRUE.equals(featured)) {
            int safeLimit = Math.min(Math.max(limit, 1), 20);
            return ResponseEntity.ok(ApiResponse.ok(districtService.listFeaturedForHome(safeLimit)));
        }
        return ResponseEntity.ok(ApiResponse.ok(districtService.listAll()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<District>> getBySlug(@PathVariable String slug) {
        return districtService.findBySlug(slug)
            .map(d -> ResponseEntity.ok(ApiResponse.ok(d)))
            .orElse(ResponseEntity.notFound().build());
    }
}
