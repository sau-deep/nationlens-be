package com.nationlens.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicConfigDto {

    private boolean otpEnabled;
    private String otpBypassHint;
}
