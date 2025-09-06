package com.tiora.mob.dto.response;

import com.tiora.mob.entity.Branch.BranchStatus;
import com.tiora.mob.entity.Branch.SalonType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BranchResponse {
    private Long branchId;
    private String branchName;
    private BranchStatus status;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String branchPhoneNumber;
    private String branchEmail;
    private String description;
    private String branchImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private SalonType salonType;
    // Add more fields as needed, but do not include the Salon entity or WeeklySchedule directly

    // Getters and setters
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public BranchStatus getStatus() { return status; }
    public void setStatus(BranchStatus status) { this.status = status; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getBranchPhoneNumber() { return branchPhoneNumber; }
    public void setBranchPhoneNumber(String branchPhoneNumber) { this.branchPhoneNumber = branchPhoneNumber; }
    public String getBranchEmail() { return branchEmail; }
    public void setBranchEmail(String branchEmail) { this.branchEmail = branchEmail; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBranchImage() { return branchImage; }
    public void setBranchImage(String branchImage) { this.branchImage = branchImage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public SalonType getSalonType() { return salonType; }
    public void setSalonType(SalonType salonType) { this.salonType = salonType; }
}
