package com.tiora.mob.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Integer durationMinutes;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isPopular;
    private String status;
    private String genderAvailability;
    private BigDecimal discountPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
