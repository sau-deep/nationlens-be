package com.nationlens.service;

import com.nationlens.dto.auth.OtpSendResponse;

public interface Msg91Service {

    /**
     * Sends an OTP to the given mobile number via MSG91 widget.
     *
     * @param mobile 10-digit Indian mobile number (without country code)
     * @return OtpSendResponse containing the reqId needed for verification
     */
    OtpSendResponse sendOtp(String mobile);

    /**
     * Verifies the OTP submitted by the user.
     *
     * @param reqId the request ID returned by sendOtp
     * @param otp   the 6-digit OTP entered by the user
     * @return true if OTP is valid, false otherwise
     */
    boolean verifyOtp(String reqId, String otp);

    /**
     * Retries/resends the OTP for an existing request.
     *
     * @param reqId the request ID returned by sendOtp
     * @return OtpSendResponse with updated reqId if applicable
     */
    OtpSendResponse retryOtp(String reqId);
}
