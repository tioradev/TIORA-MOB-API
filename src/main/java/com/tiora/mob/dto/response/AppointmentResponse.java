package com.tiora.mob.dto.response;

import com.tiora.mob.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request DTO for creating/updating appointments")
public class AppointmentResponse {

    @Schema(description = "Appointment ID", example = "123")
    private Long id;

    @Schema(description = "Unique appointment number", example = "APT-2024-001")
    private String appointmentNumber;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Customer name", example = "John Doe")
    private String customerName;

    @Schema(description = "Customer phone", example = "+1234567890")
    private String customerPhone;

    @Schema(description = "Service ID", example = "1")
    private Long serviceId;

    @Schema(description = "Service name", example = "Hair Cut")
    private String serviceName;

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee name", example = "Jane Smith")
    private String employeeName;

    @Schema(description = "Salon ID", example = "1")
    private Long salonId;

    @Schema(description = "Salon name", example = "Glamour Salon")
    private String salonName;

    @Schema(description = "Appointment date and time")
    private LocalDateTime appointmentDate;

    @Schema(description = "Estimated end time")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "Actual start time")
    private LocalDateTime actualStartTime;

    @Schema(description = "Actual end time")
    private LocalDateTime actualEndTime;

    @Schema(description = "Appointment status")
    private Appointment.AppointmentStatus status;

    @Schema(description = "Payment status")
    private Appointment.PaymentStatus paymentStatus;

    @Schema(description = "Service price", example = "50.00")
    private BigDecimal servicePrice;

    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tax amount", example = "4.50")
    private BigDecimal taxAmount;

    @Schema(description = "Total amount", example = "49.50")
    private BigDecimal totalAmount;

    @Schema(description = "Paid amount", example = "49.50")
    private BigDecimal paidAmount;

    @Schema(description = "Payment method")
    private Appointment.PaymentMethod paymentMethod;

    @Schema(description = "Customer notes", example = "Please use organic products")
    private String customerNotes;

    @Schema(description = "Internal notes", example = "Customer prefers morning appointments")
    private String internalNotes;

    @Schema(description = "Cancellation reason", example = "Personal emergency")
    private String cancellationReason;

    @Schema(description = "Who cancelled the appointment", example = "Customer")
    private String cancelledBy;

    @Schema(description = "When the appointment was cancelled")
    private LocalDateTime cancelledAt;

    @Schema(description = "Whether reminder was sent", example = "true")
    private Boolean reminderSent;

    @Schema(description = "Whether confirmation was sent", example = "true")
    private Boolean confirmationSent;

    @Schema(description = "Customer rating", example = "5")
    private Integer rating;

    @Schema(description = "Customer review", example = "Excellent service!")
    private String review;

    @Schema(description = "Review date")
    private LocalDateTime reviewDate;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Last modified date")
    private LocalDateTime lastModifiedDate;



}