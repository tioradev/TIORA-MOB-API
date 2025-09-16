package com.tiora.mob.controller;



import com.tiora.mob.dto.request.AppointmentRequest;
import com.tiora.mob.dto.response.AppointmentResponse;
import com.tiora.mob.dto.response.AppointmentActivityResponse;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RequestMapping("/mobile/appointments")
public class AppointmentController {
    @PutMapping("/customer-paid/{appointmentId}")
    public ResponseEntity<?> updateCustomerPaid(@PathVariable Long appointmentId) {
        Map<String, Object> result = appointmentService.updateCustomerPaid(appointmentId);
        if (!(Boolean) result.getOrDefault("success", false)) {
            return ResponseEntity.badRequest().body(Map.of("error", result.get("message")));
        }
        return ResponseEntity.ok(Map.of("message", result.get("message")));
    }
    @PutMapping("/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @Valid @RequestBody com.tiora.mob.dto.request.AppointmentStatusUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        appointmentService.updateAppointmentStatus(token, request);
        return ResponseEntity.ok().build();
    }
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
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

    @GetMapping("/by-number/{appointmentNumber}")
    public ResponseEntity<AppointmentResponse> getAppointmentByNumber(
            @PathVariable String appointmentNumber,
            @RequestHeader("Authorization") String token) {
        logger.info("getAppointmentByNumber called with appointmentNumber: {}", appointmentNumber);
        AppointmentResponse response = appointmentService.getAppointmentByNumber(token, appointmentNumber);
        logger.info("getAppointmentByNumber response: {}", response);
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
    /**
     * Cancel an appointment due to payment failure. Sets status to CANCELLED and reason to PAYMENTFAILED.
     */
    @PostMapping("/{id}/cancel-payment-failed")
    public ResponseEntity<AppointmentResponse> cancelAppointmentPaymentFailed(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        logger.info("cancelAppointmentPaymentFailed called for id: {}", id);
        AppointmentResponse response = appointmentService.cancelAppointmentPaymentFailed(token, id);
        logger.info("Appointment cancelled due to payment failure for id: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get appointment activities by status
     * Supports filtering by appointment status and optional customer ID
     */
    @GetMapping("/activities")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get appointment activities by status",
        description = "Retrieve appointment activities filtered by status (IN_PROGRESS, SCHEDULED, COMPLETED, CANCELLED). " +
                     "Includes salon name, branch details, barber name, location, pricing, and timestamps."
    )
    @io.swagger.v3.oas.annotations.Parameter(
        name = "status", 
        description = "Appointment status filter", 
        required = true,
        example = "IN_PROGRESS"
    )
    @io.swagger.v3.oas.annotations.Parameter(
        name = "customerId", 
        description = "Optional customer ID filter", 
        required = false,
        example = "123"
    )
    public ResponseEntity<List<AppointmentActivityResponse>> getAppointmentActivities(
            @RequestParam("status") Appointment.AppointmentStatus status,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "appointmentDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate appointmentDate,
            @RequestHeader("Authorization") String token) {

        java.time.LocalDateTime appointmentDateTime = null;
        if (appointmentDate != null) {
            appointmentDateTime = appointmentDate.atStartOfDay();
        }

        logger.info("getAppointmentActivities called with status: {}, customerId: {}, employeeId: {}, appointmentDate: {} (full day)", status, customerId, employeeId, appointmentDateTime);

        List<AppointmentActivityResponse> activities = appointmentService.getAppointmentActivitiesFiltered(status, customerId, employeeId, appointmentDateTime);

        logger.info("Found {} appointment activities for filters", activities.size());
        return ResponseEntity.ok(activities);
    }

    /**
     * Get appointment activities by multiple statuses
     * Supports filtering by multiple appointment statuses with required customer ID
     */
    @GetMapping("/activities/multi-status")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get appointment activities by multiple statuses",
        description = "Retrieve appointment activities filtered by multiple statuses for a specific customer. " +
                     "Includes salon name, branch details, barber name, location, pricing, and timestamps."
    )
    @io.swagger.v3.oas.annotations.Parameter(
        name = "statuses", 
        description = "Comma-separated appointment statuses (e.g., IN_PROGRESS,SCHEDULED,COMPLETED,CANCELLED)", 
        required = true,
        example = "IN_PROGRESS,SCHEDULED"
    )
    @io.swagger.v3.oas.annotations.Parameter(
        name = "customerId", 
        description = "Customer ID filter (required)", 
        required = true,
        example = "123"
    )
    public ResponseEntity<List<AppointmentActivityResponse>> getAppointmentActivitiesByMultipleStatuses(
            @RequestParam("statuses") String statusesParam,
            @RequestParam("customerId") Long customerId,
            @RequestHeader("Authorization") String token) {
        
        logger.info("getAppointmentActivitiesByMultipleStatuses called with statuses: {} and customerId: {}", 
                   statusesParam, customerId);
        
        // Parse comma-separated statuses
        List<Appointment.AppointmentStatus> statuses;
        try {
            statuses = Arrays.stream(statusesParam.split(","))
                .map(String::trim)
                .map(Appointment.AppointmentStatus::valueOf)
                .toList();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid appointment status provided: {}", statusesParam);
            return ResponseEntity.badRequest().build();
        }
        
        List<AppointmentActivityResponse> activities = appointmentService.getAppointmentActivitiesByStatuses(statuses, customerId);
        
        logger.info("Found {} appointment activities for statuses: {} and customerId: {}", 
                   activities.size(), statuses, customerId);
        
        return ResponseEntity.ok(activities);
    }
}