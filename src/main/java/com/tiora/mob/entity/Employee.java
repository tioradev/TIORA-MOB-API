package com.tiora.mob.entity;

import java.time.LocalTime;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;



    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String profileImageUrl;

    @Column(nullable = true)
    private Double ratings;

    @Column(nullable = true, columnDefinition = "jsonb")
    @Convert(converter = com.tiora.mob.util.MapListJsonConverter.class)
    private List<Map<String, Object>> specializations;
    // Returns default working hours (9am-6pm) for demonstration
    public com.tiora.mob.entity.WorkingHours getWorkingHoursForDate(LocalDate date) {
        return new com.tiora.mob.entity.WorkingHours(LocalTime.of(9, 0), LocalTime.of(18, 0));
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Role getRole() {
        return this.role;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private EmployeeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Double getRatings() {
        return ratings;
    }

    public List<Map<String, Object>> getSpecializations() {
        return specializations;
    }

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EmployeeStatus getStatus() {
        return status;
    }

    public Salon getSalon() {
        return salon;
    }

    public Branch getBranch() {
        return branch;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public enum Role {
        ADMIN, BARBER, RECEPTIONIST
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
}
