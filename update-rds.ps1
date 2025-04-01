param(
    [Parameter(Mandatory=$true)]
    [string]$RdsEndpoint
)

Write-Host "Updating RDS endpoint to: $RdsEndpoint" -ForegroundColor Cyan

# Read the application.properties file
$propertiesPath = "src/main/resources/application.properties"
$content = Get-Content $propertiesPath -Raw

# Replace the placeholder with the actual RDS endpoint
$updatedContent = $content -replace "your-aws-rds-endpoint", $RdsEndpoint

# Write the updated content back to the file
Set-Content -Path $propertiesPath -Value $updatedContent

Write-Host "RDS endpoint updated successfully!" -ForegroundColor Green
Write-Host "You can now run the application with: ./run-app.ps1" -ForegroundColor Yellow 