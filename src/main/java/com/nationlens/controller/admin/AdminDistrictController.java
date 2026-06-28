package com.nationlens.controller.admin;

import com.nationlens.dto.admin.AdminDistrictDto;
import com.nationlens.dto.admin.HomeFeaturedRequest;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/districts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminDistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminDistrictDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(districtService.listForAdmin()));
    }

    @PatchMapping("/{id}/featured")
    public ResponseEntity<ApiResponse<AdminDistrictDto>> updateFeatured(
            @PathVariable Long id,
            @RequestBody HomeFeaturedRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Featured status updated",
                districtService.updateHomeFeatured(id, body.getIsFeatured(), body.getHomeDisplayOrder())));
    }
}
