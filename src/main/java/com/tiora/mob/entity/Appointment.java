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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setService(Service service) { this.service = service; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalDateTime getEstimatedEndTime() { return estimatedEndTime; }
    public void setEstimatedEndTime(LocalDateTime estimatedEndTime) { this.estimatedEndTime = estimatedEndTime; }
    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public BigDecimal getServicePrice() { return servicePrice; }
    public void setServicePrice(BigDecimal servicePrice) { this.servicePrice = servicePrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public Boolean getReminderSent() { return reminderSent; }
    public void setReminderSent(Boolean reminderSent) { this.reminderSent = reminderSent; }
    public Boolean getConfirmationSent() { return confirmationSent; }
    public void setConfirmationSent(Boolean confirmationSent) { this.confirmationSent = confirmationSent; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Service getService() { return service; }
    // Removed duplicate getService() method

    public Employee getEmployee() {
        return this.employee;
    }


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