# Container Registry & Docker Image Build Automation

> **JIRA**: EM-34 | **Phase**: 2 — CI/CD Pipeline

## Overview

All FTGO microservice Docker images are built automatically by GitHub Actions
and stored in **GitHub Container Registry (GHCR)** at:

```
ghcr.io/cog-gtm/ftgo-monolith/<service-name>
```

| Service                  | Image Path                                                  | Port |
|--------------------------|-------------------------------------------------------------|------|
| Order Service            | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-order-service`         | 8081 |
| Consumer Service         | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-consumer-service`      | 8082 |
| Restaurant Service       | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-restaurant-service`    | 8083 |
| Courier Service          | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-courier-service`       | 8084 |
| MySQL (custom init)      | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-mysql`                 | 3306 |

---

## Image Tagging Strategy

Every image push produces multiple tags:

| Tag Format                      | Description                           | Example                                  |
|---------------------------------|---------------------------------------|------------------------------------------|
| `<version>-<sha>`               | Immutable release tag                 | `0.1.0-a1b2c3d`                          |
| `<branch>`                      | Mutable branch pointer                | `feat-microservices-migration-v4`        |
| `pr-<number>`                   | Pull request preview                  | `pr-42`                                  |
| `latest`                        | Default branch only                   | `latest`                                 |

**Convention**: `<service>:<version>-<git-sha>` (e.g., `ftgo-order-service:0.1.0-a1b2c3d`).

The `SERVICE_VERSION` is defined in `.github/workflows/docker-build-push.yml`
and should be bumped on each release.

---

## CI/CD Pipeline

### Build & Push (`docker-build-push.yml`)

**Trigger**: Push to `main`, `master`, or `feat/microservices-migration-*` branches.

1. **Change detection** — Only services with modified files are built (via `dorny/paths-filter`).
2. **Multi-stage Docker build** — Each service Dockerfile has a build stage (JDK 17) and a runtime stage (JRE Alpine).
3. **Push to GHCR** — Images are pushed on merge; PRs only build (no push).
4. **Vulnerability scanning** — Trivy scans each image for CRITICAL/HIGH CVEs.
5. **Layer caching** — GitHub Actions cache (`type=gha`) accelerates rebuilds.

### Image Cleanup (`docker-cleanup.yml`)

**Trigger**: Weekly (Sunday 02:00 UTC) + manual dispatch.

- Deletes untagged images older than 7 days.
- Keeps at least 10 tagged versions per service.
- Supports dry-run mode via `workflow_dispatch`.

---

## Dockerfile Architecture

Each microservice Dockerfile follows the same multi-stage pattern:

```dockerfile
# Stage 1: Build (eclipse-temurin:17-jdk-alpine)
#   - Copies Gradle wrapper, build configs, shared libs, then service source
#   - Builds the service JAR (tests skipped — they run in CI)

# Stage 2: Runtime (eclipse-temurin:17-jre-alpine)
#   - Non-root user (ftgo:ftgo)
#   - JVM container-aware flags (-XX:+UseContainerSupport, -XX:MaxRAMPercentage=75%)
#   - Health check via wget + /actuator/health
#   - Target image size: <200MB
```

Build context is always the **repository root**:

```bash
docker build -f services/ftgo-order-service/docker/Dockerfile .
```

---

## Vulnerability Scanning

[Trivy](https://github.com/aquasecurity/trivy) runs on every image build:

- **Severity**: CRITICAL and HIGH.
- **Format**: SARIF (uploaded to GitHub Security tab).
- **Artifacts**: Scan results stored for 30 days.
- **Policy**: Scans do not block the build (exit-code `0`), but findings are
  visible in the GitHub Security dashboard for triage.

---

## Local Development

### Pull pre-built images from GHCR

```bash
# Authenticate (one-time)
echo $GITHUB_PAT | docker login ghcr.io -u $GITHUB_USER --password-stdin

# Start all services
docker-compose -f docker-compose-services.yml up -d

# Use a specific tag
IMAGE_TAG=0.1.0-a1b2c3d docker-compose -f docker-compose-services.yml up -d

# Include metrics stack
docker-compose -f docker-compose-services.yml -f docker-compose-metrics.yml up -d
```

### Build images locally

```bash
# Build all services from source
docker-compose \
  -f docker-compose-services.yml \
  -f docker-compose-services.override.yml \
  up -d --build

# Build a single service
docker build -f services/ftgo-order-service/docker/Dockerfile -t ftgo-order-service:local .
```

### Docker Compose files

| File                                    | Purpose                                  |
|-----------------------------------------|------------------------------------------|
| `docker-compose.yml`                    | Legacy monolith (mysql + ftgo-application) |
| `docker-compose-services.yml`           | Microservices from GHCR                  |
| `docker-compose-services.override.yml`  | Override with local builds               |
| `docker-compose-metrics.yml`            | Prometheus + Grafana stack               |

---

## Retention Policy

| Criteria               | Value     |
|------------------------|-----------|
| Tagged versions kept   | 10 per service |
| Untagged max age       | 7 days    |
| Cleanup schedule       | Weekly (Sun 02:00 UTC) |
| Manual trigger         | Yes (with dry-run option) |

---

## Access & Permissions

- **CI Authentication**: Uses `GITHUB_TOKEN` (automatic in GitHub Actions).
- **Developer Authentication**: Personal Access Token (PAT) with `read:packages` scope.
- **Repository visibility**: Images inherit the repository's visibility settings.

```bash
# Generate a PAT at https://github.com/settings/tokens
# Required scope: read:packages (write:packages for pushing)
echo $GITHUB_PAT | docker login ghcr.io -u $GITHUB_USER --password-stdin
```
