package com.tiora.mob.service;


import com.tiora.mob.dto.response.OtpResponse;
import com.tiora.mob.entity.Customer;
import com.tiora.mob.entity.MobUser;
import com.tiora.mob.exception.UnauthorizedException;
import com.tiora.mob.repository.CustomerRepository;
import com.tiora.mob.repository.MobUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private static final String OTP_CACHE = "otpCache";
    private static final int OTP_LENGTH = 4;
    private static final int OTP_VALIDITY_MINUTES = 5;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private MobUserRepository mobUserRepository;

    // For generating random OTPs
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate and send OTP to the provided phone number with role validation
     */
    public OtpResponse generateAndSendOtp(String phoneNumber, String userRole) {
        logger.info("Generating OTP for phone: {} with role: {}", phoneNumber, userRole);
        
        // Validate user role
        if (!"CUSTOMER".equals(userRole) && !"SALON_BARBER".equals(userRole)) {
            throw new UnauthorizedException("Invalid user role. Must be CUSTOMER or SALON_BARBER");
        }
        
        // Role-specific validation
        if ("SALON_BARBER".equals(userRole)) {
            // Check if the phone number exists in mob_users table
            boolean barberExists = mobUserRepository.existsByBarberPhone(phoneNumber);
            if (!barberExists) {
                throw new UnauthorizedException("Stylist is not registered to the System, Please contact the owner");
            }
            
            // Check if barber is active
            Optional<MobUser> mobUser = mobUserRepository.findByBarberPhoneAndStatus(
                phoneNumber, MobUser.UserStatus.ACTIVE);
            if (mobUser.isEmpty()) {
                throw new UnauthorizedException("Stylist account is not active, Please contact the owner");
            }
        }

        // For testing: always use OTP '1234' for 0768391956
        String otp;
        if ("0768391956".equals(phoneNumber)) {
            otp = "1234"; // <-- Remove this block for production
        } else {
            otp = generateOtp();
        }

        // Store OTP in cache with expiration
        getOtpCache().put(phoneNumber, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES)));

        // In a real application, you would send this via SMS
        // For now, we'll just return it in the response for testing
        String message = "OTP has been sent to your phone number";


        // For development purposes, log the OTP
        logger.info("OTP for {}: {}", phoneNumber, otp);

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