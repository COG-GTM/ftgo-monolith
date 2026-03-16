# Kubernetes Deployment Guide

## Overview

This document describes the Kubernetes deployment architecture for the FTGO microservices platform. The deployment uses **Kustomize** for environment-specific configuration management across three environments: development, staging, and production.

## Architecture

### Services

| Service | Port | Image |
|---------|------|-------|
| order-service | 8081 | `ghcr.io/cog-gtm/ftgo/order-service` |
| consumer-service | 8082 | `ghcr.io/cog-gtm/ftgo/consumer-service` |
| restaurant-service | 8083 | `ghcr.io/cog-gtm/ftgo/restaurant-service` |
| courier-service | 8084 | `ghcr.io/cog-gtm/ftgo/courier-service` |
| ftgo-mysql | 3306 | `mysql:8.0` |

### Namespace Strategy

| Environment | Namespace | Purpose |
|-------------|-----------|---------|
| Development | `ftgo-dev` | Active development and integration testing |
| Staging | `ftgo-staging` | Pre-production validation |
| Production | `ftgo-prod` | Live production traffic |

## Directory Structure

```
deployment/kubernetes/
├── base/
│   └── kustomization.yaml          # Base Kustomize configuration
├── overlays/
│   ├── dev/
│   │   ├── kustomization.yaml      # Dev-specific patches
│   │   └── namespace.yaml
│   ├── staging/
│   │   ├── kustomization.yaml      # Staging-specific patches
│   │   └── namespace.yaml
│   └── prod/
│       ├── kustomization.yaml      # Prod-specific patches
│       └── namespace.yaml
├── stateful-services/
│   ├── ftgo-mysql-deployment.yml   # MySQL StatefulSet
│   └── ftgo-db-secret.yml          # Database credentials
└── scripts/                        # Deployment helper scripts

services/*/k8s/
├── deployment.yaml                 # Deployment with rolling updates
├── service.yaml                    # ClusterIP Service
├── configmap.yaml                  # Non-sensitive configuration
├── secret.yaml                     # Sensitive credentials (template)
└── hpa.yaml                        # HorizontalPodAutoscaler
```

## Per-Service Manifests

Each microservice has five Kubernetes resource files under `services/<name>/k8s/`:

- **deployment.yaml**: Deployment with rolling update strategy (`maxSurge: 1`, `maxUnavailable: 0`), resource limits, liveness/readiness/startup probes
- **service.yaml**: ClusterIP Service for inter-service communication
- **configmap.yaml**: Non-sensitive environment configuration (Spring profiles, datasource URLs, logging)
- **secret.yaml**: Template for sensitive values (database credentials) — replace placeholder values before applying
- **hpa.yaml**: HorizontalPodAutoscaler with CPU (70%) and memory (80%) targets, scale-up/down policies

## Environment Differences

### Development
- 1 replica per service
- HPA: min 1, max 3
- Reduced resource requests (100m CPU, 256Mi memory)
- Debug logging enabled
- 1Gi MySQL storage

### Staging
- 2 replicas per service
- HPA: min 2, max 5
- Standard resource requests (250m CPU, 512Mi memory)
- Info logging level

### Production
- 3 replicas per service
- HPA: min 3, max 15
- Increased resources (500m CPU, 1Gi memory; limits 1 CPU, 2Gi)
- Warning-level logging
- 20Gi MySQL storage

## Deployment

### Prerequisites

1. `kubectl` configured with cluster access
2. Docker images pushed to `ghcr.io/cog-gtm/ftgo/`
3. Kubernetes Secrets populated with actual credentials (not placeholder values)

### Manual Deployment

```bash
# Deploy to dev
kubectl apply -k deployment/kubernetes/overlays/dev/

# Deploy to staging
kubectl apply -k deployment/kubernetes/overlays/staging/

# Deploy to production
kubectl apply -k deployment/kubernetes/overlays/prod/
```

### Validate Manifests (Dry Run)

```bash
# Validate a specific overlay
kubectl kustomize deployment/kubernetes/overlays/dev/

# Dry-run apply
kubectl apply -k deployment/kubernetes/overlays/dev/ --dry-run=client
```

### Check Deployment Status

```bash
kubectl get all -n ftgo-dev
kubectl rollout status deployment/order-service -n ftgo-dev
```

## CI/CD Workflow

The deployment is automated via `.github/workflows/deploy.yml`:

### Automatic Deployment (Dev)
On push to `feat/microservices-migration` with changes to K8s manifests or deployment config, the workflow automatically deploys to the `dev` environment.

### Manual Promotion
Use the **workflow_dispatch** trigger to promote to staging or production:

1. Go to Actions > "Deploy to Kubernetes"
2. Select the target environment (`staging` or `prod`)
3. Enter the image tag (e.g., `sha-abc1234`)
4. Click "Run workflow"

### Approval Gates
Staging and production environments should be configured with **required reviewers** in GitHub repository settings under Settings > Environments. This provides approval gates before deployment proceeds.

### Deployment Flow

```
Push to branch ──> Auto-deploy to Dev
                         │
                  Manual trigger
                         │
                  Deploy to Staging (approval required)
                         │
                  Manual trigger
                         │
                  Deploy to Production (approval required)
```

## Secrets Management

**Important**: The `secret.yaml` files contain placeholder values (`REPLACE_WITH_ACTUAL_VALUE`). Before deploying:

1. Create secrets manually:
   ```bash
   kubectl create secret generic order-service-secrets \
     --from-literal=SPRING_DATASOURCE_USERNAME=<user> \
     --from-literal=SPRING_DATASOURCE_PASSWORD=<pass> \
     -n ftgo-dev
   ```

2. Or use an external secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault) integrated via the External Secrets Operator.

The MySQL database secret (`ftgo-db-secret`) must also be populated with actual credentials before deployment.

## Health Checks

All services are configured with three types of probes:

| Probe | Path | Initial Delay | Period | Failure Threshold |
|-------|------|---------------|--------|-------------------|
| Startup | `/actuator/health` | 10s | 5s | 12 |
| Liveness | `/actuator/health` | 60s | 15s | 3 |
| Readiness | `/actuator/health` | 30s | 10s | 3 |

The startup probe allows up to 70 seconds for initial JVM and Spring Boot startup before liveness checks begin.

## Rolling Update Strategy

All service deployments use a rolling update strategy:
- `maxSurge: 1` — at most 1 extra pod during update
- `maxUnavailable: 0` — zero downtime during rollout
- `revisionHistoryLimit: 5` — keep last 5 ReplicaSets for rollback

To roll back a deployment:
```bash
kubectl rollout undo deployment/order-service -n ftgo-dev
```
