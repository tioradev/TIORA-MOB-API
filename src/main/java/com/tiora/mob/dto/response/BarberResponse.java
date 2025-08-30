package com.tiora.mob.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BarberResponse {

    private Long barberId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<java.util.Map<String, Object>> specializations;
    private Integer ratings;
    private String profileImageUrl;


}