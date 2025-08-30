// ...existing code...
package com.tiora.mob.service;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.tiora.mob.config.JwtConfig;
import com.tiora.mob.dto.response.JwtResponse;
import com.tiora.mob.dto.response.JwtWithCustomerResponse;
import com.tiora.mob.dto.response.CustomerProfileResponse;
import com.tiora.mob.entity.Customer;
import com.tiora.mob.exception.UnauthorizedException;
import com.tiora.mob.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    /**
     * Generate JWT token for a phone number (for signup flow, no customerId claim)
     */
    private String generateTokenForPhone(String phoneNumber) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtConfig.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String INVALIDATED_TOKENS_CACHE = "invalidatedTokens";
    private static final String CUSTOMER_ID_CLAIM = "customerId";

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private OtpService otpService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Verify OTP and generate JWT token if valid
     * Logging added for OTP verification and token generation
     */
    public JwtResponse verifyOtpAndGenerateToken(String phoneNumber, String otp) {
        // Verify OTP
        otpService.verifyOtp(phoneNumber, otp);

        // Check if customer exists
        Optional<Customer> customerOpt = customerRepository.findByPhoneNumber(phoneNumber);
        boolean customerExists = customerOpt.isPresent();

        String token;
        boolean isProfileComplete = false;
        if (customerExists) {
            Customer customer = customerOpt.get();
            token = generateToken(customer);
            isProfileComplete = isProfileComplete(customer);
            // Map Customer to CustomerProfileResponse
            CustomerProfileResponse profile = new CustomerProfileResponse();
            profile.setId(customer.getId());
            profile.setFirstName(customer.getFirstName());
            profile.setLastName(customer.getLastName());
            profile.setEmail(customer.getEmail());
            profile.setPhoneNumber(customer.getPhoneNumber());
            profile.setGender(customer.getGender() != null ? customer.getGender().name() : null);
            profile.setMemberSince(customer.getCreatedAt());
            profile.setLastVisit(customer.getLastVisitDate());
            // You can set completedAppointments and profileComplete as needed
            profile.setProfileComplete(isProfileComplete);
            profile.setProfileImageUrl(customer.getProfileImageUrl());
            return new JwtWithCustomerResponse(token, isProfileComplete, true, profile);
        } else {
            // Generate a token with phone number only (for signup flow)
            token = generateTokenForPhone(phoneNumber);
            return new JwtResponse(token, false, false);
        }
    }

    /**
     * Generate a new token to extend the session
     */
    public JwtResponse refreshToken(String token) {
        // Invalidate old token
        logout(token);

        // Extract customer ID and generate new token
        Long customerId = getCustomerIdFromToken(token);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        String newToken = generateToken(customer);
        boolean isProfileComplete = isProfileComplete(customer);

        // Always true here, since refreshToken is only for existing customers
        return new JwtResponse(newToken, isProfileComplete, true);
    }

    /**
     * Invalidate a token on logout
     */
    public void logout(String tokenHeader) {
        String token = extractTokenFromHeader(tokenHeader);
        if (token != null) {
            // Store token in invalidated tokens cache
            Cache cache = cacheManager.getCache(INVALIDATED_TOKENS_CACHE);
            if (cache != null) {
                // Store until the token's expiration time
                Date expiration = Jwts.parserBuilder()
                        .setSigningKey(jwtConfig.getSecretKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();

                cache.put(token, expiration);
            }
        }
    }

    /**
     * Extract customer ID from token
     */
    public Long getCustomerIdFromToken(String tokenHeader) {
        String token = extractTokenFromHeader(tokenHeader);
        if (token != null) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token is in invalidated tokens cache
            Cache cache = cacheManager.getCache(INVALIDATED_TOKENS_CACHE);
            if (cache != null && cache.get(token) != null) {
                throw new UnauthorizedException("Token has been invalidated");
            }

            return claims.get(CUSTOMER_ID_CLAIM, Long.class);
        }
        throw new UnauthorizedException("Invalid token");
    }

    /**
     * Generate JWT token for a customer
     */
    private String generateToken(Customer customer) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CUSTOMER_ID_CLAIM, customer.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(customer.getPhoneNumber())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtConfig.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Check if customer profile is complete
     */
    private boolean isProfileComplete(Customer customer) {
        return StringUtils.hasText(customer.getFirstName()) &&
                StringUtils.hasText(customer.getLastName()) &&
                StringUtils.hasText(customer.getEmail());
    }

    /**
     * Extract token from Authorization header
     */
    private String extractTokenFromHeader(String header) {
        if (StringUtils.hasText(header) && header.startsWith(jwtConfig.getTokenPrefix())) {
            return header.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }
}