package com.tiora.mob.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "salons")
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salon_id")
    private Long salonId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 50)
    private String district;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(name = "owner_first_name", nullable = false, length = 50)
    private String ownerFirstName;

    @Column(name = "owner_last_name", nullable = false, length = 50)
    private String ownerLastName;

    @Column(name = "owner_phone", nullable = false, length = 20)
    private String ownerPhone;

    @Column(name = "owner_email", length = 100)
    private String ownerEmail;

    @Column(name = "br_number", length = 100)
    private String brNumber;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "owner_img_url", columnDefinition = "TEXT")
    private String ownerImgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalonStatus status = SalonStatus.ACTIVE;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Branch> branches = new ArrayList<>();

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // Utility methods
    public boolean isActive() {
        return status == SalonStatus.ACTIVE;
    }

    public String getFullOwnerName() {
        return ownerFirstName + " " + ownerLastName;
    }

    public enum SalonStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL
    }
}