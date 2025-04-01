Write-Host "Compiling and running MySQL connection test..." -ForegroundColor Cyan

# Check if Java is installed
try {
    $javaVersion = javac -version 2>&1
    Write-Host "Java compiler found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java compiler not found. Please install JDK first." -ForegroundColor Red
    exit 1
}

# Define driver download URL
$mysqlDriverUrl = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
$driverJar = "mysql-connector-j-8.0.33.jar"

# Download MySQL JDBC driver if not already present
if (-not (Test-Path $driverJar)) {
    Write-Host "Downloading MySQL JDBC driver..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $mysqlDriverUrl -OutFile $driverJar
        Write-Host "MySQL JDBC driver downloaded successfully" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Failed to download MySQL JDBC driver: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "MySQL JDBC driver already exists" -ForegroundColor Green
}

# Compile the Java code
Write-Host "Compiling TestRdsConnection.java..." -ForegroundColor Yellow
javac TestRdsConnection.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to compile TestRdsConnection.java" -ForegroundColor Red
    exit 1
}

# Run the Java program
Write-Host "`nRunning MySQL connection test..." -ForegroundColor Cyan
java -cp ".;$driverJar" TestRdsConnection

# Output troubleshooting steps based on results
Write-Host "`nIf connection failed, check these common issues:" -ForegroundColor Yellow
Write-Host "1. Security Group: Ensure port 3306 is open to your IP address" -ForegroundColor Yellow
Write-Host "2. Database Public Access: Make sure 'Publicly Accessible' option is enabled" -ForegroundColor Yellow
Write-Host "3. Database Status: Confirm the RDS instance is in 'Available' state" -ForegroundColor Yellow
Write-Host "4. Network Access: Check if you have any corporate firewall blocking outbound connections" -ForegroundColor Yellow
Write-Host "5. AWS Security: Verify your MySQL password is correct" -ForegroundColor Yellow 