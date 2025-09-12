package com.tiora.mob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiora.mob.dto.AppointmentEventDto.EventType;
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
    public void publishAppointmentUpdated(Long salonId, Long appointmentId, String oldStatus, String newStatus) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("oldStatus", oldStatus != null ? oldStatus : "");
            data.put("newStatus", newStatus != null ? newStatus : "");
            data.put("updatedBy", "mobile_backend");
            data.put("updateReason", "status_change");

            Map<String, String> fields = new HashMap<>();
            fields.put("messageType", "appointment_updated");
            fields.put("eventType", EventType.UPDATED.name());
            fields.put("sourceSystem", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salonId", String.valueOf(salonId));
            fields.put("appointmentId", String.valueOf(appointmentId));
            fields.put("priority", "NORMAL");
            fields.put("requiresNotification", "true");
            fields.put("data", toJson(data));

            log.info("Publishing UPDATED event with fields: {}", fields);
            Object messageId = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published appointment updated message with ID: {} for appointment: {}", messageId, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing appointment updated message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${salon.redis.streams.appointment-stream}")
    private String appointmentStreamKey;

    @Value("${salon.redis.streams.barber-stream}")
    private String barberStreamKey;

    public void publishAppointmentCreated(Long salonId, Long branchId, Long appointmentId, 
                                        Long customerId, Long barberId, Map<String, Object> appointmentData) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("messageType", stripQuotes("CREATED"));
            fields.put("eventType", stripQuotes("CREATED"));
            fields.put("sourceSystem", stripQuotes("mobile_backend"));
            fields.put("timestamp", stripQuotes(LocalDateTime.now().toString()));
            fields.put("salonId", stripQuotes(salonId));
            fields.put("branchId", stripQuotes(branchId));
            fields.put("appointmentId", stripQuotes(appointmentId));
            fields.put("customerId", stripQuotes(customerId));
            fields.put("barberId", stripQuotes(barberId));
            // Add appointment_number as top-level field if present
            if (appointmentData != null && appointmentData.get("appointment_number") != null) {
                fields.put("appointmentNumber", stripQuotes(appointmentData.get("appointment_number")));
            }
            // Add more required fields from appointmentData if present
            if (appointmentData != null) {
                if (appointmentData.get("customer_name") != null)
                    fields.put("customerName", stripQuotes(appointmentData.get("customer_name")));
                if (appointmentData.get("customer_phone") != null)
                    fields.put("customerPhone", stripQuotes(appointmentData.get("customer_phone")));
                if (appointmentData.get("service_names") != null) {
                    Object sn = appointmentData.get("service_names");
                    if (sn instanceof java.util.List) {
                        fields.put("serviceNames", stripQuotes(String.join(", ", ((java.util.List<?>) sn).stream().map(Object::toString).toArray(String[]::new))));
                    } else {
                        fields.put("serviceNames", stripQuotes(sn));
                    }
                }
                if (appointmentData.get("appointment_date") != null)
                    fields.put("appointmentTime", stripQuotes(appointmentData.get("appointment_date")));
                if (appointmentData.get("status") != null)
                    fields.put("status", stripQuotes(appointmentData.get("status")));
                // Add metadata as JSON string (do not strip quotes)
                if (appointmentData.get("metadata") != null)
                    fields.put("metadata", objectMapper.writeValueAsString((Map<String, Object>) appointmentData.get("metadata")));
                else
                    fields.put("metadata", objectMapper.writeValueAsString(new HashMap<>()));
            } else {
                fields.put("metadata", "{}");
            }
            fields.put("priority", stripQuotes("HIGH"));
            fields.put("requiresNotification", stripQuotes("true"));
            // Format data field as compact JSON (do not strip quotes)
            fields.put("data", objectMapper.writeValueAsString(appointmentData));

            // Publish to web backend stream
            fields.put("source", stripQuotes("WEB"));
            log.info("Publishing CREATED event with fields: {}", fields);
            Object messageIdWeb = redisTemplate.opsForStream().add(appointmentStreamKey, fields);
            log.info("Published appointment created message to web backend with ID: {} for appointment: {}", messageIdWeb, appointmentId);

            // Publish to barber stream
            fields.put("source", stripQuotes("barber_mobile"));
            Object messageIdBarber = redisTemplate.opsForStream().add(barberStreamKey, fields);
            log.info("Published appointment created message to barber stream with ID: {} for appointment: {}", messageIdBarber, appointmentId);
        } catch (Exception e) {
            log.error("Error publishing appointment created message for appointment {}: {}", appointmentId, e.getMessage(), e);
        }

    }

    // Utility method to strip quotes from any object
    private String stripQuotes(Object value) {
        if (value == null) return "";
        String str = value.toString();
        // Remove all leading/trailing quotes (single or double) recursively
        while (str.matches("^(\"|')+.*(\"|')+$")) {
            str = str.replaceAll("^(\"|')+", "");
            str = str.replaceAll("(\"|')+$", "");
        }
        // Unescape any escaped quotes and slashes
        str = str.replaceAll("\\\"", "\"");
        str = str.replaceAll("\\\\", "\\");
        return str;
    }

    public void publishAppointmentCancelled(Long salonId, Long appointmentId, String cancellationReason) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("cancellationReason", cancellationReason != null ? cancellationReason : "No reason provided");
            data.put("cancelledBy", "mobile_backend");
            data.put("cancellationTime", LocalDateTime.now().toString());

            Map<String, String> fields = new HashMap<>();
            fields.put("messageType", "appointment_cancelled");
            fields.put("eventType", EventType.CANCELLED.name());
            fields.put("sourceSystem", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salonId", String.valueOf(salonId));
            fields.put("appointmentId", String.valueOf(appointmentId));
            fields.put("priority", "HIGH");
            fields.put("requiresNotification", "true");
            fields.put("data", toJson(data));

            log.info("Publishing CANCELLED event with fields: {}", fields);
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
            fields.put("messageType", "customer_checked_in");
            fields.put("sourceSystem", "customer_app");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salonId", String.valueOf(salonId));
            fields.put("branchId", String.valueOf(branchId != null ? branchId : ""));
            fields.put("appointmentId", String.valueOf(appointmentId));
            fields.put("customerId", String.valueOf(customerId));
            fields.put("priority", "HIGH");
            fields.put("requiresNotification", "true");
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
            fields.put("messageType", "customer_being_served");
            fields.put("sourceSystem", "mobile_backend");
            fields.put("timestamp", LocalDateTime.now().toString());
            fields.put("salonId", String.valueOf(salonId));
            fields.put("appointmentId", String.valueOf(appointmentId));
            fields.put("barberId", String.valueOf(barberId != null ? barberId : ""));
            fields.put("priority", "HIGH");
            fields.put("requiresNotification", "true");
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
