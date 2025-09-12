package com.tiora.mob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BarberNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BarberNotificationConsumer.class);

    @Value("${salon.redis.streams.barber-stream}")
    private String barberNotificationsStream;
    
    @Value("${salon.redis.streams.barber-consumer-group}")
    private String consumerGroup;
    
    @Value("${salon.redis.streams.barber-consumer-name}")
    private String consumerName;

    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private FCMNotificationService fcmNotificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @PostConstruct
    public void initialize() {
        logger.info("Initializing Barber Notification Consumer...");
        
        try {
            // Create consumer group if it doesn't exist
            createConsumerGroup();
            
            // Start consuming messages
            startConsuming();
            
            logger.info("Barber Notification Consumer initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Barber Notification Consumer: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Barber Notification Consumer...");
        
        isRunning.set(false);
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Barber Notification Consumer shut down completed");
    }

    /**
     * Create consumer group for barber notifications
     */
    private void createConsumerGroup() {
        try {
            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            
            // Try to create consumer group
            streamOps.createGroup(barberNotificationsStream, consumerGroup);
            logger.info("Created consumer group: {} for stream: {}", consumerGroup, barberNotificationsStream);
            
        } catch (Exception e) {
            // Check if this is a BUSYGROUP error (consumer group already exists)
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("BUSYGROUP") || errorMessage.contains("already exists"))) {
                logger.info("Consumer group {} already exists for stream: {} - continuing", consumerGroup, barberNotificationsStream);
                return; // Exit gracefully - this is expected behavior
            }
            
            // Check if it's a nested exception with BUSYGROUP
            Throwable cause = e.getCause();
            while (cause != null) {
                String causeMessage = cause.getMessage();
                if (causeMessage != null && (causeMessage.contains("BUSYGROUP") || causeMessage.contains("already exists"))) {
                    logger.info("Consumer group {} already exists for stream: {} (nested) - continuing", consumerGroup, barberNotificationsStream);
                    return; // Exit gracefully
                }
                cause = cause.getCause();
            }
            
            // If it's not a BUSYGROUP error, log and continue (don't throw exception)
            logger.warn("Could not create consumer group {} for stream {}: {} - continuing anyway", 
                       consumerGroup, barberNotificationsStream, errorMessage);
        }
    }

    /**
     * Start consuming messages from the stream
     */
    private void startConsuming() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "barber-notification-consumer");
            t.setDaemon(true);
            return t;
        });

        isRunning.set(true);

        executorService.submit(() -> {
            logger.info("Started consuming barber notifications from stream: {}", barberNotificationsStream);
            
            while (isRunning.get()) {
                try {
                    consumeMessages();
                } catch (Exception e) {
                    logger.error("Error in barber notification consumer: {}", e.getMessage(), e);
                    
                    // Wait before retrying
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            logger.info("Barber notification consumer stopped");
        });
    }

    /**
     * Consume messages from the Redis stream
     */
    private void consumeMessages() {
        try {
            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            
            // Read pending messages first
            readPendingMessages(streamOps);
            
            // Then read new messages with timeout
            List<MapRecord<String, String, String>> messages = streamOps.read(
                Consumer.from(consumerGroup, consumerName),
                org.springframework.data.redis.connection.stream.StreamReadOptions.empty().block(Duration.ofSeconds(2)),
                StreamOffset.create(barberNotificationsStream, ReadOffset.lastConsumed())
            );

            for (MapRecord<String, String, String> message : messages) {
                processNotification(message);
                
                // Acknowledge the message
                streamOps.acknowledge(barberNotificationsStream, consumerGroup, message.getId());
            }

        } catch (Exception e) {
            logger.error("Error consuming messages: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Read and process pending messages
     */
    private void readPendingMessages(StreamOperations<String, String, String> streamOps) {
        try {
            List<MapRecord<String, String, String>> pendingMessages = streamOps.read(
                Consumer.from(consumerGroup, consumerName),
                org.springframework.data.redis.connection.stream.StreamReadOptions.empty().block(Duration.ofSeconds(1)),
                StreamOffset.create(barberNotificationsStream, ReadOffset.from("0"))
            );

            for (MapRecord<String, String, String> message : pendingMessages) {
                logger.info("Processing pending message: {}", message.getId());
                processNotification(message);
                
                // Acknowledge the message
                streamOps.acknowledge(barberNotificationsStream, consumerGroup, message.getId());
            }

            if (!pendingMessages.isEmpty()) {
                logger.info("Processed {} pending messages", pendingMessages.size());
            }

        } catch (Exception e) {
            logger.error("Error reading pending messages: {}", e.getMessage(), e);
        }
    }

    /**
     * Process individual notification message
     */
    private void processNotification(MapRecord<String, String, String> message) {
        try {
            Map<String, String> data = message.getValue();
            
            logger.info("Processing barber notification: {}", message.getId());
            logger.debug("Notification data: {}", data);

            String messageType = data.get("messageType");
            String barberIdStr = data.get("barberId");
            String appointmentIdStr = data.get("appointmentId");
            String customerName = data.get("customerName");
            String serviceName = data.get("serviceName");
            String appointmentTime = data.get("appointmentTime");
            String notificationMessage = data.get("message");

            // Validate required fields
            if (barberIdStr == null || appointmentIdStr == null) {
                logger.warn("Missing required fields in notification message: {}", message.getId());
                return;
            }

            // Remove extra quotes if present
            barberIdStr = barberIdStr.replaceAll("^\"|\"$", "");
            appointmentIdStr = appointmentIdStr.replaceAll("^\"|\"$", "");

            Long barberId = Long.valueOf(barberIdStr);
            Long appointmentId = Long.valueOf(appointmentIdStr);

            // Extract event type from messageType field (remove "appointment_" prefix)
            String eventType = null;
            if (messageType != null) {
                eventType = messageType.startsWith("appointment_") ? messageType.substring(12) : messageType;
            } else {
                logger.warn("Missing messageType in notification message: {}", message.getId());
                eventType = "unknown";
            }

            // Log warning if any important field is missing
            if (customerName == null) logger.warn("Missing customerName in notification message: {}", message.getId());
            if (serviceName == null) logger.warn("Missing serviceName in notification message: {}", message.getId());
            if (appointmentTime == null) logger.warn("Missing appointmentTime in notification message: {}", message.getId());

            // Send FCM notification to barber with actual values (may be null if missing)
            fcmNotificationService.sendAppointmentNotification(
                barberId,
                eventType,
                customerName,
                serviceName,
                appointmentTime,
                appointmentId
            );

            logger.info("Successfully processed barber notification for barber: {} and appointment: {}", 
                       barberId, appointmentId);

        } catch (Exception e) {
            logger.error("Failed to process barber notification {}: {}", message.getId(), e.getMessage(), e);
            // Don't re-throw to avoid breaking the consumer loop
        }
    }

    /**
     * Check if consumer is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Get consumer status information
     */
    public Map<String, Object> getStatus() {
        return Map.of(
            "isRunning", isRunning.get(),
            "consumerGroup", consumerGroup,
            "consumerName", consumerName,
            "stream", barberNotificationsStream
        );
    }
}
