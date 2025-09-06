package com.tiora.mob.controller;

import com.tiora.mob.dto.response.PromotionResponse;
import com.tiora.mob.entity.Promotion;
import com.tiora.mob.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mobile/promotions")
@Tag(name = "Promotions", description = "Promotion management APIs")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class PromotionController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);

    @Autowired
    private PromotionService promotionService;

    /**
     * Get all active promotions that are currently valid (within date range)
     */
    @GetMapping
    @Operation(summary = "Get active promotions", 
               description = "Retrieves all active promotions where current date is within start_date and end_date range")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        logger.info("GET /mobile/promotions - Fetching active promotions for current date");
        
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            List<PromotionResponse> response = promotions.stream()
                    .map(this::toPromotionResponse)
                    .collect(Collectors.toList());
            
            logger.info("Successfully retrieved {} active promotions", response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving active promotions: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert Promotion entity to PromotionResponse DTO
     */
    private PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getPromotionId(),
                promotion.getPromotionName(),
                promotion.getDescription(),
                promotion.getImageUrl(),
                promotion.getStatus()
        );
    }
}
