package com.nationlens.controller.analyst;

import com.nationlens.dto.analyst.AnalystDashboardDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.media.MediaLinkDto;
import com.nationlens.service.AnalystService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analyst")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','ANALYST')")
public class AnalystController {

    private final AnalystService analystService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalystDashboardDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(analystService.getDashboard()));
    }

    @GetMapping("/media/approved")
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> approvedMedia(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(analystService.listApprovedMedia(Math.min(limit, 200))));
    }

    @GetMapping("/media/pending")
    public ResponseEntity<ApiResponse<List<MediaLinkDto>>> pendingMedia(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(analystService.listPendingMedia(Math.min(limit, 200))));
    }
}
