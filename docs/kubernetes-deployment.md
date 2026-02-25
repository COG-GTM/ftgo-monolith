# Kubernetes Deployment Guide

## Overview

This document describes the Kubernetes deployment architecture for the FTGO platform microservices. The deployment uses **Kustomize** for environment-specific configuration management across three environments: `dev`, `staging`, and `prod`.

## Architecture

### Directory Structure

```
infrastructure/k8s/
├── base/                          # Shared base manifests
│   ├── kustomization.yaml         # Base kustomization (includes all resources)
│   ├── namespace.yaml             # Default namespace
│   ├── mysql/                     # MySQL StatefulSet (apps/v1)
│   │   ├── kustomization.yaml
│   │   ├── statefulset.yaml       # MySQL StatefulSet with secret refs
│   │   ├── service.yaml           # Headless service for StatefulSet
│   │   ├── configmap.yaml         # Non-sensitive MySQL config
│   │   └── secret.yaml            # Placeholder secret (replace in prod)
│   ├── services/                  # Per-service ConfigMaps, Secrets, HPAs
│   │   ├── order-service/
│   │   ├── consumer-service/
│   │   ├── restaurant-service/
│   │   └── courier-service/
│   ├── ingress/                   # Ingress configuration
│   │   └── ingress.yaml           # Path-based routing to services
│   ├── patches/                   # Strategic merge patches
│   │   ├── rolling-update-strategy.yaml  # Zero-downtime rolling updates
│   │   └── env-from-configmap.yaml       # ConfigMap injection
│   └── external-secrets/          # ExternalSecret templates (optional)
│       ├── cluster-secret-store.yaml
│       ├── external-secret-mysql.yaml
│       └── external-secret-services.yaml
├── overlays/
│   ├── dev/                       # Development environment
│   ├── staging/                   # Staging environment
│   └── prod/                      # Production environment
```

### Services

| Service | Port | Image |
|---------|------|-------|
| ftgo-order-service | 8081 | ghcr.io/cog-gtm/ftgo/ftgo-order-service |
| ftgo-consumer-service | 8082 | ghcr.io/cog-gtm/ftgo/ftgo-consumer-service |
| ftgo-restaurant-service | 8083 | ghcr.io/cog-gtm/ftgo/ftgo-restaurant-service |
| ftgo-courier-service | 8084 | ghcr.io/cog-gtm/ftgo/ftgo-courier-service |
| ftgo-mysql | 3306 | mysql:8.0 |

### Namespace Strategy

| Environment | Namespace | Purpose |
|-------------|-----------|---------|
| Development | `ftgo-dev` | Feature development and testing |
| Staging | `ftgo-staging` | Pre-production validation |
| Production | `ftgo-prod` | Live traffic |

## Environment Differences

| Configuration | Dev | Staging | Prod |
|---------------|-----|---------|------|
| Replicas (services) | 1 | 2 | 3 |
| HPA max replicas | 2 | 5 | 10 |
| CPU request | 100m | 200m | 500m |
| CPU limit | 250m | 500m | 1000m |
| Memory request | 128Mi | 256Mi | 512Mi |
| Memory limit | 256Mi | 512Mi | 1Gi |
| MySQL storage | 1Gi | 5Gi | 20Gi |
| Log level | DEBUG | INFO | WARN |
| DB pool size | 5 | 10 | 20 |
| PodDisruptionBudget | No | No | Yes |
| TLS | No | Yes | Yes |
| Ingress host | (none) | staging.ftgo.example.com | ftgo.example.com |

## Deployment

### Prerequisites

1. **Kubernetes cluster** (v1.26+) with kubectl configured
2. **Kustomize** v5.3+ installed
3. **nginx-ingress controller** installed in the cluster
4. **GitHub Environments** configured with required reviewers for staging/prod
5. **Secrets** configured:
   - `KUBE_CONFIG`: Base64-encoded kubeconfig for each environment

### Manual Deployment

```bash
# Preview rendered manifests
kustomize build infrastructure/k8s/overlays/dev

# Apply to dev environment
kustomize build infrastructure/k8s/overlays/dev | kubectl apply -f -

# Apply to staging
kustomize build infrastructure/k8s/overlays/staging | kubectl apply -f -

# Apply to production
kustomize build infrastructure/k8s/overlays/prod | kubectl apply -f -
```

### Automated Deployment (CI/CD)

#### Auto-Deploy to Dev

Merging to `main` or `feat/microservices-migration-v3` automatically deploys to the `dev` environment via the **CD: Deploy to Kubernetes** workflow.

#### Manual Deploy

1. Go to **Actions** > **CD: Deploy to Kubernetes**
2. Click **Run workflow**
3. Select the target environment and image tag
4. Optionally enable **dry run** to preview changes

#### Environment Promotion

Use the **CD: Promote Environment** workflow:

1. Go to **Actions** > **CD: Promote Environment**
2. Select the source environment (`dev` or `staging`)
3. Optionally specify an image tag
4. Click **Run workflow**
5. **Approval required**: A reviewer must approve the deployment in the GitHub Environment settings

Promotion path: `dev` → `staging` → `prod`

#### Rollback

Use the **CD: Rollback Deployment** workflow:

1. Go to **Actions** > **CD: Rollback Deployment**
2. Select the environment and service (or `all`)
3. Optionally specify a revision number
4. Click **Run workflow**

## Secret Management

### Current State (Placeholder Secrets)

The base manifests include placeholder `Secret` resources with `REPLACE_ME` values. These **must** be replaced before deployment:

```bash
# Create secrets manually per environment
kubectl create secret generic ftgo-mysql-secret \
  --from-literal=root-password=<ROOT_PASSWORD> \
  --from-literal=username=<USERNAME> \
  --from-literal=password=<PASSWORD> \
  -n ftgo-dev
```

### External Secrets (Recommended for Production)

Templates are provided in `infrastructure/k8s/base/external-secrets/` for integrating with external secret providers (AWS Secrets Manager, HashiCorp Vault, etc.).

To enable:

1. Install the [external-secrets operator](https://external-secrets.io/)
2. Configure your secret provider in `cluster-secret-store.yaml`
3. Add `external-secrets/` to the base `kustomization.yaml` resources
4. Remove placeholder secrets from `mysql/secret.yaml` and `services/*/secret.yaml`

## Health Checks

All services have readiness and liveness probes configured:

- **Readiness**: `GET /actuator/health` (initialDelay: 30s, period: 10s)
- **Liveness**: `GET /actuator/health` (initialDelay: 60s, period: 15s)

MySQL uses `mysqladmin ping` for both probes.

## Rolling Update Strategy

All deployments use a rolling update strategy for zero-downtime deployments:

- `maxSurge: 1` — allows one extra pod during rollout
- `maxUnavailable: 0` — never reduces below desired count
- `minReadySeconds: 10` — waits 10s after pod is ready before continuing
- `revisionHistoryLimit: 5` — keeps 5 previous ReplicaSets for rollback

## CI/CD Workflows

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `cd-deploy.yml` | Push to main / manual | Deploy to K8s environment |
| `cd-promote.yml` | Manual | Promote between environments |
| `cd-rollback.yml` | Manual | Rollback a deployment |
| `cd-validate-manifests.yml` | PR | Validate K8s manifests on PRs |

## Troubleshooting

### Check deployment status

```bash
kubectl get deployments -n ftgo-dev
kubectl get pods -n ftgo-dev
kubectl describe deployment ftgo-order-service -n ftgo-dev
```

### View pod logs

```bash
kubectl logs -f deployment/ftgo-order-service -n ftgo-dev
```

### Check rollout history

```bash
kubectl rollout history deployment/ftgo-order-service -n ftgo-dev
```

### Rollback manually

```bash
# Rollback to previous revision
kubectl rollout undo deployment/ftgo-order-service -n ftgo-dev

# Rollback to specific revision
kubectl rollout undo deployment/ftgo-order-service -n ftgo-dev --to-revision=2
```
