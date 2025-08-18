package com.tiora.mob.repository;


import com.tiora.mob.entity.*;
import com.tiora.mob.entity.Appointment.AppointmentStatus;
import com.tiora.mob.entity.Appointment.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // Find by service
    List<Appointment> findByServiceOrderByAppointmentDateDesc(Service service);

    // Find by  customer
    List<Appointment> findByCustomerIdOrderByAppointmentDateDesc(Long customerId);

    // Find appointments for today
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "FUNCTION('DATE', a.appointmentDate) = CURRENT_DATE ORDER BY a.appointmentDate")
    List<Appointment> findTodaysAppointments(@Param("salon") Salon salon);

    // Find appointments for specific date
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date) ORDER BY a.appointmentDate")
    List<Appointment> findAppointmentsByDate(@Param("salon") Salon salon, @Param("date") LocalDateTime date);

    // Find appointments in date range
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate")
    List<Appointment> findAppointmentsInDateRange(@Param("salon") Salon salon,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Find upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.appointmentDate > CURRENT_TIMESTAMP AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingAppointments(@Param("salon") Salon salon);

    // Find employee's appointments for specific date
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND " +
            "FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date) ORDER BY a.appointmentDate")
    List<Appointment> findEmployeeAppointmentsByDate(@Param("employee") Employee employee,
                                                     @Param("date") LocalDateTime date);

    // Find employee's upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND " +
            "a.appointmentDate > CURRENT_TIMESTAMP AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate")
    List<Appointment> findEmployeeUpcomingAppointments(@Param("employee") Employee employee);

    // Find customer's appointment history
    @Query("SELECT a FROM Appointment a WHERE a.customer = :customer " +
            "ORDER BY a.appointmentDate DESC")
    List<Appointment> findCustomerAppointmentHistory(@Param("customer") Customer customer);

    // Find appointments by payment status
    List<Appointment> findBySalonAndPaymentStatusOrderByAppointmentDate(Salon salon, PaymentStatus paymentStatus);

    // Find overdue appointments (past scheduled time but not completed)
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.appointmentDate < CURRENT_TIMESTAMP AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate")
    List<Appointment> findOverdueAppointments(@Param("salon") Salon salon);

    // Find appointments with pending payments
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.paymentStatus IN ('PENDING', 'PARTIAL') ORDER BY a.appointmentDate DESC")
    List<Appointment> findAppointmentsWithPendingPayments(@Param("salon") Salon salon);

    // Find appointments needing confirmation
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.status = 'SCHEDULED' AND a.confirmationSent = false AND " +
            "a.appointmentDate > CURRENT_TIMESTAMP ORDER BY a.appointmentDate")
    List<Appointment> findAppointmentsNeedingConfirmation(@Param("salon") Salon salon);

    // Find appointments needing reminders
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.status IN ('SCHEDULED', 'CONFIRMED') AND a.reminderSent = false AND " +
            "a.appointmentDate BETWEEN CURRENT_TIMESTAMP AND :reminderTime ORDER BY a.appointmentDate")
    List<Appointment> findAppointmentsNeedingReminders(@Param("salon") Salon salon,
                                                       @Param("reminderTime") LocalDateTime reminderTime);

    // Find appointments with pagination
    Page<Appointment> findBySalonOrderByAppointmentDateDesc(Salon salon, Pageable pageable);

    // Count appointments by status
    long countBySalonAndStatus(Salon salon, AppointmentStatus status);

    // Count appointments by payment status
    long countBySalonAndPaymentStatus(Salon salon, PaymentStatus paymentStatus);

    // Count today's appointments
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.salon = :salon AND " +
            "FUNCTION('DATE', a.appointmentDate) = CURRENT_DATE")
    long countTodaysAppointments(@Param("salon") Salon salon);

    // Count completed appointments in date range
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.salon = :salon AND " +
            "a.status = 'COMPLETED' AND a.appointmentDate BETWEEN :startDate AND :endDate")
    long countCompletedAppointmentsInRange(@Param("salon") Salon salon,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Calculate revenue in date range
    @Query("SELECT SUM(a.totalAmount) FROM Appointment a WHERE a.salon = :salon AND " +
            "a.status = 'COMPLETED' AND a.appointmentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueInRange(@Param("salon") Salon salon,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Calculate pending payment amount
    @Query("SELECT SUM(a.totalAmount - a.paidAmount) FROM Appointment a WHERE a.salon = :salon AND " +
            "a.paymentStatus IN ('PENDING', 'PARTIAL')")
    BigDecimal calculatePendingPaymentAmount(@Param("salon") Salon salon);

    // Find most popular services by appointments
    @Query("SELECT a.service, COUNT(a) as appointmentCount FROM Appointment a " +
            "WHERE a.salon = :salon AND a.appointmentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY a.service ORDER BY appointmentCount DESC")
    List<Object[]> findMostPopularServices(@Param("salon") Salon salon,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Find busiest time slots
    @Query("SELECT HOUR(a.appointmentDate) as hour, COUNT(a) as appointmentCount " +
            "FROM Appointment a WHERE a.salon = :salon AND " +
            "a.appointmentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY HOUR(a.appointmentDate) ORDER BY appointmentCount DESC")
    List<Object[]> findBusiestTimeSlots(@Param("salon") Salon salon,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Check for appointment conflicts
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND " +
            "a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') AND " +
            "((a.appointmentDate <= :startTime AND a.estimatedEndTime > :startTime) OR " +
            "(a.appointmentDate < :endTime AND a.estimatedEndTime >= :endTime) OR " +
            "(a.appointmentDate >= :startTime AND a.estimatedEndTime <= :endTime))")
    List<Appointment> findConflictingAppointments(@Param("employee") Employee employee,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    // Find appointments by rating range
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.rating BETWEEN :minRating AND :maxRating ORDER BY a.rating DESC")
    List<Appointment> findAppointmentsByRatingRange(@Param("salon") Salon salon,
                                                    @Param("minRating") Integer minRating,
                                                    @Param("maxRating") Integer maxRating);

    // Find appointments with reviews
    @Query("SELECT a FROM Appointment a WHERE a.salon = :salon AND " +
            "a.review IS NOT NULL AND a.review != '' ORDER BY a.reviewDate DESC")
    List<Appointment> findAppointmentsWithReviews(@Param("salon") Salon salon);

    // Count future appointments for a service
    long countByServiceIdAndAppointmentDateAfterAndStatusNot(Long serviceId, LocalDateTime date, AppointmentStatus status);

    // Find appointments by employee and date range
    List<Appointment> findByEmployeeAndAppointmentDateBetweenAndStatusNot(Employee employee, LocalDateTime startDate, LocalDateTime endDate, AppointmentStatus status);

    // Find appointments by salon ID, status and payment status
    @Query("SELECT a FROM Appointment a WHERE a.salon.id = :salonId AND a.status = :status AND a.paymentStatus = :paymentStatus ORDER BY a.appointmentDate DESC")
    List<Appointment> findBySalonIdAndStatusAndPaymentStatus(@Param("salonId") Long salonId, @Param("status") AppointmentStatus status, @Param("paymentStatus") PaymentStatus paymentStatus);

    // Branch-based query methods
    @Query("SELECT a FROM Appointment a WHERE a.salon.salonId = :salonId AND a.branchId = :branchId ORDER BY a.appointmentDate DESC")
    List<Appointment> findBySalonIdAndBranchIdOrderByAppointmentDateDesc(@Param("salonId") Long salonId, @Param("branchId") Long branchId);

    @Query("SELECT a FROM Appointment a WHERE a.salon.salonId = :salonId AND a.branchId = :branchId AND a.appointmentDate >= :startOfDay AND a.appointmentDate < :endOfDay ORDER BY a.appointmentDate ASC")
    List<Appointment> findBySalonIdAndBranchIdAndToday(@Param("salonId") Long salonId, @Param("branchId") Long branchId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT a FROM Appointment a WHERE a.salon.salonId = :salonId AND a.branchId = :branchId AND a.paymentStatus = 'PENDING' ORDER BY a.appointmentDate DESC")
    List<Appointment> findBySalonIdAndBranchIdAndPendingPayments(@Param("salonId") Long salonId, @Param("branchId") Long branchId);
}
