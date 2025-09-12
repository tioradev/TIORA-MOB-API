# Real-Time Appointment Notification System

This document describes the real-time appointment notification system implemented in the TIORA Mobile Backend.

## Overview

The system provides real-time appointment notifications between:
- **Customer Mobile App** → **Mobile Backend** → **Web Backend** → **Web Frontend**
- **Mobile Backend** → **Redis Streams** → **Barber Mobile App** (via FCM)

## Architecture Components

### 1. AppointmentEventDto
- Standardized event format for appointment operations
- Event types: CREATED, UPDATED, CANCELLED, COMPLETED, etc.
- Contains all relevant appointment and customer details

### 2. AppointmentStreamPublisher
- Publishes appointment events to Redis Streams
- Two streams:
  - `salon:appointment-events` - For web backend consumption
  - `salon:barber-notifications` - For barber mobile app notifications

### 3. BarberNotificationConsumer
- Consumes barber notifications from Redis Stream
- Sends FCM push notifications to barber devices
- Runs as background service with automatic retry

### 4. FCMNotificationService
- Manages Firebase Cloud Messaging integration
- Handles device token registration/unregistration
- Sends structured push notifications to barber apps

### 5. BarberDeviceToken Entity
- Stores FCM device tokens for barbers
- Supports multiple devices per barber
- Automatic cleanup of inactive tokens

## API Endpoints

### Appointment Activities
```
GET /mobile/appointments/activities?status=IN_PROGRESS&customerId=123
GET /mobile/appointments/activities/multi-status?statuses=IN_PROGRESS,SCHEDULED&customerId=123
```

### Barber Notifications
```
POST /mobile/barber-notifications/register-token
POST /mobile/barber-notifications/unregister-token
POST /mobile/barber-notifications/test-notification
```

### System Status
```
GET /mobile/system/health
GET /mobile/system/redis-status
POST /mobile/system/test-redis-publish
```

## Real-Time Flow

### 1. Appointment Creation
1. Customer books appointment via mobile app
2. Mobile backend saves appointment with status SCHEDULED
3. `publishAppointmentCreatedEvent()` sends event to Redis
4. Web backend receives event and updates web frontend
5. Barber receives FCM push notification

### 2. Appointment Updates
1. Any appointment status change triggers event
2. Event published to both streams
3. Web frontend shows real-time updates
4. Barber gets notification for relevant changes

### 3. Appointment Cancellation
1. Cancellation triggers `publishAppointmentCancelledEvent()`
2. Real-time notification to web frontend
3. FCM notification to assigned barber

## Configuration

### Redis Connection
Configured to connect to shared Redis server at `54.169.11.244:6379`
```properties
spring.data.redis.host=54.169.11.244
spring.data.redis.port=6379
spring.data.redis.database=1
spring.data.redis.timeout=2000ms
```

### Firebase Cloud Messaging
Add Firebase Admin SDK dependency and configure:
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

Place Firebase service account key in `src/main/resources/` and set environment variable:
```bash
export GOOGLE_APPLICATION_CREDENTIALS=path/to/firebase-service-account-key.json
```

## Database Schema

### Barber Device Tokens
```sql
CREATE TABLE barber_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    barber_id BIGINT NOT NULL,
    device_token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20),
    app_version VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);
```

## Testing

### 1. Test Redis Connectivity
```bash
curl http://localhost:8081/mobile/system/redis-status
```

### 2. Test Event Publishing
```bash
curl -X POST "http://localhost:8081/mobile/system/test-redis-publish?message=Test"
```

### 3. Test FCM Registration
```bash
curl -X POST "http://localhost:8081/mobile/barber-notifications/register-token" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "barberId=1&deviceToken=FCM_TOKEN&deviceType=ANDROID"
```

### 4. Monitor Redis Streams
On web backend server:
```bash
redis-cli XREAD BLOCK 0 STREAMS salon:appointment-events $
redis-cli XREAD BLOCK 0 STREAMS salon:barber-notifications $
```

## Deployment Notes

### Environment Variables
```bash
# Redis Configuration
SPRING_DATA_REDIS_HOST=54.169.11.244
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_DATABASE=1

# Firebase Configuration
GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-key.json
```

### Docker Configuration
Update `docker-compose.yml` to remove local Redis and use external Redis server.

### Security Groups
Ensure mobile backend server (52.221.206.194) can access Redis on web backend server (54.169.11.244:6379).

## Monitoring and Troubleshooting

### Health Checks
- `/mobile/system/health` - Overall system health
- `/mobile/system/redis-status` - Redis connectivity status

### Logs
- Check application logs for Redis connection errors
- Monitor FCM delivery status
- Track consumer group lag in Redis streams

### Common Issues
1. **Redis Connection Failed**: Check security groups and network connectivity
2. **FCM Not Working**: Verify Firebase configuration and device tokens
3. **Events Not Consumed**: Check consumer group status and Redis streams

## Future Enhancements

1. **Message Persistence**: Store notifications in database for offline delivery
2. **Batch Notifications**: Group multiple events for efficiency
3. **A/B Testing**: Different notification strategies for user segments
4. **Analytics**: Track notification delivery and engagement rates
5. **Scheduling**: Support for scheduled notifications and reminders
