# Container Registry and Docker Image Build Automation

## Overview

The FTGO platform uses **GitHub Container Registry (GHCR)** at `ghcr.io` for storing and distributing Docker images. Automated CI/CD pipelines build, scan, and push images on every merge to the main branch.

## Architecture

```
                    ┌─────────────────────┐
                    │   GitHub Actions CI  │
                    │                      │
   Push to main ──▶│  1. Gradle Build     │
                    │  2. Docker Build     │
                    │  3. Push to GHCR     │
                    │  4. Trivy Scan       │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │   GitHub Container   │
                    │   Registry (GHCR)    │
                    │   ghcr.io/cog-gtm/  │
                    │     ftgo/<service>   │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                 ▼
        Local Dev        Staging/Prod      Vulnerability
        (docker compose) (K8s deploy)      Scan Reports
```

## Container Registry

### Registry Details

| Property       | Value                                      |
|----------------|--------------------------------------------|
| Registry       | GitHub Container Registry (GHCR)           |
| Base URL       | `ghcr.io`                                  |
| Image prefix   | `ghcr.io/cog-gtm/ftgo/<service-name>`      |
| Authentication | `GITHUB_TOKEN` (CI) or GitHub PAT (local)  |

### Available Images

| Image                                              | Description             | Port |
|----------------------------------------------------|-------------------------|------|
| `ghcr.io/cog-gtm/ftgo/ftgo-order-service`          | Order Service           | 8081 |
| `ghcr.io/cog-gtm/ftgo/ftgo-consumer-service`       | Consumer Service        | 8082 |
| `ghcr.io/cog-gtm/ftgo/ftgo-restaurant-service`     | Restaurant Service      | 8083 |
| `ghcr.io/cog-gtm/ftgo/ftgo-courier-service`        | Courier Service         | 8084 |
| `ghcr.io/cog-gtm/ftgo/ftgo-application`            | Legacy Monolith         | 8080 |
| `ghcr.io/cog-gtm/ftgo/ftgo-mysql`                  | MySQL with FTGO schema  | 3306 |

## Image Tagging Strategy

Images are tagged using a multi-tag strategy for flexibility and traceability:

| Tag Format                        | Example                          | Description                    |
|-----------------------------------|----------------------------------|--------------------------------|
| `<version>-<git-sha>`            | `0.1.0-a1b2c3d`                 | Immutable release tag          |
| `<branch-name>`                  | `main`, `feat-microservices-v3`  | Latest from branch             |
| `latest`                         | `latest`                         | Latest from default branch     |
| `<sha>`                          | `a1b2c3d`                        | Git SHA short reference        |

### Tag Selection Guide

- **Production deployments**: Use `<version>-<git-sha>` for reproducibility
- **Staging/testing**: Use `<branch-name>` for latest from a branch
- **Local development**: Use `latest` for convenience

## Dockerfiles

### Location

All Dockerfiles are in `infrastructure/docker/dockerfiles/`:

| File                   | Purpose                                       |
|------------------------|-----------------------------------------------|
| `Dockerfile.service`   | Multi-stage build for microservices            |
| `Dockerfile.monolith`  | Multi-stage build for legacy monolith          |
| `Dockerfile.mysql`     | Custom MySQL with pre-loaded schema            |
| `.dockerignore`        | Build context exclusions                       |

### Multi-Stage Build Design

The `Dockerfile.service` uses a two-stage build:

**Stage 1 - Builder (`eclipse-temurin:17-jdk-jammy`)**:
- Copies Gradle wrapper and build config (cached layer)
- Copies shared libraries needed for compilation
- Downloads dependencies (cached layer)
- Copies service source and builds with Gradle

**Stage 2 - Runtime (`eclipse-temurin:17-jre-jammy`)**:
- Minimal JRE-only base image
- Non-root `ftgo` user for security
- Health checks via Spring Boot Actuator
- Proper signal handling with `exec` form entrypoint
- JVM tuned for container environments

### Build Arguments

The `Dockerfile.service` accepts these build arguments:

| Argument       | Description                                   | Example                              |
|----------------|-----------------------------------------------|--------------------------------------|
| `SERVICE_NAME` | Name of the service                           | `ftgo-order-service`                 |
| `SERVICE_PORT` | Port the service listens on                   | `8081`                               |
| `SERVICE_PATH` | Gradle project path relative to root          | `services/ftgo-order-service`        |

### Building Locally

```bash
# Build a specific service
docker build \
  --build-arg SERVICE_NAME=ftgo-order-service \
  --build-arg SERVICE_PORT=8081 \
  --build-arg SERVICE_PATH=services/ftgo-order-service \
  -f infrastructure/docker/dockerfiles/Dockerfile.service \
  -t ftgo-order-service:local .

# Build the monolith
docker build \
  -f infrastructure/docker/dockerfiles/Dockerfile.monolith \
  -t ftgo-application:local .

# Build MySQL
docker build \
  -f infrastructure/docker/dockerfiles/Dockerfile.mysql \
  -t ftgo-mysql:local .
```

## CI/CD Workflows

### Docker Build & Push (`docker-build-push.yml`)

**Triggers**: Push to `main`, `master`, or `feat/microservices-migration-v3`

**What it does**:
1. Determines version from `gradle.properties`
2. Builds all service images in parallel using a matrix strategy
3. Pushes images to GHCR with multi-tag strategy
4. Uses GitHub Actions cache for Docker layer caching

**Manual trigger**: Supports `workflow_dispatch` with options for:
- Selecting specific services to build
- Enabling/disabling push to registry

### Vulnerability Scan (`docker-vulnerability-scan.yml`)

**Triggers**: After successful Docker build, weekly schedule, or manual

**What it does**:
1. Scans each service image with Trivy for OS and library vulnerabilities
2. Scans Dockerfiles for misconfigurations
3. Uploads results to GitHub Security tab (SARIF format)
4. Fails on CRITICAL or HIGH vulnerabilities

### Image Cleanup (`docker-cleanup.yml`)

**Triggers**: Daily at 02:00 UTC, or manual

**Retention policy**:
- Keeps latest 10 tagged versions per service
- Removes untagged versions
- Supports dry-run mode for testing

## Local Development

### Using Registry Images

The fastest way to run services locally is to pull pre-built images:

```bash
# 1. Authenticate with GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin

# 2. Set required environment variables
export DB_PASSWORD=mysqlpw

# 3. Start all services
docker compose -f infrastructure/docker/docker-compose.registry.yml up -d

# 4. Start with monitoring
docker compose \
  -f infrastructure/docker/docker-compose.registry.yml \
  -f infrastructure/docker/docker-compose.monitoring.yml up -d

# 5. Use a specific image version
IMAGE_TAG=0.1.0-a1b2c3d docker compose \
  -f infrastructure/docker/docker-compose.registry.yml up -d
```

### Using Local Builds

For development with local code changes:

```bash
# Use the services compose file (builds from source)
docker compose -f infrastructure/docker/docker-compose.services.yml up -d --build
```

### Docker Compose Files

| File                              | Purpose                            |
|-----------------------------------|------------------------------------|
| `docker-compose.registry.yml`     | Pull pre-built images from GHCR    |
| `docker-compose.services.yml`     | Build from local source code       |
| `docker-compose.monitoring.yml`   | Prometheus + Grafana stack          |
| `../../docker-compose.yml`        | Legacy monolith compose (root)     |

## Security

### Image Security Features

- **Non-root user**: All services run as the `ftgo` user
- **Minimal base images**: Eclipse Temurin JRE-only images (no JDK in runtime)
- **No secrets in images**: Configuration via environment variables
- **Health checks**: All services include Docker HEALTHCHECK instructions
- **Vulnerability scanning**: Automated Trivy scans on every build
- **OCI labels**: Proper image metadata for traceability

### Vulnerability Scanning

Trivy scans cover:
- **OS packages**: Vulnerabilities in base image packages
- **Application libraries**: Vulnerabilities in Java dependencies
- **Dockerfile misconfigurations**: Security best practice violations

Results are uploaded to the GitHub **Security** tab for centralized tracking.

## Troubleshooting

### Authentication Issues

```bash
# Verify GHCR authentication
docker login ghcr.io -u USERNAME
# Use a GitHub PAT with read:packages scope

# Check if image exists
docker manifest inspect ghcr.io/cog-gtm/ftgo/ftgo-order-service:latest
```

### Build Failures

```bash
# Build with verbose output
docker build --progress=plain \
  --build-arg SERVICE_NAME=ftgo-order-service \
  --build-arg SERVICE_PORT=8081 \
  --build-arg SERVICE_PATH=services/ftgo-order-service \
  -f infrastructure/docker/dockerfiles/Dockerfile.service .

# Check image size
docker images | grep ftgo
```

### Service Health

```bash
# Check service health
docker compose -f infrastructure/docker/docker-compose.registry.yml ps

# View service logs
docker compose -f infrastructure/docker/docker-compose.registry.yml logs ftgo-order-service

# Access actuator endpoints
curl http://localhost:8081/actuator/health
```
