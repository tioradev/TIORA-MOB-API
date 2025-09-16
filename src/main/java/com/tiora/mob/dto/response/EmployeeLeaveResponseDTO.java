package com.tiora.mob.dto.response;

public class EmployeeLeaveResponseDTO {
    private Long id;
    private Long employeeId;
    private String startDate;
    private String endDate;
    private String leaveReason;
    private String status;
    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
