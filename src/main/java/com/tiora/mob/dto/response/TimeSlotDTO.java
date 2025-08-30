package com.tiora.mob.dto.response;

public class TimeSlotDTO {
    private String startTime; // e.g., "10:00"
    private String endTime;   // e.g., "10:30"

    public TimeSlotDTO() {}
    public TimeSlotDTO(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
