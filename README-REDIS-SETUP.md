# Quick Setup Guide for Local Redis Testing

## 🚀 Quick Start (Windows)

### 1. Install Redis on Windows

**Option A: Using Chocolatey (Recommended)**
```powershell
# Run as Administrator
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Then install Redis
choco install redis-64 -y
```

**Option B: Manual Download**
- Download from: https://github.com/MicrosoftArchive/redis/releases
- Install Redis-x64-3.0.504.msi
- Redis starts automatically on port 6379

**Option C: Docker**
```powershell
docker run --name redis-local -p 6379:6379 -d redis:alpine
```

### 2. Test Redis Installation

```powershell
# Test Redis CLI
redis-cli ping
# Should return: PONG

# Test Redis connection from PowerShell
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect("localhost", 6379)
    Write-Host "✅ Redis connection successful" -ForegroundColor Green
    $tcpClient.Close()
} catch {
    Write-Host "❌ Redis connection failed" -ForegroundColor Red
}
```

### 3. Start Your Spring Boot App

```powershell
# In your project directory
.\mvnw.cmd spring-boot:run
```

### 4. Test the Health Endpoints

```powershell
# Test Redis health
curl http://localhost:8080/api/mobile/system/redis-health

# Test Redis status
curl http://localhost:8080/api/mobile/system/redis-status

# Test overall health
curl http://localhost:8080/api/mobile/system/health
```

### 5. Test Appointment Creation → Notification Flow

```powershell
# Create a test appointment
$appointmentData = @{
    customerId = 1
    salonId = 1
    serviceIds = @(1)
    employeeId = 1
    branchId = 1
    appointmentDate = "2025-09-10T10:00:00"
    estimatedEndTime = "2025-09-10T11:00:00"
    servicePrice = 25.00
    discountAmount = 0.00
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer YOUR_JWT_TOKEN"  # Get a valid token first
}

curl -Method POST -Uri "http://localhost:8080/api/mobile/appointments" -Body $appointmentData -Headers $headers
```

## 🔄 For Production Deployment

### Environment Variables (.env file):
```bash
# Production Redis
REDIS_HOST=54.169.11.244
REDIS_PORT=6379
REDIS_PASSWORD=your_production_password

# Local Development Redis
# REDIS_HOST=localhost
# REDIS_PORT=6379
# REDIS_PASSWORD=
```

### Docker Compose for Production:
```yaml
version: '3.8'
services:
  app:
    image: your-app:latest
    environment:
      - REDIS_HOST=54.169.11.244
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    ports:
      - "8080:8080"
```

## 🧪 Testing Real-Time Notifications

### Monitor Redis Streams (if Redis CLI is available):
```bash
# Terminal 1: Monitor appointment events
redis-cli XREAD BLOCK 0 STREAMS salon:appointment-events $

# Terminal 2: Monitor barber notifications  
redis-cli XREAD BLOCK 0 STREAMS salon:barber-notifications $

# Terminal 3: Monitor app logs
```

### Expected Flow:
1. ✅ Create appointment → Redis stream event published
2. ✅ Barber notification consumer picks up event
3. ✅ FCM notification attempted (logs will show)
4. ✅ Cross-backend communication works

## 🐛 Troubleshooting

### Redis Connection Issues:
```powershell
# Check if Redis service is running
Get-Service -Name "*redis*"

# Start Redis service
Start-Service Redis

# Check if port 6379 is in use
netstat -an | findstr 6379
```

### Application Issues:
```powershell
# Check application logs
.\mvnw.cmd spring-boot:run | findstr -i "redis"

# Test specific endpoints
curl http://localhost:8080/api/mobile/system/redis-health -v
```

You're absolutely right - this approach allows you to:
- ✅ **Develop locally** with localhost Redis
- ✅ **Deploy to production** with environment variables
- ✅ **No code changes** needed between environments
- ✅ **Easy testing** and debugging locally
