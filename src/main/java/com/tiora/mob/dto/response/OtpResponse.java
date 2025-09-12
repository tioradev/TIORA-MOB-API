package com.tiora.mob.dto.response;

public class OtpResponse {
    private String phoneNumber;
    private String message;
    private String otp;

    public OtpResponse(String phoneNumber, String message, String otp) {
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.otp = otp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getOtp() {
        return otp;
    }
}