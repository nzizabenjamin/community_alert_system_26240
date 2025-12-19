# PowerShell script to view Spring Boot logs with filtering
# Usage: .\view-logs.ps1 [filter]

param(
    [string]$Filter = "error|Error|ERROR|exception|Exception|EXCEPTION|DEBUG|WARN"
)

Write-Host "=== Spring Boot Application Logs ===" -ForegroundColor Cyan
Write-Host "Filtering for: $Filter" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
Write-Host ""

# If running in background, you can tail a log file
# For now, just show instructions
Write-Host "To view logs:" -ForegroundColor Green
Write-Host "1. Run: mvn spring-boot:run" -ForegroundColor White
Write-Host "2. Watch the console output" -ForegroundColor White
Write-Host "3. Look for lines containing: DEBUG, ERROR, WARN, Exception" -ForegroundColor White
Write-Host ""
Write-Host "To filter logs while running, use:" -ForegroundColor Green
Write-Host "  mvn spring-boot:run 2>&1 | Select-String -Pattern 'error|Error|ERROR|exception|Exception|DEBUG' -Context 2,2" -ForegroundColor White

