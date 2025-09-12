package com.tiora.mob.controller;

import com.tiora.mob.service.AppointmentStreamPublisher;
import com.tiora.mob.service.BarberNotificationConsumer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mobile/system")
@Tag(name = "System Status", description = "APIs for checking system status and connectivity")
public class SystemStatusController {

    private static final Logger logger = LoggerFactory.getLogger(SystemStatusController.class);

    @Autowired
    private AppointmentStreamPublisher appointmentStreamPublisher;

    @Autowired
    private BarberNotificationConsumer barberNotificationConsumer;
    
    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Check Redis connectivity
     */
    @GetMapping("/redis-health")
    @Operation(
        summary = "Check Redis connectivity",
        description = "Check if the mobile backend can connect to Redis server"
    )
    public ResponseEntity<Map<String, Object>> testRedisHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simple Redis connection test
            redisTemplate.getConnectionFactory().getConnection().ping();
            
            result.put("status", "UP");
            result.put("redis", "Connected");
            result.put("timestamp", LocalDateTime.now());
            result.put("host", redisHost + ":" + redisPort);
            
            logger.info("Redis health check successful");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("redis", "Connection failed");
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            result.put("host", redisHost + ":" + redisPort);
            
            logger.error("Redis health check failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Check Redis connectivity
     */
    @GetMapping("/redis-status")
    @Operation(
        summary = "Check Redis connectivity",
        description = "Check if the mobile backend can connect to Redis server"
    )
    public ResponseEntity<Map<String, Object>> checkRedisStatus() {
        logger.info("Checking Redis connectivity status");

        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean redisConnected = appointmentStreamPublisher.testConnection();
            
            status.put("redis", Map.of(
                "connected", redisConnected,
                "status", redisConnected ? "Connected" : "Disconnected",
                "host", redisHost + ":" + redisPort
            ));
            
            status.put("barberConsumer", barberNotificationConsumer.getStatus());
            
            status.put("overall", Map.of(
                "status", redisConnected ? "Healthy" : "Unhealthy",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Error checking Redis status: {}", e.getMessage(), e);
            
            status.put("redis", Map.of(
                "connected", false,
                "status", "Error: " + e.getMessage()
            ));
            
            status.put("overall", Map.of(
                "status", "Error",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "error", e.getMessage()
            ));

            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Basic health check for the mobile backend"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("service", "TIORA Mobile Backend");
        health.put("version", "1.0.0");
        
        // Check various components
        boolean redisHealthy = false;
        try {
            redisHealthy = appointmentStreamPublisher.testConnection();
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
        }
        
        health.put("components", Map.of(
            "redis", redisHealthy ? "UP" : "DOWN",
            "barberConsumer", barberNotificationConsumer.isRunning() ? "UP" : "DOWN",
            "database", "UP" // Assume DB is up if we can respond
        ));
        
        boolean overallHealthy = redisHealthy && barberNotificationConsumer.isRunning();
        health.put("overall", overallHealthy ? "HEALTHY" : "DEGRADED");

        return ResponseEntity.ok(health);
    }

    /**
     * Test appointment event publishing
     */
    @PostMapping("/test-redis-publish")
    @Operation(
        summary = "Test Redis stream publishing",
        description = "Send a test event to Redis streams"
    )
    public ResponseEntity<Map<String, String>> testRedisPublish(
            @RequestParam(value = "message", defaultValue = "Test message") String message) {
        
        logger.info("Testing Redis stream publishing with message: {}", message);

        try {
            // Create a test event
            com.tiora.mob.dto.AppointmentEventDto testEvent = com.tiora.mob.dto.AppointmentEventDto.builder()
                .eventType(com.tiora.mob.dto.AppointmentEventDto.EventType.CREATED)
                .appointmentId(999L)
                .salonId(1L)
                .employeeId(1L)
                .customerId(1L)
                .customerName("Test Customer")
                .customerPhone("+1234567890")
                .serviceName("Test Service")
                .appointmentTime(java.time.LocalDateTime.now().plusHours(1))
                .status("SCHEDULED")
                .source("MOBILE_TEST")
                .timestamp(java.time.LocalDateTime.now())
                .metadata(Map.of("test", "true", "message", message))
                .build();

            appointmentStreamPublisher.publishAppointmentEvent(testEvent);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test event published successfully to Redis streams",
                "testMessage", message,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            logger.error("Failed to test Redis publishing: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to publish test event: " + e.getMessage()
            ));
        }
    }
}
