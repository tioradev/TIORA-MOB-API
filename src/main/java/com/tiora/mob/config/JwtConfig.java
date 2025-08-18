package com.tiora.mob.config;


import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private int expiration;

    /**
     * Get JWT secret key
     */
    @Bean
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get token expiration time in milliseconds
     */
    public int getExpiration() {
        return expiration;
    }

    /**
     * Get token expiration time in seconds
     */
    public int getExpirationInSeconds() {
        return expiration / 1000;
    }

    /**
     * Get token header name
     */
    public String getTokenHeader() {
        return "Authorization";
    }

    /**
     * Get token prefix
     */
    public String getTokenPrefix() {
        return "Bearer ";
    }
}