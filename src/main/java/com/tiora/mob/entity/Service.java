package com.tiora.mob.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_availability", nullable = false)
    private GenderAvailability genderAvailability = GenderAvailability.BOTH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    public BigDecimal getFinalPrice() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
            return price.subtract(discount);
        }
        return price;
    }

    public enum GenderAvailability {
        MALE, FEMALE, BOTH
    }

    public enum ServiceCategory {
        HAIRCUT, HAIR_STYLING, HAIR_COLOR, HAIR_TREATMENT,
        BEARD_TRIM, BEARD_STYLING, SHAVING,
        FACIAL, MASSAGE, MANICURE, PEDICURE,
        EYEBROW, MAKEUP, SKIN_CARE,
        PACKAGE, OTHER
    }

    public enum ServiceStatus {
        ACTIVE, INACTIVE, TEMPORARILY_UNAVAILABLE
    }


}