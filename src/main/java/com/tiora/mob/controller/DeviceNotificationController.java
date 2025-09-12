package com.tiora.mob.controller;

import com.tiora.mob.service.FCMNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mobile/device-notifications")
@Tag(name = "Device Notifications", description = "APIs for managing device tokens and notifications for employees and customers")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class DeviceNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceNotificationController.class);

    @Autowired
    private FCMNotificationService fcmNotificationService;

    /**
     * Register device token for employee
     */
    @PostMapping("/register-employee-token")
    @Operation(
        summary = "Register FCM device token for employee",
        description = "Register a Firebase Cloud Messaging device token for employee push notifications"
    )
    public ResponseEntity<Map<String, String>> registerEmployeeDeviceToken(
            @Parameter(description = "Employee ID", required = true, example = "123")
            @RequestParam("employeeId") Long employeeId,
            @Parameter(description = "FCM device token", required = true)
            @RequestParam("deviceToken") String deviceToken,
            @Parameter(description = "Device type", example = "ANDROID")
            @RequestParam(value = "deviceType", required = false, defaultValue = "ANDROID") String deviceType,
            @Parameter(description = "App version", example = "1.0.0")
            @RequestParam(value = "appVersion", required = false, defaultValue = "1.0.0") String appVersion,
            @RequestHeader("Authorization") String token) {
        logger.info("Registering device token for employee: {}", employeeId);
        try {
            fcmNotificationService.registerDeviceTokenForEmployee(employeeId, deviceToken, deviceType, appVersion);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Device token registered successfully",
                "employeeId", String.valueOf(employeeId)
            ));
        } catch (Exception e) {
            logger.error("Failed to register device token for employee {}: {}", employeeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to register device token: " + e.getMessage()
            ));
        }
    }

    /**
     * Register device token for customer
     */
    @PostMapping("/register-customer-token")
    @Operation(
        summary = "Register FCM device token for customer",
        description = "Register a Firebase Cloud Messaging device token for customer push notifications"
    )
    public ResponseEntity<Map<String, String>> registerCustomerDeviceToken(
            @Parameter(description = "Customer ID", required = true, example = "456")
            @RequestParam("customerId") Long customerId,
            @Parameter(description = "FCM device token", required = true)
            @RequestParam("deviceToken") String deviceToken,
            @Parameter(description = "Device type", example = "ANDROID")
            @RequestParam(value = "deviceType", required = false, defaultValue = "ANDROID") String deviceType,
            @Parameter(description = "App version", example = "1.0.0")
            @RequestParam(value = "appVersion", required = false, defaultValue = "1.0.0") String appVersion,
            @RequestHeader("Authorization") String token) {
        logger.info("Registering device token for customer: {}", customerId);
        try {
            fcmNotificationService.registerDeviceTokenForCustomer(customerId, deviceToken, deviceType, appVersion);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Device token registered successfully",
                "customerId", String.valueOf(customerId)
            ));
        } catch (Exception e) {
            logger.error("Failed to register device token for customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to register device token: " + e.getMessage()
            ));
        }
    }

    /**
     * Unregister device token
     */
    @PostMapping("/unregister-token")
    @Operation(
        summary = "Unregister FCM device token",
        description = "Unregister a Firebase Cloud Messaging device token"
    )
    public ResponseEntity<Map<String, String>> unregisterDeviceToken(
            @Parameter(description = "FCM device token", required = true)
            @RequestParam("deviceToken") String deviceToken,
            @RequestHeader("Authorization") String token) {

        logger.info("Unregistering device token");

        try {
            fcmNotificationService.unregisterDeviceToken(deviceToken);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Device token unregistered successfully"
            ));
        } catch (Exception e) {
            logger.error("Failed to unregister device token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to unregister device token: " + e.getMessage()
            ));
        }
    }

    /**
     * Send test notification to employee
     */
    @PostMapping("/test-employee-notification")
    @Operation(
        summary = "Send test notification to employee",
        description = "Send a test push notification to verify FCM integration for employees"
    )
    public ResponseEntity<Map<String, String>> sendTestEmployeeNotification(
            @Parameter(description = "Employee ID", required = true, example = "123")
            @RequestParam("employeeId") Long employeeId,
            @Parameter(description = "Test message", example = "Test notification")
            @RequestParam(value = "message", required = false, defaultValue = "Test notification") String message,
            @RequestHeader("Authorization") String token) {
        logger.info("Sending test notification to employee: {}", employeeId);
        try {
            fcmNotificationService.sendNotificationToEmployee(
                employeeId,
                "Test Notification",
                message,
                Map.of("type", "TEST", "employeeId", String.valueOf(employeeId))
            );
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test notification sent successfully",
                "employeeId", String.valueOf(employeeId)
            ));
        } catch (Exception e) {
            logger.error("Failed to send test notification to employee {}: {}", employeeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to send test notification: " + e.getMessage()
            ));
        }
    }

    /**
     * Send test notification to customer
     */
    @PostMapping("/test-customer-notification")
    @Operation(
        summary = "Send test notification to customer",
        description = "Send a test push notification to verify FCM integration for customers"
    )
    public ResponseEntity<Map<String, String>> sendTestCustomerNotification(
            @Parameter(description = "Customer ID", required = true, example = "456")
            @RequestParam("customerId") Long customerId,
            @Parameter(description = "Test message", example = "Test notification")
            @RequestParam(value = "message", required = false, defaultValue = "Test notification") String message,
            @RequestHeader("Authorization") String token) {
        logger.info("Sending test notification to customer: {}", customerId);
        try {
            fcmNotificationService.sendNotificationToCustomer(
                customerId,
                "Test Notification",
                message,
                Map.of("type", "TEST", "customerId", String.valueOf(customerId))
            );
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test notification sent successfully",
                "customerId", String.valueOf(customerId)
            ));
        } catch (Exception e) {
            logger.error("Failed to send test notification to customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to send test notification: " + e.getMessage()
            ));
        }
    }
}
