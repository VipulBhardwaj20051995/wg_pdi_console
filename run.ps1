Write-Host "Building and running the WovenGold PDI application..." -ForegroundColor Green

# Stop any existing Java processes
Write-Host "Stopping existing processes..." -ForegroundColor Yellow
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

# Clean and compile the application
Write-Host "Compiling application..." -ForegroundColor Yellow
./mvnw clean compile

# Run the application with Spring Boot
Write-Host "Starting application..." -ForegroundColor Cyan
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=9090" 