package com.tiora.mob.repository;

import com.tiora.mob.entity.Employee;
import com.tiora.mob.entity.Salon;
import com.tiora.mob.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tiora.mob.entity.Employee.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.tiora.mob.entity.Employee.EmployeeStatus;



@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

        // Find available barbers by serviceId in specialization_services (JSON) and servesGender
                        @Query(value = "SELECT * FROM employees WHERE status = 'ACTIVE' AND role = 'BARBER' AND serves_gender = :gender AND branch_id = :branchId AND specializations @> CAST(:serviceJson AS jsonb)", nativeQuery = true)
                        List<Employee> findAvailableBarbersByServiceAndGenderAndBranch(@Param("serviceJson") String serviceJson, @Param("gender") String gender, @Param("branchId") Long branchId);
    // Find by salon
    List<Employee> findBySalonOrderByFirstName(Salon salon);

    // Find by salon and status
    List<Employee> findBySalonAndStatus(Salon salon, EmployeeStatus status);

    // Find by role
    List<Employee> findByRole(Role role);

    // Find by salon and role
    List<Employee> findBySalonAndRole(Salon salon, Role role);

    // Find by email
    Optional<Employee> findByEmail(String email);

    // Find by username
    Optional<Employee> findByUsername(String username);

    // Find by username with salon and branch eagerly fetched
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.salon LEFT JOIN FETCH e.branch WHERE e.username = :username")
    Optional<Employee> findByUsernameWithDetails(@Param("username") String username);

    // Find by phone number
    Optional<Employee> findByPhoneNumber(String phoneNumber);

    // Find active employees by salon
    List<Employee> findBySalonAndStatusOrderByFirstName(Salon salon, EmployeeStatus status);

    // Count employees by branch
    long countByBranch(Branch branch);

    // Count active employees by branch
    long countByBranchAndStatus(Branch branch, EmployeeStatus status);

    // Search employees by name
    @Query("SELECT e FROM Employee e WHERE e.salon = :salon AND " +
            "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Employee> searchEmployeesByName(@Param("salon") Salon salon, @Param("searchTerm") String searchTerm);

    // Find employees with pagination
    Page<Employee> findBySalonOrderByCreatedAtDesc(Salon salon, Pageable pageable);

    // Count employees by salon and status
    long countBySalonAndStatus(Salon salon, EmployeeStatus status);

    // Count employees by salon and role
    long countBySalonAndRole(Salon salon, Role role);

    // Find employees by salary range
    @Query("SELECT e FROM Employee e WHERE e.salon = :salon AND " +
            "e.baseSalary BETWEEN :minSalary AND :maxSalary")
    List<Employee> findBySalaryRange(@Param("salon") Salon salon,
                                     @Param("minSalary") BigDecimal minSalary,
                                     @Param("maxSalary") BigDecimal maxSalary);

    // Find employees hired in date range
    @Query("SELECT e FROM Employee e WHERE e.salon = :salon AND " +
            "e.hireDate BETWEEN :startDate AND :endDate ORDER BY e.hireDate DESC")
    List<Employee> findByHireDateRange(@Param("salon") Salon salon,
                                       @Param("startDate") java.time.LocalDate startDate,
                                       @Param("startDate") java.time.LocalDate endDate);

    // Find employees with upcoming birthdays
    @Query("SELECT e FROM Employee e WHERE e.salon = :salon AND " +
            "MONTH(e.dateOfBirth) = :month AND DAY(e.dateOfBirth) >= :day " +
            "ORDER BY DAY(e.dateOfBirth)")
    List<Employee> findUpcomingBirthdays(@Param("salon") Salon salon,
                                         @Param("month") int month,
                                         @Param("day") int day);

    // Find stylists available for appointments
    @Query("SELECT e FROM Employee e WHERE e.salon = :salon AND " +
            "e.status = 'ACTIVE' AND e.role IN ('STYLIST', 'BARBER', 'MANAGER')")
    List<Employee> findAvailableStylists(@Param("salon") Salon salon);

    // Check if email exists (excluding current employee)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employee e WHERE LOWER(e.email) = LOWER(:email) AND e.employeeId != :excludeEmployeeId")
    boolean existsByEmailIgnoreCaseAndEmployeeIdNot(@Param("email") String email, @Param("excludeEmployeeId") Long excludeEmployeeId);

    // Check if phone exists (excluding current employee)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employee e WHERE e.phoneNumber = :phoneNumber AND e.employeeId != :excludeEmployeeId")
    boolean existsByPhoneNumberAndEmployeeIdNot(@Param("phoneNumber") String phoneNumber, @Param("excludeEmployeeId") Long excludeEmployeeId);

    // Find employees by multiple roles
    List<Employee> findBySalonAndRoleIn(Salon salon, List<Role> roles);

    // Find top performing employees by appointments
    @Query("SELECT e FROM Employee e LEFT JOIN e.appointments a " +
            "WHERE e.salon = :salon AND a.createdAt >= :since " +
            "GROUP BY e ORDER BY COUNT(a) DESC")
    List<Employee> findTopPerformingEmployees(@Param("salon") Salon salon,
                                              @Param("since") LocalDateTime since,
                                              Pageable pageable);

    // Find employees by salon ID and status
    List<Employee> findBySalonSalonIdAndStatus(Long salonId, EmployeeStatus status);

}