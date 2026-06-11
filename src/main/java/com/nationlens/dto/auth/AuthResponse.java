package com.nationlens.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String displayName;
    private String email;
    private List<String> roles;

    public AuthResponse(String token, Long userId, String displayName, String email, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.roles = roles;
    }
}
