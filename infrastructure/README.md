# Infrastructure

Deployment configurations and environment management for the FTGO microservices platform.

## Structure

```
infrastructure/
  +-- docker/          # Docker Compose files for local development
  +-- kubernetes/
  |   +-- base/        # Base Kubernetes manifests (shared across envs)
  |   +-- overlays/    # Kustomize overlays per environment
  |       +-- dev/
  |       +-- staging/
  |       +-- prod/
  +-- config/          # Externalized configuration per environment
  |   +-- dev/
  |   +-- staging/
  |   +-- prod/
  +-- scripts/         # Build and deployment helper scripts
```

## Local Development

```bash
docker-compose -f infrastructure/docker/docker-compose.services.yml up
```
