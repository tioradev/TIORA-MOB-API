
package com.tiora.mob.service;

import com.tiora.mob.dto.response.ServiceResponse;
import com.tiora.mob.dto.response.AvailableTimeSlotsResponse;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.entity.Employee;
import com.tiora.mob.entity.Service;
import com.tiora.mob.exception.ResourceNotFoundException;
import com.tiora.mob.repository.AppointmentRepository;
import com.tiora.mob.repository.EmployeeRepository;
import com.tiora.mob.repository.ServiceRepository;
import com.tiora.mob.util.TimeSlotUtil;
import org.springframework.beans.factory.annotation.Autowired;
// Use fully qualified name for annotation if needed
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.tiora.mob.entity.WorkingHours;

@org.springframework.stereotype.Service
public class AvailabilityService {
    @Autowired
    private com.tiora.mob.repository.EmployeeLeaveRepository employeeLeaveRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    public AvailableTimeSlotsResponse getAvailableTimeSlots(Long salonId, String serviceIds, Long barberId, String date, String customerGender) {
        java.time.LocalDate selectedDate = java.time.LocalDate.parse(date);
        // Check if barber is on APPROVED leave for selected date
        java.util.List<com.tiora.mob.entity.EmployeeLeave> leaves = employeeLeaveRepository.findByEmployee_EmployeeId(barberId);
        boolean isOnLeave = leaves.stream().anyMatch(l ->
            l.getStatus() == com.tiora.mob.entity.EmployeeLeave.LeaveStatus.APPROVED &&
            ( !selectedDate.isBefore(l.getStartDate()) && !selectedDate.isAfter(l.getEndDate()) )
        );
        if (isOnLeave) {
            throw new com.tiora.mob.exception.ResourceNotFoundException("You selected Barber is not available today. Please select another Day or change the barber");
        }
        Employee employee = employeeRepository.findByEmployeeIdAndSalonId(barberId, salonId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        // Parse serviceIds and sum durations
        int totalDuration = 0;
        String[] serviceIdArr = serviceIds.split(",");
        for (String sid : serviceIdArr) {
            Long serviceId = Long.parseLong(sid.trim());
            com.tiora.mob.entity.Service service = serviceRepository.findByIdAndSalonId(serviceId, salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
            totalDuration += service.getDurationMinutes();
        }
        List<Appointment> appointments = appointmentRepository
            .findByEmployeeIdAndDate(barberId, LocalDate.parse(date));
        // Exclude CANCELLED appointments so those timeslots are available
        List<Appointment> activeAppointments = appointments.stream()
            .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
            .collect(java.util.stream.Collectors.toList());
        WorkingHours workingHours = employee.getWorkingHoursForDate(LocalDate.parse(date));
        List<AvailableTimeSlotsResponse.TimeSlot> slots = com.tiora.mob.util.TimeSlotUtil.calculateAvailableSlots(
            workingHours, activeAppointments, totalDuration
        );
        return new AvailableTimeSlotsResponse(
            slots,
            employee.getEmployeeId(),
            employee.getFirstName() + " " + employee.getLastName(),
            totalDuration
        );
    }

    /**
     * Get available services for a salon, filtered by genderAvailability.
     * @param salonId the salon id
     * @param gender MALE, FEMALE, or BOTH (case-insensitive, null/empty means BOTH)
     * @return list of ServiceResponse
     */
    // Remove duplicate methods and use builder for ServiceResponse
    public List<ServiceResponse> getAvailableServicesBySalonId(Long salonId, String gender) {
        String genderNorm = (gender == null || gender.isBlank()) ? "BOTH" : gender.trim().toUpperCase();
        List<com.tiora.mob.entity.Service> services = serviceRepository.findAllWithSalon()
                .stream()
                .filter(s -> s.getSalon() != null && s.getSalon().getSalonId().equals(salonId))
                .filter(s -> s.getStatus() == com.tiora.mob.entity.Service.ServiceStatus.ACTIVE)
                .filter(s -> {
                    String avail = s.getGenderAvailability() != null ? s.getGenderAvailability().name() : "BOTH";
                    return avail.equals(genderNorm);
                })
                .collect(Collectors.toList());
        return services.stream().map(this::toServiceResponse).collect(Collectors.toList());
    }

    private ServiceResponse toServiceResponse(com.tiora.mob.entity.Service service) {
        return ServiceResponse.builder()
            .id(service.getId())
            .name(service.getName())
            .description(service.getDescription())
            .category(service.getCategory() != null ? service.getCategory().name() : null)
            .durationMinutes(service.getDurationMinutes())
            .price(service.getPrice())
            .imageUrl(service.getImageUrl())
            .isPopular(service.getIsPopular())
            .status(service.getStatus() != null ? service.getStatus().name() : null)
            .genderAvailability(service.getGenderAvailability() != null ? service.getGenderAvailability().name() : null)
            .discountPercentage(service.getDiscountPercentage())
            .createdAt(service.getCreatedAt())
            .updatedAt(service.getUpdatedAt())
            .build();
    }



}
