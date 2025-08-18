package com.tiora.mob.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Available barbers response for booking")
public class AvailableBarbersResponse {
    
    @Schema(description = "List of available barbers")
    @JsonProperty("available_barbers")
    private List<AvailableBarberResponse> availableBarbers;
    
    @Schema(description = "Total number of barbers found", example = "3")
    @JsonProperty("total_barbers")
    private Integer totalBarbers;
    
    @Schema(description = "Total number of service providers in salon", example = "5")
    @JsonProperty("total_service_providers")
    private Integer totalServiceProviders;
    
    @Schema(description = "Total number of employees in salon", example = "8")
    @JsonProperty("total_employees")
    private Integer totalEmployees;
    
    @Schema(description = "Response message", example = "Available barbers retrieved successfully")
    @JsonProperty("message")
    private String message;
    
    @Schema(description = "Success status", example = "true")
    @JsonProperty("success")
    private boolean success;

    // Default constructor
    public AvailableBarbersResponse() {
        this.success = true;
        this.message = "Available barbers retrieved successfully";
        this.availableBarbers = new ArrayList<>();
        this.totalBarbers = 0;
        this.totalServiceProviders = 0;
        this.totalEmployees = 0;
    }

    // Constructor for success case
    public AvailableBarbersResponse(List<AvailableBarberResponse> availableBarbers,
                                    Integer totalServiceProviders, Integer totalEmployees) {
        this();
        this.availableBarbers = availableBarbers;
        this.totalBarbers = availableBarbers.size();
        this.totalServiceProviders = totalServiceProviders;
        this.totalEmployees = totalEmployees;
    }

    // Constructor for error/empty case
    public AvailableBarbersResponse(String message, boolean success) {
        this();
        this.message = message;
        this.success = success;
    }

    // Getters and Setters
    public List<AvailableBarberResponse> getAvailableBarbers() {
        return availableBarbers;
    }

    public void setAvailableBarbers(List<AvailableBarberResponse> availableBarbers) {
        this.availableBarbers = availableBarbers;
        this.totalBarbers = availableBarbers != null ? availableBarbers.size() : 0;
    }

    public Integer getTotalBarbers() {
        return totalBarbers;
    }

    public void setTotalBarbers(Integer totalBarbers) {
        this.totalBarbers = totalBarbers;
    }

    public Integer getTotalServiceProviders() {
        return totalServiceProviders;
    }

    public void setTotalServiceProviders(Integer totalServiceProviders) {
        this.totalServiceProviders = totalServiceProviders;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
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
