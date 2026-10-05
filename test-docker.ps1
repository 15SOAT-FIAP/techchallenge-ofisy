$ErrorActionPreference = "Stop"

Write-Host "Logging in..."
$body = @{ email = "admin@ofisy.com"; password = "password" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/login" -Method Post -Body $body -ContentType "application/json"
$token = $response.token

if (-not $token) {
    Write-Host "Failed to obtain token"
    exit 1
}

$headers = @{ Authorization = "Bearer $token" }

$stocks = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/stocks" -Method Get -Headers $headers
if (-not $stocks.content -or $stocks.content.Count -eq 0) {
    Write-Host "No stock found to test!"
    exit 1
}

# Find a stock with some quantity, or just pick the next one
$stockId = $stocks.content[5].id
$currentQty = $stocks.content[5].quantity

if ($currentQty -le 0) {
    $currentQty = 5 # arbitrary if it's already 0
}

Write-Host "Consuming $currentQty units of stock $stockId..."
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/stocks/$stockId/consume?quantity=$currentQty" -Method Post -Headers $headers

Write-Host "Consumed stock. Event published to SQS!"
