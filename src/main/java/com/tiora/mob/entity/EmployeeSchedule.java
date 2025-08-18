package com.tiora.mob.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employee_schedules")
@EntityListeners(AuditingEntityListener.class)
public class EmployeeSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "break_start_time")
    private LocalTime breakStartTime;
    
    @Column(name = "break_end_time")
    private LocalTime breakEndTime;
    
    @Column(name = "is_working_day", nullable = false)
    private Boolean isWorkingDay = true;
    
    @Column(name = "is_half_day")
    private Boolean isHalfDay = false;
    
    @Column(name = "max_appointments")
    private Integer maxAppointments;
    
    @Column(name = "hourly_slots")
    private Integer hourlySlots = 2; // Default 2 appointments per hour
    
    @Column(length = 500)
    private String notes;
    
    @OneToMany(mappedBy = "employeeSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleException> scheduleExceptions = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;





    // Utility methods
    public boolean hasBreak() {
        return breakStartTime != null && breakEndTime != null;
    }
    
    public long getWorkingHours() {
        if (!isWorkingDay) return 0;
        
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        
        if (hasBreak()) {
            long breakMinutes = java.time.Duration.between(breakStartTime, breakEndTime).toMinutes();
            totalMinutes -= breakMinutes;
        }
        
        return totalMinutes / 60;
    }
    
    public boolean isAvailableAt(LocalTime time) {
        if (!isWorkingDay) return false;
        
        boolean inWorkingHours = !time.isBefore(startTime) && !time.isAfter(endTime);
        
        if (!inWorkingHours) return false;
        
        if (hasBreak()) {
            boolean inBreakTime = !time.isBefore(breakStartTime) && !time.isAfter(breakEndTime);
            return !inBreakTime;
        }
        
        return true;
    }
    
    // Nested entity for schedule exceptions (holidays, leaves, etc.)
    @Entity
    @Table(name = "schedule_exceptions")
    @EntityListeners(AuditingEntityListener.class)
    public static class ScheduleException {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_schedule_id", nullable = false)
        private EmployeeSchedule employeeSchedule;
        
        @Column(name = "exception_date", nullable = false)
        private java.time.LocalDate exceptionDate;
        
        @Enumerated(EnumType.STRING)
        @Column(name = "exception_type", nullable = false)
        private ExceptionType exceptionType;
        
        @Column(name = "custom_start_time")
        private LocalTime customStartTime;
        
        @Column(name = "custom_end_time")
        private LocalTime customEndTime;
        
        @Column(length = 500)
        private String reason;
        
        @Column(name = "is_approved")
        private Boolean isApproved = false;
        
        @Column(name = "approved_by", length = 100)
        private String approvedBy;
        
        @Column(name = "approved_at")
        private LocalDateTime approvedAt;
        
        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;
        
        @LastModifiedDate
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;
        
        // Constructors
        public ScheduleException() {}
        
        public ScheduleException(EmployeeSchedule employeeSchedule, java.time.LocalDate exceptionDate, 
                               ExceptionType exceptionType, String reason) {
            this.employeeSchedule = employeeSchedule;
            this.exceptionDate = exceptionDate;
            this.exceptionType = exceptionType;
            this.reason = reason;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public EmployeeSchedule getEmployeeSchedule() { return employeeSchedule; }
        public void setEmployeeSchedule(EmployeeSchedule employeeSchedule) { this.employeeSchedule = employeeSchedule; }
        
        public java.time.LocalDate getExceptionDate() { return exceptionDate; }
        public void setExceptionDate(java.time.LocalDate exceptionDate) { this.exceptionDate = exceptionDate; }
        
        public ExceptionType getExceptionType() { return exceptionType; }
        public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }
        
        public LocalTime getCustomStartTime() { return customStartTime; }
        public void setCustomStartTime(LocalTime customStartTime) { this.customStartTime = customStartTime; }
        
        public LocalTime getCustomEndTime() { return customEndTime; }
        public void setCustomEndTime(LocalTime customEndTime) { this.customEndTime = customEndTime; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Boolean getIsApproved() { return isApproved; }
        public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
        
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        
        public LocalDateTime getApprovedAt() { return approvedAt; }
        public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public void approve(String approvedBy) {
            this.isApproved = true;
            this.approvedBy = approvedBy;
            this.approvedAt = LocalDateTime.now();
        }
        
        public enum ExceptionType {
            LEAVE, SICK_LEAVE, HOLIDAY, CUSTOM_HOURS, UNAVAILABLE
        }
    }
}
