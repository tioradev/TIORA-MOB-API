package com.tiora.mob.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BarberAuthResponse {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private Long salonId;
    private Long branchId;
    private String profileImageUrl;
    private Double ratings;
    private String specializations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

        public Long getEmployeeId() {
            return employeeId;
        }
        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPhoneNumber() {
            return phoneNumber;
        }
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public Long getSalonId() {
            return salonId;
        }
        public void setSalonId(Long salonId) {
            this.salonId = salonId;
        }
        public Long getBranchId() {
            return branchId;
        }
        public void setBranchId(Long branchId) {
            this.branchId = branchId;
        }
        public String getProfileImageUrl() {
            return profileImageUrl;
        }
        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }
        public Double getRatings() {
            return ratings;
        }
        public void setRatings(Double ratings) {
            this.ratings = ratings;
        }
        public String getSpecializations() {
            return specializations;
        }
        public void setSpecializations(String specializations) {
            this.specializations = specializations;
        }
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
}
