package com.tiora.mob.service;



import com.tiora.mob.dto.request.ProfileUpdateRequest;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.entity.Customer;
import com.tiora.mob.exception.ResourceNotFoundException;
import com.tiora.mob.exception.UnauthorizedException;
import com.tiora.mob.repository.AppointmentRepository;
import com.tiora.mob.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuthService authService;

    /**
     * Get customer profile details
     */
    public CustomerProfileResponse getCustomerProfile(String token) {
        logger.info("getCustomerProfile called with token: {}", token);
        Long customerId = authService.getCustomerIdFromToken(token);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        CustomerProfileResponse response = new CustomerProfileResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setGender(customer.getGender() != null ? customer.getGender().toString() : null);
        response.setMemberSince(customer.getCreatedAt());

        // Check if profile is complete
        boolean isComplete = StringUtils.hasText(customer.getFirstName()) &&
                StringUtils.hasText(customer.getLastName()) &&
                StringUtils.hasText(customer.getEmail());
        response.setProfileComplete(isComplete);

        // Get appointment stats
        List<Appointment> appointments = appointmentRepository.findByCustomerIdOrderByAppointmentDateDesc(customerId);
        response.setCompletedAppointments(
                (int) appointments.stream()
                        .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                        .count()
        );

        // Get last visit date
//        appointments.stream()
//                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
//                .max(Comparator.comparing(Appointment::getAppointmentDate))
//                .ifPresent(a -> response.setLastVisit(a.getAppointmentDate()));

        logger.info("getCustomerProfile response: {}", response);
        return response;
    }

    @Transactional
    public void updateCustomerProfile(String token, ProfileUpdateRequest request) {
        logger.info("updateCustomerProfile called with token: {}, request: {}", token, request);
        Long customerId = authService.getCustomerIdFromToken(token);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        logger.info("Customer profile updated for id: {}", customerId);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public Customer getCustomerFromToken(String token) {
        Long customerId = authService.getCustomerIdFromToken(token);
        return getCustomerById(customerId);
    }

    public Customer getCustomerByPhoneNumber(String phoneNumber){
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber).get();
        return customer;
    }
}