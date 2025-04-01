Write-Host "Building WovenGold PDI Application..." -ForegroundColor Cyan
./mvnw clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Check errors above." -ForegroundColor Red
    exit 1
}

Write-Host "Starting WovenGold PDI Application..." -ForegroundColor Green
java -jar target/wovengold-pdi-0.0.1-SNAPSHOT.jar 