package com.tiora.mob.service;

import com.tiora.mob.entity.Promotion;
import com.tiora.mob.entity.Promotion.PromotionStatus;
import com.tiora.mob.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Get all active promotions that are currently valid (within date range)
     */
    public List<Promotion> getActivePromotions() {
        LocalDate currentDate = LocalDate.now();
        logger.info("Fetching active promotions for date: {}", currentDate);
        
        List<Promotion> promotions = promotionRepository.findActivePromotionsInDateRange(PromotionStatus.ACTIVE, currentDate);
        logger.info("Found {} active promotions for current date", promotions.size());
        
        return promotions;
    }

    /**
     * Get all active promotions regardless of date range
     */
    public List<Promotion> getAllActivePromotions() {
        logger.info("Fetching all active promotions");
        List<Promotion> promotions = promotionRepository.findByStatusOrderByCreatedAtDesc(PromotionStatus.ACTIVE);
        logger.info("Found {} active promotions", promotions.size());
        return promotions;
    }
}
