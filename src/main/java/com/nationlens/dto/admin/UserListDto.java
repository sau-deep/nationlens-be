package com.nationlens.dto.admin;

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
public class UserListDto {
    private Long id;
    private String email;
    private String mobile;
    private String displayName;
    private Boolean isActive;
    private Boolean emailVerified;
    private String gamificationLevel;
    private Integer contributionScore;
    private LocalDateTime createdAt;
    private List<String> roles;
}
