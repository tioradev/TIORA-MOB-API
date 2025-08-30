package com.tiora.mob.controller;



import com.tiora.mob.dto.request.OtpVerificationRequest;
import com.tiora.mob.dto.request.PhoneRequest;
import com.tiora.mob.dto.response.JwtResponse;
import com.tiora.mob.dto.response.OtpResponse;
import com.tiora.mob.service.AuthService;
import com.tiora.mob.service.OtpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@RequestMapping("/mobile/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody PhoneRequest request) {
        logger.info("sendOtp called with phone: {}", request.getPhoneNumber());
        OtpResponse response = otpService.generateAndSendOtp(request.getPhoneNumber());
        logger.info("OTP sent response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<JwtResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        logger.info("verifyOtp called with phone: {}", request.getPhoneNumber());
        JwtResponse response = authService.verifyOtpAndGenerateToken(request.getPhoneNumber(), request.getOtp());
        logger.info("verifyOtp response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String token) {
        logger.info("refreshToken called");
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}