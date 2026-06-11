package com.nationlens.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendResponse {

    private String reqId;
    private String widgetId;
    private boolean success;
    private String message;
}
