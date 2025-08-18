package com.tiora.mob.controller;



import com.tiora.mob.dto.request.AppointmentRequest;
import com.tiora.mob.dto.response.AppointmentResponse;
import com.tiora.mob.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
          @RequestBody AppointmentRequest request,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(appointmentService.createAppointment(token, request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getCustomerAppointments(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(appointmentService.getCustomerAppointments(token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(token, id));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, String>> cancelAppointment(
//            @PathVariable Long id,
//            @RequestHeader("Authorization") String token) {
//        appointmentService.cancelAppointment(token, id);
//        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
//    }
}