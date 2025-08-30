package com.tiora.mob.controller;

import com.tiora.mob.dto.response.TimeSlotDTO;
import com.tiora.mob.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/time-slots")
    public ResponseEntity<com.tiora.mob.dto.response.AvailableTimeSlotsResponse> getAvailableTimeSlots(
            @RequestParam(name = "barber_id") Long barberId,
            @RequestParam(name = "service_ids") String serviceIds,
            @RequestParam(name = "date") String date,
            @RequestParam(name = "salonId") Long salonId,
            @RequestParam(name = "customerGender", required = false) String customerGender
    ) {
        com.tiora.mob.dto.response.AvailableTimeSlotsResponse response = availabilityService.getAvailableTimeSlots(
            salonId, serviceIds, barberId, date, customerGender
        );
        return ResponseEntity.ok(response);
    }
}
