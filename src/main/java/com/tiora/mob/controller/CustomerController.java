package com.tiora.mob.controller;



import com.tiora.mob.dto.request.ProfileUpdateRequest;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/mobile/customers")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(
            @RequestHeader("Authorization") String token) {
        logger.info("getCustomerProfile called");
        CustomerProfileResponse response = customerService.getCustomerProfile(token);
        logger.info("getCustomerProfile response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("updateProfile called with request: {}", request);
        customerService.updateCustomerProfile(token, request);
        logger.info("Profile updated successfully");
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}