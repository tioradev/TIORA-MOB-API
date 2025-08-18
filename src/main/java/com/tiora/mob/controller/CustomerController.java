package com.tiora.mob.controller;



import com.tiora.mob.dto.request.ProfileUpdateRequest;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(customerService.getCustomerProfile(token));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        customerService.updateCustomerProfile(token, request);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}