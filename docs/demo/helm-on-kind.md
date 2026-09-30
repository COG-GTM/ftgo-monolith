# Demo: FTGO on Kubernetes with one Helm chart (kind)

Epic [AB-402](https://cog-gtm.atlassian.net/browse/AB-402), ticket [AB-413](https://cog-gtm.atlassian.net/browse/AB-413).

The demo runs in about **12 minutes** once images are prebuilt (the timings below are from a rehearsal). You don't need
to know the repo: every step is one command, run from the repository root, and each one says what you should see.

| # | Section                                                   | Command                                         | Time      |
|---|-----------------------------------------------------------|-------------------------------------------------|-----------|
| 0 | [Pre-flight](#0-pre-flight-the-day-before)                | `make demo-reset`                               | before    |
| 1 | [Before: the legacy deployment](#1-before-the-legacy-deployment) | `make demo-legacy`                       | ~1 min    |
| 2 | [Chart tour](#2-chart-tour)                               | `helm template … \| less`                       | ~2 min    |
| 3 | [Deploy](#3-deploy-make-kind-demo)                        | `make kind-demo`                                | ~4 min    |
| 4 | [Use it](#4-use-it-open-the-app-and-place-an-order)       | browser + `make demo-order`                     | ~1 min    |
| 5 | [Operate](#5-operate-test-upgrade-failed-upgrade-rollback) | `make helm-test demo-upgrade demo-rollback`    | ~4 min    |
| 6 | [Teardown](#6-teardown)                                   | `helm uninstall` / `make kind-down`             | ~1 min    |
| 7 | [Reset for the next run](#7-reset-for-the-next-run)       | `make demo-reset QUICK=1`                       | ~1 min    |

What the audience should take away: before the epic, the only working path was Docker Compose, and the Kubernetes
YAML couldn't even be applied. Now a single versioned chart owns the app, MySQL and the Flyway migrations, with
install, test, upgrade, rollback and uninstall all through Helm.

## 0. Pre-flight (the day before)

### Tools

Every cluster target checks these first (`make tools`) and fails with a clear message if one is missing or the wrong
version.

| Tool    | Version                                       | Check                           |
|---------|-----------------------------------------------|---------------------------------|
| Docker  | 24+ with the Compose v2 plugin, daemon running | `docker version`, `docker compose version` |
| kind    | v0.24.x                                       | `kind version`                  |
| kubectl | v1.30–v1.32 (cluster is v1.31)                | `kubectl version --client`      |
| Helm    | v3.16.x                                       | `helm version`                  |
| JDK     | 8, exported as `JAVA_HOME`                    | `$JAVA_HOME/bin/java -version`  |
| curl, jq, git, make | any                               | –                               |

JDK 8 is needed for the image build's Gradle 4.10 and for the end-to-end tests that `make kind-demo` runs last.

### Machine

- **Docker memory: at least 4 GB** for the Docker VM (Docker Desktop → Settings → Resources); 6 GB is comfortable. The
  kind node runs MySQL (768 Mi limit) and the app (512 Mi limit, 2 pods during the upgrade), and the Gradle build
  runs on the host.
- **Disk:** about 5 GB free for the node image, the app images and the Gradle cache.
- **Port 8081 free:** `lsof -i :8081` (macOS/Linux) must print nothing. The kind cluster binds `127.0.0.1:8081`.
- Close VPN clients that intercept Docker networking.

### Prebuild and preload everything (offline / conference Wi-Fi)

```bash
export JAVA_HOME=/path/to/jdk8
make demo-reset
```

[`scripts/demo-reset.sh`](../../scripts/demo-reset.sh) deletes the `ftgo` kind cluster if it exists, recreates it,
builds the `ftgo-application` and `ftgo-flyway` images and loads them into the node, then preloads every third-party
image the chart uses (`mysql:8.0.39` and the digest-pinned `curlimages/curl` used by `helm test`). It finishes by
printing the start state: no Helm release, no PVC, no Secret. It takes about 5 minutes with a warm Gradle cache.

After that, the demo pulls nothing from a registry: `make kind-demo` reuses the cluster, and its image build is
fully cached.

If Docker Hub rate-limits you (`429 Too Many Requests`), use a mirror for the preload:
`DOCKERHUB_MIRROR=mirror.gcr.io make demo-reset`. Behind a Maven proxy, set `MAVEN_MIRROR_URL` as well (see
[`scripts/build-images.sh`](../../scripts/build-images.sh)).

### Optional: skip the Gradle build during the demo

`make kind-demo` rebuilds the images. With a warm build cache that takes under a minute, but you can skip it
entirely by deploying the images `make demo-reset` already loaded:

```bash
make deploy smoke      # instead of make kind-demo; uses the tag in build/kind-image-tag
```

### Dry run

Rehearse sections 1–6 once, then run `make demo-reset QUICK=1` (section 7). You are now in the start state.

## 1. Before: the legacy deployment

```bash
make demo-legacy
```

[`scripts/demo-legacy.sh`](../../scripts/demo-legacy.sh) shows the deployment paths the repo had before this epic.
It reads them from `origin/master` (the Kubernetes YAML was deleted in AB-412; set `LEGACY_REF` to use another
commit) and applies the legacy MySQL manifests to the kind cluster with `--dry-run=server`, so nothing is created.

Talk track:

- **`build-and-run.sh` + `docker-compose.yml`**: the only path that worked. It runs Gradle on the host and
  `docker-compose up`, and the database had no tables until you ran Flyway by hand. Compose is still in the repo for
  the inner loop and now runs Flyway (AB-412).
- **`deployment/kubernetes/`**: `kubernetes-deploy-all.sh` applies the MySQL StatefulSet, then
  `*/src/deployment/kubernetes/*.yml`, a glob that matches no files. There is no app workload at all.

What you should see:

```
secret/ftgo-db-secret created (server dry run)
service/ftgo-mysql created (server dry run)
error: resource mapping not found for name: "ftgo-mysql" namespace: "" from "STDIN": no matches for kind "StatefulSet" in version "apps/v1beta1"
App manifests matching */src/deployment/kubernetes/*.yml on origin/master:
  (none)
```

`apps/v1beta1` was removed in Kubernetes 1.16. For the full captured failure with `apps/v1` patched in (the
StatefulSet is then accepted, the MySQL pod runs, and there is still nothing to reach on 8081), see
[as-is baseline, "Before evidence"](../deployment/as-is.md#before-evidence-legacy-path-on-kind).

## 2. Chart tour

```bash
tree deployment/helm/ftgo        # or: find deployment/helm/ftgo -type f | sort
```

```
deployment/helm/ftgo/
├── Chart.yaml                     chart version (SemVer, bumped on every chart change; CI enforces it) + appVersion
├── values.yaml                    production-shaped defaults: ClusterIP Service, generated MySQL passwords
├── values-kind.yaml               only what differs on kind: NodePort 30081, IfNotPresent pulls, small resources
├── values.schema.json             rejects bad values before anything reaches the cluster
├── files/schema.sql               → mysql/schema.sql: creates the ftgo database and user on first start
└── templates/
    ├── app-deployment.yaml        app Deployment; Flyway initContainer; RollingUpdate with maxUnavailable 0
    ├── app-service.yaml           ClusterIP by default, NodePort on kind
    ├── app-configmap.yaml         non-secret Spring settings
    ├── db-secret.yaml             passwords generated once, kept across upgrade and uninstall
    ├── mysql-statefulset.yaml     MySQL 8.0.39 StatefulSet + PVC template
    ├── mysql-service.yaml         headless Service
    ├── mysql-initdb-configmap.yaml  files/schema.sql for /docker-entrypoint-initdb.d
    ├── migrations-job.yaml        alternative Flyway mode: hook Job (migrations.mode=hook)
    ├── tests/test-health.yaml     helm test: curls /actuator/health and /orders
    └── NOTES.txt
```

Show the kind overrides next to the defaults:

```bash
cat deployment/helm/ftgo/values-kind.yaml
diff <(helm template ftgo deployment/helm/ftgo) \
     <(helm template ftgo deployment/helm/ftgo -f deployment/helm/ftgo/values-kind.yaml) | less
```

Then walk through what gets applied:

```bash
helm template ftgo deployment/helm/ftgo -f deployment/helm/ftgo/values-kind.yaml | less
```

In `less`, `/kind: ` jumps from resource to resource. Point out the `flyway-migrate` initContainer in the Deployment
(it migrates the schema before each app pod starts) and the readiness probe on `/actuator/health`.

The schema at work: `helm template ftgo deployment/helm/ftgo --set app.replicaCount=-1` fails with
`values don't meet the specifications of the schema(s)`, so a typo never reaches the cluster.

## 3. Deploy: `make kind-demo`

```bash
make kind-demo
```

This runs `kind-up` (already up, so it's a no-op) → `images` (cached build, loaded into the node) →
`deploy` (`helm upgrade --install … --wait`) → `smoke` → `e2e` (the repo's existing end-to-end tests against
localhost:8081). While it runs, in a second terminal:

```bash
kubectl --context kind-ftgo get pods -w
```

You'll see `ftgo-mysql-0` become Ready, then `ftgo-application-…` go `Init:0/1` (Flyway waits for MySQL, then
migrates) → `Running`. It should end with `BUILD SUCCESSFUL` from the end-to-end tests, and `helm list` shows
revision 1, `deployed`.

## 4. Use it: open the app and place an order

Open <http://localhost:8081>, the operations dashboard, served by the app pod through the NodePort.

Note: the dashboard currently shows sample data and its "Add Consumer" dialog does not open. These are app bugs
that predate this epic, so place the order through the API:

```bash
make demo-order
```

[`scripts/demo-order.sh`](../../scripts/demo-order.sh) prints each request as it sends it: `POST /consumers` →
`POST /restaurants` (with one menu item) → `POST /orders` (2 × Chicken Vindaloo) → `GET /orders/{id}`. You should
see:

```
{"orderId":4,"state":"APPROVED","orderTotal":"24.68","restaurantName":"Demo Curry House",...}
```

Remember the order ID; section 5 shows that it survives a failed upgrade and a rollback. The IDs start above 1
because the end-to-end tests in section 3 created data too.

The same by hand:

```bash
curl -s -H 'Content-Type: application/json' -d '{"name":{"firstName":"Ada","lastName":"Lovelace"}}' localhost:8081/consumers
curl -s -H 'Content-Type: application/json' -d '{"name":"Demo Curry House","address":{"street1":"1 High Street","city":"Oakland","state":"CA","zip":"94619"},"menu":{"menuItemDTOs":[{"id":"1","name":"Chicken Vindaloo","price":"12.34"}]}}' localhost:8081/restaurants
curl -s -H 'Content-Type: application/json' -d '{"consumerId":<consumerId>,"restaurantId":<id>,"lineItems":[{"menuItemId":"1","quantity":2}]}' localhost:8081/orders
curl -s localhost:8081/orders/<orderId>
```

## 5. Operate: test, upgrade, failed upgrade, rollback

Each target prints the `helm`/`kubectl` commands it runs, and fails if the scenario does not behave as described.
They are documented in more detail in [deployment/kind/README.md](../../deployment/kind/README.md#release-lifecycle).

### `helm test`

```bash
make helm-test
```

This runs the chart's test pod, which curls `/actuator/health` and `/orders?consumerId=0` through the Service.
Expect `Phase: Succeeded`. For the negative case, `make demo-helm-test` scales MySQL to 0, shows `helm test` failing,
and restores it (adds about 2 minutes).

### Upgrade: scale out

```bash
make demo-upgrade REPLICAS=2
```

This runs `helm upgrade --reset-then-reuse-values --set app.replicaCount=2 --wait`. The history shows revision 2,
`Upgrade complete`, and two app pods are Running. The rolling update keeps the old pod serving until the new one is
Ready (`maxUnavailable: 0`).

### Deliberately failed upgrade, then rollback

```bash
make demo-rollback
```

The script:

1. creates a consumer (data written before the bad release);
2. upgrades with `app.image.tag=does-not-exist --wait --timeout 2m`. The new pod goes `ErrImagePull` /
   `ImagePullBackOff`, and after 2 minutes Helm marks revision 3 `failed`. Meanwhile the old pods keep serving:
   the script curls `/actuator/health` during the failure;
3. runs `helm rollback ftgo 2 --wait`;
4. reads the consumer back and runs `helm test`.

```
REVISION  STATUS      DESCRIPTION
1         superseded  Install complete
2         superseded  Upgrade complete
3         failed      Upgrade "ftgo" failed: context deadline exceeded
4         deployed    Rollback to 2
```

### Data is intact

```bash
curl -s localhost:8081/orders/<orderId from section 4>
```

It returns the same order. The failed upgrade and the rollback only touched the app Deployment. MySQL, its PVC and
the retained Secret were never replaced.

## 6. Teardown

```bash
helm --kube-context kind-ftgo uninstall ftgo --wait
kubectl --context kind-ftgo get pvc,secret -l app.kubernetes.io/instance=ftgo
```

Point out what is left: the MySQL PVC `data-ftgo-mysql-0` (created from the StatefulSet's `volumeClaimTemplates`)
and the Secret `ftgo-mysql` (`helm.sh/resource-policy: keep`). This is deliberate, so a reinstall gets its data
and passwords back. `make demo-reinstall` shows that. For a clean slate, delete both together, as described in
[deployment/kind/README.md](../../deployment/kind/README.md#uninstall-reinstall-and-a-clean-slate).

To remove everything:

```bash
make kind-down      # deletes the cluster, every container and the localhost:8081 binding
```

## 7. Reset for the next run

| You want to…                                       | Run                          | Time     |
|----------------------------------------------------|------------------------------|----------|
| Run the demo again on the same cluster             | `make demo-reset QUICK=1`    | ~1 min   |
| Start from scratch (e.g. after `make kind-down`)   | `make demo-reset`            | ~5 min   |

`QUICK=1` uninstalls the release if present, deletes the MySQL PVC and Secret and the leftover `helm test` pod,
and checks that the preloaded images are still on the node. Both modes are safe to run repeatedly, and neither
touches your Git working tree.

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| App pod `ErrImagePull` / `ImagePullBackOff` for `ftgo-application:<tag>` or `ftgo-flyway:<tag>` (outside the deliberate rollback scenario) | The image was built but not loaded into the kind node (`kind load`), for example after the cluster was recreated, or you deployed with a hand-typed tag. The chart pulls `IfNotPresent`, and there is no registry for these images. | `make images deploy` (builds, loads into the node, and deploys the new tag). Check with `docker exec ftgo-control-plane crictl images \| grep ftgo`. |
| `ftgo-mysql-0` or the `helm test` pod `ImagePullBackOff`, event shows `429 Too Many Requests` | Docker Hub rate limit (MySQL and curl are not preloaded) | `DOCKERHUB_MIRROR=mirror.gcr.io make demo-reset QUICK=1` preloads them from a mirror. |
| `ftgo-mysql-0` `Pending`; `kubectl get pvc` shows `data-ftgo-mysql-0` `Pending` | No default StorageClass (a kind cluster not created from `deployment/kind/kind-config.yaml`, or the `local-path-provisioner` pod is not running) | `kubectl -n local-path-storage get pods` and `kubectl get storageclass` (expect `standard (default)`). If it is missing, run `make kind-down demo-reset`. |
| PVC `Pending` with `waiting for first consumer` | Normal for `WaitForFirstConsumer`: it binds once the MySQL pod is scheduled | Wait. If the pod is also Pending, see `kubectl describe pod ftgo-mysql-0` (usually insufficient memory: raise Docker memory). |
| `make kind-up` fails: `Bind for 127.0.0.1:8081 failed: port is already allocated` | Something else (often `./build-and-run.sh` / Compose, or a `kubectl port-forward`) is using 8081 | `lsof -i :8081` to find it. For Compose, run `docker compose down`, then `make kind-down demo-reset`. |
| `kind cluster 'ftgo' exists but does not map localhost:8081` | The cluster was created without `kind-config.yaml` | `make kind-down demo-reset` |
| App pod `Init:CrashLoopBackOff`, Flyway log says `Access denied for user` | The MySQL Secret was deleted but the PVC was kept, so the passwords no longer match the data | `make demo-reset QUICK=1` (deletes both). |
| `make e2e` fails with `InaccessibleObjectException` | `JAVA_HOME` is not JDK 8 | `export JAVA_HOME=/path/to/jdk8` |

More diagnostics:

```bash
kubectl --context kind-ftgo get events --sort-by=.lastTimestamp | tail -20
kubectl --context kind-ftgo logs deploy/ftgo-application -c flyway-migrate
kubectl --context kind-ftgo logs deploy/ftgo-application
helm --kube-context kind-ftgo history ftgo
```
