package com.tiora.mob.repository;

import com.tiora.mob.entity.Promotion;
import com.tiora.mob.entity.Promotion.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Find all active promotions that are currently valid (within date range)
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = :status AND :currentDate BETWEEN p.startDate AND p.endDate ORDER BY p.createdAt DESC")
    List<Promotion> findActivePromotionsInDateRange(@Param("status") PromotionStatus status, @Param("currentDate") LocalDate currentDate);

    /**
     * Find all active promotions
     */
    List<Promotion> findByStatusOrderByCreatedAtDesc(PromotionStatus status);

    /**
     * Find promotions by status and date range
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = :status AND p.startDate <= :endDate AND p.endDate >= :startDate ORDER BY p.createdAt DESC")
    List<Promotion> findByStatusAndDateRange(@Param("status") PromotionStatus status, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}
