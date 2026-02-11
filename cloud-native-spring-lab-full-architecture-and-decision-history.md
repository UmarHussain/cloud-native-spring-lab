
# Cloud Native Spring Boot Lab – Full Architectural + Decision History Snapshot

---
# 1. Executive Overview

This document represents the complete architectural state and decision evolution of the Cloud-Native Spring Boot Lab.

The system evolved from:
- Running services in IntelliJ
- To containerized microservices in Docker
- To orchestrated services via Docker Compose
- To production-aligned Kubernetes-ready architecture

Key design goals:
- Stateless microservices
- Data-layer concurrency correctness
- Environment-driven configuration
- Production-grade containerization
- Clean separation of responsibilities
- Cloud-native portability

---
# 2. System Architecture

## Services

### order-service
Responsibilities:
- Accept order requests
- Validate order payload
- Call inventory-service
- Maintain order state
- Apply optimistic locking

### inventory-service
Responsibilities:
- Manage SKU stock
- Reserve/deduct stock
- Handle concurrency conflicts

## Infrastructure Components

- PostgreSQL 16 (single instance, multiple databases)
- Redis (sessions + caching)
- Docker Engine (WSL2 Ubuntu)
- Docker Compose (local orchestration)
- Kubernetes (target deployment model)

---
# 3. Database Strategy

Single PostgreSQL container.

Multiple databases:
- orderdb
- inventorydb

Separate users:
- order_user
- inventory_user

Separate schemas:
- orders
- inventory

Example setup:

```sql
CREATE DATABASE orderdb;
CREATE USER order_user WITH PASSWORD 'orderpass';
GRANT ALL PRIVILEGES ON DATABASE orderdb TO order_user;

\c orderdb
CREATE SCHEMA orders AUTHORIZATION order_user;
ALTER ROLE order_user IN DATABASE orderdb SET search_path = orders;
```

---
# 4. Concurrency Strategy

## Optimistic Locking

```java
@Version
private Long version;
```

Exception Handling:

```java
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
ResponseEntity<?> handleOptimistic() {
    return ResponseEntity.status(409)
        .body("Concurrent update detected");
}
```

## Retry Pattern

```java
for (int i = 0; i < 3; i++) {
    try {
        postTxAttempt(...);
        break;
    } catch (OptimisticLockException e) {
        Thread.sleep(50);
    }
}
```

---
# 5. Idempotency Strategy

- Unique DB constraint on idempotency_key
- Duplicate requests return stored result
- Prevents double stock deduction

---
# 6. Caching Strategy

## Cache-Aside Pattern

1. Read Redis
2. On miss → read DB
3. Populate cache AFTER_COMMIT

```java
@TransactionalEventListener(phase = AFTER_COMMIT)
public void updateCache(Event e) {
    redisTemplate.opsForValue().set(...);
}
```

## Redis CAS Lua

```lua
local currentVersion = redis.call("HGET", KEYS[1], "version")
if tonumber(ARGV[1]) > tonumber(currentVersion) then
    redis.call("HMSET", KEYS[1], "data", ARGV[2], "version", ARGV[1])
end
```

---
# 7. Authentication & Session Model

## JWT Example

```json
{
  "sub": "userId",
  "perms": ["ORDER_CREATE"],
  "pv": 5,
  "sid": "session-id",
  "iat": 1710000000,
  "exp": 1710000900
}
```

## Redis Session Key

```
session:{sessionId}
```

Fields:
- userId
- createdAt
- lastSeenAt
- permissionVersion

---
# 8. Configuration Strategy

application.yml:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/orderdb}
    username: ${DB_USER:order_user}
    password: ${DB_PASS:orderpass}
```

Docker Compose:

```yaml
environment:
  DB_URL: ${ORDER_DB_URL}
  DB_USER: ${ORDER_DB_USER}
  DB_PASS: ${ORDER_DB_PASS}
```

---
# 9. Docker Setup

Dockerfile:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Docker network:

```bash
docker network create cloud-net
docker network connect cloud-net postgres
```

---
# 10. Docker Compose

```yaml
services:
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"

  order-service:
    build: ./services/order-service
    environment:
      DB_URL: ${ORDER_DB_URL}
      DB_USER: ${ORDER_DB_USER}
      DB_PASS: ${ORDER_DB_PASS}
```

---
# 11. WSL Configuration

Disable Windows PATH injection:

```
/etc/wsl.conf
[interop]
appendWindowsPath=false
```

Install tools:

```bash
sudo apt install -y docker.io maven git
```

---
# 12. Kubernetes Structure

```
K8s/
  postgres-deployment.yaml
  order-deployment.yaml
  inventory-deployment.yaml
  service.yaml
  configmap.yaml
  secret.yaml
```

Deployment example:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: order
          image: order-service:dev
          envFrom:
            - configMapRef:
                name: order-config
```

---
# 13. Key Decisions & Lessons

- Optimistic locking scales across replicas
- Schema isolation avoids public schema permission issues
- Environment-driven configuration follows 12-Factor principles
- Docker Compose orchestrates the full system locally
- localhost inside container ≠ host machine
- Plugin version must be specified without Spring Boot parent
- Attach containers to networks instead of recreating them

---
# End of Document
