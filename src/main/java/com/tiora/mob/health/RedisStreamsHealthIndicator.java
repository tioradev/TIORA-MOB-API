package com.tiora.mob.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStreamsHealthIndicator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisStreamsHealthIndicator.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${salon.redis.streams.appointment-stream}")
    private String appointmentStreamKey;

    public boolean isHealthy() {
        try {
            // Test Redis connection
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            
            // Test stream operations
            String testStreamKey = "health:test:" + System.currentTimeMillis();
            Map<String, String> testMessage = new HashMap<>();
            testMessage.put("ping", "pong");
            Object messageId = redisTemplate.opsForStream().add(testStreamKey, testMessage);
            
            // Clean up test message
            redisTemplate.opsForStream().delete(testStreamKey, messageId.toString());
            
            log.info("Redis Streams health check passed - Connection: {}", pong);
            return true;
                
        } catch (Exception e) {
            log.error("Redis Streams health check failed: {}", e.getMessage());
            return false;
        }
    }

    public String getHealthStatus() {
        return isHealthy() ? "UP" : "DOWN";
    }
}
