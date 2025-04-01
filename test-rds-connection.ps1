Write-Host "Testing RDS connection to dev.c9ic2i82ui7u.us-east-1.rds.amazonaws.com:3306" -ForegroundColor Cyan

# Test TCP connectivity 
$hostname = "dev.c9ic2i82ui7u.us-east-1.rds.amazonaws.com"
$port = 3306
$timeout = 5000

$tcpClient = New-Object System.Net.Sockets.TcpClient
$connection = $tcpClient.BeginConnect($hostname, $port, $null, $null)
$wait = $connection.AsyncWaitHandle.WaitOne($timeout, $false)

if (-not $wait) {
    Write-Host "Connection to $hostname on port $port failed (timeout)" -ForegroundColor Red
    $tcpClient.Close()
} else {
    try {
        $tcpClient.EndConnect($connection)
        Write-Host "Connection to $hostname on port $port successful!" -ForegroundColor Green
    } catch {
        Write-Host "Connection to $hostname on port $port failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    $tcpClient.Close()
}

Write-Host "`nIP address resolution:" -ForegroundColor Cyan
try {
    $ipAddresses = [System.Net.Dns]::GetHostAddresses($hostname)
    foreach ($ip in $ipAddresses) {
        Write-Host "$hostname resolves to: $ip" -ForegroundColor Green
    }
} catch {
    Write-Host "Failed to resolve $hostname: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nConnection instructions:" -ForegroundColor Cyan
Write-Host "1. Make sure your security group allows connections from your IP on port 3306" -ForegroundColor Yellow
Write-Host "2. Check that the database instance is running and publicly accessible" -ForegroundColor Yellow
Write-Host "3. Verify your local firewall allows outbound traffic on port 3306" -ForegroundColor Yellow
Write-Host "4. Consider using an SSH tunnel or VPN if the database is in a private subnet" -ForegroundColor Yellow 