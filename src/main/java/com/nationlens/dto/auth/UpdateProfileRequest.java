package com.nationlens.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 60)
    private String displayName;

    private String preferredLanguage;

    private String mobile;

    private Long districtId;
}
