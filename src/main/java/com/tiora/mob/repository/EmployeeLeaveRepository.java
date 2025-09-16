package com.tiora.mob.repository;

import com.tiora.mob.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    java.util.List<EmployeeLeave> findByEmployee_EmployeeIdAndStartDate(Long employeeId, java.time.LocalDate startDate);
    boolean existsByEmployee_EmployeeIdAndStartDate(Long employeeId, java.time.LocalDate startDate);
    List<EmployeeLeave> findByEmployee_EmployeeId(Long employeeId);
}
