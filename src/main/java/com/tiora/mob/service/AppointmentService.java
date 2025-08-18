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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public AppointmentResponse createAppointment(String token, AppointmentRequest appointmentRequest) {
        logger.info("Creating appointment: customer phone={}, serviceIds={}, employeeId={}, salonId={}",
                appointmentRequest.getCustomerPhone(), appointmentRequest.getServiceIds(), appointmentRequest.getEmployeeId(), appointmentRequest.getSalonId());

        // Find or create customer
        Customer customer = customerService.getCustomerByPhoneNumber(appointmentRequest.getCustomerPhone());

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
        appointment.setAppointmentNumber(generateAppointmentNumber());
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
        appointment.setPaymentMethod(null); // Removed payment method
        appointment.setCustomerNotes(""); // Removed customer notes
        appointment.setInternalNotes(""); // Removed internal notes

        // Calculate total amount (service price - discount, no tax)
        BigDecimal total = totalServicePrice.subtract(appointment.getDiscountAmount());
        appointment.setTotalAmount(total);

        appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
        appointment.setPaymentStatus(Appointment.PaymentStatus.PENDING);

        appointment = appointmentRepository.save(appointment);
        logger.info("Appointment created successfully for customer={}, appointmentNumber={}", customer.getPhoneNumber(), appointment.getAppointmentNumber());
        return mapToAppointmentResponse(appointment) ;
    }

    public List<AppointmentResponse> getCustomerAppointments(String token) {
        Customer customer = customerService.getCustomerFromToken(token);

        List<Appointment> appointments = appointmentRepository
                .findByCustomerIdOrderByAppointmentDateDesc(customer.getId());

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(String token, Long appointmentId) {

        // Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment"+ "id "+appointmentId));

        return mapToAppointmentResponse(appointment);
    }



    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentNumber(appointment.getAppointmentNumber());
        response.setCustomerId(appointment.getCustomer().getId());
        response.setCustomerName(appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName());
        response.setCustomerPhone(appointment.getCustomer().getPhoneNumber());
        response.setServiceId(appointment.getService().getId());
        response.setServiceName(appointment.getService().getName());
        response.setEmployeeId(appointment.getEmployee().getEmployeeId());
        response.setEmployeeName(appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName());
        response.setSalonId(appointment.getSalon().getSalonId());
        response.setSalonName(appointment.getSalon().getName());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setEstimatedEndTime(appointment.getEstimatedEndTime());
        response.setActualStartTime(appointment.getActualStartTime());
        response.setActualEndTime(appointment.getActualEndTime());
        response.setStatus(appointment.getStatus());
        response.setPaymentStatus(appointment.getPaymentStatus());
        response.setServicePrice(appointment.getServicePrice());
        response.setDiscountAmount(appointment.getDiscountAmount());
        response.setTaxAmount(appointment.getTaxAmount());
        response.setTotalAmount(appointment.getTotalAmount());
        response.setPaidAmount(appointment.getPaidAmount());
        response.setPaymentMethod(appointment.getPaymentMethod());
        response.setCustomerNotes(appointment.getCustomerNotes());
        response.setInternalNotes(appointment.getInternalNotes());
        response.setCancellationReason(appointment.getCancellationReason());
        response.setCancelledBy(appointment.getCancelledBy());
        response.setCancelledAt(appointment.getCancelledAt());
        response.setReminderSent(appointment.getReminderSent());
        response.setConfirmationSent(appointment.getConfirmationSent());
        response.setRating(appointment.getRating());
        response.setReview(appointment.getReview());
        response.setReviewDate(appointment.getReviewDate());
        response.setCreatedDate(appointment.getCreatedAt());
        response.setLastModifiedDate(appointment.getUpdatedAt());
        return response;
    }

    // Generate unique appointment number
    private String generateAppointmentNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = appointmentRepository.count() + 1;
        return String.format("APT-%s-%04d", datePrefix, count);
    }
}
