package com.nationlens.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String mobile;

    @Email
    private String email;

    @NotBlank
    private String displayName;

    @NotBlank
    @Size(min = 8)
    private String password;

    private String preferredLanguage = "en";
}
