package com.tiora.mob.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerCreateRequest {
    @NotBlank
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @NotBlank
    @Email
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @NotBlank
    @Pattern(regexp = "^\\d{10,15}$", message = "Phone number must be 10-15 digits")
    @Schema(description = "Phone number", example = "9876543210")
    private String phoneNumber;
}
