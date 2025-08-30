package com.tiora.mob.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public Integer getTotalVisits() { return totalVisits; }
    public void setTotalVisits(Integer totalVisits) { this.totalVisits = totalVisits; }
    public java.math.BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(java.math.BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public CustomerTier getCustomerTier() { return customerTier; }
    public void setCustomerTier(CustomerTier customerTier) { this.customerTier = customerTier; }
    public LocalDateTime getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(LocalDateTime lastVisitDate) { this.lastVisitDate = lastVisitDate; }
    public String getPreferredStylist() { return preferredStylist; }
    public void setPreferredStylist(String preferredStylist) { this.preferredStylist = preferredStylist; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getNewsletterSubscribed() { return newsletterSubscribed; }
    public void setNewsletterSubscribed(Boolean newsletterSubscribed) { this.newsletterSubscribed = newsletterSubscribed; }
    public Boolean getSmsNotifications() { return smsNotifications; }
    public void setSmsNotifications(Boolean smsNotifications) { this.smsNotifications = smsNotifications; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;


    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "total_visits")
    private Integer totalVisits = 0;

    @Column(name = "total_spent", precision = 10, scale = 2)
    private java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;

    @Column(name = "last_visit_date")
    private LocalDateTime lastVisitDate;

    @Column(name = "preferred_stylist", length = 100)
    private String preferredStylist;

    @Column(length = 1000)
    private String notes;

    @Column(name = "newsletter_subscribed")
    private Boolean newsletterSubscribed = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = true;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void incrementVisits() {
        this.totalVisits = (this.totalVisits == null) ? 1 : this.totalVisits + 1;
        this.lastVisitDate = LocalDateTime.now();
        updateCustomerTier();
    }

    public void addSpentAmount(java.math.BigDecimal amount) {
        if (this.totalSpent == null) {
            this.totalSpent = amount;
        } else {
            this.totalSpent = this.totalSpent.add(amount);
        }
        updateCustomerTier();
    }

    public void addLoyaltyPoints(Integer points) {
        if (this.loyaltyPoints == null) {
            this.loyaltyPoints = points;
        } else {
            this.loyaltyPoints = this.loyaltyPoints + points;
        }
    }

    private void updateCustomerTier() {
        if (totalSpent != null) {
            if (totalSpent.compareTo(java.math.BigDecimal.valueOf(100000)) >= 0) {
                this.customerTier = CustomerTier.PLATINUM;
            } else if (totalSpent.compareTo(java.math.BigDecimal.valueOf(50000)) >= 0) {
                this.customerTier = CustomerTier.GOLD;
            } else if (totalSpent.compareTo(java.math.BigDecimal.valueOf(20000)) >= 0) {
                this.customerTier = CustomerTier.SILVER;
            } else {
                this.customerTier = CustomerTier.BRONZE;
            }
        }
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum CustomerStatus {
        ACTIVE, INACTIVE, BLOCKED, BLACKLISTED
    }

    public enum CustomerTier {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
