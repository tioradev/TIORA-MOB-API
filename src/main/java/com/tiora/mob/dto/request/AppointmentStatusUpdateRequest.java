package com.tiora.mob.dto.request;

public class AppointmentStatusUpdateRequest {
    private String cancellationReason;

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    private Long appointmentId;
    private String status;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
