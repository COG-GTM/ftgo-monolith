# ADR-0002: Container Registry and Docker Image Build Automation

## Status

Accepted

## Date

2026-03-03

## Context

As part of the microservices migration (EM-34), we need to establish automated Docker image builds for each microservice and configure a container registry for storing and distributing images. The monolith has basic Docker support via `docker-compose.yml`, but there is no container registry configured, no image tagging strategy, and no automated build pipeline for individual microservices.

## Decision

### Container Registry: GitHub Container Registry (GHCR)

We chose GHCR (`ghcr.io`) for the following reasons:

- **Native GitHub integration**: Seamless authentication using `GITHUB_TOKEN` in GitHub Actions, no additional secrets needed
- **Package-level permissions**: Fine-grained access control tied to repository permissions
- **Cost**: Free for public repositories, generous limits for private repos within GitHub plans
- **Proximity to source**: Images stored alongside source code in the same platform
- **OCI compliance**: Full support for OCI image specifications

### Multi-stage Dockerfiles

Each microservice gets an optimized multi-stage Dockerfile:

- **Builder stage**: `eclipse-temurin:17-jdk-alpine` with Gradle dependency caching
- **Runtime stage**: `eclipse-temurin:17-jre-alpine` for minimal image size (target < 200MB)
- **Security**: Non-root user (`ftgo:ftgo`, UID 1001), tini init for signal handling
- **Health checks**: Spring Boot Actuator health endpoint monitoring

### Image Tagging Strategy

Images follow the convention `<service>:<version>-<git-sha>`:

| Tag Type | Pattern | Example |
|----------|---------|---------|
| Git SHA | `sha-<short-sha>` | `sha-abc1234` |
| Branch | `<branch-name>` | `main`, `feat-xyz` |
| Semantic version | `<major>.<minor>.<patch>` | `1.0.0` |
| Latest | `latest` | On default branch only |

### CI/CD Pipeline

- **Build & Push**: GitHub Actions workflow triggered on merge to main
- **Vulnerability Scanning**: Trivy scans integrated post-build with SARIF reporting
- **PR Validation**: Hadolint linting + build validation (no push) on PRs
- **Image Cleanup**: Weekly scheduled cleanup of untagged/old images

## Consequences

### Positive

- Automated, reproducible Docker builds for all microservices
- Consistent image tagging enables reliable deployments and rollbacks
- Vulnerability scanning catches security issues before deployment
- Minimal runtime images reduce attack surface and resource usage
- Local development supported via Docker Compose with registry fallback

### Negative

- GHCR rate limits may affect high-frequency CI builds (mitigated by caching)
- Multi-stage builds increase initial build time (mitigated by layer caching)
- Alpine-based images may have compatibility issues with some native libraries (acceptable for JVM workloads)

### Risks

- Base image updates may introduce breaking changes (mitigated by pinned versions)
- GHCR availability dependency (low risk, GitHub has strong SLA)

## References

- [EM-30: Microservices Repository Structure](./0001-mono-repo-structure-and-naming-conventions.md)
- [GitHub Container Registry docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Eclipse Temurin Docker images](https://hub.docker.com/_/eclipse-temurin)
- [Trivy vulnerability scanner](https://github.com/aquasecurity/trivy)
