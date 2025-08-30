package com.tiora.mob.entity;

import com.tiora.mob.util.JsonObjectListConverter;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.entity.Branch;
import com.tiora.mob.entity.EmployeeSchedule;
import com.tiora.mob.entity.Salon;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    public Long getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<java.util.Map<String, Object>> getSpecializations() { return specializations; }
    public Integer getRatings() { return ratings; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public EmployeeStatus getStatus() { return status; }
    public Role getRole() { return role; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 50)
    private String username;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String address;

    @Column(length = 50)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "base_salary", precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "experience_years")
    private Integer experience;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "emergency_relationship", length = 50)
    private String emergencyRelationship;

    @Column(name = "ratings")
    private Integer ratings;

    @Column(name = "specializations", columnDefinition = "jsonb")
    @Convert(converter = JsonObjectListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<java.util.Map<String, Object>> specializations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "serves_gender")
    private ServesGender servesGender = ServesGender.BOTH;

    @Column(name = "employee_weekly_schedule", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String employeeWeeklySchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(length = 1000)
    private String notes;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeeSchedule> schedules = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;




    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum Role {
        OWNER, MANAGER, STYLIST, BARBER, RECEPTIONIST, CLEANER, OTHER
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
    }

    public enum ServesGender {
        MALE, FEMALE, BOTH
    }

}
