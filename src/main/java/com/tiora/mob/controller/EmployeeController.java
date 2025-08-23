package com.tiora.mob.controller;



import com.tiora.mob.dto.response.BarberResponse;
import com.tiora.mob.dto.response.TimeSlotResponse;
import com.tiora.mob.service.AvailabilityService;
import com.tiora.mob.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/mobile/employee")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping("/salon/{salonId}")
    public ResponseEntity<List<BarberResponse>> getBarbersBySalon(@PathVariable Long salonId) {
        logger.info("getBarbersBySalon called with salonId: {}", salonId);
        List<BarberResponse> responses = employeeService.getBarbersBySalon(salonId);
        logger.info("getBarbersBySalon response count: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{barberId}")
    public ResponseEntity<BarberResponse> getBarberById(@PathVariable Long barberId) {
        logger.info("getBarberById called with barberId: {}", barberId);
        BarberResponse response = employeeService.getBarberById(barberId);
        logger.info("getBarberById response: {}", response);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{barberId}/availability")
//    public ResponseEntity<List<TimeSlotResponse>> getBarberAvailability(
//            @PathVariable Long barberId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        return ResponseEntity.ok(a.getAvailableTimeSlots(barberId, date));
//    }
}