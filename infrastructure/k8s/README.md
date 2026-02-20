# FTGO Kubernetes Deployment

Kustomize-based Kubernetes deployment manifests for FTGO microservices with environment promotion support.

## Directory Structure

```
infrastructure/k8s/
├── base/                          # Base manifests shared across environments
│   ├── kustomization.yaml         # Root Kustomize config
│   ├── namespace.yaml             # Base namespace
│   ├── consumer-service/          # Consumer service manifests
│   │   ├── kustomization.yaml
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── serviceaccount.yaml
│   ├── courier-service/           # Courier service manifests
│   ├── order-service/             # Order service manifests
│   └── restaurant-service/        # Restaurant service manifests
├── overlays/
│   ├── dev/                       # Development environment
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   └── patches/
│   │       ├── replicas.yaml      # 1 replica per service
│   │       └── resource-limits.yaml # Lower resource limits
│   ├── staging/                   # Staging environment
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   └── patches/
│   │       ├── replicas.yaml      # 2 replicas per service
│   │       └── resource-limits.yaml # Medium resource limits
│   └── production/                # Production environment
│       ├── kustomization.yaml
│       ├── namespace.yaml
│       ├── pdb.yaml               # Pod Disruption Budgets
│       └── patches/
│           ├── replicas.yaml      # 3 replicas per service
│           └── resource-limits.yaml # Higher resource limits
└── README.md
```

## Services

| Service | Port | Health Check |
|---------|------|-------------|
| consumer-service | 8080 | `/actuator/health/liveness`, `/actuator/health/readiness` |
| courier-service | 8080 | `/actuator/health/liveness`, `/actuator/health/readiness` |
| order-service | 8080 | `/actuator/health/liveness`, `/actuator/health/readiness` |
| restaurant-service | 8080 | `/actuator/health/liveness`, `/actuator/health/readiness` |

## Environment Configuration

| Setting | Dev | Staging | Production |
|---------|-----|---------|------------|
| Namespace | `ftgo-dev` | `ftgo-staging` | `ftgo-production` |
| Replicas | 1 | 2 | 3 |
| CPU Request | 100m | 250m | 500m |
| CPU Limit | 250m | 500m | 1000m |
| Memory Request | 128Mi | 256Mi | 512Mi |
| Memory Limit | 256Mi | 512Mi | 1Gi |
| Log Level | DEBUG | INFO | WARN |
| PDB | No | No | Yes (minAvailable: 1) |

## Probes

Each service is configured with three types of probes:

- **Startup Probe**: Allows up to 150s for initial startup (`initialDelaySeconds: 10`, `periodSeconds: 5`, `failureThreshold: 30`)
- **Liveness Probe**: Restarts container if unhealthy (`periodSeconds: 10`, `failureThreshold: 3`)
- **Readiness Probe**: Removes pod from service if not ready (`periodSeconds: 5`, `failureThreshold: 3`)

## Local Development

### Prerequisites

- [kubectl](https://kubernetes.io/docs/tasks/tools/) configured with cluster access
- [kustomize](https://kubectl.docs.kubernetes.io/installation/kustomize/) v5.3.0+

### Preview manifests

```bash
# Preview base manifests
kustomize build infrastructure/k8s/base

# Preview environment-specific manifests
kustomize build infrastructure/k8s/overlays/dev
kustomize build infrastructure/k8s/overlays/staging
kustomize build infrastructure/k8s/overlays/production
```

### Apply manifests locally

```bash
# Deploy to dev
kustomize build infrastructure/k8s/overlays/dev | kubectl apply -f -

# Deploy to staging
kustomize build infrastructure/k8s/overlays/staging | kubectl apply -f -

# Deploy to production
kustomize build infrastructure/k8s/overlays/production | kubectl apply -f -
```

### Set a specific image tag

```bash
cd infrastructure/k8s/overlays/dev
kustomize edit set image ftgo/order-service=ftgo/order-service:v1.2.3
kustomize build . | kubectl apply -f -
```

## CI/CD Deployment

Deployments are automated via the **Deploy: Kubernetes** GitHub Actions workflow (`.github/workflows/deploy.yml`).

### Trigger a deployment

1. Go to **Actions** > **Deploy: Kubernetes**
2. Click **Run workflow**
3. Select parameters:
   - **Environment**: `dev`, `staging`, or `production`
   - **Service**: Individual service name or `all`
   - **Image Tag**: Docker image tag to deploy
   - **Dry Run**: `true` to validate without applying

### Environment Promotion Flow

```
dev  ──────>  staging  ──────>  production
 (auto-suggest)  (auto-suggest)
```

1. **Deploy to dev**: Trigger workflow with `environment: dev`
2. **Promote to staging**: After successful dev deployment, trigger workflow with `environment: staging` and the same image tag
3. **Promote to production**: After successful staging deployment, trigger workflow with `environment: production` and the same image tag

**Best practice**: Always run with `dry_run: true` first when promoting to staging or production.

### Workflow Features

- **Manifest validation**: All Kustomize overlays are validated before deployment
- **Rolling updates**: Zero-downtime deployments with `maxSurge: 1`, `maxUnavailable: 0`
- **Rollout monitoring**: Automatically waits for deployment rollout completion
- **Health verification**: Checks pod and service status post-deployment
- **Deployment summary**: GitHub Actions step summary with deployment details
- **Concurrency control**: Only one deployment per environment at a time
- **Environment protection**: GitHub environment protection rules supported

## Rollback

To rollback a deployment:

```bash
# Check rollout history
kubectl rollout history deployment/<prefix>-<service> -n <namespace>

# Rollback to previous revision
kubectl rollout undo deployment/<prefix>-<service> -n <namespace>

# Rollback to a specific revision
kubectl rollout undo deployment/<prefix>-<service> -n <namespace> --to-revision=<N>
```

Where `<prefix>` is `dev-`, `staging-`, or `prod-` depending on the environment.
