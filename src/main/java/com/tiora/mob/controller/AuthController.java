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

@RestController
@RequestMapping("/api/mobile/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp( @RequestBody PhoneRequest request) {
        return ResponseEntity.ok(otpService.generateAndSendOtp(request.getPhoneNumber()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<JwtResponse> verifyOtp( @RequestBody OtpVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndGenerateToken(
                request.getPhoneNumber(), request.getOtp()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}