package com.tiora.mob.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class WeeklySchedule {
    
    private Map<String, DaySchedule> schedule;
    
    public WeeklySchedule() {
        this.schedule = new HashMap<>();
        // Initialize with default 9 AM - 6 PM schedule for all days
        initializeDefaultSchedule();
    }
    
    private void initializeDefaultSchedule() {
        LocalTime defaultOpening = LocalTime.of(9, 0);
        LocalTime defaultClosing = LocalTime.of(18, 0);
        
        schedule.put("MONDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("TUESDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("WEDNESDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("THURSDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("FRIDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("SATURDAY", new DaySchedule(defaultOpening, defaultClosing, true));
        schedule.put("SUNDAY", new DaySchedule(defaultOpening, defaultClosing, true));
    }
    
    public Map<String, DaySchedule> getSchedule() {
        return schedule;
    }
    
    public void setSchedule(Map<String, DaySchedule> schedule) {
        this.schedule = schedule;
    }
    
    public DaySchedule getDaySchedule(String day) {
        return schedule.get(day.toUpperCase());
    }
    
    public void setDaySchedule(String day, DaySchedule daySchedule) {
        schedule.put(day.toUpperCase(), daySchedule);
    }
    
    public static class DaySchedule {
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime openingTime;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime closingTime;
        
        private boolean isOpen;
        
        public DaySchedule() {}
        
        public DaySchedule(LocalTime openingTime, LocalTime closingTime, boolean isOpen) {
            this.openingTime = openingTime;
            this.closingTime = closingTime;
            this.isOpen = isOpen;
        }
        
        // Getters and Setters
        public LocalTime getOpeningTime() {
            return openingTime;
        }
        
        public void setOpeningTime(LocalTime openingTime) {
            this.openingTime = openingTime;
        }
        
        public LocalTime getClosingTime() {
            return closingTime;
        }
        
        public void setClosingTime(LocalTime closingTime) {
            this.closingTime = closingTime;
        }
        
        public boolean isOpen() {
            return isOpen;
        }
        
        public void setOpen(boolean open) {
            isOpen = open;
        }
    }
}
