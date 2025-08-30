package com.tiora.mob.controller;

import com.tiora.mob.entity.Branch;
import com.tiora.mob.entity.Branch.SalonType;
import com.tiora.mob.service.BranchService;
import com.tiora.mob.dto.response.BranchResponse;


import com.tiora.mob.dto.response.SalonResponse;
import com.tiora.mob.entity.Salon;
import com.tiora.mob.service.SalonService;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RequestMapping("/mobile/salons")
public class SalonController {

    private static final Logger logger = LoggerFactory.getLogger(SalonController.class);

    @Autowired
    private BranchService branchService;

    @GetMapping("/branches")
    public ResponseEntity<List<BranchResponse>> getBranchesBySalonType(@RequestParam("salon_type") String salonType) {
        logger.info("Fetching branches for salon_type: {}", salonType);
        SalonType type;
        try {
            type = SalonType.valueOf(salonType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        List<Branch> branches = branchService.getBranchesBySalonType(type);
        List<BranchResponse> response = branches.stream().map(this::toBranchResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private BranchResponse toBranchResponse(Branch branch) {
        BranchResponse dto = new BranchResponse();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchName(branch.getBranchName());
        dto.setStatus(branch.getStatus());
        dto.setLongitude(branch.getLongitude());
        dto.setLatitude(branch.getLatitude());
        dto.setBranchPhoneNumber(branch.getBranchPhoneNumber());
        dto.setBranchEmail(branch.getBranchEmail());
        dto.setBranchImage(branch.getBranchImage());
        dto.setCreatedAt(branch.getCreatedAt());
        dto.setUpdatedAt(branch.getUpdatedAt());
        dto.setSalonType(branch.getSalonType());
        return dto;
    }

    @Autowired
    private SalonService salonService;

    @GetMapping
    public ResponseEntity<List<SalonResponse>> getAllSalons() {
        logger.info("Fetching all salons");
        List<Salon> salons = salonService.getActiveSalons();
        logger.info("Total salons fetched: {}", salons.size());
        List<SalonResponse> response = salons.stream().map(this::toSalonResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<SalonResponse> getSalonById(
            @Parameter(description = "Salon ID", example = "1") @PathVariable Long id) {
        logger.info("Fetching salon by ID: {}", id);
        try {
            Salon salon = salonService.getSalonById(id);
            return ResponseEntity.ok(toSalonResponse(salon));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<SalonResponse>> searchSalons(
            @Parameter(description = "Search query", example = "Glamour") @RequestParam String query) {
        List<Salon> salons = salonService.searchSalons(query);
        List<SalonResponse> response = salons.stream().map(this::toSalonResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private SalonResponse toSalonResponse(Salon salon) {
        SalonResponse dto = new SalonResponse();
        dto.setId(salon.getSalonId());
        dto.setName(salon.getName());
        dto.setAddress(salon.getAddress());
        dto.setPhoneNumber(salon.getPhoneNumber());
        dto.setImageUrl(salon.getImageUrl());
        // Set other fields as needed, e.g. city, rating, openingTime, closingTime if available
        return dto;
    }

}