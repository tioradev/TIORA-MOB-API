package com.tiora.mob.dto.response;

public class EmployeeAppointmentStatsDTO {
    private int todaysAppointments;
    private int totalAppointments;
    private int completedAppointments;
    private int pendingAppointments;

    public int getTodaysAppointments() { return todaysAppointments; }
    public void setTodaysAppointments(int todaysAppointments) { this.todaysAppointments = todaysAppointments; }
    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }
    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }
}
