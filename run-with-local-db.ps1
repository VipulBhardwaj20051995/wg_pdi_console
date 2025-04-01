Write-Host "Starting WovenGold PDI with local H2 database..." -ForegroundColor Cyan

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Please install JDK first." -ForegroundColor Red
    exit 1
}

# Check if application jar exists
$jarFile = Get-ChildItem -Path . -Filter "*wovengold*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $jarFile) {
    Write-Host "ERROR: No WovenGold PDI JAR file found. Please build the application first." -ForegroundColor Red
    exit 1
}

Write-Host "Found JAR file: $($jarFile.Name)" -ForegroundColor Green

# Run the application with local profile
Write-Host "Running application with local H2 database profile..." -ForegroundColor Cyan
Write-Host "H2 console will be available at: http://localhost:9090/h2-console" -ForegroundColor Yellow
Write-Host "JDBC URL: jdbc:h2:mem:wovengold_pdi   Username: sa   Password: (empty)" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""

java -jar $jarFile.FullName --spring.profiles.active=local 