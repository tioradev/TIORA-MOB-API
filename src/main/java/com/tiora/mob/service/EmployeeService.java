package com.tiora.mob.service;



import com.tiora.mob.dto.response.BarberResponse;
import com.tiora.mob.dto.response.TimeSlotResponse;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.entity.Employee;
import com.tiora.mob.entity.Salon;
import com.tiora.mob.exception.ResourceNotFoundException;
import com.tiora.mob.repository.AppointmentRepository;
import com.tiora.mob.repository.BarberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;


    public BarberResponse getBarberById(Long barberId) {
        logger.info("getBarberById called with barberId: {}", barberId);
        // Find barber by ID
        Employee employee = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber" + "id " + barberId));

        // Map to response DTO
        BarberResponse response = mapToBarberResponse(employee);
        logger.info("getBarberById response: {}", response);
        return response;
    }


    /**
     * Get barber's actual specializations
     */
    private List<String> getBarberSpecializations(Employee employee) {
        List<String> specializations = employee.getSpecializations();

        if (specializations != null && !specializations.isEmpty()) {
            return specializations;
        }

        // Fallback: return some default specializations
        return Arrays.asList("General Services");
    }


    public List<BarberResponse> getBarbersBySalon(Long salonId) {
        logger.info("getBarbersBySalon called with salonId: {}", salonId);
        List<Employee> employees = barberRepository.findBySalonIdAndIsActiveTrue(salonId);
        List<BarberResponse> responses = employees.stream()
                .map(this::mapToBarberResponse)
                .collect(Collectors.toList());
        logger.info("getBarbersBySalon response count: {}", responses.size());
        return responses;
    }



    private BarberResponse mapToBarberResponse(Employee employee) {
        return BarberResponse.builder()
                .barberId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .specializations(employee.getSpecializations())
                .ratings(employee.getRatings())
                .profileImageUrl(employee.getProfileImageUrl())
                .build();
    }
}
