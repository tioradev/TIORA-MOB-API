package com.tiora.mob.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Available dates response for booking")
public class AvailableDatesResponse {
    
    @Schema(description = "List of available dates")
    @JsonProperty("available_dates")
    private List<LocalDate> availableDates;
    
    @Schema(description = "Total duration of selected services in minutes", example = "45")
    @JsonProperty("total_duration_minutes")
    private Integer totalDurationMinutes;
    
    @Schema(description = "Salon opening time", example = "09:00")
    @JsonProperty("salon_opening_time")
    private String salonOpeningTime;
    
    @Schema(description = "Salon closing time", example = "18:00")
    @JsonProperty("salon_closing_time")
    private String salonClosingTime;
    
    @Schema(description = "Response message", example = "Available dates retrieved successfully")
    @JsonProperty("message")
    private String message;
    
    @Schema(description = "Success status", example = "true")
    @JsonProperty("success")
    private boolean success;

    // Default constructor
    public AvailableDatesResponse() {
        this.success = true;
        this.message = "Available dates retrieved successfully";
    }

    // Constructor with parameters
    public AvailableDatesResponse(List<LocalDate> availableDates, Integer totalDurationMinutes,
                                  String salonOpeningTime, String salonClosingTime) {
        this();
        this.availableDates = availableDates;
        this.totalDurationMinutes = totalDurationMinutes;
        this.salonOpeningTime = salonOpeningTime;
        this.salonClosingTime = salonClosingTime;
    }

    // Getters and Setters
    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public String getSalonOpeningTime() {
        return salonOpeningTime;
    }

    public void setSalonOpeningTime(String salonOpeningTime) {
        this.salonOpeningTime = salonOpeningTime;
    }

    public String getSalonClosingTime() {
        return salonClosingTime;
    }

    public void setSalonClosingTime(String salonClosingTime) {
        this.salonClosingTime = salonClosingTime;
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
