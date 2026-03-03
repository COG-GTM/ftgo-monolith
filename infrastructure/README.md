# FTGO Infrastructure

This directory contains infrastructure-as-code and CI/CD configuration for the FTGO microservices platform.

## Structure

```
infrastructure/
  docker/       # Shared Docker configurations, base images, and docker-compose files
  kubernetes/   # Kubernetes manifests, Helm charts, and cluster configuration
  ci/           # CI/CD pipeline definitions (GitHub Actions, etc.)
```

## Docker

Shared Docker configurations including:
- Base Dockerfiles for Java services
- Docker Compose files for local development
- Container registry configuration

## Kubernetes

Kubernetes deployment resources including:
- Namespace definitions
- Shared ConfigMaps and Secrets
- Ingress and networking configuration
- Helm chart templates

## CI/CD

Continuous integration and deployment pipeline configuration:
- GitHub Actions workflow definitions
- Build and test automation
- Deployment promotion workflows (dev -> staging -> production)

See [ADR-0001](../docs/adr/0001-mono-repo-structure-and-naming-conventions.md) for architectural context.
