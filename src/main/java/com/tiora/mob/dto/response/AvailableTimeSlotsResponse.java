package com.tiora.mob.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Available time slots response")
public class AvailableTimeSlotsResponse {
    
    @Schema(description = "List of available time slots")
    @JsonProperty("available_slots")
    private List<TimeSlot> availableSlots;
    
    @Schema(description = "Barber ID", example = "1")
    @JsonProperty("barber_id")
    private Long barberId;
    
    @Schema(description = "Barber name", example = "John Doe")
    @JsonProperty("barber_name")
    private String barberName;
    
    @Schema(description = "Total duration of selected services in minutes", example = "45")
    @JsonProperty("total_duration_minutes")
    private Integer totalDurationMinutes;
    
    @Schema(description = "Buffer time between appointments in minutes", example = "15")
    @JsonProperty("buffer_time_minutes")
    private Integer bufferTimeMinutes;
    
    @Schema(description = "Response message", example = "Available time slots retrieved successfully")
    @JsonProperty("message")
    private String message;
    
    @Schema(description = "Success status", example = "true")
    @JsonProperty("success")
    private boolean success;

    // Inner class for time slot
    @Schema(description = "Time slot information")
    public static class TimeSlot {
        
        @Schema(description = "Start time", example = "09:00")
        @JsonProperty("start_time")
        private LocalTime startTime;
        
        @Schema(description = "End time", example = "09:45")
        @JsonProperty("end_time")
        private LocalTime endTime;
        
        @Schema(description = "Whether slot is available", example = "true")
        @JsonProperty("is_available")
        private Boolean isAvailable;
        
        @Schema(description = "Reason if not available", example = "Already booked")
        @JsonProperty("unavailable_reason")
        private String unavailableReason;

        // Default constructor
        public TimeSlot() {}

        // Constructor with parameters
        public TimeSlot(LocalTime startTime, LocalTime endTime, Boolean isAvailable, String unavailableReason) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.isAvailable = isAvailable;
            this.unavailableReason = unavailableReason;
        }

        // Getters and Setters
        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }

        public Boolean getIsAvailable() {
            return isAvailable;
        }

        public void setIsAvailable(Boolean isAvailable) {
            this.isAvailable = isAvailable;
        }

        public String getUnavailableReason() {
            return unavailableReason;
        }

        public void setUnavailableReason(String unavailableReason) {
            this.unavailableReason = unavailableReason;
        }
    }

    // Default constructor
    public AvailableTimeSlotsResponse() {
        this.success = true;
        this.message = "Available time slots retrieved successfully";
        this.bufferTimeMinutes = 15; // Default buffer time
    }

    // Constructor with parameters
    public AvailableTimeSlotsResponse(List<TimeSlot> availableSlots, Long barberId, String barberName,
                                      Integer totalDurationMinutes) {
        this();
        this.availableSlots = availableSlots;
        this.barberId = barberId;
        this.barberName = barberName;
        this.totalDurationMinutes = totalDurationMinutes;
    }

    // Getters and Setters
    public List<TimeSlot> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlot> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public Integer getBufferTimeMinutes() {
        return bufferTimeMinutes;
    }

    public void setBufferTimeMinutes(Integer bufferTimeMinutes) {
        this.bufferTimeMinutes = bufferTimeMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
