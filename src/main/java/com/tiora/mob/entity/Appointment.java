package com.tiora.mob.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_number", unique = true, nullable = false, length = 20)
    private String appointmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;

    @Column(name = "estimated_end_time")
    private LocalDateTime estimatedEndTime;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "service_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal servicePrice;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "confirmation_sent")
    private Boolean confirmationSent = false;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review", length = 1000)
    private String review;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // Utility methods
    private void generateAppointmentNumber() {
        this.appointmentNumber = "APT" + System.currentTimeMillis();
    }

    private void calculateEstimatedEndTime() {
        if (this.appointmentDate != null && this.service != null && this.service.getDurationMinutes() != null) {
            this.estimatedEndTime = this.appointmentDate.plusMinutes(this.service.getDurationMinutes());
        }
    }

    private void calculateTotalAmount() {
        BigDecimal total = this.servicePrice;
        if (this.discountAmount != null) {
            total = total.subtract(this.discountAmount);
        }
        if (this.taxAmount != null) {
            total = total.add(this.taxAmount);
        }
        this.totalAmount = total;
    }

    public BigDecimal getPendingAmount() {
        if (this.paidAmount == null) {
            return this.totalAmount;
        }
        return this.totalAmount.subtract(this.paidAmount);
    }

    public boolean isFullyPaid() {
        return this.paidAmount != null && this.paidAmount.compareTo(this.totalAmount) >= 0;
    }

    public void markAsCompleted() {
        this.status = AppointmentStatus.COMPLETED;
        this.actualEndTime = LocalDateTime.now();
        if (this.actualStartTime == null) {
            this.actualStartTime = this.appointmentDate;
        }
    }

    public void markAsStarted() {
        this.status = AppointmentStatus.IN_PROGRESS;
        this.actualStartTime = LocalDateTime.now();
    }

    public void cancelAppointment(String reason, String cancelledBy) {
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = LocalDateTime.now();
    }

    public enum AppointmentStatus {
        SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum PaymentStatus {
        PENDING, PARTIAL, PAID, REFUNDED
    }

    public enum PaymentMethod {
        CASH, CARD, UPI, NET_BANKING, DIGITAL_WALLET, OTHER
    }



}