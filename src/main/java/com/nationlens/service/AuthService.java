package com.nationlens.service;

import com.nationlens.domain.entity.Role;
import com.nationlens.domain.entity.User;
import com.nationlens.dto.auth.AuthResponse;
import com.nationlens.dto.auth.LoginRequest;
import com.nationlens.dto.auth.RegisterRequest;
import com.nationlens.repository.RoleRepository;
import com.nationlens.repository.UserRepository;
import com.nationlens.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (request.getMobile() != null && userRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile already registered");
        }

        Role citizenRole = roleRepository.findByCode("CITIZEN")
            .orElseThrow(() -> new IllegalStateException("CITIZEN role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPreferredLanguage(request.getPreferredLanguage());
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(Set.of(citizenRole));
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getUsername())
            .or(() -> userRepository.findByMobile(request.getUsername()))
            .orElseThrow();
        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        List<String> roles = user.getRoles().stream().map(Role::getCode).toList();
        return new AuthResponse(token, user.getId(), user.getDisplayName(), user.getEmail(), roles);
    }
}
