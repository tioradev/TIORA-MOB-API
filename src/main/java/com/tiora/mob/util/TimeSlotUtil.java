package com.tiora.mob.util;

import com.tiora.mob.dto.response.AvailableTimeSlotsResponse;
import com.tiora.mob.entity.Appointment;
import com.tiora.mob.entity.WorkingHours;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotUtil {
    public static List<AvailableTimeSlotsResponse.TimeSlot> calculateAvailableSlots(
            WorkingHours workingHours,
            List<Appointment> appointments,
            int serviceDurationMinutes
    ) {
        List<AvailableTimeSlotsResponse.TimeSlot> slots = new ArrayList<>();
        if (workingHours == null) return slots;
        LocalTime start = workingHours.getStartTime();
        LocalTime end = workingHours.getEndTime();
        List<TimeInterval> occupied = new ArrayList<>();
        for (Appointment appt : appointments) {
            occupied.add(new TimeInterval(
                appt.getAppointmentDate().toLocalTime(),
                appt.getEstimatedEndTime().toLocalTime()
            ));
        }
        LocalTime slotStart = start;
        while (slotStart.plusMinutes(serviceDurationMinutes).compareTo(end) <= 0) {
            LocalTime slotEnd = slotStart.plusMinutes(serviceDurationMinutes);
            boolean overlaps = false;
            for (TimeInterval interval : occupied) {
                if (interval.overlaps(slotStart, slotEnd)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                slots.add(new AvailableTimeSlotsResponse.TimeSlot(slotStart, slotEnd, true, null));
            } else {
                slots.add(new AvailableTimeSlotsResponse.TimeSlot(slotStart, slotEnd, false, "Already booked"));
            }
            slotStart = slotStart.plusMinutes(15); // 15-min granularity
        }
        return slots;
    }

    private static class TimeInterval {
        private final LocalTime start;
        private final LocalTime end;

        public TimeInterval(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public boolean overlaps(LocalTime slotStart, LocalTime slotEnd) {
            return !(slotEnd.compareTo(start) <= 0 || slotStart.compareTo(end) >= 0);
        }
    }
}
