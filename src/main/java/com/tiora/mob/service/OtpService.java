package com.tiora.mob.service;


import com.tiora.mob.dto.response.OtpResponse;
import com.tiora.mob.entity.Customer;
import com.tiora.mob.exception.UnauthorizedException;
import com.tiora.mob.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final String OTP_CACHE = "otpCache";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CustomerRepository customerRepository;

    // For generating random OTPs
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate and send OTP to the provided phone number
     */
    public OtpResponse generateAndSendOtp(String phoneNumber) {
        // Check if customer exists or create a new one
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhoneNumber(phoneNumber);
                    newCustomer.setCreatedAt(LocalDateTime.now());
                    return customerRepository.save(newCustomer);
                });

        // Generate OTP
        String otp = generateOtp();

        // Store OTP in cache with expiration
        getOtpCache().put(phoneNumber, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES)));

        // In a real application, you would send this via SMS
        // For now, we'll just return it in the response for testing
        String message = "OTP has been sent to your phone number";

        // For development purposes, log the OTP
        System.out.println("OTP for " + phoneNumber + ": " + otp);

        return new OtpResponse(phoneNumber, message);
    }

    /**
     * Verify OTP against the phone number
     */
    public boolean verifyOtp(String phoneNumber, String otp) {
        OtpData otpData = getOtpCache().get(phoneNumber, OtpData.class);

        if (otpData == null) {
            throw new UnauthorizedException("No OTP requested for this number");
        }

        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            getOtpCache().evict(phoneNumber);
            throw new UnauthorizedException("OTP has expired");
        }

        if (!otpData.otp.equals(otp)) {
            throw new UnauthorizedException("Invalid OTP");
        }

        // OTP verified successfully, remove from cache
        getOtpCache().evict(phoneNumber);
        return true;
    }

    /**
     * Generate a random OTP of specified length
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Get the OTP cache
     */
    private Cache getOtpCache() {
        return cacheManager.getCache(OTP_CACHE);
    }

    /**
     * Inner class to store OTP data with expiry time
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}