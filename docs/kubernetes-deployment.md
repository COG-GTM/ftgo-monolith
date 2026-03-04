# Kubernetes Deployment Guide

## Overview

The FTGO platform deploys 4 microservices and a MySQL database to Kubernetes using **Helm charts** for templating and **Kustomize** overlays as an alternative. Continuous deployment is managed via **GitHub Actions** with optional **ArgoCD** integration.

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  Kubernetes Cluster               │
│                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ ftgo-dev │  │ftgo-stag │  │ftgo-prod │       │
│  │          │  │          │  │          │       │
│  │ 4 svcs   │  │ 4 svcs   │  │ 4 svcs   │       │
│  │ 1 mysql  │  │ 1 mysql  │  │ 1 mysql  │       │
│  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────┘
```

### Services

| Service | Port | Image |
|---------|------|-------|
| ftgo-order-service | 8081 | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-order-service` |
| ftgo-consumer-service | 8082 | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-consumer-service` |
| ftgo-restaurant-service | 8083 | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-restaurant-service` |
| ftgo-courier-service | 8084 | `ghcr.io/cog-gtm/ftgo-monolith/ftgo-courier-service` |
| MySQL | 3306 | `mysql:8.0` |

## Namespace Strategy

| Environment | Namespace | Purpose |
|-------------|-----------|---------|
| Development | `ftgo-dev` | Auto-deployed on merge to main |
| Staging | `ftgo-staging` | Promotion from dev (requires approval) |
| Production | `ftgo-prod` | Promotion from staging (requires approval) |

## Deployment Methods

### Option 1: Helm Charts (Recommended)

```bash
# Deploy to dev
helm upgrade --install ftgo deploy/helm/ftgo \
  -f deploy/helm/ftgo/values.yaml \
  -f deploy/helm/ftgo/values-dev.yaml \
  --namespace ftgo-dev --create-namespace

# Deploy to staging
helm upgrade --install ftgo deploy/helm/ftgo \
  -f deploy/helm/ftgo/values.yaml \
  -f deploy/helm/ftgo/values-staging.yaml \
  --set global.imageTag=<TAG> \
  --namespace ftgo-staging --create-namespace

# Deploy to production
helm upgrade --install ftgo deploy/helm/ftgo \
  -f deploy/helm/ftgo/values.yaml \
  -f deploy/helm/ftgo/values-prod.yaml \
  --set global.imageTag=<TAG> \
  --namespace ftgo-prod --create-namespace
```

### Option 2: Kustomize

```bash
# Deploy to dev
kubectl kustomize deploy/k8s/overlays/dev | kubectl apply -f -

# Deploy to staging
kubectl kustomize deploy/k8s/overlays/staging | kubectl apply -f -

# Deploy to production
kubectl kustomize deploy/k8s/overlays/prod | kubectl apply -f -
```

### Option 3: ArgoCD (GitOps)

```bash
# Apply ArgoCD project and applications
kubectl apply -f deploy/k8s/argocd/project.yaml
kubectl apply -f deploy/k8s/argocd/application-dev.yaml
kubectl apply -f deploy/k8s/argocd/application-staging.yaml
kubectl apply -f deploy/k8s/argocd/application-prod.yaml
```

Dev syncs automatically. Staging and prod require manual sync (approval gate).

## CD Pipeline

### Automatic Deployment (dev)

On merge to `main`, the `cd-deploy.yml` workflow:
1. Validates the Helm chart (lint + template)
2. Deploys to `ftgo-dev` namespace
3. Verifies all deployments are ready
4. Runs smoke tests

### Environment Promotion

Use the `cd-promote.yml` workflow:
1. Go to Actions > "CD: Promote Environment"
2. Select source environment (`dev` or `staging`)
3. Enter the image tag to promote
4. Approve the deployment when prompted by GitHub Environment protection rules

```
dev ──[auto on merge]──> staging ──[manual approval]──> prod
                              │                              │
                         approval gate                  approval gate
```

**Required GitHub Environment Setup:**
- Create environments: `dev`, `staging`, `production` in repo Settings > Environments
- Add required reviewers for `staging` and `production` environments
- Add `KUBECONFIG` secret to each environment

## Secret Management

### External Secrets Operator (Recommended)

Secrets are managed via the [External Secrets Operator](https://external-secrets.io/) which syncs from a cloud secret store (AWS Secrets Manager, Vault, GCP Secret Manager).

Required secrets in your secret store:
- `ftgo/<namespace>/mysql` — `root-password`, `username`, `password`, `database`
- `ftgo/<namespace>/jwt` — `secret`
- `ftgo/<namespace>/ghcr` — `dockerconfigjson`

### Sealed Secrets (Alternative)

If External Secrets Operator is not available, set `externalSecrets.enabled=false` in values to use [Sealed Secrets](https://sealed-secrets.netlify.app/).

```bash
# Seal a secret
kubeseal --format yaml < secret.yaml > sealed-secret.yaml
```

### Manual (Dev/Local Only)

```bash
kubectl create secret generic ftgo-mysql-credentials \
  --from-literal=root-password=<ROOT_PASSWORD> \
  --from-literal=username=<DB_USER> \
  --from-literal=password=<DB_PASSWORD> \
  --from-literal=database=ftgo \
  -n ftgo-dev

kubectl create secret generic ftgo-jwt-secret \
  --from-literal=jwt-secret=<JWT_SECRET> \
  -n ftgo-dev
```

## Zero-Downtime Deployment

All microservice deployments are configured with:

- **Rolling Update Strategy**: `maxUnavailable: 0`, `maxSurge: 1` — ensures at least the current number of pods are always available
- **Startup Probe**: Allows up to 200s for JVM startup before health checks begin
- **Liveness Probe**: Checks `/actuator/health/liveness` every 15s
- **Readiness Probe**: Checks `/actuator/health/readiness` every 10s — pods only receive traffic when ready
- **PodDisruptionBudget**: Guarantees minimum available pods during voluntary disruptions
- **HPA**: Auto-scales based on CPU (70%) and memory (80%) utilization

## Resource Profiles

| Environment | Replicas | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-------------|----------|-------------|-----------|----------------|--------------|
| dev | 1 | 50m | 250m | 128Mi | 384Mi |
| staging | 2 | 100m | 500m | 256Mi | 512Mi |
| prod | 3 | 250m | 1000m | 384Mi | 768Mi |

## File Structure

```
deploy/
├── helm/ftgo/                    # Helm chart
│   ├── Chart.yaml
│   ├── values.yaml               # Base values
│   ├── values-dev.yaml           # Dev overrides
│   ├── values-staging.yaml       # Staging overrides
│   ├── values-prod.yaml          # Production overrides
│   └── templates/
│       ├── _helpers.tpl
│       ├── namespace.yaml
│       ├── configmap.yaml
│       ├── external-secrets.yaml
│       ├── sealed-secrets.yaml
│       ├── mysql-statefulset.yaml
│       ├── microservice-deployment.yaml
│       ├── microservice-service.yaml
│       ├── microservice-hpa.yaml
│       ├── microservice-pdb.yaml
│       ├── serviceaccount.yaml
│       └── networkpolicy.yaml
├── k8s/
│   ├── base/                     # Kustomize base
│   ├── overlays/
│   │   ├── dev/
│   │   ├── staging/
│   │   └── prod/
│   └── argocd/                   # ArgoCD applications
└── ...

.github/workflows/
├── cd-deploy.yml                 # Auto-deploy to dev
└── cd-promote.yml                # Environment promotion
```
