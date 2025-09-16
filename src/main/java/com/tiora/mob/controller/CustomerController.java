
package com.tiora.mob.controller;



import com.tiora.mob.dto.request.ProfileUpdateRequest;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.service.CustomerService;

import com.tiora.mob.dto.request.CustomerCreateRequest;
import com.tiora.mob.dto.request.ProfileUpdateRequest;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RequestMapping("/mobile/customers")
public class CustomerController {
    @Autowired
    private com.tiora.mob.repository.BranchRepository branchRepository;

    @GetMapping("/latest-visits/{customerId}")
    public ResponseEntity<Map<String, Object>> getLatestVisits(@PathVariable Long customerId) {
        logger.info("getLatestVisits called for customerId: {}", customerId);
        String latestVisitJson = customerService.getLatestVisitJsonByCustomerId(customerId);
        java.util.Map<String, String> visitsMap = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> visitsList = new java.util.ArrayList<>();
        try {
            if (latestVisitJson != null && !latestVisitJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                visitsMap = mapper.readValue(latestVisitJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                for (String branchIdStr : visitsMap.keySet()) {
                    Long branchId = null;
                    try { branchId = Long.valueOf(branchIdStr); } catch (Exception ignore) {}
                    com.tiora.mob.entity.Branch branch = branchId != null ? branchRepository.findById(branchId).orElse(null) : null;
                    java.util.Map<String, Object> visitInfo = new java.util.HashMap<>();
                    visitInfo.put("branchId", branchIdStr);
                    visitInfo.put("visitDate", visitsMap.get(branchIdStr));
                    visitInfo.put("branchName", branch != null ? branch.getBranchName() : null);
                    visitInfo.put("ratings", branch != null ? branch.getLatitude() : null); // Replace with branch.getRatings() if available
                    visitsList.add(visitInfo);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing latestVisitJson for customerId {}: {}", customerId, e.getMessage());
        }
        return ResponseEntity.ok(Map.of("customerId", customerId, "latestVisits", visitsList));
    }
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> createCustomer(@Validated @RequestBody CustomerCreateRequest request) {
        logger.info("createCustomer called with request: {}", request);
        Long customerId = customerService.createCustomer(request);
        logger.info("Customer created with id: {}", customerId);
        return ResponseEntity.ok(Map.of("id", customerId, "message", "Customer created successfully"));
    }

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

    // ...other endpoints...
}