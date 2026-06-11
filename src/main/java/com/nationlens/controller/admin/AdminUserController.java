package com.nationlens.controller.admin;

import com.nationlens.dto.admin.UpdateUserRolesRequest;
import com.nationlens.dto.admin.UserListDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminUserController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserListDto>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userManagementService.listUsers(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserListDto>> getUser(@PathVariable Long id) {
        return userManagementService.findById(id)
            .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserListDto>> updateRoles(
            @PathVariable Long id,
            @RequestBody UpdateUserRolesRequest request) {
        UserListDto updated = userManagementService.updateRoles(id, request.getRoleCodes());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserListDto>> activate(@PathVariable Long id) {
        UserListDto updated = userManagementService.setActive(id, true);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserListDto>> deactivate(@PathVariable Long id) {
        UserListDto updated = userManagementService.setActive(id, false);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
