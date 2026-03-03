# FTGO Base Images

## Approved Base Images

All FTGO microservices use the following approved base images:

### Build Stage
- **eclipse-temurin:17-jdk-alpine** - OpenJDK 17 JDK on Alpine Linux for building applications

### Runtime Stage
- **eclipse-temurin:17-jre-alpine** - OpenJDK 17 JRE on Alpine Linux for running applications

## Image Selection Rationale

| Criteria | Choice | Rationale |
|----------|--------|-----------|
| JDK Vendor | Eclipse Temurin | Well-maintained, TCK-certified, widely adopted |
| Java Version | 17 (LTS) | Long-term support, modern features, container support |
| OS Base | Alpine Linux | Minimal footprint (~5MB), reduces attack surface |
| Runtime | JRE only | Excludes development tools, smaller image size |

## Security Requirements

- All runtime images use non-root user (`ftgo:ftgo`, UID/GID 1001)
- Tini init system for proper PID 1 signal handling
- Health checks configured on all service containers
- No shell access in production (consider distroless for future)

## Image Size Targets

| Service | Target Size | Notes |
|---------|-------------|-------|
| ftgo-order-service | < 200MB | Includes JRE + application JAR |
| ftgo-consumer-service | < 200MB | Includes JRE + application JAR |
| ftgo-restaurant-service | < 200MB | Includes JRE + application JAR |
| ftgo-courier-service | < 200MB | Includes JRE + application JAR |

## Updating Base Images

When updating base images:
1. Test in CI first with the new image tag
2. Run vulnerability scan (Trivy) against the new base
3. Verify all health checks pass
4. Update all Dockerfiles consistently
