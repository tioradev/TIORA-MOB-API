package com.tiora.mob.dto.response;


public class JwtResponse {
    private String token;
    private boolean isProfileComplete;

    public JwtResponse(String token, boolean isProfileComplete) {
        this.token = token;
        this.isProfileComplete = isProfileComplete;
    }

    public String getToken() {
        return token;
    }

    public boolean isProfileComplete() {
        return isProfileComplete;
    }
}