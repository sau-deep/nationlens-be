package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.entity.EntityDetailDto;
import com.nationlens.dto.entity.EntitySummaryDto;
import com.nationlens.service.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/entities")
@RequiredArgsConstructor
public class PublicEntityController {

    private final EntityService entityService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EntitySummaryDto>>> list(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(defaultValue = "6") int limit
    ) {
        if (Boolean.TRUE.equals(featured)) {
            int safeLimit = Math.min(Math.max(limit, 1), 20);
            List<EntitySummaryDto> items = entityService.listFeaturedForHome(safeLimit);
            Page<EntitySummaryDto> page = new org.springframework.data.domain.PageImpl<>(items, pageable, items.size());
            return ResponseEntity.ok(ApiResponse.ok(page));
        }
        if (type != null && !type.isBlank()) {
            List<EntitySummaryDto> items = entityService.listByType(type.trim().toUpperCase());
            Page<EntitySummaryDto> page = new org.springframework.data.domain.PageImpl<>(items, pageable, items.size());
            return ResponseEntity.ok(ApiResponse.ok(page));
        }
        return ResponseEntity.ok(ApiResponse.ok(entityService.listEntities(pageable)));
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<ApiResponse<List<EntitySummaryDto>>> listByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(ApiResponse.ok(entityService.listByDistrict(districtId)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<EntityDetailDto>> getBySlug(@PathVariable String slug) {
        return entityService.findBySlug(slug)
            .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
            .orElse(ResponseEntity.notFound().build());
    }
}
