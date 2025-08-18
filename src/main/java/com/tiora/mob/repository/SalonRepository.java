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

    // Find by salon ID
    Optional<Salon> findBySalonId(Long salonId);

    // Find active salons
    List<Salon> findByStatusOrderByName(Salon.SalonStatus status);

    // Find by district
    List<Salon> findByDistrictIgnoreCase(String district);

    // Search salons by name or district
    @Query("SELECT s FROM Salon s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.district) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Salon> searchSalons(@Param("searchTerm") String searchTerm);

    // Find salons with pagination
    Page<Salon> findByStatusOrderByCreatedAtDesc(Salon.SalonStatus status, Pageable pageable);


}