package com.nationlens.service;

import com.nationlens.dto.auth.OtpSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Msg91ServiceImpl implements Msg91Service {

    private final RestTemplate restTemplate;

    @Value("${msg91.authkey}")
    private String authkey;

    @Value("${msg91.widget-id}")
    private String widgetId;

    @Value("${msg91.base-url}")
    private String baseUrl;

    @Value("${nationlens.otp.enabled:false}")
    private boolean otpEnabled;

    @Value("${nationlens.otp.dev-bypass-code:123456}")
    private String devBypassCode;

    public Msg91ServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OtpSendResponse sendOtp(String mobile) {
        if (!otpEnabled) {
            log.info("OTP disabled – skipping MSG91 send for mobile {}", mobile);
            return new OtpSendResponse("dev-bypass", widgetId, true, "OTP delivery skipped (OTP disabled)");
        }

        String url = baseUrl + "/sendOtp";
        Map<String, String> body = new HashMap<>();
        body.put("widgetId", widgetId);
        body.put("identifier", "91" + mobile);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
            );
            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && "success".equals(responseBody.get("type"))) {
                String reqId = (String) responseBody.get("message");
                return new OtpSendResponse(reqId, widgetId, true, "OTP sent successfully");
            } else {
                String message = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.warn("MSG91 sendOtp non-success response: {}", responseBody);
                return new OtpSendResponse(null, widgetId, false, message);
            }
        } catch (Exception e) {
            log.error("MSG91 sendOtp failed for mobile {}: {}", mobile, e.getMessage(), e);
            return new OtpSendResponse(null, widgetId, false, "Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOtp(String reqId, String otp) {
        if (!otpEnabled) {
            boolean valid = devBypassCode.equals(otp);
            if (valid) {
                log.info("OTP bypass code accepted (OTP disabled)");
            } else {
                log.warn("Invalid OTP bypass attempt while OTP is disabled");
            }
            return valid;
        }

        String url = baseUrl + "/verifyOtp";
        Map<String, String> body = new HashMap<>();
        body.put("widgetId", widgetId);
        body.put("reqId", reqId);
        body.put("otp", otp);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
            );
            Map<?, ?> responseBody = response.getBody();
            boolean success = responseBody != null && "success".equals(responseBody.get("type"));
            if (!success) {
                log.warn("MSG91 verifyOtp failed for reqId {}: {}", reqId, responseBody);
            }
            return success;
        } catch (Exception e) {
            log.error("MSG91 verifyOtp failed for reqId {}: {}", reqId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public OtpSendResponse retryOtp(String reqId) {
        if (!otpEnabled) {
            log.info("OTP disabled – skipping MSG91 retry for reqId {}", reqId);
            return new OtpSendResponse(reqId != null ? reqId : "dev-bypass", widgetId, true,
                "OTP delivery skipped (OTP disabled)");
        }

        String url = baseUrl + "/retryOtp";
        Map<String, String> body = new HashMap<>();
        body.put("widgetId", widgetId);
        body.put("reqId", reqId);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
            );
            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && "success".equals(responseBody.get("type"))) {
                String newReqId = responseBody.containsKey("message") ? (String) responseBody.get("message") : reqId;
                return new OtpSendResponse(newReqId, widgetId, true, "OTP resent successfully");
            } else {
                String message = responseBody != null ? (String) responseBody.get("message") : "Unknown error";
                log.warn("MSG91 retryOtp non-success response for reqId {}: {}", reqId, responseBody);
                return new OtpSendResponse(reqId, widgetId, false, message);
            }
        } catch (Exception e) {
            log.error("MSG91 retryOtp failed for reqId {}: {}", reqId, e.getMessage(), e);
            return new OtpSendResponse(reqId, widgetId, false, "Failed to retry OTP: " + e.getMessage());
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authkey", authkey);
        return headers;
    }
}
