package com.tiora.mob.repository;

import com.tiora.mob.entity.*;
import com.tiora.mob.entity.Appointment.AppointmentStatus;
import com.tiora.mob.entity.Appointment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Find by appointment number
    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    // Find by salon
    List<Appointment> findBySalonOrderByAppointmentDateDesc(Salon salon);

    // Find by salon and status
    List<Appointment> findBySalonAndStatusOrderByAppointmentDate(Salon salon, AppointmentStatus status);

    // Find by customer
    List<Appointment> findByCustomerOrderByAppointmentDateDesc(Customer customer);

    // Find by employee
    List<Appointment> findByEmployeeOrderByAppointmentDateDesc(Employee employee);

    // Find by customerId ordered by appointment date desc
    List<Appointment> findByCustomerIdOrderByAppointmentDateDesc(Long customerId);

    // Find conflicting appointments for an employee
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND ((a.appointmentDate < :endTime AND a.estimatedEndTime > :startTime))")
    List<Appointment> findConflictingAppointments(@Param("employee") Employee employee, @Param("startTime") java.time.LocalDateTime startTime, @Param("endTime") java.time.LocalDateTime endTime);

    // Find appointments by employeeId and date (stub for compatibility)
    @Query("SELECT a FROM Appointment a WHERE a.employee.id = :employeeId AND FUNCTION('DATE', a.appointmentDate) = :date")
    List<Appointment> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") java.time.LocalDate date);
}


