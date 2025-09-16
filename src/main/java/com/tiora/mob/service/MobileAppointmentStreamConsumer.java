package com.tiora.mob.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MobileAppointmentStreamConsumer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MobileAppointmentStreamConsumer.class);

    @Value("${salon.redis.streams.appointment-stream}")
    private String appointmentStreamKey;

    @Value("${salon.redis.streams.consumer-group}")
    private String consumerGroup;

    @Value("${salon.redis.streams.consumer-name}")
    private String consumerName;

    @Autowired
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;
    private volatile boolean consuming = false;

    @PostConstruct
    public void init() {
        // Create consumer group if it doesn't exist
        createConsumerGroupIfNotExists();
        // Start consuming messages asynchronously (don't block startup)
        CompletableFuture.runAsync(() -> {
            try {
                startConsuming();
            } catch (Exception e) {
                log.error("Failed to start consuming appointment stream: {}", e.getMessage(), e);
            }
        });
    }

    private void createConsumerGroupIfNotExists() {
        try {
            // Create consumer group - correct parameter order: key, readOffset, group
            redisTemplate.opsForStream()
                .createGroup(appointmentStreamKey, ReadOffset.from("0"), consumerGroup);
            log.info("Created consumer group: {} for stream: {}", consumerGroup, appointmentStreamKey);
        } catch (Exception e) {
            log.debug("Consumer group {} already exists for stream {}: {}", 
                     consumerGroup, appointmentStreamKey, e.getMessage());
        }
    }

    public void startConsuming() {
        consuming = true;
        log.info("Starting to consume messages from stream: {} with consumer: {}", appointmentStreamKey, consumerName);

        while (consuming) {
            try {
                // Use consumer group reading with '>' to get new messages with timeout
                List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                    .read(Consumer.from(consumerGroup, consumerName),
                          org.springframework.data.redis.connection.stream.StreamReadOptions.empty().block(Duration.ofSeconds(2)),
                          StreamOffset.create(appointmentStreamKey, ReadOffset.from(">")));

                for (MapRecord<String, Object, Object> message : messages) {
                    processMessage(message);
                    
                    // Acknowledge the message
                    redisTemplate.opsForStream().acknowledge(appointmentStreamKey, consumerGroup, message.getId());
                }

                // Poll every 2 seconds
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.info("Consumer interrupted, stopping...");
                consuming = false;
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error consuming appointment messages: {}", e.getMessage(), e);
                try {
                    Thread.sleep(5000); // Wait 5 seconds before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Stopped consuming messages from stream: {}", appointmentStreamKey);
    }

    public void stopConsuming() {
        consuming = false;
        log.info("Stopping message consumption...");
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        try {
            Map<Object, Object> fields = message.getValue();
            log.info("Raw message fields: {}", fields);
            String messageType = (String) fields.get("messageType");
            String sourceSystem = (String) fields.get("sourceSystem");

            if (messageType == null || sourceSystem == null) {
                log.error("Missing required fields in message: messageType={}, sourceSystem={}");
                return;
            }

            log.debug("Received message type: {} from system: {}", messageType, sourceSystem);

            switch (messageType) {
                case "appointment_updated":
                    handleAppointmentUpdatedFromWeb(fields);
                    break;
                case "barber_status_changed":
                    handleBarberStatusChanged(fields);
                    break;
                case "service_availability_changed":
                    handleServiceAvailabilityChanged(fields);
                    break;
                case "data_sync_required":
                    handleDataSyncRequired(fields);
                    break;
                default:
                    log.info("Received unhandled message type: {} from web backend", messageType);
            }
        } catch (Exception e) {
            log.error("Error processing message {}: {}", message.getId(), e.getMessage(), e);
        }
    }

    private void handleAppointmentUpdatedFromWeb(Map<Object, Object> fields) {
        String appointmentId = (String) fields.get("appointment_id");
        String salonId = (String) fields.get("salon_id");
        
        log.info("Appointment {} updated from web backend for salon {}", appointmentId, salonId);
        
        // TODO: Update local mobile backend data
        // TODO: Notify mobile apps if needed
        // Example: appointmentService.syncAppointmentFromWeb(appointmentId, salonId);
    }

    private void handleBarberStatusChanged(Map<Object, Object> fields) {
        String barberId = (String) fields.get("barber_id");
        String salonId = (String) fields.get("salon_id");
        String data = (String) fields.get("data");
        
        log.info("Barber {} status changed from web backend for salon {} - data: {}", barberId, salonId, data);
        
        // TODO: Update barber availability in mobile backend
        // Example: barberService.updateBarberStatusFromWeb(barberId, salonId, data);
    }

    private void handleServiceAvailabilityChanged(Map<Object, Object> fields) {
        String salonId = (String) fields.get("salon_id");
        String branchId = (String) fields.get("branch_id");
        String data = (String) fields.get("data");
        
        log.info("Service availability changed from web backend for salon {} branch {} - data: {}", 
                salonId, branchId, data);
        
        // TODO: Update service availability in mobile backend
        // Example: serviceService.updateServiceAvailabilityFromWeb(salonId, branchId, data);
    }

    private void handleDataSyncRequired(Map<Object, Object> fields) {
        String salonId = (String) fields.get("salon_id");
        
        log.info("Data sync required from web backend for salon {}", salonId);
        
        // TODO: Trigger data synchronization
        // Example: syncService.performFullSyncForSalon(salonId);
    }
}
