package com.nationlens.controller.stateadmin;

import com.nationlens.domain.entity.User;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.stateadmin.*;
import com.nationlens.service.StateAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/state-admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN','STATE_ADMIN')")
public class StateAdminController {

    private final StateAdminService stateAdminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StateAdminDashboardDto>> dashboard(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.getDashboard(user)));
    }

    @GetMapping("/media")
    public ResponseEntity<ApiResponse<List<StateMediaItemDto>>> media(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.listMedia(user, status)));
    }

    @PatchMapping("/media/{id}/approve")
    public ResponseEntity<ApiResponse<StateMediaItemDto>> approveMedia(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.approveMedia(user, id)));
    }

    @PatchMapping("/media/{id}/reject")
    public ResponseEntity<ApiResponse<StateMediaItemDto>> rejectMedia(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.rejectMedia(user, id)));
    }

    @GetMapping("/entities")
    public ResponseEntity<ApiResponse<List<StateEntityItemDto>>> entities(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.listEntities(user)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<StateUserItemDto>>> users(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(stateAdminService.listUsers(user)));
    }
}
