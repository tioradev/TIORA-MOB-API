package com.tiora.mob.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tiora.mob.dto.response.AvailableBarberResponse;
import com.tiora.mob.dto.response.AvailableBarbersResponse;
import com.tiora.mob.dto.response.AvailableDatesResponse;
import com.tiora.mob.dto.response.AvailableTimeSlotsResponse;
import com.tiora.mob.entity.*;
import com.tiora.mob.repository.AppointmentRepository;
import com.tiora.mob.repository.EmployeeRepository;
import com.tiora.mob.repository.SalonRepository;
import com.tiora.mob.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class AvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Get available dates for selected services
     */
    public AvailableDatesResponse getAvailableDates(List<Long> serviceIds, Long salonId, Integer daysAhead) {
        logger.info("Getting available dates for services: {} in salon: {}", serviceIds, salonId);
        
        try {
            // Validate salon exists
            Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found with ID: " + salonId));
            
            // Calculate total duration
            List<Service> services = serviceRepository.findAllById(serviceIds);
            int totalDuration = services.stream()
                .mapToInt(Service::getDurationMinutes)
                .sum();
            
            // Generate available dates (simplified logic)
            List<LocalDate> availableDates = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            for (int i = 1; i <= daysAhead; i++) {
                LocalDate date = today.plusDays(i);
                // Skip Sundays (simplified business rule)
                if (date.getDayOfWeek().getValue() != 7) {
                    availableDates.add(date);
                }
            }
            
            AvailableDatesResponse response = new AvailableDatesResponse();
            response.setAvailableDates(availableDates);
            response.setTotalDurationMinutes(totalDuration);
            response.setSalonOpeningTime("09:00");
            response.setSalonClosingTime("18:00");
            response.setMessage("Available dates retrieved successfully");
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting available dates: {}", e.getMessage());
            throw new RuntimeException("Failed to get available dates: " + e.getMessage());
        }
    }

    /**
     * Get available barbers for selected services and date
     */
    public AvailableBarbersResponse getAvailableBarbers(List<Long> serviceIds, LocalDate date, Long salonId, String customerGender) {
        logger.info("Getting available barbers for services: {} on date: {} in salon: {} for customer gender: {}", serviceIds, date, salonId, customerGender);
        
        try {
            // 1. Validate input
            if (serviceIds == null || serviceIds.isEmpty()) {
                return new AvailableBarbersResponse("Service IDs cannot be empty", false);
            }
            
            if (date.isBefore(LocalDate.now())) {
                return new AvailableBarbersResponse("Cannot get barbers for past dates", false);
            }
            
            // 2. Validate services exist
            List<Service> services = serviceRepository.findAllById(serviceIds);
            if (services.size() != serviceIds.size()) {
                return new AvailableBarbersResponse("One or more services not found", false);
            }
            
            // 3. Parse customer gender
            Employee.Gender customerGenderEnum = parseCustomerGender(customerGender);
            
            // 4. Check if salon is open on this date
            WorkingHours salonHours = getSalonWorkingHours(salonId, date.getDayOfWeek());
            if (salonHours == null || !salonHours.isOpen()) {
                logger.info("Salon is closed on {}", date.getDayOfWeek());
                return new AvailableBarbersResponse("Salon is closed on " + date.getDayOfWeek().toString(), true);
            }
            
            // 5. Get all active employees in the salon who can provide services (barbers and stylists only)
            List<Employee> allEmployees = employeeRepository.findBySalonSalonIdAndStatus(salonId, Employee.EmployeeStatus.ACTIVE);
            
            // Filter employees to only include service providers (exclude receptionists, cleaners, etc.)
            List<Employee> serviceProviders = allEmployees.stream()
                .filter(employee -> isServiceProvider(employee.getRole()))
                .collect(Collectors.toList());
            
            if (serviceProviders.isEmpty()) {
                logger.info("No service providers (barbers/stylists) found in salon {}", salonId);
                return new AvailableBarbersResponse("No service providers (barbers/stylists) found in this salon", true);
            }
            
            List<AvailableBarberResponse> availableBarbers = new ArrayList<>();
            
            logger.info("Found {} service providers in salon {} to check for gender compatibility", serviceProviders.size(), salonId);
            
            for (Employee barber : serviceProviders) {
                logger.debug("Checking barber: {} {} (ID: {}, serves_gender: {})", 
                            barber.getFirstName(), barber.getLastName(), barber.getEmployeeId(), barber.getServesGender());
                
                // 6. Check if barber serves customers of the specified gender
                if (!canBarberServeGender(barber, customerGenderEnum)) {
                    logger.debug("Barber {} does not serve {} customers - EXCLUDED", barber.getEmployeeId(), customerGender);
                    continue;
                }
                
                logger.debug("Barber {} can serve {} customers - proceeding with other checks", barber.getEmployeeId(), customerGender);
                
                // 7. Check if barber is working on this date
                WorkingHours barberHours = getBarberWorkingHours(barber, date.getDayOfWeek());
                if (barberHours == null || !barberHours.isOpen()) {
                    logger.debug("Barber {} is not working on {}", barber.getEmployeeId(), date.getDayOfWeek());
                    continue;
                }
                
                // 8. Check if barber has any availability (optional - for performance)
                if (!hasAnyAvailability(barber, date, services)) {
                    logger.debug("Barber {} has no availability on {}", barber.getEmployeeId(), date);
                    continue;
                }
                
                // 9. Build response for available barber
                AvailableBarberResponse response = buildBarberResponse(barber, serviceIds);
                availableBarbers.add(response);
            }
            
            // 10. Build final response with appropriate message
            if (availableBarbers.isEmpty()) {
                if (serviceProviders.size() == 1) {
                    return new AvailableBarbersResponse("The available barber does not serve " + customerGender + " customers or is fully booked on this date", true);
                } else {
                    return new AvailableBarbersResponse("No barbers are available who serve " + customerGender + " customers on this date. All suitable barbers are either fully booked or not working.", true);
                }
            }
            
            AvailableBarbersResponse finalResponse = new AvailableBarbersResponse(availableBarbers, serviceProviders.size(), allEmployees.size());
            finalResponse.setMessage(String.format("Found %d available barber%s who serve %s customers for the selected services", 
                                   availableBarbers.size(), availableBarbers.size() == 1 ? "" : "s", customerGender));
            
            logger.info("Found {} available barbers out of {} service providers ({} total employees) for {} customers", 
                       availableBarbers.size(), serviceProviders.size(), allEmployees.size(), customerGender);
            return finalResponse;
            
        } catch (Exception e) {
            logger.error("Error getting available barbers: {}", e.getMessage());
            return new AvailableBarbersResponse("Failed to get available barbers: " + e.getMessage(), false);
        }
    }

    /**
     * Get available time slots for a barber on a specific date
     */
    public AvailableTimeSlotsResponse getAvailableTimeSlots(Long barberId, List<Long> serviceIds, LocalDate date, Long salonId, String customerGender) {
        logger.info("=== Getting available time slots for barber: {} on date: {} ({}) for services: {} for customer gender: {} ===", 
                   barberId, date, date.getDayOfWeek(), serviceIds, customerGender);
        
        try {
            // 1. Validate barber exists
            Employee barber = employeeRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found with ID: " + barberId));
            
            logger.info("Found barber: {} {} (ID: {})", barber.getFirstName(), barber.getLastName(), barberId);
            logger.info("Barber weekly schedule JSON: '{}'", barber.getEmployeeWeeklySchedule());
            
            // 2. Validate services exist
            List<Service> services = serviceRepository.findAllById(serviceIds);
            if (services.size() != serviceIds.size()) {
                throw new RuntimeException("One or more services not found");
            }
            
            // 3. Parse customer gender and check if barber can serve this customer
            Employee.Gender customerGenderEnum = parseCustomerGender(customerGender);
            if (!canBarberServeGender(barber, customerGenderEnum)) {
                logger.info("Barber {} cannot serve {} customers", barberId, customerGender);
                return createEmptyResponse(barberId, barber, 0, "This barber does not serve " + customerGender + " customers");
            }
            
            // 4. Check if barber is a service provider (not receptionist, cleaner, etc.)
            if (!isServiceProvider(barber.getRole())) {
                logger.info("Employee {} is not a service provider (role: {})", barberId, barber.getRole());
                return createEmptyResponse(barberId, barber, 0, "This employee is not a service provider");
            }
            
            // 5. Calculate total duration + buffer
            int totalDuration = services.stream()
                .mapToInt(Service::getDurationMinutes)
                .sum();
            int bufferTime = 15; // 15 minutes buffer
            int totalTimeNeeded = totalDuration + bufferTime;
            
            // 6. Check salon operating hours for this date
            WorkingHours salonHours = getSalonWorkingHours(salonId, date.getDayOfWeek());
            if (salonHours == null || !salonHours.isOpen()) {
                return createEmptyResponse(barberId, barber, totalDuration, "Salon closed on this date");
            }
            
            // 7. Get barber's working hours for this date
            WorkingHours barberHours = getBarberWorkingHours(barber, date.getDayOfWeek());
            logger.info("CRITICAL DEBUG - Barber {} working hours for {}: {}", 
                       barberId, date.getDayOfWeek(), 
                       barberHours == null ? "NULL (not working)" : 
                       (barberHours.isOpen() ? 
                        String.format("OPEN from %s to %s", barberHours.getStartTime(), barberHours.getEndTime()) :
                        "CLOSED"));
            
            if (barberHours == null || !barberHours.isOpen()) {
                logger.info("Barber {} is not available on {} - barberHours: {}", 
                           barberId, date.getDayOfWeek(), 
                           barberHours == null ? "null" : "closed");
                return createEmptyResponse(barberId, barber, totalDuration, "Barber not available on this date");
            }
            
            // 8. Calculate effective working window
            LocalTime workStart = salonHours.getStartTime().isAfter(barberHours.getStartTime()) 
                ? salonHours.getStartTime() : barberHours.getStartTime();
            LocalTime workEnd = salonHours.getEndTime().isBefore(barberHours.getEndTime()) 
                ? salonHours.getEndTime() : barberHours.getEndTime();
            
            logger.debug("Effective working window for barber {} on {}: {} - {} (salon: {} - {}, barber: {} - {})", 
                        barberId, date.getDayOfWeek(), workStart, workEnd,
                        salonHours.getStartTime(), salonHours.getEndTime(),
                        barberHours.getStartTime(), barberHours.getEndTime());
            
            logger.info("FINAL WORKING WINDOW - Barber {}: {} to {} on {}", 
                       barberId, workStart, workEnd, date.getDayOfWeek());
            
            // 9. Get existing appointments (sorted by start time)
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            List<Appointment> existingAppointments = appointmentRepository
                .findByEmployeeAndAppointmentDateBetweenAndStatusNot(
                    barber, startOfDay, endOfDay, Appointment.AppointmentStatus.CANCELLED);
            
            // Sort appointments by start time
            existingAppointments.sort((a1, a2) -> a1.getAppointmentDate().compareTo(a2.getAppointmentDate()));
            
            logger.info("SLOT GENERATION DEBUG - Found {} existing appointments for barber {} on {}", 
                       existingAppointments.size(), barberId, date);
            
            for (int i = 0; i < existingAppointments.size(); i++) {
                Appointment apt = existingAppointments.get(i);
                LocalTime aptStart = apt.getAppointmentDate().toLocalTime();
                LocalTime aptEnd = apt.getEstimatedEndTime() != null 
                    ? apt.getEstimatedEndTime().toLocalTime() 
                    : aptStart.plusMinutes(60);
                logger.info("SLOT GENERATION DEBUG - Appointment {}: {} - {} (ID: {})", 
                           i + 1, aptStart, aptEnd, apt.getId());
            }
            
            // 10. Find available continuous time blocks
            List<AvailableTimeSlotsResponse.TimeSlot> availableSlots = 
                findAvailableTimeBlocks(workStart, workEnd, existingAppointments, totalTimeNeeded, totalDuration, date);
            
            // 11. Build response
            AvailableTimeSlotsResponse response = new AvailableTimeSlotsResponse();
            response.setBarberId(barberId);
            response.setBarberName(barber.getFirstName() + " " + barber.getLastName());
            response.setTotalDurationMinutes(totalDuration);
            response.setAvailableSlots(availableSlots);
            
            logger.info("Found {} available slots for barber {}", availableSlots.size(), barberId);
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting available time slots: {}", e.getMessage());
            throw new RuntimeException("Failed to get available time slots: " + e.getMessage());
        }
    }


    /**
     * Helper method to parse comma-separated service IDs
     */
    public List<Long> parseServiceIds(String serviceIds) {
        try {
            return Arrays.stream(serviceIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid service IDs format: " + serviceIds);
        }
    }

    // ===== HELPER METHODS FOR IMPROVED AVAILABILITY CHECKING =====
    
    /**
     * Inner class to represent working hours
     */
    private static class WorkingHours {
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean open;
        
        public WorkingHours(LocalTime startTime, LocalTime endTime, boolean open) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.open = open;
        }
        
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public boolean isOpen() { return open; }
    }
    
    /**
     * Get salon working hours for a specific day
     */
    private WorkingHours getSalonWorkingHours(Long salonId, DayOfWeek dayOfWeek) {
        // Default salon hours - you can enhance this to read from database
        // For now, using standard business hours
        WorkingHours salonHours;
        switch (dayOfWeek) {
            case SUNDAY:
                salonHours = new WorkingHours(LocalTime.of(10, 0), LocalTime.of(16, 0), true); // Sunday: 10 AM - 4 PM
                break;
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                salonHours = new WorkingHours(LocalTime.of(9, 0), LocalTime.of(18, 0), true); // Weekdays: 9 AM - 6 PM
                break;
            case SATURDAY:
                salonHours = new WorkingHours(LocalTime.of(8, 0), LocalTime.of(19, 0), true); // Saturday: 8 AM - 7 PM
                break;
            default:
                salonHours = new WorkingHours(LocalTime.of(9, 0), LocalTime.of(18, 0), false);
                break;
        }
        
        logger.debug("Salon working hours for {}: {} - {} (open: {})", 
                    dayOfWeek, salonHours.getStartTime(), salonHours.getEndTime(), salonHours.isOpen());
        
        return salonHours;
    }
    
    /**
     * Get barber working hours for a specific day
     */
    private WorkingHours getBarberWorkingHours(Employee barber, DayOfWeek dayOfWeek) {
        // Try to parse employee weekly schedule if available
        String weeklySchedule = barber.getEmployeeWeeklySchedule();
        
        logger.info("SCHEDULE PARSING - Barber {}: Parsing schedule for {}", 
                   barber.getEmployeeId(), dayOfWeek);
        logger.info("SCHEDULE JSON: '{}'", weeklySchedule);
        
        if (weeklySchedule != null && !weeklySchedule.trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode scheduleNode = mapper.readTree(weeklySchedule);
                
                // Convert DayOfWeek to lowercase string (monday, tuesday, etc.)
                String dayKey = dayOfWeek.name().toLowerCase();
                logger.info("SCHEDULE PARSING - Looking for day key: '{}'", dayKey);
                
                JsonNode dayNode = scheduleNode.get(dayKey);
                logger.info("SCHEDULE PARSING - Day node for '{}': {}", dayKey, 
                           dayNode == null ? "NOT FOUND" : dayNode.toString());
                
                if (dayNode != null) {
                    // Check if the day is available (has both start and end times)
                    JsonNode availableNode = dayNode.get("available");
                    JsonNode startNode = dayNode.get("start");
                    JsonNode endNode = dayNode.get("end");
                    
                    logger.info("SCHEDULE PARSING - available: {}, start: {}, end: {}", 
                               availableNode != null ? availableNode.asBoolean() : "null",
                               startNode != null ? startNode.asText() : "null",
                               endNode != null ? endNode.asText() : "null");
                    
                    // If available is explicitly false, barber is not working
                    if (availableNode != null && !availableNode.asBoolean()) {
                        logger.info("SCHEDULE RESULT - Barber {} is NOT AVAILABLE on {} (available=false)", 
                                   barber.getEmployeeId(), dayKey);
                        return null; // Return null instead of false working hours
                    }
                    
                    // If start and end times are provided, use them
                    if (startNode != null && endNode != null && 
                        !startNode.asText().isEmpty() && !endNode.asText().isEmpty()) {
                        try {
                            LocalTime startTime = LocalTime.parse(startNode.asText());
                            LocalTime endTime = LocalTime.parse(endNode.asText());
                            
                            logger.info("SCHEDULE PARSING - Parsed times: {} to {}", startTime, endTime);
                            
                            // Validate that start time is before end time
                            if (startTime.isBefore(endTime)) {
                                logger.info("SCHEDULE RESULT - Barber {} working hours on {}: {} - {} ✓", 
                                           barber.getEmployeeId(), dayKey, startTime, endTime);
                                
                                return new WorkingHours(startTime, endTime, true);
                            } else {
                                logger.warn("SCHEDULE ERROR - Invalid working hours for barber {} on {}: start={} is not before end={}", 
                                           barber.getEmployeeId(), dayKey, startTime, endTime);
                                return null; // Return null for invalid times
                            }
                        } catch (Exception timeParseException) {
                            logger.warn("SCHEDULE ERROR - Error parsing time for barber {} on {}: start={}, end={} - {}", 
                                       barber.getEmployeeId(), dayKey, startNode.asText(), endNode.asText(), timeParseException.getMessage());
                            return null; // Return null for parse errors
                        }
                    } else {
                        logger.info("SCHEDULE ERROR - Barber {} has incomplete schedule for {}: start={}, end={}", 
                                   barber.getEmployeeId(), dayKey, 
                                   startNode != null ? startNode.asText() : "null", 
                                   endNode != null ? endNode.asText() : "null");
                        return null; // Return null for incomplete schedule
                    }
                } else {
                    logger.info("SCHEDULE ERROR - No schedule entry found for barber {} on {}", barber.getEmployeeId(), dayKey);
                    return null; // Return null if no entry for this day
                }
                
            } catch (Exception e) {
                logger.warn("SCHEDULE ERROR - Error parsing weekly schedule for barber {}: {}", barber.getEmployeeId(), e.getMessage());
                return null; // Return null for JSON parsing errors
            }
        } else {
            logger.info("SCHEDULE ERROR - Barber {} has no weekly schedule", barber.getEmployeeId());
            return null; // Return null if no weekly schedule
        }
    }
    
    /**
     * Create empty response when no slots are available
     */
    private AvailableTimeSlotsResponse createEmptyResponse(Long barberId, Employee barber, int totalDuration, String message) {
        AvailableTimeSlotsResponse response = new AvailableTimeSlotsResponse();
        response.setBarberId(barberId);
        response.setBarberName(barber.getFirstName() + " " + barber.getLastName());
        response.setTotalDurationMinutes(totalDuration);
        response.setAvailableSlots(new ArrayList<>());
        
        logger.info("No available slots for barber {}: {}", barberId, message);
        return response;
    }
    
    /**
     * Find available time blocks efficiently
     */
    private List<AvailableTimeSlotsResponse.TimeSlot> findAvailableTimeBlocks(
            LocalTime workStart, LocalTime workEnd, List<Appointment> existingAppointments, 
            int totalTimeNeeded, int displayDuration, LocalDate date) {
        
        List<AvailableTimeSlotsResponse.TimeSlot> availableSlots = new ArrayList<>();
        LocalTime currentTime = workStart;
        
        logger.info("SLOT GENERATION DEBUG - Finding time blocks between {} and {} for date {}", workStart, workEnd, date);
        logger.info("SLOT GENERATION DEBUG - Initial currentTime set to workStart: {}", currentTime);
        
        // Validate working hours make sense
        if (workStart.isAfter(workEnd) || workStart.equals(workEnd)) {
            logger.warn("Invalid working hours: start={}, end={}", workStart, workEnd);
            return availableSlots;
        }
        
        // If it's today, make sure we don't show past time slots
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            logger.info("SLOT GENERATION DEBUG - Today's date detected. Current time: {}, Work start: {}", now, workStart);
            
            if (now.isAfter(workStart)) {
                // Round up to next 15-minute interval from current time
                int currentMinute = now.getMinute();
                int roundedMinute = ((currentMinute / 15) + 1) * 15;
                LocalTime adjustedTime;
                if (roundedMinute >= 60) {
                    adjustedTime = now.plusHours(1).withMinute(0);
                } else {
                    adjustedTime = now.withMinute(roundedMinute).withSecond(0).withNano(0);
                }
                
                logger.info("SLOT GENERATION DEBUG - Current time {} is after work start {}, adjusted to {}", now, workStart, adjustedTime);
                
                // Use the later of adjusted time or work start
                currentTime = adjustedTime.isAfter(workStart) ? adjustedTime : workStart;
                logger.info("SLOT GENERATION DEBUG - Final currentTime for today: {}", currentTime);
            } else {
                logger.info("SLOT GENERATION DEBUG - Current time {} is before work start {}, using work start", now, workStart);
                currentTime = workStart;
            }
        }
        
        for (Appointment appointment : existingAppointments) {
            LocalTime appointmentStart = appointment.getAppointmentDate().toLocalTime();
            LocalTime appointmentEnd = appointment.getEstimatedEndTime() != null 
                ? appointment.getEstimatedEndTime().toLocalTime() 
                : appointmentStart.plusMinutes(60); // Default 1 hour if no end time
            
            // CRITICAL FIX: Validate appointment times - if end time is before start time, it's invalid data
            if (appointmentEnd.isBefore(appointmentStart)) {
                logger.warn("SLOT GENERATION WARNING - Invalid appointment found (ID: {}): {} - {} (end before start). Using default 1-hour duration.", 
                           appointment.getId(), appointmentStart, appointmentEnd);
                appointmentEnd = appointmentStart.plusMinutes(60); // Fix with 1-hour default
            }
            
            // Also ensure appointment times are within working hours to avoid weird edge cases
            if (appointmentStart.isBefore(workStart) || appointmentEnd.isAfter(workEnd.plusHours(2))) {
                logger.warn("SLOT GENERATION WARNING - Appointment (ID: {}) outside reasonable hours: {} - {}. Adjusting to working hours.", 
                           appointment.getId(), appointmentStart, appointmentEnd);
                
                // If appointment starts before work, move it to work start
                if (appointmentStart.isBefore(workStart)) {
                    Duration originalDuration = Duration.between(appointmentStart, appointmentEnd);
                    appointmentStart = workStart;
                    appointmentEnd = appointmentStart.plus(originalDuration);
                }
                
                // If appointment ends way after work (more than 2 hours), cap it
                if (appointmentEnd.isAfter(workEnd.plusHours(2))) {
                    appointmentEnd = appointmentStart.plusMinutes(60); // Default 1 hour
                }
            }
            
            logger.info("SLOT GENERATION DEBUG - Processing appointment: {} - {}, currentTime before: {}", 
                       appointmentStart, appointmentEnd, currentTime);
            
            // Check gap before this appointment
            long gapMinutes = Duration.between(currentTime, appointmentStart).toMinutes();
            
            logger.info("SLOT GENERATION DEBUG - Gap before appointment: {} minutes", gapMinutes);
            
            if (gapMinutes >= totalTimeNeeded) {
                // Generate slots in this gap
                logger.info("SLOT GENERATION DEBUG - Generating slots in gap from {} to {}", currentTime, appointmentStart);
                List<AvailableTimeSlotsResponse.TimeSlot> slotsInGap = generateSlotsInRange(currentTime, appointmentStart, totalTimeNeeded, displayDuration);
                availableSlots.addAll(slotsInGap);
                logger.info("SLOT GENERATION DEBUG - Added {} slots in gap", slotsInGap.size());
            } else {
                logger.info("SLOT GENERATION DEBUG - Gap too small ({} minutes), skipping", gapMinutes);
            }
            
            // Move current time to end of this appointment
            currentTime = appointmentEnd;
            logger.info("SLOT GENERATION DEBUG - Updated currentTime to appointment end: {}", currentTime);
        }
        
        // Check final gap after last appointment
        long finalGapMinutes = Duration.between(currentTime, workEnd).toMinutes();
        logger.info("SLOT GENERATION DEBUG - Final gap from {} to {}: {} minutes", currentTime, workEnd, finalGapMinutes);
        
        if (finalGapMinutes >= totalTimeNeeded) {
            logger.info("SLOT GENERATION DEBUG - Generating final slots from {} to {}", currentTime, workEnd);
            List<AvailableTimeSlotsResponse.TimeSlot> finalSlots = generateSlotsInRange(currentTime, workEnd, totalTimeNeeded, displayDuration);
            availableSlots.addAll(finalSlots);
            logger.info("SLOT GENERATION DEBUG - Added {} final slots", finalSlots.size());
        } else {
            logger.info("SLOT GENERATION DEBUG - Final gap too small ({} minutes), no final slots", finalGapMinutes);
        }
        
        logger.info("SLOT GENERATION DEBUG - Generated {} total available slots for date {}", availableSlots.size(), date);
        return availableSlots;
    }
    
    /**
     * Generate slots within a time range
     */
    private List<AvailableTimeSlotsResponse.TimeSlot> generateSlotsInRange(
            LocalTime start, LocalTime end, int totalTimeNeeded, int displayDuration) {
        
        List<AvailableTimeSlotsResponse.TimeSlot> slots = new ArrayList<>();
        LocalTime current = start;
        int intervalMinutes = 15; // 15-minute intervals
        
        logger.info("SLOT GENERATION DEBUG - generateSlotsInRange called with: start={}, end={}, totalTimeNeeded={}, displayDuration={}", 
                    start, end, totalTimeNeeded, displayDuration);
        
        // Validate input parameters
        if (start.isAfter(end) || start.equals(end)) {
            logger.warn("Invalid time range for slot generation: start={}, end={}", start, end);
            return slots;
        }
        
        if (totalTimeNeeded <= 0 || displayDuration <= 0) {
            logger.warn("Invalid duration parameters: totalTimeNeeded={}, displayDuration={}", totalTimeNeeded, displayDuration);
            return slots;
        }
        
        while (current.plusMinutes(totalTimeNeeded).isBefore(end) || 
               current.plusMinutes(totalTimeNeeded).equals(end)) {
            
            AvailableTimeSlotsResponse.TimeSlot slot = new AvailableTimeSlotsResponse.TimeSlot();
            slot.setStartTime(current);
            slot.setEndTime(current.plusMinutes(displayDuration)); // Show actual service duration (without buffer)
            slot.setIsAvailable(true);
            slot.setUnavailableReason(null);
            
            slots.add(slot);
            logger.debug("Generated slot: {} - {}", current, current.plusMinutes(displayDuration));
            
            current = current.plusMinutes(intervalMinutes);
        }
        
        logger.debug("Generated {} slots in range {} - {}", slots.size(), start, end);
        return slots;
    }
    
    // ===== ADDITIONAL HELPER METHODS FOR IMPROVED BARBER AVAILABILITY =====
    
    /**
     * Build barber response with real data from database
     */
    private AvailableBarberResponse buildBarberResponse(Employee barber, List<Long> serviceIds) {
        AvailableBarberResponse response = new AvailableBarberResponse();
        response.setBarberId(barber.getEmployeeId());
        response.setName(barber.getFirstName() + " " + barber.getLastName());
        response.setImageUrl(barber.getProfileImageUrl());
        
        // Get actual specializations from barber entity
        List<String> specializations = getBarberSpecializations(barber);
        response.setSpecialties(specializations);
        
        // Get actual ratings and experience
        response.setRatings(getBarberRating(barber.getEmployeeId()));
        response.setExperienceYears(barber.getExperience() != null ? barber.getExperience() : 0);
        response.setCanPerformServices(true); // Already validated above
        
        // Set serves gender
        response.setServesGender(barber.getServesGender() != null ? barber.getServesGender().toString() : "BOTH");
        
        return response;
    }
    
    /**
     * Get barber's actual specializations
     */
    private List<String> getBarberSpecializations(Employee barber) {
        List<String> specializations = barber.getSpecializations();
        
        if (specializations != null && !specializations.isEmpty()) {
            return specializations;
        }
        
        // Fallback: return some default specializations
        return Arrays.asList("General Services");
    }
    
    /**
     * Get barber's average rating from appointments
     */
    private Integer getBarberRating(Long barberId) {
        try {
            // Get employee entity first
            Employee barber = employeeRepository.findById(barberId).orElse(null);
            if (barber == null) {
                return 4; // Default rating if barber not found
            }
            
            // Get recent appointments with ratings
            LocalDateTime startDate = LocalDateTime.now().minusYears(1); // Last year
            LocalDateTime endDate = LocalDateTime.now();
            
            List<Appointment> allAppointments = appointmentRepository
                .findByEmployeeAndAppointmentDateBetweenAndStatusNot(
                    barber, startDate, endDate, Appointment.AppointmentStatus.CANCELLED);
            
            // Filter appointments that have ratings
            List<Appointment> ratedAppointments = allAppointments.stream()
                .filter(a -> a.getRating() != null && a.getRating() > 0)
                .collect(Collectors.toList());
            
            if (ratedAppointments.isEmpty()) {
                // Check if barber has a rating in their profile
                Integer profileRating = barber.getRatings();
                return profileRating != null ? profileRating : 4; // Default rating
            }
            
            double avgRating = ratedAppointments.stream()
                .mapToInt(Appointment::getRating)
                .average()
                .orElse(4.0);
            
            return (int) Math.round(avgRating);
            
        } catch (Exception e) {
            logger.warn("Error calculating rating for barber {}: {}", barberId, e.getMessage());
            return 4; // Default rating on error
        }
    }
    
    /**
     * Check if barber has any time slots available (performance optimization)
     */
    private boolean hasAnyAvailability(Employee barber, LocalDate date, List<Service> services) {
        try {
            // Calculate total time needed
            int totalDuration = services.stream()
                .mapToInt(Service::getDurationMinutes)
                .sum() + 15; // Add buffer
            
            // Get working hours
            WorkingHours barberHours = getBarberWorkingHours(barber, date.getDayOfWeek());
            WorkingHours salonHours = getSalonWorkingHours(barber.getSalon().getSalonId(), date.getDayOfWeek());
            
            if (barberHours == null || salonHours == null || !barberHours.isOpen() || !salonHours.isOpen()) {
                logger.debug("Barber {} or salon not open on {}", barber.getEmployeeId(), date.getDayOfWeek());
                return false;
            }
            
            // Calculate effective working time
            LocalTime workStart = salonHours.getStartTime().isAfter(barberHours.getStartTime()) 
                ? salonHours.getStartTime() : barberHours.getStartTime();
            LocalTime workEnd = salonHours.getEndTime().isBefore(barberHours.getEndTime()) 
                ? salonHours.getEndTime() : barberHours.getEndTime();
            
            long totalWorkingMinutes = Duration.between(workStart, workEnd).toMinutes();
            
            if (totalWorkingMinutes < totalDuration) {
                logger.debug("Not enough working hours for barber {} on {}: {} minutes available, {} needed", 
                           barber.getEmployeeId(), date.getDayOfWeek(), totalWorkingMinutes, totalDuration);
                return false; // Not enough working hours for this service combination
            }
            
            // Get existing appointments
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            List<Appointment> existingAppointments = appointmentRepository
                .findByEmployeeAndAppointmentDateBetweenAndStatusNot(
                    barber, startOfDay, endOfDay, Appointment.AppointmentStatus.CANCELLED);
            
            // Calculate total booked time
            long bookedMinutes = existingAppointments.stream()
                .mapToLong(appointment -> {
                    LocalDateTime start = appointment.getAppointmentDate();
                    LocalDateTime end = appointment.getEstimatedEndTime() != null 
                        ? appointment.getEstimatedEndTime() 
                        : start.plusMinutes(60); // Default 1 hour
                    return Duration.between(start, end).toMinutes();
                })
                .sum();
            
            // Check if there's enough free time
            boolean hasAvailability = (totalWorkingMinutes - bookedMinutes) >= totalDuration;
            
            logger.debug("Barber {} availability check on {}: working={}min, booked={}min, needed={}min, available={}", 
                        barber.getEmployeeId(), date.getDayOfWeek(), totalWorkingMinutes, bookedMinutes, totalDuration, hasAvailability);
            
            return hasAvailability;
            
        } catch (Exception e) {
            logger.warn("Error checking availability for barber {}: {}", barber.getEmployeeId(), e.getMessage());
            return true; // Default to available on error to not exclude barbers unnecessarily
        }
    }
    
    /**
     * Check if an employee role is a service provider (can perform salon services)
     */
    private boolean isServiceProvider(Employee.Role role) {
        switch (role) {
            case BARBER:
            case STYLIST:
            case OWNER:      // Owners can typically perform services
            case MANAGER:    // Managers might also perform services
                return true;
            case RECEPTIONIST:
            case CLEANER:
            case OTHER:
            default:
                return false;
        }
    }
    
    /**
     * Check if barber can serve customers of the specified gender
     */
    private boolean canBarberServeGender(Employee barber, Employee.Gender customerGender) {
        Employee.ServesGender servesGender = barber.getServesGender();
        
        logger.debug("Checking barber {} (ID: {}) - serves_gender: {}, customer_gender: {}", 
                    barber.getFirstName() + " " + barber.getLastName(), 
                    barber.getEmployeeId(), 
                    servesGender, 
                    customerGender);
        
        if (servesGender == null) {
            // Default to serving both if not specified
            servesGender = Employee.ServesGender.BOTH;
            logger.debug("Barber {} has null serves_gender, defaulting to BOTH", barber.getEmployeeId());
        }
        
        boolean canServe = false;
        switch (servesGender) {
            case BOTH:
                canServe = true;
                break;
            case MALE:
                canServe = customerGender == Employee.Gender.MALE;
                break;
            case FEMALE:
                canServe = customerGender == Employee.Gender.FEMALE;
                break;
            default:
                canServe = false;
        }
        
        logger.debug("Barber {} (serves: {}) can serve {} customer: {}", 
                    barber.getEmployeeId(), servesGender, customerGender, canServe);
        
        return canServe;
    }
    
    /**
     * Parse customer gender string to enum
     */
    private Employee.Gender parseCustomerGender(String customerGender) {
        if (customerGender == null || customerGender.trim().isEmpty()) {
            return Employee.Gender.OTHER; // Default if not specified
        }
        
        try {
            return Employee.Gender.valueOf(customerGender.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid customer gender: {}, defaulting to OTHER", customerGender);
            return Employee.Gender.OTHER;
        }
    }
}
