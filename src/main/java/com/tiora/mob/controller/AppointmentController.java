package com.tiora.mob.controller;



import com.tiora.mob.dto.request.AppointmentRequest;
import com.tiora.mob.dto.response.AppointmentResponse;
import com.tiora.mob.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RequestMapping("/mobile/appointments")
public class AppointmentController {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody AppointmentRequest request,
            @RequestHeader("Authorization") String token) {
        logger.info("createAppointment called with request: {}", request);
        AppointmentResponse response = appointmentService.createAppointment(token, request);
        logger.info("createAppointment response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getCustomerAppointments(
            @RequestHeader("Authorization") String token) {
        logger.info("getCustomerAppointments called");
        List<AppointmentResponse> responses = appointmentService.getCustomerAppointments(token);
        logger.info("getCustomerAppointments response count: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        logger.info("getAppointmentById called with id: {}", id);
        AppointmentResponse response = appointmentService.getAppointmentById(token, id);
        logger.info("getAppointmentById response: {}", response);
        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, String>> cancelAppointment(
//            @PathVariable Long id,
//            @RequestHeader("Authorization") String token) {
//        logger.info("cancelAppointment called with id: {}", id);
//        appointmentService.cancelAppointment(token, id);
//        logger.info("Appointment cancelled successfully for id: {}", id);
//        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
//    }
}