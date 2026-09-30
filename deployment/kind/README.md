# FTGO on a local kind cluster

One command takes you from nothing to FTGO running on Kubernetes at http://localhost:8081. It deploys the Helm chart
in `deployment/helm/ftgo` to a single-node kind cluster.

```bash
make kind-demo   # kind-up → images → deploy → smoke → e2e
make kind-down   # delete the cluster (and with it every container and the localhost:8081 binding)
```

## Prerequisites

The versions are pinned in the `Makefile`, and every cluster target checks them first (`make tools`).

| Tool    | Version                          | Check                                               |
|---------|----------------------------------|-----------------------------------------------------|
| Docker  | 24+, daemon running              | daemon reachable                                    |
| kind    | v0.24.0                          | same major.minor                                    |
| kubectl | v1.31.0                          | within one minor version (Kubernetes skew policy)   |
| Helm    | v3.16.2                          | same major.minor                                    |
| JDK     | 8 (`JAVA_HOME`)                  | `make e2e` only; the Gradle 4.10 build needs Java 8 |

Host port 8081 must be free.

## Targets

| Target      | What it does                                                                                                   |
|-------------|----------------------------------------------------------------------------------------------------------------|
| `kind-up`   | Creates cluster `ftgo` from `kind-config.yaml` (node `kindest/node:v1.31.0`, host `127.0.0.1:8081` → NodePort 30081) unless it already exists. |
| `images`    | `scripts/build-images.sh --kind-load ftgo`: builds `ftgo-application` and `ftgo-flyway`, loads them into the cluster, and records the tag in `build/kind-image-tag` (`<sha>-<timestamp>`, or `<sha>-dirty-<timestamp>` for uncommitted changes). |
| `deploy`    | `helm upgrade --install ftgo deployment/helm/ftgo -f deployment/helm/ftgo/values-kind.yaml --wait --timeout 10m`, with both image tags set from `build/kind-image-tag`, so every rebuild rolls the app. Flyway migrates the schema in the app pod's initContainer. |
| `smoke`     | `curl -f http://localhost:8081/actuator/health` and the dashboard at `http://localhost:8081/`.                 |
| `e2e`       | `DOCKER_HOST_IP=localhost ./gradlew :ftgo-end-to-end-tests:cleanTest :ftgo-end-to-end-tests:test`.             |
| `kind-down` | `kind delete cluster --name ftgo`.                                                                              |
| `lint`      | `scripts/helm-lint.sh`: `helm lint --strict` and `helm template \| kubeconform -strict -kubernetes-version 1.31.0` for the default values, `values-kind.yaml` and hook-mode migrations, and checks that invalid values are rejected. Needs Helm and kubeconform v0.6.7, no cluster. |
| `helm-test` | `helm test ftgo --logs`: runs the chart's test hooks, if any.                                                   |

Overrides: `CLUSTER`, `RELEASE`, `NAMESPACE`, and `MAVEN_MIRROR_URL` (passed to the image build; see `scripts/build-images.sh`).

## CI

`.github/workflows/helm-chart.yml` runs on every pull request (and push to `master`) that touches `deployment/**`,
`ftgo-flyway/**`, `Makefile`, `scripts/**` or the workflow itself:

- **Lint and schema-validate**: `make lint`, plus a check that the workflow's tool versions match the `Makefile`.
- **Chart version bump**: `scripts/check-chart-version-bump.sh <base>` fails if `deployment/helm/ftgo/**` changed but
  `version` in `Chart.yaml` did not increase.
- **Install on kind**: `helm/kind-action` creates cluster `ftgo` from `kind-config.yaml`, then
  `make images deploy smoke`, `make helm-test` and `make e2e`. On failure it dumps pods, events and the
  app/Flyway/MySQL logs.

## Troubleshooting

- **`kind cluster 'ftgo' exists but does not map localhost:8081`**: the cluster was created without `kind-config.yaml`. Run `make kind-down && make kind-up`.
- **MySQL pod stuck in `ErrImagePull` with `429 Too Many Requests`**: Docker Hub rate limiting. Run `docker pull mysql:8.0.39` on the host with credentials (or through a mirror), then `docker save --platform linux/amd64 mysql:8.0.39 -o mysql.tar && kind load image-archive mysql.tar --name ftgo` (use your platform; `kind load docker-image` can fail on multi-arch images).
- **Logs**: `kubectl logs deploy/ftgo-application -c flyway-migrate` (migrations) and `kubectl logs deploy/ftgo-application` (app).
