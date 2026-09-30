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

Overrides: `CLUSTER`, `RELEASE`, `NAMESPACE`, and `MAVEN_MIRROR_URL` (passed to the image build; see `scripts/build-images.sh`).

## Release lifecycle

These targets run against a release deployed with `make kind-up images deploy` (or `make kind-demo`). Each one is
re-runnable, prints every `helm`/`kubectl` command before running it, and exits non-zero if the scenario does not
behave as expected. They use the same `CLUSTER`, `RELEASE` and `NAMESPACE` (kube-context `kind-$(CLUSTER)`).

| Target                           | Script                        | What it does |
|----------------------------------|-------------------------------|--------------|
| `helm-test`                      | –                             | `helm test ftgo --logs`: runs the chart's test pod (`templates/tests/test-health.yaml`), which curls `GET /actuator/health` and `GET /orders?consumerId=0` on the app Service and fails on any non-2xx response. |
| `demo-helm-test`                 | `scripts/demo-helm-test.sh`   | `helm test` on the healthy release (passes), with the MySQL StatefulSet scaled to 0 (fails), and after MySQL is restored (passes). |
| `demo-upgrade [REPLICAS=2]`      | `scripts/demo-upgrade.sh`     | `helm upgrade --reset-then-reuse-values --set app.replicaCount=$(REPLICAS) --wait`, then `helm history`. |
| `demo-rollback`                  | `scripts/demo-rollback.sh`    | Creates a consumer, upgrades `app.image.tag` to a nonexistent tag with `--wait --timeout 2m` (fails), shows the old pod still serving, runs `helm rollback ftgo <last good revision> --wait`, reads the consumer back and runs `helm test`. |
| `demo-reinstall [CLEAN=1]`       | `scripts/demo-reinstall.sh`   | Creates a consumer, `helm uninstall`, shows what is left behind, reinstalls as `make deploy` does, and reads the consumer back. `CLEAN=1` deletes the MySQL PVC and Secret between uninstall and reinstall, so the consumer is gone. |

The app Deployment uses `RollingUpdate` with `maxSurge: 1`, `maxUnavailable: 0` and `revisionHistoryLimit: 5`
(`app.rollingUpdate`, `app.revisionHistoryLimit`). A new pod has to be Ready before an old one is removed, so a bad
upgrade leaves the previous version serving until you roll back.

### Upgrade → failed upgrade → rollback

`make demo-upgrade demo-rollback` on a fresh install produces:

```
REVISION  STATUS      DESCRIPTION
1         superseded  Install complete
2         superseded  Upgrade complete
3         failed      Upgrade "ftgo" failed: context deadline exceeded
4         deployed    Rollback to 2
```

Revision 3 never becomes Ready (`ImagePullBackOff`), revision 2's pods keep serving throughout, and the rollback
creates revision 4 with revision 2's manifest and values. MySQL is not touched, so data created before the failed
upgrade is still there. `GET /consumers/{id}` returns `consumerId: 0` (a known app bug), so the demo compares names.

### Uninstall, reinstall and a clean slate

`helm uninstall ftgo` does **not** delete:

- the MySQL PVC `data-ftgo-mysql-0`: it is created from the StatefulSet's `volumeClaimTemplates`, and Kubernetes does
  not delete those PVCs when the StatefulSet is deleted;
- the Secret `ftgo-mysql`: it is annotated `helm.sh/resource-policy: keep` (`templates/db-secret.yaml`), and Helm reports
  it as kept.

The last `helm test` pod (`ftgo-application-test-health`) is also left behind, because Helm does not delete test hook
resources on uninstall. It is harmless: the next `helm test` replaces it (`hook-delete-policy: before-hook-creation`).

A reinstall under the same release name and namespace adopts the kept Secret (its Helm ownership annotations still
match). `ftgo.mysql.retainedPassword` (`templates/_helpers.tpl`) reads it with `lookup` and reuses the stored
passwords, which are the ones MySQL was initialised with on the kept PVC, so the app connects and all data is still
there. Setting `mysql.auth.password` / `mysql.auth.rootPassword` to a different value fails the reinstall rather than
diverging from the data on disk.

For a clean slate, delete both after the uninstall:

```bash
helm --kube-context kind-ftgo uninstall ftgo --wait
kubectl --context kind-ftgo delete pvc -l app.kubernetes.io/instance=ftgo,app.kubernetes.io/component=mysql
kubectl --context kind-ftgo delete secret ftgo-mysql
kubectl --context kind-ftgo delete pod -l app.kubernetes.io/instance=ftgo,app.kubernetes.io/component=test
```

(`make demo-reinstall CLEAN=1` does this.) Delete them together: deleting only the Secret makes the reinstall generate
new random passwords that do not match the MySQL data on the kept PVC, so the app and Flyway fail with `Access denied`
until you delete the PVC as well. Deleting only the PVC is harmless: MySQL initialises the new volume with the kept
passwords. `make kind-down` deletes everything with the cluster.

## Troubleshooting

- **`kind cluster 'ftgo' exists but does not map localhost:8081`**: the cluster was created without `kind-config.yaml`. Run `make kind-down && make kind-up`.
- **MySQL pod stuck in `ErrImagePull` with `429 Too Many Requests`**: Docker Hub rate limiting. Run `docker pull mysql:8.0.39` on the host with credentials (or through a mirror), then `docker save --platform linux/amd64 mysql:8.0.39 -o mysql.tar && kind load image-archive mysql.tar --name ftgo` (use your platform; `kind load docker-image` can fail on multi-arch images).
- **Logs**: `kubectl logs deploy/ftgo-application -c flyway-migrate` (migrations) and `kubectl logs deploy/ftgo-application` (app).
