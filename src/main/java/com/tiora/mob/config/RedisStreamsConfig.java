package com.tiora.mob.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Redis Streams Configuration for Mobile Backend
 * Handles consumer group creation with proper error handling
 * Ensures unique consumer groups to avoid conflicts with Web Backend
 */
@Configuration
@Slf4j
public class RedisStreamsConfig {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${salon.redis.streams.appointment-stream}")
    private String appointmentStreamKey;
    
    @Value("${salon.redis.streams.consumer-group}")
    private String consumerGroup;
    
    @Value("${salon.redis.streams.barber-stream:salon:barber-notifications}")
    private String barberStreamKey;
    
    @Value("${salon.redis.streams.barber-consumer-group:mobile-barber-group-v3}")
    private String barberConsumerGroup;

    @PostConstruct
    public void initializeConsumerGroups() {
        log.info("🚀 Initializing Redis Stream consumer groups for Mobile Backend...");
        
        try {
            // Test Redis connection first
            testRedisConnection();
            
            // Create appointment events consumer group (for receiving web backend events)
            createConsumerGroupSafely(appointmentStreamKey, consumerGroup);
            
            // Create barber notifications consumer group (for sending FCM notifications)
            createConsumerGroupSafely(barberStreamKey, barberConsumerGroup);
            
            log.info("✅ Redis Stream consumer groups initialized successfully");
            log.info("📋 Mobile Backend using groups: {} and {}", consumerGroup, barberConsumerGroup);
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize consumer groups: {}", e.getMessage());
            log.warn("⚠️  Application will continue without Redis Stream consumer groups");
            // Don't re-throw - let application start even if Redis consumer groups fail
        }
    }

    /**
     * Test Redis connection before creating consumer groups
     */
    private void testRedisConnection() {
        try {
            redisTemplate.opsForValue().set("mobile-backend:health-check", "OK");
            String result = (String) redisTemplate.opsForValue().get("mobile-backend:health-check");
            redisTemplate.delete("mobile-backend:health-check");
            
            if ("OK".equals(result)) {
                log.info("✅ Redis connection verified successfully");
            } else {
                throw new RuntimeException("Redis health check failed");
            }
        } catch (Exception e) {
            log.error("❌ Redis connection test failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Create consumer group safely with proper BUSYGROUP error handling
     */
    private void createConsumerGroupSafely(String streamKey, String consumerGroup) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
            log.info("✅ Created NEW consumer group: {} for stream: {}", consumerGroup, streamKey);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String exceptionType = e.getClass().getSimpleName();
            
            if (errorMessage.contains("busygroup") || errorMessage.contains("already exists") || 
                exceptionType.contains("RedisBusyException")) {
                log.info("ℹ️  Consumer group {} already exists for stream: {} - This is expected and OK", consumerGroup, streamKey);
                // Don't throw exception for BUSYGROUP - this is normal
            } else if (errorMessage.contains("no such key") || errorMessage.contains("nogroup")) {
                log.warn("⚠️  Stream {} does not exist yet. Consumer group {} will be created when stream is first used", streamKey, consumerGroup);
                // Don't throw exception for missing stream - this is also normal
            } else {
                log.error("❌ Unexpected error creating consumer group {} for stream {}: {} ({})", 
                         consumerGroup, streamKey, e.getMessage(), exceptionType);
                throw new RuntimeException("Failed to create consumer group", e);
            }
        }
    }
}
