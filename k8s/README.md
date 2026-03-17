# FTGO Kubernetes Deployment

## Overview

This directory serves as the entry point for FTGO Kubernetes deployments. The actual manifests
are organized using [Kustomize](https://kustomize.io/) under `deployment/kubernetes/`.

## Directory Structure

```
deployment/kubernetes/
├── base/                    # Base manifests (shared across all environments)
│   ├── mysql/               # MySQL StatefulSet, Service, ConfigMap, Secret
│   ├── order-service/       # Order Service Deployment, Service, ConfigMap, HPA
│   ├── consumer-service/    # Consumer Service Deployment, Service, ConfigMap, HPA
│   ├── restaurant-service/  # Restaurant Service Deployment, Service, ConfigMap, HPA
│   ├── courier-service/     # Courier Service Deployment, Service, ConfigMap, HPA
│   ├── ingress/             # NGINX Ingress resource
│   └── kustomization.yml    # Root base kustomization
├── overlays/
│   ├── dev/                 # Dev environment (ftgo-dev namespace, auto-deploy)
│   ├── staging/             # Staging environment (ftgo-staging namespace, manual promotion)
│   └── prod/                # Prod environment (ftgo-prod namespace, approval-gated)
├── argocd/                  # ArgoCD Application and Project resources
│   ├── project.yml          # ArgoCD AppProject for FTGO
│   ├── app-dev.yml          # Dev Application (auto-sync on merge to main)
│   ├── app-staging.yml      # Staging Application (manual sync)
│   ├── app-prod.yml         # Prod Application (manual sync, approval required)
│   └── promotion-workflow.yml # Promotion strategy and sync windows
└── stateful-services/       # [Legacy] Original MySQL-only manifests
```

## Quick Start

### Deploy to dev environment

```bash
kubectl apply -k deployment/kubernetes/overlays/dev
```

### Deploy to staging environment

```bash
kubectl apply -k deployment/kubernetes/overlays/staging
```

### Deploy to prod environment

```bash
kubectl apply -k deployment/kubernetes/overlays/prod
```

### Deploy ArgoCD applications (requires ArgoCD installed)

```bash
kubectl apply -k deployment/kubernetes/argocd
```

## Environment Promotion

| Environment | Namespace     | Deployment Trigger       | Approval Required |
|-------------|---------------|--------------------------|-------------------|
| Dev         | ftgo-dev      | Auto on merge to main    | No                |
| Staging     | ftgo-staging  | Manual ArgoCD sync       | Team lead         |
| Prod        | ftgo-prod     | Manual ArgoCD sync       | Release manager   |

### Promotion Commands (ArgoCD CLI)

```bash
# Promote to staging
argocd app sync ftgo-staging

# Promote to production
argocd app sync ftgo-prod

# Rollback to previous revision
argocd app rollback ftgo-<env> <revision>
```

## Secret Management

Secrets are defined as Kubernetes Secret resources with placeholder values in base manifests.
For production deployments, replace with one of:

- [Sealed Secrets](https://sealed-secrets.netlify.app/)
- [External Secrets Operator](https://external-secrets.io/)
- Cloud provider secret managers (AWS Secrets Manager, GCP Secret Manager, Azure Key Vault)

**Never commit actual secret values to the repository.**
