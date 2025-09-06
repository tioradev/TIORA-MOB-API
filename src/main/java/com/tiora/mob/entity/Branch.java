package com.tiora.mob.entity;

import com.tiora.mob.config.WeeklyScheduleConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branches")
@EntityListeners(AuditingEntityListener.class)
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchStatus status = BranchStatus.ACTIVE;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "branch_phone_number", length = 20)
    private String branchPhoneNumber;

    @Column(name = "branch_email", length = 100)
    private String branchEmail;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Weekly schedule stored as JSON
    @Convert(converter = WeeklyScheduleConverter.class)
    @Column(name = "weekly_schedule", columnDefinition = "TEXT")
    private WeeklySchedule weeklySchedule = new WeeklySchedule();

    @Column(name = "branch_image", columnDefinition = "TEXT")
    private String branchImage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "salon_type", nullable = false, length = 20)
    private SalonType salonType;

    // --- Added for DTO/controller compatibility ---
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public BranchStatus getStatus() { return status; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public String getBranchPhoneNumber() { return branchPhoneNumber; }
    public String getBranchEmail() { return branchEmail; }
    public String getDescription() { return description; }
    public String getBranchImage() { return branchImage; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public SalonType getSalonType() { return salonType; }

    // Utility methods
    public boolean isActive() {
        return status == BranchStatus.ACTIVE;
    }

    public boolean isOpenOn(String dayOfWeek) {
        if (weeklySchedule == null) return false;
        WeeklySchedule.DaySchedule daySchedule = weeklySchedule.getDaySchedule(dayOfWeek);
        return daySchedule != null && daySchedule.isOpen();
    }

    public enum BranchStatus {
        ACTIVE, INACTIVE, TEMPORARILY_CLOSED, PERMANENTLY_CLOSED
    }
    public enum SalonType {
        GENTS, LADIES, UNISEX
    }
}

