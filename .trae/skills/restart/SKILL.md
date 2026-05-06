---
name: restart
description: Restart backend (mall-api + mall-admin) and frontend (mall-admin-web + mall-mobile) services
version: 2.0.0
---

# Restart Services

Restart the mall platform backend and frontend services, including mobile services.

## Ports

| Service | Port | PID Source |
|---------|------|------------|
| mall-api (Mobile/C-End) | 8080 | Java process |
| mall-admin (Admin) | 8082 | Java process |
| mall-admin-web | 5174 | Node/Vite dev server |
| mall-mobile (H5) | 3002 | Node process |

## Steps

### 1. Stop existing services

Kill processes by port to avoid orphaned processes:

```powershell
# Stop backend Java processes (mall-api:8080, mall-admin:8082)
foreach ($port in 8080, 8082) {
    $pids = (netstat -ano | Select-String ":${port} " | Select-String "LISTENING" | ForEach-Object { ($_ -split '\s+')[-1] })
    if ($pids) {
        foreach ($pid in $pids) {
            try {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                Write-Host "Killed PID $pid on port $port"
            } catch {
                Write-Host "Failed to kill PID $pid on port $port"
            }
        }
    }
}

# Stop frontend Node processes (mall-admin-web:5174, mall-mobile:3002)
foreach ($port in 5174, 3002) {
    $pids = (netstat -ano | Select-String ":${port} " | Select-String "LISTENING" | ForEach-Object { ($_ -split '\s+')[-1] })
    if ($pids) {
        foreach ($pid in $pids) {
            try {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                Write-Host "Killed PID $pid on port $port"
            } catch {
                Write-Host "Failed to kill PID $pid on port $port"
            }
        }
    }
}
```

### 2. Build backend (optional - use existing JARs)

```powershell
# Skip rebuild to save time - use existing JARs
Write-Host "Skipping rebuild, using existing JAR files"
```

### 3. Start backend services

Launch mall-api (mobile API) and mall-admin (admin API):

```powershell
# mall-api (Mobile/C-End API, port 8080)
Write-Host "Starting mall-api on port 8080..."
$apiProcess = Start-Process -FilePath "java" -ArgumentList "-Xmx256m", "-jar", "D:\project\mall\mall-api\target\mall-api-1.0.0-SNAPSHOT.jar", "--server.port=8080" -WindowStyle Normal -PassThru
Write-Host "mall-api started with PID $($apiProcess.Id)"

# mall-admin (Admin API, port 8082)
Write-Host "Starting mall-admin on port 8082..."
$adminProcess = Start-Process -FilePath "java" -ArgumentList "-Xmx256m", "-jar", "D:\project\mall\mall-admin\target\mall-admin-1.0.0-SNAPSHOT.jar", "--server.port=8082" -WindowStyle Normal -PassThru
Write-Host "mall-admin started with PID $($adminProcess.Id)"

# Wait for backends to start
Write-Host "Waiting for backends to start..."
Start-Sleep -Seconds 15
```

### 4. Start frontend services

Launch both mall-admin-web and mall-mobile:

```powershell
# mall-admin-web dev server (port 5174)
Write-Host "Starting mall-admin-web on port 5174..."
$adminWebProcess = Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd D:\project\mall\mall-admin-web; npm run dev" -WindowStyle Normal -PassThru
Write-Host "mall-admin-web started with PID $($adminWebProcess.Id)"

# mall-mobile H5 dev server (port 3002)
Write-Host "Starting mall-mobile on port 3002..."
$mobileProcess = Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd D:\project\mall\mall-mobile; npm run dev:h5" -WindowStyle Normal -PassThru
Write-Host "mall-mobile started with PID $($mobileProcess.Id)"

# Wait for frontends to start
Write-Host "Waiting for frontends to start..."
Start-Sleep -Seconds 12
```

### 5. Verify services are up

```powershell
# Check all ports are listening
$ports = @(8080, 8082, 5174, 3002)
$serviceNames = @{
    8080 = "mall-api (mobile backend)"
    8082 = "mall-admin (admin backend)"
    5174 = "mall-admin-web (admin frontend)"
    3002 = "mall-mobile (mobile frontend)"
}

Write-Host ""
Write-Host "=== Service Status Check ==="
foreach ($port in $ports) {
    $listening = (netstat -ano | Select-String ":${port} " | Select-String "LISTENING")
    $serviceName = $serviceNames[$port]
    if ($listening) {
        Write-Host "✅ OK: $serviceName (port $port) is up"
    } else {
        Write-Host "❌ FAIL: $serviceName (port $port) is NOT up"
    }
}

Write-Host ""
Write-Host "=== Access URLs ==="
Write-Host "Mobile App:      http://localhost:3002"
Write-Host "Admin Console:   http://localhost:5174"
Write-Host "Mobile API Doc:  http://localhost:8080/doc.html"
Write-Host "Admin API Doc:   http://localhost:8082/doc.html"
```

## Notes

- This will start all services: mall-api (8080), mall-admin (8082), mall-admin-web (5174), and mall-mobile (3002)
- Using existing JAR files to save time
- Docker infra (MySQL, Redis, RabbitMQ, MinIO) should already be running
- mall-api is required for mobile app functionality
- mall-mobile runs as a UniApp H5 dev server
