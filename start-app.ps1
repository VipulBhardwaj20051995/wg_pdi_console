Write-Host "Starting WovenGold PDI Application" -ForegroundColor Green
Write-Host "---------------------------------" -ForegroundColor Green

# Stop any existing Java processes
Write-Host "Stopping any existing Java processes..." -ForegroundColor Yellow
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

# Clean and compile the project
Write-Host "Building the application..." -ForegroundColor Yellow
./mvnw clean install -DskipTests

# Check if build was successful
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! See errors above." -ForegroundColor Red
    exit 1
}

# Create uploads directory if it doesn't exist
if (-not (Test-Path -Path "./uploads")) {
    Write-Host "Creating uploads directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "./uploads" | Out-Null
}

# Run the application
Write-Host "Starting the application on port 9090..." -ForegroundColor Cyan
Write-Host "API testing instructions are in api-tests.md" -ForegroundColor Cyan
Write-Host "---------------------------------" -ForegroundColor Green

# Run the application and capture the process ID
$process = Start-Process -FilePath "java" -ArgumentList "-jar", "target/wovengold-pdi-0.0.1-SNAPSHOT.jar", "--server.port=9090" -PassThru

# Display process info 
Write-Host "Application started with Process ID: $($process.Id)" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the application when done" -ForegroundColor Yellow

# Keep the script running until user presses Ctrl+C
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
}
finally {
    # Clean up when the script is interrupted
    Write-Host "Stopping application..." -ForegroundColor Yellow
    Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
} 