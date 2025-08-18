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

@RestController
@RequestMapping("/api/mobile/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping("/salon/{salonId}")
    public ResponseEntity<List<BarberResponse>> getBarbersBySalon(@PathVariable Long salonId) {
        return ResponseEntity.ok(employeeService.getBarbersBySalon(salonId));
    }

    @GetMapping("/{barberId}")
    public ResponseEntity<BarberResponse> getBarberById(@PathVariable Long barberId) {
        return ResponseEntity.ok(employeeService.getBarberById(barberId));
    }

//    @GetMapping("/{barberId}/availability")
//    public ResponseEntity<List<TimeSlotResponse>> getBarberAvailability(
//            @PathVariable Long barberId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        return ResponseEntity.ok(a.getAvailableTimeSlots(barberId, date));
//    }
}