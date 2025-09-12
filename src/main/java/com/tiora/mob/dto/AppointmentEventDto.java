package com.tiora.mob.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventDto {
    
    private EventType eventType;
    private Long appointmentId;
    private Long salonId;
    private Long employeeId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String serviceName;
    private LocalDateTime appointmentTime;
    private String status;
    private String source; // "MOBILE" or "WEB"
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    public enum EventType {
        CREATED,
        UPDATED,
        CANCELLED,
        COMPLETED,
        PAYMENT_RECEIVED,
        PAYMENT_FAILED,
        RESCHEDULED,
        CONFIRMED,
        STARTED,
        NO_SHOW
    }
}
