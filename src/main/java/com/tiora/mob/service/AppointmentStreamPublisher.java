package com.tiora.mob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiora.mob.dto.AppointmentEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AppointmentStreamPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentStreamPublisher.class);
    
    @Value("${salon.redis.streams.appointment-stream}")
    private String appointmentEventsStream;
    
    @Value("${salon.redis.streams.barber-stream}")
    private String barberNotificationsStream;

    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publish appointment event to Redis Stream
     */
    public void publishAppointmentEvent(AppointmentEventDto event) {
        try {
            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            
            Map<String, String> eventData = new HashMap<>();
            eventData.put("eventType", event.getEventType().toString());
            eventData.put("appointmentId", String.valueOf(event.getAppointmentId()));
            eventData.put("salonId", String.valueOf(event.getSalonId()));
            eventData.put("barberId", event.getEmployeeId() != null ? String.valueOf(event.getEmployeeId()) : "");
            eventData.put("customerId", String.valueOf(event.getCustomerId()));
            eventData.put("customerName", event.getCustomerName());
            eventData.put("customerPhone", event.getCustomerPhone());
            eventData.put("serviceName", event.getServiceName());
            eventData.put("appointmentTime", event.getAppointmentTime().toString());
            eventData.put("status", event.getStatus());
            eventData.put("messageType", "appointment_created");
            eventData.put("sourceSystem", "mobile_backend");
            eventData.put("source", event.getSource());
            eventData.put("timestamp", event.getTimestamp().toString());
            
            // Add metadata as JSON string
            if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
                eventData.put("metadata", objectMapper.writeValueAsString(event.getMetadata()));
            } else {
                eventData.put("metadata", "{}");
            }

            // Publish to appointment events stream
            var recordId = streamOps.add(appointmentEventsStream, eventData);
            
            logger.info("Published appointment event {} to stream: {} with message ID: {}", 
                       event.getEventType(), appointmentEventsStream, recordId.getValue());

            // If there's a barber involved, publish only via MobileAppointmentStreamPublisher
            // ...existing code...

        } catch (Exception e) {
            logger.error("Failed to publish appointment event to Redis Stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish appointment event", e);
        }
    }

    /**
     * Publish barber-specific notification
     */
    private void publishBarberNotification(AppointmentEventDto event) {
    // Method removed: publishing handled in MobileAppointmentStreamPublisher only
    }

    /**
     * Create human-readable notification message
     */
    private String createNotificationMessage(AppointmentEventDto event) {
        String timeStr = event.getAppointmentTime().toLocalTime().toString();
        String dateStr = event.getAppointmentTime().toLocalDate().toString();
        
        switch (event.getEventType()) {
            case CREATED:
                return String.format("New appointment booked: %s for %s service on %s at %s", 
                                   event.getCustomerName(), event.getServiceName(), dateStr, timeStr);
            case UPDATED:
                return String.format("Appointment updated: %s for %s service on %s at %s", 
                                   event.getCustomerName(), event.getServiceName(), dateStr, timeStr);
            case CANCELLED:
                return String.format("Appointment cancelled: %s for %s service on %s at %s", 
                                   event.getCustomerName(), event.getServiceName(), dateStr, timeStr);
            case COMPLETED:
                return String.format("Appointment completed: %s for %s service", 
                                   event.getCustomerName(), event.getServiceName());
            case RESCHEDULED:
                return String.format("Appointment rescheduled: %s for %s service to %s at %s", 
                                   event.getCustomerName(), event.getServiceName(), dateStr, timeStr);
            case STARTED:
                return String.format("Appointment started: %s for %s service", 
                                   event.getCustomerName(), event.getServiceName());
            default:
                return String.format("Appointment %s: %s for %s service", 
                                   event.getEventType().toString().toLowerCase(), 
                                   event.getCustomerName(), event.getServiceName());
        }
    }

    /**
     * Test Redis connection
     */
    public boolean testConnection() {
        try {
            redisTemplate.opsForValue().set("test:connection", "OK");
            String result = redisTemplate.opsForValue().get("test:connection");
            redisTemplate.delete("test:connection");
            return "OK".equals(result);
        } catch (Exception e) {
            logger.error("Redis connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
