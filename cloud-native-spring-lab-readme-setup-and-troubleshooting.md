# Cloud Native Spring Boot Lab -- Setup & Troubleshooting Guide

This document helps a new developer fully set up and run the project
locally, including WSL, Docker, PostgreSQL, networking, and common
BuildKit issues.

------------------------------------------------------------------------

# 1. Prerequisites

## Windows 11 + WSL2

Ensure WSL2 is installed:

``` powershell
wsl --install
```

Verify:

``` powershell
wsl -l -v
```

Ubuntu must show Version 2.

------------------------------------------------------------------------

# 2. WSL Configuration (IMPORTANT)

Prevent Windows path conflicts inside Linux.

Edit:

``` bash
sudo nano /etc/wsl.conf
```

Add:

    [interop]
    appendWindowsPath=false

Then shutdown WSL:

``` powershell
wsl --shutdown
```

Restart Ubuntu.

------------------------------------------------------------------------

# 3. Install Required Tools Inside WSL

``` bash
sudo apt update
sudo apt install -y docker.io maven git
```

Enable Docker:

``` bash
sudo systemctl enable docker
sudo systemctl start docker
```

Add user to docker group:

``` bash
sudo usermod -aG docker $USER
```

Logout & login again.

Verify:

``` bash
docker ps
```

------------------------------------------------------------------------

# 4. PostgreSQL via Docker

Create network:

``` bash
docker network create cloud-net
```

Run Postgres:

``` bash
docker run -d   --name postgres   --network cloud-net   -e POSTGRES_PASSWORD=admin   -p 5432:5432   postgres:16
```

------------------------------------------------------------------------

# 5. Database Initialization

Enter container:

``` bash
docker exec -it postgres psql -U postgres
```

Create databases & users:

``` sql
CREATE DATABASE orderdb;
CREATE USER order_user WITH PASSWORD 'orderpass';
GRANT ALL PRIVILEGES ON DATABASE orderdb TO order_user;

\c orderdb
CREATE SCHEMA orders AUTHORIZATION order_user;
ALTER ROLE order_user IN DATABASE orderdb SET search_path = orders;
```

Repeat similarly for inventorydb.

------------------------------------------------------------------------

# 6. Build Services

Inside each service directory:

``` bash
mvn clean package -DskipTests
```

Ensure Spring Boot plugin includes repackage goal.

------------------------------------------------------------------------

# 7. Dockerfile Structure

``` dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

------------------------------------------------------------------------

# 8. Build Docker Images

``` bash
docker build -t order-service:dev .
docker build -t inventory-service:dev .
```

If BuildKit fails (connection reset by peer), disable temporarily:

``` bash
DOCKER_BUILDKIT=0 docker build -t order-service:dev .
```

------------------------------------------------------------------------

# 9. Common BuildKit Issue

Symptom:

    failed to resolve source metadata for docker.io/library/eclipse-temurin
    connection reset by peer

Solution:

-   Use shorter tag (e.g., eclipse-temurin:21-jre)
-   Or temporarily disable BuildKit
-   Ensure network access from WSL
-   Restart Docker

------------------------------------------------------------------------

# 10. Run Services

Example:

``` bash
docker run --rm   --name order-service   --network cloud-net   -p 8080:8080   -e DB_URL=jdbc:postgresql://postgres:5432/orderdb   -e DB_USER=order_user   -e DB_PASS=orderpass   order-service:dev
```

Access:

    http://localhost:8080/actuator/health

------------------------------------------------------------------------

# 11. Docker Compose (Recommended)

Create docker-compose.yml at project root.

Use .env file for variables.

Run:

``` bash
docker compose up -d
```

------------------------------------------------------------------------

# 12. Common Problems & Solutions

## Problem: "permission denied for schema public"

Solution: - Use separate schema per service - Configure Flyway
default-schema

## Problem: localhost not reachable from container

Inside container: - Use service/container name (postgres) - Not
localhost

## Problem: Maven uses Windows version in WSL

Fix: - Disable Windows path injection via wsl.conf

## Problem: JAVA_HOME not defined

Install OpenJDK:

``` bash
sudo apt install openjdk-21-jdk
```

Set:

``` bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

------------------------------------------------------------------------

# 13. Architecture Expectations

Each service: - Stateless - Environment-driven config - Uses optimistic
locking - Own schema - Dockerized

Postgres: - Single instance - Multiple databases - Schema isolation

------------------------------------------------------------------------

# 14. Final Checklist

-   WSL configured correctly
-   Docker running without sudo
-   Network cloud-net exists
-   Postgres container running
-   Databases created
-   Services built
-   Images built
-   Services can connect via postgres hostname
-   Health endpoints responding

------------------------------------------------------------------------

# End of README
