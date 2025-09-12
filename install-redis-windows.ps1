# install-redis-windows.ps1
# Script to install Redis on Windows for local development

Write-Host "=== Installing Redis for Windows ===" -ForegroundColor Cyan

# Method 1: Using Chocolatey (Recommended)
Write-Host "`n1. Checking if Chocolatey is installed..." -ForegroundColor Yellow
if (Get-Command choco -ErrorAction SilentlyContinue) {
    Write-Host "✅ Chocolatey found. Installing Redis..." -ForegroundColor Green
    try {
        choco install redis-64 -y
        Write-Host "✅ Redis installed via Chocolatey" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to install Redis via Chocolatey: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Chocolatey not found. Please install Chocolatey first:" -ForegroundColor Red
    Write-Host "Run this in Admin PowerShell:" -ForegroundColor Yellow
    Write-Host 'Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString("https://community.chocolatey.org/install.ps1"))' -ForegroundColor Cyan
    
    # Method 2: Manual Download
    Write-Host "`n2. Alternative: Manual Installation" -ForegroundColor Yellow
    Write-Host "Download Redis from: https://github.com/MicrosoftArchive/redis/releases" -ForegroundColor Cyan
    Write-Host "- Download Redis-x64-3.0.504.msi" -ForegroundColor White
    Write-Host "- Install with default settings" -ForegroundColor White
    Write-Host "- Redis will start automatically on port 6379" -ForegroundColor White
}

# Method 3: Docker (Alternative)
Write-Host "`n3. Alternative: Using Docker" -ForegroundColor Yellow
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "✅ Docker found. You can also run Redis in Docker:" -ForegroundColor Green
    Write-Host "docker run --name redis-local -p 6379:6379 -d redis:alpine" -ForegroundColor Cyan
    Write-Host "docker start redis-local  # to start later" -ForegroundColor Cyan
    Write-Host "docker stop redis-local   # to stop" -ForegroundColor Cyan
} else {
    Write-Host "❌ Docker not found" -ForegroundColor Red
}

Write-Host "`n=== Next Steps ===" -ForegroundColor Cyan
Write-Host "1. Restart PowerShell after installation" -ForegroundColor White
Write-Host "2. Test Redis: redis-cli ping" -ForegroundColor White
Write-Host "3. Redis should respond with: PONG" -ForegroundColor White
Write-Host "4. Then test your Spring Boot app" -ForegroundColor White
