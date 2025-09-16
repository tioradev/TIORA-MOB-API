package com.tiora.mob.service;

import com.tiora.mob.dto.response.EmployeeAppointmentStatsDTO;


import com.tiora.mob.dto.AppointmentEventDto;
import com.tiora.mob.dto.request.AppointmentRequest;
import com.tiora.mob.dto.response.AppointmentResponse;
import com.tiora.mob.dto.response.AppointmentActivityResponse;
import com.tiora.mob.entity.*;
import com.tiora.mob.exception.ResourceNotFoundException;
import com.tiora.mob.exception.UnauthorizedException;
import com.tiora.mob.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class AppointmentService {
    @org.springframework.beans.factory.annotation.Value("${app.unpaid-appointment-cancel-minutes:5}")
    private int unpaidAppointmentCancelMinutes;
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void cancelUnpaidAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(unpaidAppointmentCancelMinutes);
    List<Appointment> unpaidAppointments = appointmentRepository.findByCustomerPaidAndStatusAndCreatedAtBefore(0, Appointment.AppointmentStatus.PENDING, cutoff);
        for (Appointment appointment : unpaidAppointments) {
            appointmentRepository.delete(appointment);
        }
    }
    @Transactional
        public Map<String, Object> updateCustomerPaid(Long appointmentId) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElse(null);
            if (appointment == null) {
                return Map.of("success", false, "message", "Appointment not found with ID: " + appointmentId);
            }
            if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
                return Map.of("success", false, "message", "Customer paid status can only be updated for PENDING appointments.");
            }
        appointment.setCustomerPaid(1);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        // Publish appointment created event to Redis Stream (moved from createAppointment)
        publishAppointmentCreatedEvent(appointment, List.of(appointment.getService()), null);
        return Map.of("success", true, "message", "Customer paid status updated to 1 and status changed to SCHEDULED");
    }
    // ...existing code...

    @org.springframework.beans.factory.annotation.Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public void updateAppointmentStatus(String token, com.tiora.mob.dto.request.AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
            .orElseThrow(() -> new com.tiora.mob.exception.ResourceNotFoundException("Appointment not found"));
        Appointment.AppointmentStatus newStatus = Appointment.AppointmentStatus.valueOf(request.getStatus());
        appointment.setStatus(newStatus);

        if (newStatus == Appointment.AppointmentStatus.CANCELLED) {
            Long employeeId = appointment.getEmployee() != null ? appointment.getEmployee().getEmployeeId() : null;
            String reason = request.getCancellationReason() != null && !request.getCancellationReason().isEmpty()
                ? request.getCancellationReason()
                : "cancelled by Stylist - " + employeeId;
            appointment.setCancellationReason(reason);
            appointment.setCancelledBy(String.valueOf(employeeId));
            appointment.setCancelledAt(java.time.LocalDateTime.now());
            // Publish customer notification for CANCELLED
            streamPublisher.publishCustomerNotification(
                appointment.getCustomer().getId(),
                appointment.getId(),
                "CANCELLED",
                reason
            );
                // Publish salon notification for CANCELLED
                if (appointment.getSalon() != null) {
                    streamPublisher.publishAppointmentUpdated(
                        appointment.getSalon().getId(),
                        appointment.getId(),
                        request.getStatus(),
                        "CANCELLED"
                    );
                }
        }

        appointmentRepository.save(appointment);

        if (newStatus == Appointment.AppointmentStatus.COMPLETED) {
            Customer customer = appointment.getCustomer();
            String latestVisitJson = customer.getLatestVisitJson();
            java.util.Map<Long, String> visitMap = new java.util.HashMap<>();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.time.LocalDate today = java.time.LocalDate.now();
            Long branchId = appointment.getBranch() != null ? appointment.getBranch().getBranchId() : null;
            try {
                if (latestVisitJson != null && !latestVisitJson.isEmpty()) {
                    visitMap = mapper.readValue(latestVisitJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<Long, String>>() {});
                }
                if (branchId != null) {
                    visitMap.put(branchId, today.toString());
                    customer.setLatestVisitJson(mapper.writeValueAsString(visitMap));
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                // log error or handle as needed
            }
            customerRepository.save(customer);
            // Publish customer notification for COMPLETED
            streamPublisher.publishCustomerNotification(
                appointment.getCustomer().getId(),
                appointment.getId(),
                "COMPLETED",
                "Your appointment has been completed."
            );
                // Publish salon notification for COMPLETED
                if (appointment.getSalon() != null) {
                    streamPublisher.publishAppointmentUpdated(
                        appointment.getSalon().getId(),
                        appointment.getId(),
                        request.getStatus(),
                        "COMPLETED"
                    );
                }
        }
    }

    public EmployeeAppointmentStatsDTO getEmployeeAppointmentStats(Long employeeId) {
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDateTime startOfDay = today.atStartOfDay();
    java.time.LocalDateTime endOfDay = today.atTime(java.time.LocalTime.MAX);
    int todaysAppointments = appointmentRepository.countByEmployee_EmployeeIdAndAppointmentDateBetween(employeeId, startOfDay, endOfDay);
    int completedAppointments = appointmentRepository.countByEmployee_EmployeeIdAndStatus(employeeId, Appointment.AppointmentStatus.COMPLETED);
    int pendingAppointments = appointmentRepository.countByEmployee_EmployeeIdAndStatus(employeeId, Appointment.AppointmentStatus.SCHEDULED);
    int scheduledAppointments = appointmentRepository.countByEmployee_EmployeeIdAndStatus(employeeId, Appointment.AppointmentStatus.SCHEDULED);
        int totalAppointments = scheduledAppointments + completedAppointments;
        EmployeeAppointmentStatsDTO stats = new EmployeeAppointmentStatsDTO();
        stats.setTodaysAppointments(todaysAppointments);
        stats.setTotalAppointments(totalAppointments);
        stats.setCompletedAppointments(completedAppointments);
        stats.setPendingAppointments(pendingAppointments);
        return stats;
    }
    public List<AppointmentActivityResponse> getAppointmentActivitiesFiltered(
            Appointment.AppointmentStatus status,
            Long customerId,
            Long employeeId,
            java.time.LocalDateTime appointmentDate) {
        List<Appointment> appointments;
        if (appointmentDate != null) {
            java.time.LocalDateTime startDate = appointmentDate;
            java.time.LocalDateTime endDate = appointmentDate.plusDays(1);
            appointments = appointmentRepository.findByStatusAndAppointmentDateBetween(status, startDate, endDate);
        } else if (customerId != null && employeeId != null) {
            appointments = appointmentRepository.findByStatusAndCustomerIdAndEmployeeId(
                status, customerId, employeeId);
        } else if (customerId != null) {
            appointments = appointmentRepository.findByCustomerIdAndStatusWithDetails(customerId, status);
        } else if (employeeId != null) {
            appointments = appointmentRepository.findByStatusAndEmployeeId(status, employeeId);
        } else {
            appointments = appointmentRepository.findByStatusWithDetails(status);
        }
        return appointments.stream()
            .map(this::mapToAppointmentActivityResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);


    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private AppointmentStreamPublisher appointmentStreamPublisher;

    @Autowired
    private MobileAppointmentStreamPublisher streamPublisher;

    @Transactional
    public AppointmentResponse createAppointment(String token, AppointmentRequest appointmentRequest) {
        logger.info("Creating appointment: serviceIds={}, employeeId={}, salonId={}",
                appointmentRequest.getServiceIds(), appointmentRequest.getEmployeeId(), appointmentRequest.getSalonId());


        // Find customer by ID (customerPhone is no longer supported)
        Customer customer = customerService.getCustomerById(appointmentRequest.getCustomerId());

        // Validate and get services
        List<Service> services = new ArrayList<>();
        BigDecimal totalServicePrice = BigDecimal.ZERO;

        for (Long serviceId : appointmentRequest.getServiceIds()) {
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));
            services.add(service);
            totalServicePrice = totalServicePrice.add(service.getPrice());
        }

        Employee employee = employeeRepository.findById(appointmentRequest.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Salon salon = salonRepository.findById(appointmentRequest.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        // Check for appointment conflicts
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                employee, appointmentRequest.getAppointmentDate(), appointmentRequest.getEstimatedEndTime());
        if (!conflicts.isEmpty()) {
            logger.warn("Appointment conflict detected for employeeId={} at {}", appointmentRequest.getEmployeeId(), appointmentRequest.getAppointmentDate());
            throw new RuntimeException("Employee has conflicting appointment at this time");
        }

        // Create appointment for the first service (main appointment)
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setService(services.get(0)); // Set first service as primary
        appointment.setEmployee(employee);
        appointment.setSalon(salon);
        appointment.setBranchId(appointmentRequest.getBranchId()); // Set branch ID
        appointment.setAppointmentDate(appointmentRequest.getAppointmentDate());
        appointment.setEstimatedEndTime(appointmentRequest.getEstimatedEndTime());
        
        // Set timestamps with current time in the format: 2025-09-06 12:00:00
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        
        appointment.setServicePrice(totalServicePrice); // Use total price of all services
        appointment.setDiscountAmount(appointmentRequest.getDiscountAmount() != null ? appointmentRequest.getDiscountAmount() : BigDecimal.ZERO);
        appointment.setTaxAmount(BigDecimal.ZERO); // Removed tax amount
                // Set payment method if provided
                if (appointmentRequest.getPaymentMethod() != null) {
                        try {
                                appointment.setPaymentMethod(Appointment.PaymentMethod.valueOf(appointmentRequest.getPaymentMethod().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                                throw new RuntimeException("Invalid payment method: " + appointmentRequest.getPaymentMethod());
                        }
                } else {
                        appointment.setPaymentMethod(null);
                }
                appointment.setCustomerNotes(""); // Removed customer notes
                appointment.setInternalNotes(""); // Removed internal notes

                // Set rating if provided
                if (appointmentRequest.getRating() != null) {
                        appointment.setRating(appointmentRequest.getRating());
                }

        // Calculate total amount (service price - discount, no tax)
        BigDecimal total = totalServicePrice.subtract(appointment.getDiscountAmount());
        appointment.setTotalAmount(total);

    appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        appointment.setPaymentStatus(Appointment.PaymentStatus.PENDING);

        // Set a temporary appointment number to satisfy NOT NULL constraint
        appointment.setAppointmentNumber("TEMP-" + System.currentTimeMillis());
        appointment = appointmentRepository.save(appointment);
        // Generate the final appointment number using id and date
        String appointmentNumber = generateAppointmentNumber(appointment.getId(), appointment.getAppointmentDate());
        appointment.setAppointmentNumber(appointmentNumber);
        appointment = appointmentRepository.save(appointment);
        
    // Removed Redis publish from appointment creation
        
        logger.info("Appointment created successfully for customer={}, appointmentNumber={}", customer.getPhoneNumber(), appointment.getAppointmentNumber());
        return mapToAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getCustomerAppointments(String token) {
        Customer customer = customerService.getCustomerFromToken(token);

        List<Appointment> appointments = appointmentRepository
                .findByCustomerIdOrderByAppointmentDateDesc(customer.getId());

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long appointmentId, AppointmentRequest appointmentRequest) {
        logger.info("Updating appointment with ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
        
        // Update appointment details if provided
        if (appointmentRequest.getAppointmentDate() != null) {
            appointment.setAppointmentDate(appointmentRequest.getAppointmentDate());
        }
        
        if (appointmentRequest.getEstimatedEndTime() != null) {
            appointment.setEstimatedEndTime(appointmentRequest.getEstimatedEndTime());
        }
        
        // Update employee if provided
        if (appointmentRequest.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(appointmentRequest.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            appointment.setEmployee(employee);
        }
        
        // Update timestamps with current time in the format: 2025-09-06 12:00:00
        LocalDateTime now = LocalDateTime.now();
        appointment.setUpdatedAt(now);
        
        // Update payment status and set payment received timestamp if payment is made
        if (appointmentRequest.getPaymentMethod() != null) {
            try {
                appointment.setPaymentMethod(Appointment.PaymentMethod.valueOf(appointmentRequest.getPaymentMethod().toUpperCase()));
                // Set payment received timestamp when payment method is updated
                appointment.setPaymentReceivedAt(now);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment method: " + appointmentRequest.getPaymentMethod());
            }
        }
        
        // Save updated appointment
        appointment = appointmentRepository.save(appointment);
        
        // Publish appointment updated event to Redis Stream
        publishAppointmentUpdatedEvent(appointment);
        
        logger.info("Appointment updated successfully with ID: {}", appointmentId);
        return mapToAppointmentResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String cancellationReason) {
        logger.info("Cancelling appointment with ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
        
        // Set cancellation details with proper timestamp format: 2025-09-06 12:00:00
        LocalDateTime now = LocalDateTime.now();
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancellationReason);
        appointment.setCancelledBy("Customer"); // or get from token
        appointment.setCancelledAt(now);
        appointment.setUpdatedAt(now);
        
        // Save cancelled appointment
        appointmentRepository.save(appointment);
        
        // Publish appointment cancelled event to Redis Stream
        publishAppointmentCancelledEvent(appointment);
        
        logger.info("Appointment cancelled successfully with ID: {}", appointmentId);
    }

    @Transactional
    public void markAppointmentAsCompleted(Long appointmentId) {
        logger.info("Marking appointment as completed with ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
        
        // Set completion details with proper timestamp format: 2025-09-06 12:00:00
        LocalDateTime now = LocalDateTime.now();
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setActualEndTime(now);
        appointment.setUpdatedAt(now);
        
        // Set actual start time if not already set
        if (appointment.getActualStartTime() == null) {
            appointment.setActualStartTime(appointment.getAppointmentDate());
        }
        
        // Save completed appointment
        appointmentRepository.save(appointment);
        
        logger.info("Appointment marked as completed with ID: {}", appointmentId);
    }

    @Transactional
    public void markAppointmentAsStarted(Long appointmentId) {
        logger.info("Marking appointment as started with ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
        
        // Set start details with proper timestamp format: 2025-09-06 12:00:00
        LocalDateTime now = LocalDateTime.now();
        appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
        appointment.setActualStartTime(now);
        appointment.setUpdatedAt(now);
        
        // Save started appointment
        appointmentRepository.save(appointment);
        
        logger.info("Appointment marked as started with ID: {}", appointmentId);
    }


        public AppointmentResponse getAppointmentByNumber(String token, String appointmentNumber) {
                // Get appointment by business key
                Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                                .orElseThrow(() -> new ResourceNotFoundException("Appointment number "+appointmentNumber));
                return mapToAppointmentResponse(appointment);
        }



    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .id(appointment.getId())
            .appointmentNumber(appointment.getAppointmentNumber())
            .customerId(appointment.getCustomer().getId())
            .customerName(appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName())
            .customerPhone(appointment.getCustomer().getPhoneNumber())
            .serviceId(appointment.getService().getId())
            .serviceName(appointment.getService().getName())
            .employeeId(appointment.getEmployee().getEmployeeId())
            .employeeName(appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName())
            .salonId(appointment.getSalon().getSalonId())
            .salonName(appointment.getSalon().getName())
            .appointmentDate(appointment.getAppointmentDate())
            .estimatedEndTime(appointment.getEstimatedEndTime())
            .actualStartTime(appointment.getActualStartTime())
            .actualEndTime(appointment.getActualEndTime())
            .status(appointment.getStatus())
            .paymentStatus(appointment.getPaymentStatus())
            .servicePrice(appointment.getServicePrice())
            .discountAmount(appointment.getDiscountAmount())
            .taxAmount(appointment.getTaxAmount())
            .totalAmount(appointment.getTotalAmount())
            .paidAmount(appointment.getPaidAmount())
            .paymentMethod(appointment.getPaymentMethod())
            .customerNotes(appointment.getCustomerNotes())
            .internalNotes(appointment.getInternalNotes())
            .cancellationReason(appointment.getCancellationReason())
            .cancelledBy(appointment.getCancelledBy())
            .cancelledAt(appointment.getCancelledAt())
            .reminderSent(appointment.getReminderSent())
            .confirmationSent(appointment.getConfirmationSent())
            .rating(appointment.getRating())
            .review(appointment.getReview())
            .reviewDate(appointment.getReviewDate())
            .createdDate(appointment.getCreatedAt())
            .lastModifiedDate(appointment.getUpdatedAt())
            .build();
    }


        /**
         * Cancels an appointment due to payment failure. Sets status to CANCELLED and reason to PAYMENTFAILED.
         */
        @Transactional
        public AppointmentResponse cancelAppointmentPaymentFailed(String token, Long appointmentId) {
                Customer customer = customerService.getCustomerFromToken(token);
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
                // Only allow the customer who booked to cancel
                if (!appointment.getCustomer().getId().equals(customer.getId())) {
                        throw new UnauthorizedException("You are not authorized to cancel this appointment");
                }
                
                String oldStatus = appointment.getStatus().toString();
                appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                appointment.setCancellationReason("PAYMENTFAILED");
                appointment.setCancelledAt(java.time.LocalDateTime.now());
                appointment = appointmentRepository.save(appointment);
                
                // Publish appointment cancelled event to Redis Stream
                publishAppointmentCancelledEvent(appointment);
                
                // Optionally: add logic to free up the timeslot if needed
                return mapToAppointmentResponse(appointment);
        }

    /**
     * Publishes appointment created event to Redis Stream for web backend notification
     */
    private void publishAppointmentCreatedEvent(Appointment appointment, List<Service> services, AppointmentRequest request) {
        try {
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointment_number", appointment.getAppointmentNumber());
            appointmentData.put("appointment_date", appointment.getAppointmentDate().toString());
            appointmentData.put("estimated_end_time", appointment.getEstimatedEndTime().toString());
            appointmentData.put("service_names", services.stream().map(Service::getName).collect(Collectors.toList()));
            appointmentData.put("service_price", appointment.getServicePrice().toString());
            appointmentData.put("total_amount", appointment.getTotalAmount().toString());
            appointmentData.put("payment_method", appointment.getPaymentMethod() != null ? appointment.getPaymentMethod().toString() : "");
            appointmentData.put("status", appointment.getStatus().toString());
            appointmentData.put("payment_status", appointment.getPaymentStatus().toString());
            appointmentData.put("booking_platform", "mobile_app");
            appointmentData.put("customer_name", appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName());
            appointmentData.put("customer_phone", appointment.getCustomer().getPhoneNumber());
            appointmentData.put("employee_name", appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName());

            // Publish to mobile appointment stream
            streamPublisher.publishAppointmentCreated(
                appointment.getSalon().getId(),
                appointment.getBranchId(),
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getEmployee().getEmployeeId(),
                appointmentData
            );
            // Do NOT publish to barber notification stream for new appointment creation
        } catch (Exception e) {
            logger.error("Failed to publish appointment created event for appointment {}: {}", 
                        appointment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Publishes appointment updated event to Redis Stream for web backend notification
     */
    private void publishAppointmentUpdatedEvent(Appointment appointment) {
        try {
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointment_number", appointment.getAppointmentNumber());
            appointmentData.put("appointment_date", appointment.getAppointmentDate().toString());
            appointmentData.put("estimated_end_time", appointment.getEstimatedEndTime() != null ? appointment.getEstimatedEndTime().toString() : "");
            appointmentData.put("actual_start_time", appointment.getActualStartTime() != null ? appointment.getActualStartTime().toString() : "");
            appointmentData.put("actual_end_time", appointment.getActualEndTime() != null ? appointment.getActualEndTime().toString() : "");
            appointmentData.put("status", appointment.getStatus().toString());
            appointmentData.put("payment_status", appointment.getPaymentStatus().toString());
            appointmentData.put("payment_received_at", appointment.getPaymentReceivedAt() != null ? appointment.getPaymentReceivedAt().toString() : "");
            appointmentData.put("created_at", appointment.getCreatedAt().toString());
            appointmentData.put("updated_at", appointment.getUpdatedAt().toString());

            streamPublisher.publishAppointmentUpdated(
                appointment.getSalon().getId(),
                appointment.getId(),
                "IN_PROGRESS", // old status (could be stored if needed)
                appointment.getStatus().toString() // new status
            );
        } catch (Exception e) {
            logger.error("Failed to publish appointment updated event for appointment {}: {}", 
                        appointment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Publishes appointment cancelled event to Redis Stream for web backend notification
     */
    private void publishAppointmentCancelledEvent(Appointment appointment) {
        try {
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointment_number", appointment.getAppointmentNumber());
            appointmentData.put("cancellation_reason", appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "");
            appointmentData.put("cancelled_by", appointment.getCancelledBy() != null ? appointment.getCancelledBy() : "");
            appointmentData.put("cancelled_at", appointment.getCancelledAt() != null ? appointment.getCancelledAt().toString() : "");
            appointmentData.put("status", appointment.getStatus().toString());
            appointmentData.put("updated_at", appointment.getUpdatedAt().toString());

            streamPublisher.publishAppointmentCancelled(
                appointment.getSalon().getId(),
                appointment.getId(),
                appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Customer cancellation"
            );
        } catch (Exception e) {
            logger.error("Failed to publish appointment cancelled event for appointment {}: {}", 
                        appointment.getId(), e.getMessage(), e);
        }
    }

        // Generate unique appointment number using id and date
        private String generateAppointmentNumber(Long id, LocalDateTime appointmentDate) {
                String datePrefix = appointmentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                return String.format("APT-%s-%04d", datePrefix, id);
        }

    /**
     * Get appointment activities by status
     * @param status The appointment status filter
     * @param customerId Optional customer ID filter
     * @return List of appointment activities
     */
    public List<AppointmentActivityResponse> getAppointmentActivities(Appointment.AppointmentStatus status, Long customerId) {
        logger.info("Fetching appointment activities for status: {} and customerId: {}", status, customerId);
        
        List<Appointment> appointments;
        
        if (customerId != null) {
            appointments = appointmentRepository.findByCustomerIdAndStatusWithDetails(customerId, status);
        } else {
            appointments = appointmentRepository.findByStatusWithDetails(status);
        }
        
        return appointments.stream()
            .map(this::mapToAppointmentActivityResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get appointment activities by multiple statuses
     * @param statuses List of appointment statuses to filter
     * @param customerId Optional customer ID filter
     * @return List of appointment activities
     */
    public List<AppointmentActivityResponse> getAppointmentActivitiesByStatuses(List<Appointment.AppointmentStatus> statuses, Long customerId) {
        logger.info("Fetching appointment activities for statuses: {} and customerId: {}", statuses, customerId);
        
        List<Appointment> appointments;
        
        if (customerId != null) {
            appointments = appointmentRepository.findByCustomerIdAndStatusInWithDetails(customerId, statuses);
        } else {
            appointments = appointmentRepository.findByStatusInWithDetails(statuses);
        }
        
        return appointments.stream()
            .map(this::mapToAppointmentActivityResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map Appointment entity to AppointmentActivityResponse DTO
     */
    private AppointmentActivityResponse mapToAppointmentActivityResponse(Appointment appointment) {
        // Get branch from the appointment entity directly
        Branch branch = appointment.getBranch();
        
        return AppointmentActivityResponse.builder()
            .appointmentId(appointment.getId())
            .appointmentNumber(appointment.getAppointmentNumber())
            .status(appointment.getStatus())
            .salonName(appointment.getSalon() != null ? appointment.getSalon().getName() : null)
            .branchName(branch != null ? branch.getBranchName() : null)
            .barberName(appointment.getEmployee() != null ? 
                appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName() : null)
            .latitude(branch != null ? branch.getLatitude() : null)
            .longitude(branch != null ? branch.getLongitude() : null)
            .appointmentDateTime(appointment.getAppointmentDate())
            .servicePrice(appointment.getServicePrice())
            .discountAmount(appointment.getDiscountAmount())
            .totalAmount(appointment.getTotalAmount())
            .paymentStatus(appointment.getPaymentStatus())
            .actualStartTime(appointment.getActualStartTime())
            .actualEndTime(appointment.getActualEndTime())
            .cancellationReason(appointment.getCancellationReason())
            .cancelledAt(appointment.getCancelledAt())
            .cancelledBy(appointment.getCancelledBy())
            .serviceName(appointment.getService() != null ? appointment.getService().getName() : null)
            .branchAddress(branch != null ? buildBranchAddress(branch) : null)
            .branchPhoneNumber(branch != null ? branch.getBranchPhoneNumber() : null)
            .build();
    }

    /**
     * Build branch address from branch coordinates
     */
    private String buildBranchAddress(Branch branch) {
        if (branch.getLatitude() != null && branch.getLongitude() != null) {
            return String.format("Lat: %s, Lng: %s", branch.getLatitude(), branch.getLongitude());
        }
        return "Location not available";
    }
}
