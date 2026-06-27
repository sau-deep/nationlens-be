package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.common.PublicConfigDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicConfigController {

    @Value("${nationlens.otp.enabled:false}")
    private boolean otpEnabled;

    @Value("${nationlens.otp.dev-bypass-code:123456}")
    private String devBypassCode;

    @Value("${nationlens.otp.dev-login:false}")
    private boolean devLoginEnabled;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PublicConfigDto>> getConfig() {
        String hint = otpEnabled ? null : "Use " + devBypassCode;
        return ResponseEntity.ok(ApiResponse.ok(new PublicConfigDto(otpEnabled, hint, devLoginEnabled)));
    }
}
