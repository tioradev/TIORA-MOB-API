
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
    @Schema(description = "List of Service IDs. Required. Must not be empty. Each must be a valid service ID.", example = "[1, 2, 3]", required = true)
    @NotEmpty(message = "At least one service ID is required")
    private List<Long> serviceIds;

    @Schema(description = "Employee ID. Required. Must be a valid employee ID.", example = "1", required = true)
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Schema(description = "Salon ID. Required. Must be a valid salon ID.", example = "1", required = true)
    @NotNull(message = "Salon ID is required")
    private Long salonId;

    @Schema(description = "Branch ID. Required. Must be a valid branch ID.", example = "1", required = true)
    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @Schema(description = "Customer ID. Required. Must be a valid customer in the system. All customer details will be filled from the database.", example = "123", required = true)
    private Long customerId;

    @Schema(description = "Payment method. Optional. Allowed values: CASH, CARD, ADVANCE, FULL.", example = "CASH")
    private String paymentMethod;

    @Schema(description = "Rating for the appointment. Optional. Integer value.", example = "5")
    private Integer rating;

    @Schema(description = "Appointment date and time. Required. Must be a future date/time. Format: yyyy-MM-dd'T'HH:mm:ss.", required = true)
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;

    @Schema(description = "Estimated end time. Optional. Format: yyyy-MM-dd'T'HH:mm:ss.")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "Service price. Required. Must be greater than 0.", example = "50.00", required = true)
    @NotNull(message = "Service price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Service price must be greater than 0")
    private BigDecimal servicePrice;

    @Schema(description = "Discount amount. Optional. Must be 0 or greater.", example = "5.00")
    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;
}