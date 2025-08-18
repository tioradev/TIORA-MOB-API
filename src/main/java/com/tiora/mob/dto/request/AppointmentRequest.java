package com.tiora.mob.dto.request;


import com.tiora.mob.entity.Appointment;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request DTO for creating/updating appointments")
public class AppointmentRequest {

    // Customer details for auto-creation
    @Schema(description = "Customer first name", example = "John", required = true)
    @NotBlank(message = "Customer first name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String customerFirstName;

    @Schema(description = "Customer last name", example = "Doe", required = true)
    @NotBlank(message = "Customer last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String customerLastName;

    @Schema(description = "Customer phone number", example = "+1234567890", required = true)
    @NotBlank(message = "Customer phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String customerPhone;

    @Schema(description = "Customer gender", example = "MALE")
    private String customerGender;

    @Schema(description = "List of Service IDs", example = "[1, 2, 3]", required = true)
    @NotEmpty(message = "At least one service ID is required")
    private List<Long> serviceIds;

    @Schema(description = "Employee ID", example = "1", required = true)
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Schema(description = "Salon ID", example = "1", required = true)
    @NotNull(message = "Salon ID is required")
    private Long salonId;

    @Schema(description = "Branch ID", example = "1", required = true)
    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @Schema(description = "Appointment date and time", required = true)
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;

    @Schema(description = "Estimated end time")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "Service price", example = "50.00", required = true)
    @NotNull(message = "Service price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Service price must be greater than 0")
    private BigDecimal servicePrice;

    @Schema(description = "Discount amount", example = "5.00")
    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;



}