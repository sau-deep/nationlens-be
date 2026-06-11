package com.nationlens.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String mobile;
    private String displayName;
    private String preferredLanguage;
    private Long districtId;
    private Integer contributionScore;
    private String gamificationLevel;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private List<String> roles;
}
