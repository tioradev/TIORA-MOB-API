package com.tiora.mob.dto.response;

import com.tiora.mob.entity.Promotion.PromotionStatus;

public class PromotionResponse {
    private Long promotionId;
    private String promotionName;
    private String description;
    private String imageUrl;
    private PromotionStatus status;

    // Default constructor
    public PromotionResponse() {}

    // Constructor with all fields
    public PromotionResponse(Long promotionId, String promotionName, String description, String imageUrl, PromotionStatus status) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    // Getters and setters
    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public PromotionStatus getStatus() {
        return status;
    }

    public void setStatus(PromotionStatus status) {
        this.status = status;
    }
}
