package com.tiora.mob.controller;



import com.tiora.mob.dto.response.SalonResponse;
import com.tiora.mob.entity.Salon;
import com.tiora.mob.service.SalonService;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/salons")
public class SalonController {

    private static final Logger logger = LoggerFactory.getLogger(SalonController.class);

    @Autowired
    private SalonService salonService;

    @GetMapping
    public ResponseEntity<List<Salon>> getAllSalons() {
        logger.info("Fetching all salons");
        List<Salon> salons = salonService.getActiveSalons();
        logger.info("Total salons fetched: {}", salons.size());
        return ResponseEntity.ok(salons);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Salon> getSalonById(
            @Parameter(description = "Salon ID", example = "1") @PathVariable Long id) {
        logger.info("Fetching salon by ID: {}", id);
        try {
            Salon salon = salonService.getSalonById(id);
            return ResponseEntity.ok(salon);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<Salon>> searchSalons(
            @Parameter(description = "Search query", example = "Glamour") @RequestParam String query) {
        List<Salon> salons = salonService.searchSalons(query);
        return ResponseEntity.ok(salons);
    }

}