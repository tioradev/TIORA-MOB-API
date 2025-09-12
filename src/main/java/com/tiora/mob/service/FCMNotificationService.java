package com.tiora.mob.service;

import com.tiora.mob.entity.DeviceToken;
import com.tiora.mob.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FCMNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FCMNotificationService.class);

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    // TODO: Add Firebase Admin SDK dependency and configuration
    // @Autowired
    // private FirebaseMessaging firebaseMessaging;

    /**
     * Send notification to barber
     */
    @Transactional
    public void sendNotificationToEmployee(Long employeeId, String title, String body, Map<String, String> data) {
        logger.info("Sending notification to employee: {} with title: {}", employeeId, title);
        try {
            List<DeviceToken> activeTokens = deviceTokenRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
            if (activeTokens.isEmpty()) {
                logger.warn("No active device tokens found for employee: {}", employeeId);
                return;
            }
            for (DeviceToken token : activeTokens) {
                sendToDevice(token.getDeviceToken(), title, body, data);
                deviceTokenRepository.updateLastUsedAt(token.getDeviceToken(), LocalDateTime.now());
            }
        } catch (Exception e) {
            logger.error("Failed to send notification to employee {}: {}", employeeId, e.getMessage(), e);
        }
    }

    public void sendNotificationToCustomer(Long customerId, String title, String body, Map<String, String> data) {
        logger.info("Sending notification to customer: {} with title: {}", customerId, title);
        try {
            List<DeviceToken> activeTokens = deviceTokenRepository.findByCustomerIdAndIsActiveTrue(customerId);
            if (activeTokens.isEmpty()) {
                logger.warn("No active device tokens found for customer: {}", customerId);
                return;
            }
            for (DeviceToken token : activeTokens) {
                sendToDevice(token.getDeviceToken(), title, body, data);
                deviceTokenRepository.updateLastUsedAt(token.getDeviceToken(), LocalDateTime.now());
            }
        } catch (Exception e) {
            logger.error("Failed to send notification to customer {}: {}", customerId, e.getMessage(), e);
        }
    }

    /**
     * Send appointment notification to barber
     */
    public void sendAppointmentNotification(Long barberId, String eventType, 
                                          String customerName, String serviceName, 
                                          String appointmentTime, Long appointmentId) {
        
        String title = getNotificationTitle(eventType);
        String body = String.format("%s - %s service at %s", customerName, serviceName, appointmentTime);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "APPOINTMENT");
        data.put("eventType", eventType);
        data.put("appointmentId", String.valueOf(appointmentId));
        data.put("barberId", String.valueOf(barberId));
        data.put("customerName", customerName);
        data.put("serviceName", serviceName);
        data.put("appointmentTime", appointmentTime);
        data.put("timestamp", LocalDateTime.now().toString());

    sendNotificationToEmployee(barberId, title, body, data);
    }

    /**
     * Register device token for barber
     */
    @org.springframework.transaction.annotation.Transactional
    public void registerDeviceTokenForEmployee(Long employeeId, String deviceToken, String deviceType, String appVersion) {
        logger.info("Registering device token for employee: {}", employeeId);
        try {
            // Try to find an active token for this employee
            List<DeviceToken> tokens = deviceTokenRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
            if (!tokens.isEmpty()) {
                DeviceToken token = tokens.get(0);
                token.setDeviceToken(deviceToken);
                token.setDeviceType(deviceType);
                token.setAppVersion(appVersion);
                token.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(token);
            } else {
                DeviceToken newToken = DeviceToken.builder()
                    .employeeId(employeeId)
                    .deviceToken(deviceToken)
                    .deviceType(deviceType)
                    .appVersion(appVersion)
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now())
                    .build();
                deviceTokenRepository.save(newToken);
            }
            deviceTokenRepository.deactivateOtherTokensForEmployee(employeeId, deviceToken);
            logger.info("Successfully registered device token for employee: {}", employeeId);
        } catch (Exception e) {
            logger.error("Failed to register device token for employee {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Failed to register device token", e);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void registerDeviceTokenForCustomer(Long customerId, String deviceToken, String deviceType, String appVersion) {
        logger.info("Registering device token for customer: {}", customerId);
        try {
            // Try to find an active token for this customer
            List<DeviceToken> tokens = deviceTokenRepository.findByCustomerIdAndIsActiveTrue(customerId);
            if (!tokens.isEmpty()) {
                DeviceToken token = tokens.get(0);
                token.setDeviceToken(deviceToken);
                token.setDeviceType(deviceType);
                token.setAppVersion(appVersion);
                token.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(token);
            } else {
                DeviceToken newToken = DeviceToken.builder()
                    .customerId(customerId)
                    .deviceToken(deviceToken)
                    .deviceType(deviceType)
                    .appVersion(appVersion)
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now())
                    .build();
                deviceTokenRepository.save(newToken);
            }
            deviceTokenRepository.deactivateOtherTokensForCustomer(customerId, deviceToken);
            logger.info("Successfully registered device token for customer: {}", customerId);
        } catch (Exception e) {
            logger.error("Failed to register device token for customer {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to register device token", e);
        }
    }

    /**
     * Unregister device token
     */
    public void unregisterDeviceToken(String deviceToken) {
        logger.info("Unregistering device token: {}", deviceToken);
        
        try {
            var tokenOpt = deviceTokenRepository.findByDeviceTokenAndIsActiveTrue(deviceToken);
            if (tokenOpt.isPresent()) {
                DeviceToken token = tokenOpt.get();
                token.setIsActive(false);
                deviceTokenRepository.save(token);
                logger.info("Successfully unregistered device token");
            } else {
                logger.warn("Device token not found or already inactive: {}", deviceToken);
            }

        } catch (Exception e) {
            logger.error("Failed to unregister device token: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification to specific device token
     */
    private void sendToDevice(String deviceToken, String title, String body, Map<String, String> data) {
        logger.info("Sending FCM notification to device token: {} with title: {}", 
                   deviceToken.substring(0, Math.min(20, deviceToken.length())) + "...", title);
        
        try {
            // TODO: Implement actual FCM sending using Firebase Admin SDK
            // This is a placeholder implementation
            
            /*
            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                        .setBadge(1)
                        .setSound("default")
                        .build())
                    .build())
                .build();

            String response = firebaseMessaging.send(message);
            logger.info("Successfully sent FCM notification: {}", response);
            */
            
            // For now, just log the notification details
            logger.info("FCM Notification Details - Title: {}, Body: {}, Data: {}", title, body, data);
            
        } catch (Exception e) {
            logger.error("Failed to send FCM notification to device token: {}", e.getMessage(), e);
            
            // If token is invalid, mark it as inactive
            if (e.getMessage().contains("registration-token-not-registered") || 
                e.getMessage().contains("invalid-registration-token")) {
                
                try {
                    var tokenOpt = deviceTokenRepository.findByDeviceTokenAndIsActiveTrue(deviceToken);
                    if (tokenOpt.isPresent()) {
                        DeviceToken token = tokenOpt.get();
                        token.setIsActive(false);
                        deviceTokenRepository.save(token);
                        logger.info("Marked invalid device token as inactive: {}", deviceToken);
                    }
                } catch (Exception saveEx) {
                    logger.error("Failed to mark invalid token as inactive: {}", saveEx.getMessage());
                }
            }
        }
    }

    /**
     * Get notification title based on event type
     */
    private String getNotificationTitle(String eventType) {
        switch (eventType.toUpperCase()) {
            case "CREATED":
                return "New Appointment Booked";
            case "UPDATED":
                return "Appointment Updated";
            case "CANCELLED":
                return "Appointment Cancelled";
            case "RESCHEDULED":
                return "Appointment Rescheduled";
            case "STARTED":
                return "Appointment Started";
            case "COMPLETED":
                return "Appointment Completed";
            case "PAYMENT_RECEIVED":
                return "Payment Received";
            default:
                return "Appointment Notification";
        }
    }

    /**
     * Clean up old inactive tokens (should be called periodically)
     */
    public void cleanupOldTokens() {
        logger.info("Cleaning up old inactive device tokens");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Keep tokens for 30 days
            deviceTokenRepository.deleteInactiveTokensOlderThan(cutoffDate);
            
            logger.info("Successfully cleaned up old inactive device tokens");
        } catch (Exception e) {
            logger.error("Failed to clean up old device tokens: {}", e.getMessage(), e);
        }
    }
}
