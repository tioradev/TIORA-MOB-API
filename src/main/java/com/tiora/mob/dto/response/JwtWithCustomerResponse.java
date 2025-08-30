package com.tiora.mob.dto.response;

import com.tiora.mob.dto.response.CustomerProfileResponse;

public class JwtWithCustomerResponse extends JwtResponse {
    private CustomerProfileResponse customer;

    public JwtWithCustomerResponse(String token, boolean isProfileComplete, boolean customerExists, CustomerProfileResponse customer) {
        super(token, isProfileComplete, customerExists);
        this.customer = customer;
    }

    public CustomerProfileResponse getCustomer() {
        return customer;
    }
}
