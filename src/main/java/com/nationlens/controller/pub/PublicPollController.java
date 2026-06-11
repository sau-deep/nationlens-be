package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.poll.PollDto;
import com.nationlens.dto.poll.VoteRequest;
import com.nationlens.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/polls")
@RequiredArgsConstructor
public class PublicPollController {

    private final PollService pollService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PollDto>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(pollService.listActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PollDto>> getById(@PathVariable Long id) {
        return pollService.findById(id)
            .map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<PollDto>> vote(
        @PathVariable Long id,
        @Valid @RequestBody VoteRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(pollService.vote(id, request.getPollOptionId(), userId)));
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.nationlens.domain.entity.User u) {
            return u.getId();
        }
        throw new IllegalStateException("Cannot extract user id");
    }
}
