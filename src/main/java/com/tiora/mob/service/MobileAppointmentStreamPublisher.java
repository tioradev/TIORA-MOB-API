package com.tiora.mob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileAppointmentStreamPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mobile.redis.streams.appointment-stream}")
    private String appointmentStreamKey;

    public void publishAppointmentCreated(Long salonId, Long branchId, Long appointmentId, 
                                        Long customerId, Long barberId, Map<String, Object> appointmentData) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("message_type", "appointment_created");
            fields.put("source_system", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salon_id", String.valueOf(salonId));
            fields.put("branch_id", String.valueOf(branchId != null ? branchId : ""));
            fields.put("appointment_id", String.valueOf(appointmentId));
            fields.put("customer_id", String.valueOf(customerId));
            fields.put("barber_id", String.valueOf(barberId != null ? barberId : ""));
            fields.put("priority", "HIGH");
            fields.put("requires_notification", "true");
            fields.put("data", toJson(appointmentData));

            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published appointment created message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing appointment created message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    public void publishAppointmentUpdated(Long salonId, Long appointmentId, String oldStatus, String newStatus) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("old_status", oldStatus != null ? oldStatus : "");
            data.put("new_status", newStatus != null ? newStatus : "");
            data.put("updated_by", "mobile_backend");
            data.put("update_reason", "status_change");

            Map<String, String> fields = new HashMap<>();
            fields.put("message_type", "appointment_updated");
            fields.put("source_system", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salon_id", String.valueOf(salonId));
            fields.put("appointment_id", String.valueOf(appointmentId));
            fields.put("priority", "NORMAL");
            fields.put("requires_notification", "true");
            fields.put("data", toJson(data));

            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published appointment updated message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing appointment updated message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    public void publishAppointmentCancelled(Long salonId, Long appointmentId, String cancellationReason) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("cancellation_reason", cancellationReason != null ? cancellationReason : "No reason provided");
            data.put("cancelled_by", "mobile_backend");
            data.put("cancellation_time", LocalDateTime.now().toString());

            Map<String, String> fields = new HashMap<>();
            fields.put("message_type", "appointment_cancelled");
            fields.put("source_system", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salon_id", String.valueOf(salonId));
            fields.put("appointment_id", String.valueOf(appointmentId));
            fields.put("priority", "HIGH");
            fields.put("requires_notification", "true");
            fields.put("data", toJson(data));

            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published appointment cancelled message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing appointment cancelled message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    public void publishCustomerCheckedIn(Long salonId, Long branchId, Long appointmentId, Long customerId, 
                                       Map<String, Object> checkInData) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("message_type", "customer_checked_in");
            fields.put("source_system", "customer_app");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salon_id", String.valueOf(salonId));
            fields.put("branch_id", String.valueOf(branchId != null ? branchId : ""));
            fields.put("appointment_id", String.valueOf(appointmentId));
            fields.put("customer_id", String.valueOf(customerId));
            fields.put("priority", "HIGH");
            fields.put("requires_notification", "true");
            fields.put("data", toJson(checkInData));

            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published customer checked in message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing customer checked in message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    public void publishCustomerBeingServed(Long salonId, Long appointmentId, Long barberId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("service_started_time", LocalDateTime.now().toString());
            data.put("barber_id", barberId != null ? barberId : "");
            data.put("status", "in_progress");

            Map<String, String> fields = new HashMap<>();
            fields.put("message_type", "customer_being_served");
            fields.put("source_system", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salon_id", String.valueOf(salonId));
            fields.put("appointment_id", String.valueOf(appointmentId));
            fields.put("barber_id", String.valueOf(barberId != null ? barberId : ""));
            fields.put("priority", "HIGH");
            fields.put("requires_notification", "true");
            fields.put("data", toJson(data));

            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published customer being served message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing customer being served message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error converting data to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
