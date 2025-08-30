package com.tiora.mob.dto.response;


public class JwtResponse {
    private String token;
    private boolean isProfileComplete;
    private boolean customerExists;

    public JwtResponse(String token, boolean isProfileComplete, boolean customerExists) {
        this.token = token;
        this.isProfileComplete = isProfileComplete;
        this.customerExists = customerExists;
    }

    public String getToken() {
        return token;
    }

    public boolean isProfileComplete() {
        return isProfileComplete;
    }

    public boolean isCustomerExists() {
        return customerExists;
    }
}