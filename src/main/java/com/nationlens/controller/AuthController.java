package com.nationlens.controller;

import com.nationlens.domain.entity.Role;
import com.nationlens.domain.entity.User;
import com.nationlens.dto.auth.AuthResponse;
import com.nationlens.dto.auth.LoginRequest;
import com.nationlens.dto.auth.RegisterRequest;
import com.nationlens.dto.auth.UpdateProfileRequest;
import com.nationlens.dto.auth.UserProfileDto;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.repository.UserRepository;
import com.nationlens.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .or(() -> userRepository.findByMobile(userDetails.getUsername()))
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        UserProfileDto profile = UserProfileDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .mobile(user.getMobile())
            .displayName(user.getDisplayName())
            .preferredLanguage(user.getPreferredLanguage())
            .districtId(user.getDistrictId())
            .contributionScore(user.getContributionScore())
            .gamificationLevel(user.getGamificationLevel())
            .isActive(user.getIsActive())
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .roles(user.getRoles().stream().map(Role::getCode).toList())
            .build();

        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .or(() -> userRepository.findByMobile(userDetails.getUsername()))
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            user.setMobile(request.getMobile());
        }
        if (request.getDistrictId() != null) {
            user.setDistrictId(request.getDistrictId());
        }
        userRepository.save(user);

        UserProfileDto profile = UserProfileDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .mobile(user.getMobile())
            .displayName(user.getDisplayName())
            .preferredLanguage(user.getPreferredLanguage())
            .districtId(user.getDistrictId())
            .contributionScore(user.getContributionScore())
            .gamificationLevel(user.getGamificationLevel())
            .isActive(user.getIsActive())
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .roles(user.getRoles().stream().map(Role::getCode).toList())
            .build();

        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
