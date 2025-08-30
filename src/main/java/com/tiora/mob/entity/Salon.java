package com.tiora.mob.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "salons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalonStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum SalonStatus {
        ACTIVE, INACTIVE, BLOCKED
    }

    public Long getSalonId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
