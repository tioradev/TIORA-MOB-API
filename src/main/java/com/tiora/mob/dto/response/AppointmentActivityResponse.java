package com.tiora.mob.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tiora.mob.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Appointment activity details response")
public class AppointmentActivityResponse {
    
    @Schema(description = "Appointment ID", example = "123")
    private Long appointmentId;
    
    @Schema(description = "Appointment number", example = "APT1234567890")
    private String appointmentNumber;
    
    @Schema(description = "Appointment status", example = "IN_PROGRESS")
    private Appointment.AppointmentStatus status;
    
    @Schema(description = "Salon name", example = "Elite Hair Studio")
    private String salonName;
    
    @Schema(description = "Branch name", example = "Downtown Branch")
    private String branchName;
    
    @Schema(description = "Barber/Staff name", example = "John Smith")
    private String barberName;
    
    @Schema(description = "Branch latitude", example = "1.3521")
    private BigDecimal latitude;
    
    @Schema(description = "Branch longitude", example = "103.8198")
    private BigDecimal longitude;
    
    @Schema(description = "Appointment date and time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentDateTime;
    
    @Schema(description = "Service price", example = "50.00")
    private BigDecimal servicePrice;
    
    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discountAmount;
    
    @Schema(description = "Total amount", example = "45.00")
    private BigDecimal totalAmount;
    
    @Schema(description = "Payment status", example = "PAID")
    private Appointment.PaymentStatus paymentStatus;
    
    @Schema(description = "Actual start time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;
    
    @Schema(description = "Actual end time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;
    
    @Schema(description = "Cancellation reason", example = "Customer requested")
    private String cancellationReason;
    
    @Schema(description = "Cancelled at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledAt;
    
    @Schema(description = "Cancelled by", example = "Customer")
    private String cancelledBy;
    
    @Schema(description = "Service name", example = "Hair Cut & Styling")
    private String serviceName;
    
    @Schema(description = "Branch address")
    private String branchAddress;
    
    @Schema(description = "Branch phone number")
    private String branchPhoneNumber;
}
