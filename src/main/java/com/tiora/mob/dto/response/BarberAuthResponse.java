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
}
