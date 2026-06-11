package com.nationlens.controller;

import com.nationlens.domain.entity.Role;
import com.nationlens.domain.entity.User;
import com.nationlens.dto.auth.AuthResponse;
import com.nationlens.dto.auth.OtpSendRequest;
import com.nationlens.dto.auth.OtpSendResponse;
import com.nationlens.dto.auth.OtpVerifyRequest;
import com.nationlens.dto.common.ApiResponse;
import com.nationlens.repository.RoleRepository;
import com.nationlens.repository.UserRepository;
import com.nationlens.security.JwtService;
import com.nationlens.service.Msg91Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class AuthOtpController {

    private final Msg91Service msg91Service;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<OtpSendResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {
        OtpSendResponse response = msg91Service.sendOtp(request.getMobile());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        boolean valid = msg91Service.verifyOtp(request.getReqId(), request.getOtp());
        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByMobile(request.getMobile())
            .orElseGet(() -> createUserForMobile(request.getMobile()));

        // Mark mobile as verified
        if (!Boolean.TRUE.equals(user.getMobileVerified())) {
            user.setMobileVerified(true);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        List<String> roles = user.getRoles().stream().map(Role::getCode).toList();
        AuthResponse authResponse = new AuthResponse(
            token, user.getId(), user.getDisplayName(), user.getEmail(), roles
        );
        return ResponseEntity.ok(ApiResponse.ok(authResponse));
    }

    @PostMapping("/retry")
    public ResponseEntity<ApiResponse<OtpSendResponse>> retryOtp(
            @RequestBody Map<String, String> body) {
        String reqId = body.get("reqId");
        if (reqId == null || reqId.isBlank()) {
            throw new IllegalArgumentException("reqId is required");
        }
        OtpSendResponse response = msg91Service.retryOtp(reqId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private User createUserForMobile(String mobile) {
        Role citizenRole = roleRepository.findByCode("CITIZEN")
            .orElseThrow(() -> new IllegalStateException("CITIZEN role not found"));

        User newUser = new User();
        newUser.setMobile(mobile);
        newUser.setDisplayName("User_" + mobile.substring(6));
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setMobileVerified(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setRoles(Set.of(citizenRole));
        return userRepository.save(newUser);
    }
}
