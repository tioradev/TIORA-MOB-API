package com.tiora.mob.repository;


import com.tiora.mob.entity.Salon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {


    // Find active salons
    List<Salon> findByStatusOrderByName(Salon.SalonStatus status);


    // Find salons with pagination
    Page<Salon> findByStatusOrderByCreatedAtDesc(Salon.SalonStatus status, Pageable pageable);


}