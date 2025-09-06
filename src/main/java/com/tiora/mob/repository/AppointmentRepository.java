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

    // Find appointments by status with all related entities for activity display
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.salon s " +
           "JOIN FETCH a.employee e " +
           "JOIN FETCH a.service srv " +
           "LEFT JOIN FETCH a.branch b " +
           "WHERE a.status = :status " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByStatusWithDetails(@Param("status") AppointmentStatus status);

    // Find appointments by multiple statuses with all related entities for activity display
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.salon s " +
           "JOIN FETCH a.employee e " +
           "JOIN FETCH a.service srv " +
           "LEFT JOIN FETCH a.branch b " +
           "WHERE a.status IN :statuses " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByStatusInWithDetails(@Param("statuses") List<AppointmentStatus> statuses);

    // Find appointments by customer and status with all related entities
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.salon s " +
           "JOIN FETCH a.employee e " +
           "JOIN FETCH a.service srv " +
           "LEFT JOIN FETCH a.branch b " +
           "WHERE a.customer.id = :customerId AND a.status = :status " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByCustomerIdAndStatusWithDetails(@Param("customerId") Long customerId, @Param("status") AppointmentStatus status);

    // Find appointments by customer and multiple statuses with all related entities
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.salon s " +
           "JOIN FETCH a.employee e " +
           "JOIN FETCH a.service srv " +
           "LEFT JOIN FETCH a.branch b " +
           "WHERE a.customer.id = :customerId AND a.status IN :statuses " +
           "ORDER BY a.appointmentDate DESC")
    List<Appointment> findByCustomerIdAndStatusInWithDetails(@Param("customerId") Long customerId, @Param("statuses") List<AppointmentStatus> statuses);
}


