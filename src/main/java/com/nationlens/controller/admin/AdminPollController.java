package com.nationlens.controller.admin;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.poll.AdminPollRequest;
import com.nationlens.dto.poll.PollDto;
import com.nationlens.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/polls")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NATIONAL_ADMIN')")
public class AdminPollController {

    private final PollService pollService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PollDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(pollService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PollDto>> getById(@PathVariable Long id) {
        return pollService.findById(id)
            .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PollDto>> create(
            @Valid @RequestBody AdminPollRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Poll created", pollService.create(request, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PollDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminPollRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Poll updated", pollService.update(id, request)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<PollDto>> setActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("isActive"));
        return ResponseEntity.ok(ApiResponse.ok("Poll status updated", pollService.setActive(id, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pollService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.nationlens.domain.entity.User u) {
            return u.getId();
        }
        throw new IllegalStateException("Cannot extract user id");
    }
}
