package com.tiora.mob.repository;



import com.tiora.mob.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT b FROM Employee b WHERE b.salon = :salonId AND b.status = 'ACTIVE'")
    List<Employee> findBySalonIdAndIsActiveTrue(Long salonId);

}