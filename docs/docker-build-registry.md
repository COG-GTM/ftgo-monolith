# Docker Build and Container Registry Setup

## Overview

This document describes the Docker image build process and container registry configuration for the FTGO microservices. Each microservice has an optimized multi-stage Dockerfile and is automatically built and pushed to GitHub Container Registry (ghcr.io) via GitHub Actions CI.

## Container Registry

**Registry**: GitHub Container Registry (ghcr.io)

**Image Naming Convention**:
```
ghcr.io/cog-gtm/ftgo/<service-name>:<tag>
```

**Available Images**:
| Service | Image |
|---------|-------|
| Order Service | `ghcr.io/cog-gtm/ftgo/order-service` |
| Consumer Service | `ghcr.io/cog-gtm/ftgo/consumer-service` |
| Restaurant Service | `ghcr.io/cog-gtm/ftgo/restaurant-service` |
| Courier Service | `ghcr.io/cog-gtm/ftgo/courier-service` |

## Image Tagging Strategy

Each Docker image is tagged with multiple identifiers for flexibility:

| Tag Pattern | Example | Description | Mutable |
|-------------|---------|-------------|---------|
| `<version>-<sha>` | `1.0.0-abc1234` | Semantic version + git SHA | No |
| `<version>` | `1.0.0` | Semantic version from git tag | No |
| `<branch>` | `feat-microservices-migration` | Branch name (sanitized) | Yes |
| `sha-<sha>` | `sha-abc1234` | Short git commit SHA | No |
| `latest` | `latest` | Latest on default branch (main/master) | Yes |
| `pr-<number>` | `pr-42` | Pull request number (build only, not pushed) | N/A |

**Recommended for Production**: Use the `<version>-<sha>` tag for immutable, traceable deployments.

**Recommended for Development**: Use the `<branch>` tag or `sha-<sha>` for development environments.

## Dockerfile Architecture

Each microservice uses a **multi-stage Docker build** optimized for caching and minimal image size.

### Stage 1: Builder

```
gradle:4.10.2-jdk8 (Builder)
├── Gradle wrapper + build config (cached layer)
├── Shared libraries (cached layer)
└── Service source → Gradle build
```

- Base image: `gradle:4.10.2-jdk8` (matches project's Gradle 4.10.2 + Java 8)
- Dependency caching: Build config files are copied first so that dependency resolution is cached
- Only the service source triggers a rebuild when code changes

### Stage 2: Runtime

```
openjdk:8-jre-alpine (Runtime)
├── curl + dumb-init (minimal utilities)
├── Non-root user (appuser:appgroup)
├── Application JAR
└── Health check + signal handling
```

- Base image: `openjdk:8-jre-alpine` (~85MB vs ~500MB for full JDK)
- **Non-root user**: Runs as `appuser` for security
- **Signal handling**: Uses `dumb-init` as PID 1 for proper SIGTERM propagation
- **Health checks**: Spring Boot Actuator `/actuator/health` endpoint
- **JVM optimizations**: Container-aware memory settings (`-XX:+UseCGroupMemoryLimitForHeap`)

### Service Port Mapping

| Service | Port |
|---------|------|
| Order Service | 8081 |
| Consumer Service | 8082 |
| Restaurant Service | 8083 |
| Courier Service | 8084 |

## CI/CD Pipeline

### Workflow: `.github/workflows/docker-build.yml`

The GitHub Actions workflow automatically builds and pushes Docker images.

**Triggers**:
- **Push** to `feat/microservices-migration`, `main`, or `master`: Builds and pushes images
- **Pull Request** to those branches: Builds images only (no push) for validation

**Features**:
- **Change detection**: Only builds services that have changed (on PRs)
- **Matrix builds**: Parallel builds for all 4 services
- **Docker layer caching**: Uses GitHub Actions cache for faster builds
- **Build summaries**: Generates summary with image tags and metadata
- **Concurrency control**: Cancels in-progress builds when new commits are pushed

### Workflow Diagram

```
Push/PR to branch
      │
      ▼
┌─────────────────┐
│ Detect Changes   │ ── Determines which services changed
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────┐
│ Build Matrix (parallel)                          │
│ ┌─────────────┐ ┌──────────────┐                │
│ │ order-svc   │ │ consumer-svc │ ...             │
│ │ 1. Checkout  │ │ 1. Checkout  │                │
│ │ 2. Buildx    │ │ 2. Buildx    │                │
│ │ 3. Login     │ │ 3. Login     │                │
│ │ 4. Metadata  │ │ 4. Metadata  │                │
│ │ 5. Build+Push│ │ 5. Build+Push│                │
│ └─────────────┘ └──────────────┘                │
└────────┬────────────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│ Build Summary    │ ── Reports overall status
└─────────────────┘
```

## Local Development

### Using Docker Compose

A dedicated `docker-compose.microservices.yml` file is provided for local development:

```bash
# Start all microservices with MySQL
docker-compose -f docker-compose.microservices.yml up --build

# Start a specific service
docker-compose -f docker-compose.microservices.yml up --build order-service

# View logs
docker-compose -f docker-compose.microservices.yml logs -f order-service

# Stop all services
docker-compose -f docker-compose.microservices.yml down

# Stop and remove volumes
docker-compose -f docker-compose.microservices.yml down -v
```

> **Note**: The microservices compose file uses port `3307` for MySQL to avoid conflicts with the monolith's `docker-compose.yml` which uses port `3306`.

### Building Individual Images

```bash
# Build from the repository root
docker build -f services/order-service/docker/Dockerfile -t order-service .
docker build -f services/consumer-service/docker/Dockerfile -t consumer-service .
docker build -f services/restaurant-service/docker/Dockerfile -t restaurant-service .
docker build -f services/courier-service/docker/Dockerfile -t courier-service .
```

### Pulling Images from Registry

```bash
# Pull a specific version
docker pull ghcr.io/cog-gtm/ftgo/order-service:sha-abc1234

# Pull latest from a branch
docker pull ghcr.io/cog-gtm/ftgo/order-service:feat-microservices-migration
```

## Security Considerations

- **Non-root execution**: All containers run as the `appuser` non-root user
- **Minimal base image**: Alpine-based JRE image reduces attack surface
- **No secrets in images**: Environment variables are injected at runtime via Docker Compose or Kubernetes
- **Registry authentication**: Uses `GITHUB_TOKEN` (automatic in GitHub Actions) for ghcr.io push access
- **Image signing**: Can be extended with cosign/sigstore for supply chain security (future enhancement)

## Troubleshooting

### Build fails with "no matching manifest"
Ensure you're building on `linux/amd64`. The Dockerfiles currently target single-platform builds.

### Health check failing
Verify the service is fully started (Spring Boot can take 30-60s). The health check has a 60s start period to account for this.

### Out of memory during build
The Gradle build in the builder stage may need more memory. Increase Docker's memory limit or add `-Dorg.gradle.jvmargs=-Xmx512m` to the build command.

### Cannot pull from ghcr.io
Ensure you have read access to the `COG-GTM` organization packages. For public packages, no authentication is needed. For private packages:
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```
