package com.tiora.mob.dto.response;

public class OtpResponse {
    private String phoneNumber;
    private String message;

    public OtpResponse(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }
}