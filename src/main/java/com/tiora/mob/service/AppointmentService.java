
package com.tiora.mob.service;


import com.tiora.mob.dto.request.AppointmentRequest;
import com.tiora.mob.dto.response.AppointmentResponse;
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

        appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
        appointment.setPaymentStatus(Appointment.PaymentStatus.PENDING);

        // Set a temporary appointment number to satisfy NOT NULL constraint
        appointment.setAppointmentNumber("TEMP-" + System.currentTimeMillis());
        appointment = appointmentRepository.save(appointment);
        // Generate the final appointment number using id and date
        String appointmentNumber = generateAppointmentNumber(appointment.getId(), appointment.getAppointmentDate());
        appointment.setAppointmentNumber(appointmentNumber);
        appointment = appointmentRepository.save(appointment);
        
        // Publish appointment created event to Redis Stream
        publishAppointmentCreatedEvent(appointment, services, appointmentRequest);
        
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
                streamPublisher.publishAppointmentCancelled(
                    appointment.getSalon().getId(),
                    appointment.getId(),
                    "PAYMENTFAILED"
                );
                
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

            streamPublisher.publishAppointmentCreated(
                appointment.getSalon().getId(),
                appointment.getBranchId(),
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getEmployee().getEmployeeId(),
                appointmentData
            );
        } catch (Exception e) {
            logger.error("Failed to publish appointment created event for appointment {}: {}", 
                        appointment.getId(), e.getMessage(), e);
        }
    }

        // Generate unique appointment number using id and date
        private String generateAppointmentNumber(Long id, LocalDateTime appointmentDate) {
                String datePrefix = appointmentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                return String.format("APT-%s-%04d", datePrefix, id);
        }
}
