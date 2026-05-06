---
name: restart
description: Restart backend (mall-api + mall-admin) and frontend (mall-admin-web) services
version: 1.0.0
---

# Restart Services

Restart the mall platform backend and frontend services.

## Ports

| Service | Port | PID Source |
|---------|------|------------|
| mall-api (C-End) | 8080 | Java process |
| mall-admin (Admin) | 8082 | Java process |
| mall-admin-web | 5174 | Node/Vite dev server |
| mall-mobile (H5) | 3002 | Node process (optional) |

## Steps

### 1. Stop existing services

Kill processes by port to avoid orphaned processes:

```bash
# Stop backend Java processes (mall-api:8080, mall-admin:8082)
for port in 8080 8082; do
  pid=$(netstat -ano 2>/dev/null | grep ":${port} " | grep LISTENING | awk '{print $5}' | head -1)
  if [ -n "$pid" ]; then
    taskkill //F //PID "$pid" 2>/dev/null
    echo "Killed PID $pid on port $port"
  fi
done

# Stop frontend Node processes (mall-admin-web:5174, mall-mobile:3002)
for port in 5174 3002; do
  pid=$(netstat -ano 2>/dev/null | grep ":${port} " | grep LISTENING | awk '{print $5}' | head -1)
  if [ -n "$pid" ]; then
    taskkill //F //PID "$pid" 2>/dev/null
    echo "Killed PID $pid on port $port"
  fi
done
```

### 2. Build backend

```bash
cd D:/project/mall
mvn package -DskipTests -pl mall-common,mall-model,mall-dao,mall-service,mall-api,mall-admin -am
```

### 3. Start backend services

Launch in background:

```bash
# mall-api (C-End API, port 8080)
java -Xmx256m -jar D:/project/mall/mall-api/target/mall-api-1.0.0-SNAPSHOT.jar --server.port=8080 &

# mall-admin (Admin API, port 8082)
java -Xmx256m -jar D:/project/mall/mall-admin/target/mall-admin-1.0.0-SNAPSHOT.jar --server.port=8082 &
```

### 4. Start frontend (if needed)

```bash
# mall-admin-web dev server (port 5174)
cd D:/project/mall/mall-admin-web && npm run dev &

# mall-mobile H5 dev server (port 3002) — only if needed
# cd D:/project/mall/mall-mobile && npm run dev:h5 &
```

### 5. Verify services are up

Wait a few seconds, then check:

```bash
# Check ports are listening
for port in 8080 8082 5174; do
  if netstat -ano 2>/dev/null | grep ":${port} " | grep -q LISTENING; then
    echo "OK: port $port is up"
  else
    echo "FAIL: port $port is NOT up"
  fi
done

# Quick health check on backend APIs
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "8080 not ready"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health 2>/dev/null || echo "8082 not ready"
```

## Notes

- Backend JARs must be rebuilt after Java code changes; frontend Vite dev server hot-reloads automatically.
- If only backend changed, no need to restart frontend.
- If only frontend changed, no need to rebuild/restart backend.
- Docker infra (MySQL, Redis, RabbitMQ, MinIO) should already be running via `docker-compose up -d`.
