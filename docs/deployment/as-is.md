# FTGO deployment surface: as-is baseline

Part of epic [AB-402](https://cog-gtm.atlassian.net/browse/AB-402) (scripts + plain Kubernetes YAML → Helm chart on kind). Ticket: [AB-403](https://cog-gtm.atlassian.net/browse/AB-403).

This document inventories every deployment artifact on `master` as of this ticket, records the known defects with file/line references, and maps each artifact to its target in the Helm chart (`deployment/helm/ftgo/`). Later tickets reference rows by their ID (`L1`, `L2`, ...). No application or script behaviour is changed by this ticket.

## How the app is deployed today

There are two paths. Only the first one works.

1. **Docker Compose (works, with a manual step).** `build-and-run.sh` → `./gradlew assemble` on the host → `docker-compose build` → `docker-compose down -v` (wipes the MySQL volume) → start `mysql` → `./gradlew waitForMySql` → start `ftgo-application` on `localhost:8081`. `mysql/schema.sql` only creates the empty `ftgo` database and user; the tables come from Flyway, which `build-and-run.sh` never runs (only `build-and-test-all.sh` runs `./gradlew :ftgo-flyway:flywayMigrate`). So after `build-and-run.sh` alone the app starts but database-backed requests fail until Flyway is run by hand (D15).
2. **Kubernetes (broken).** `deployment/kubernetes/scripts/kubernetes-deploy-all.sh` applies the MySQL StatefulSet and then a glob that matches no files. On any Kubernetes ≥ 1.16 it fails on the first StatefulSet; even with that fixed there is no `ftgo-application` workload, no in-cluster migrations and no way to reach the app except port-forward scripts that select on labels nothing sets. See [Before evidence](#before-evidence-legacy-path-on-kind).

## Inventory and legacy → Helm mapping

Helm target kinds: **template** (a chart template), **value** (a `values.yaml` key), **hook** (Helm hook / initContainer), **helm test**, **make** (replaced by a `Makefile`/`scripts/` target for kind in AB-409), **retire** (deleted, with no replacement needed), **keep** (stays for docker-compose inner-loop dev; decided in AB-412).

| ID | Artifact | Purpose | Inputs (env, ports, creds) | Depends on | Helm target | Ticket |
| --- | --- | --- | --- | --- | --- | --- |
| L1 | `build-and-run.sh` | Host Gradle build, compose build, recreate stack, wait for MySQL, print Swagger URL | `DOCKER_HOST_IP` (via `set-env.sh`); compose ports 3306, 8081 | `set-env.sh`, `docker-compose.yml`, Gradle `waitForMySql` (buildSrc), `show-swagger-ui-urls.sh` | **keep** for compose dev; k8s equivalent is **make** `kind-demo` (`kind-up` → `images` → `deploy`) | AB-405 (must not regress), AB-409, AB-412 |
| L2 | `start-services.sh` | `docker-compose up` MySQL, wait, start rest, wait for `/health` | `DOCKER_HOST_IP` | `wait-for-mysql.sh`, `wait-for-services.sh` | **keep** (compose) / **make** `deploy` (`helm upgrade --install --wait`) | AB-409, AB-412 |
| L3 | `start-infrastructure-services.sh` | `docker-compose up -d --build mysql` | extra compose args | `docker-compose.yml` | **keep** (compose) / **template** `mysql-statefulset.yaml` | AB-407, AB-412 |
| L4 | `build-and-restart-application.sh` | Rebuild jar + image, force-recreate container, tail logs | – | Gradle, compose | **keep** (compose) / **make** `images` + `deploy` (a new image tag rolls the Deployment) | AB-405, AB-409, AB-412 |
| L5 | `set-env.sh` | Derive `DOCKER_HOST_IP` from `hostname`/`DOCKER_HOST`; `COMPOSE_HTTP_TIMEOUT=240` | `DOCKER_HOST`, `DOCKER_HOST_IP` | – | **keep** (compose + e2e). On kind the host is always `localhost` (`DOCKER_HOST_IP=localhost` in `make e2e`) | AB-409, AB-412 |
| L6 | `wait-for-mysql.sh` + `mysql-cli.sh` | Poll `select 1` through a throwaway `mysql:5.7.13` client container as root | `DOCKER_HOST_IP`, root password `rootpassword` hardcoded | Docker | **template**: MySQL `readinessProbe` (`mysqladmin ping`) + Flyway initContainer wait-for-MySQL; `helm --wait` replaces polling | AB-407, AB-408 |
| L7 | `wait-for-services.sh` | Poll `http://$DOCKER_HOST_IP:8081/health` | `DOCKER_HOST_IP`, port 8081 | curl | **template**: readiness/liveness/startup probes on `/actuator/health`; **helm test** `test-health.yaml` | AB-406, AB-410 |
| L8 | `docker-compose.yml` | `mysql` (built from `./mysql`) on 3306; `ftgo-application` on `8081:8080` | Plaintext DB creds; `SPRING_DATASOURCE_*`, `JAVA_OPTS`; stale Kafka/Zookeeper/Sleuth/Zipkin vars | `mysql/`, `ftgo-application/Dockerfile` | App env → **template** `app-deployment.yaml` + `app-configmap.yaml`; creds → **template** `mysql-secret.yaml`; `JAVA_OPTS` → **value**; stale vars → **retire**; file itself **keep** for compose dev (cleaned up) | AB-406, AB-407, AB-412 |
| L9 | `mysql/Dockerfile` | `FROM mysql:5.7.13` + `schema.sql` in `/docker-entrypoint-initdb.d` | – | Docker Hub `mysql:5.7.13` | **retire** for k8s (official pinned `mysql` image, see ADR); **keep** for compose | AB-405, AB-407 |
| L10 | `mysql/schema.sql` | `create database ftgo`; grant all on `ftgo.*` to `mysqluser` | DB name, app user | MySQL entrypoint creating `MYSQL_USER` | **template** `mysql-initdb-configmap.yaml` mounted at `/docker-entrypoint-initdb.d` (single copy of the SQL) | AB-407 |
| L11 | `ftgo-application/Dockerfile` | Copy prebuilt `build/libs/ftgo-application.jar` onto `openjdk:8u171-jre-alpine`; HEALTHCHECK on `/actuator/health` | `JAVA_OPTS` | Host `./gradlew assemble` | Multi-stage build on a maintained JRE 8 base; image repo/tag/pullPolicy → **value** `image.*` | AB-405, AB-406 |
| L12 | Host Flyway step `./gradlew :ftgo-flyway:flywayMigrate` (`build-and-test-all.sh:42`, `ftgo-flyway/build.gradle`) | Apply `V1__create_ftgo_db.sql`, `V2__add_courier_optimization_and_api_tracking.sql` | `DOCKER_HOST_IP`; root / `rootpassword` hardcoded | MySQL on host port 3306 | **hook**: `ftgo-flyway` image run as initContainer on the app Deployment (default) or pre-install/pre-upgrade Job (`migrations.mode: hook`) | AB-405, AB-408 |
| L13 | `build-and-test-all.sh` | CI entry point: compose MySQL, Flyway, build, integration tests, compose up, e2e | `DOCKER_COMPOSE`, `--keep-running`, `--assemble-only` | L5, L8, L12, L7, `run-end-to-end-tests.sh` | **keep** for compose CI; chart gets its own CI pipeline | AB-411, AB-412 |
| L14 | `run-end-to-end-tests.sh` | `./gradlew :ftgo-end-to-end-tests:cleanTest :ftgo-end-to-end-tests:test` | `DOCKER_HOST_IP` (read by `EndToEndTests.java:7`), port 8081 hardcoded (`EndToEndTests.java:8`) | Running app on `$DOCKER_HOST_IP:8081` | **make** `e2e` against kind (`DOCKER_HOST_IP=localhost`, kind `extraPortMappings` 8081 → NodePort) | AB-409 |
| L15 | `deployment/kubernetes/stateful-services/ftgo-mysql-deployment.yml` | MySQL headless Service `ftgo-mysql` + StatefulSet | Inline root/user passwords; 1Gi PVC | `msapatterns/mysql:latest` from Docker Hub | **template** `mysql-statefulset.yaml` + `mysql-service.yaml` (apps/v1, selector, pinned image, Secret refs, probe, PVC size/class → **value**) | AB-407, AB-412 (delete) |
| L16 | `deployment/kubernetes/stateful-services/ftgo-db-secret.yml` | Secret `ftgo-db-secret` (username/password, base64) | Committed creds | – | **template** `mysql-secret.yaml` (demo defaults in values, `mysql.auth.existingSecret` override) | AB-407, AB-412 (delete) |
| L17 | `deployment/kubernetes/misc/create-db-secret.sh` | `kubectl create secret generic ftgo-db-secret` (same Secret as L16, again) | Plaintext creds on the command line | kubectl | **retire** (duplicate of L16; replaced by chart Secret / `existingSecret`) | AB-407, AB-412 |
| L18 | `deployment/kubernetes/scripts/kubernetes-deploy-all.sh` | Apply L15/L16, wait for `ftgo-mysql-0`, apply `*/src/deployment/kubernetes/*.yml` | – | L15, L16, L19 | **make** `deploy` (`helm upgrade --install ftgo deployment/helm/ftgo -f values-kind.yaml --wait`) | AB-409, AB-412 (delete) |
| L19 | `deployment/kubernetes/scripts/kubernetes-wait-for-ready-pods.sh` | Poll `.status.containerStatuses[0].ready` for named pods | pod names | kubectl | **retire** (`helm --wait`, probes) | AB-409, AB-412 |
| L20 | `deployment/kubernetes/scripts/port-forwards.sh` | Background `kubectl port-forward` for `svc=ftgo-application` pod to 8081, PID files | – | kubectl | **retire**: kind `extraPortMappings` host 8081 → app NodePort; Service `type`/`nodePort` → **value** | AB-409, AB-412 |
| L21 | `deployment/kubernetes/scripts/kubernetes-kill-port-forwarding.sh` | Kill PIDs from `port-forward-*.pid` | PID files | L20 | **retire** (no port-forwards) | AB-409, AB-412 |
| L22 | `deployment/kubernetes/scripts/kubernetes-run-end-to-end-tests.sh` | Wait for `application=ftgo` pods, port-forward, run e2e with `DOCKER_HOST_IP=localhost` | – | L19, L20, L14 | **make** `e2e`; **helm test** for smoke checks | AB-409, AB-410, AB-412 |
| L23 | `deployment/kubernetes/scripts/kubernetes-deploy-and-test.sh` | L18 then L22 | – | L18, L22 | **make** `kind-demo` | AB-409, AB-412 |
| L24 | `deployment/kubernetes/scripts/kubernetes-delete-all.sh` | `kubectl delete` everything applied by L18 | – | same (broken) glob as L18 | **make** / `helm uninstall ftgo` | AB-409, AB-410, AB-412 |
| L25 | `deployment/kubernetes/scripts/kubernetes-delete-volumes.sh` | Delete PVC `ftgo-mysql-persistent-storage-ftgo-mysql-0` | – | L15 naming | **retire**: document PVC behaviour on `helm uninstall` (PVCs from `volumeClaimTemplates` are kept); `make kind-down` removes everything | AB-410, AB-412 |

Not deployment artifacts, left out of scope: `open-swagger-uis.sh` and `show-swagger-ui-urls.sh` (print/open Swagger URL for `$DOCKER_HOST_IP:8081`), `setenv-circle-ci.sh` and `.circleci/` (CI; AB-411 decides the CI story).

## Known defects

| # | Defect | Where | Fixed by |
| --- | --- | --- | --- |
| D1 | StatefulSet uses `apiVersion: apps/v1beta1`, removed in Kubernetes 1.16; the API server rejects it | `deployment/kubernetes/stateful-services/ftgo-mysql-deployment.yml:15` | AB-407 |
| D2 | StatefulSet has no `spec.selector` (required in `apps/v1`) | `ftgo-mysql-deployment.yml:19-25` (spec has `serviceName`, `replicas`, `template` only) | AB-407 |
| D3 | `serviceName: "mysql"` does not match the headless Service `ftgo-mysql`, so pod DNS (`ftgo-mysql-0.mysql`) never resolves | `ftgo-mysql-deployment.yml:20` vs `:4` | AB-407 |
| D4 | Image `msapatterns/mysql:latest` with `imagePullPolicy: Always`: unpinned, needs registry access (fails offline on kind), not reproducible | `ftgo-mysql-deployment.yml:30-31` | AB-405, AB-407 |
| D5 | `--ignore-db-dir=lost+found` arg; the option was removed in MySQL 8.0 and the server refuses to start with it | `ftgo-mysql-deployment.yml:32-33` | AB-407 |
| D6 | `kubernetes-deploy-all.sh` applies `*/src/deployment/kubernetes/*.yml`, which matches no files (no module has `src/deployment/kubernetes`), so `ftgo-application` is never deployed; `kubernetes-delete-all.sh` uses the same glob | `deployment/kubernetes/scripts/kubernetes-deploy-all.sh:7`, `kubernetes-delete-all.sh:3` | AB-406, AB-409 |
| D7 | Port-forward / wait scripts select on `svc=ftgo-application` and `application=ftgo`; no manifest sets either label | `port-forwards.sh:10`, `kubernetes-run-end-to-end-tests.sh:5` | AB-409 (retired) |
| D8 | DB credentials in plaintext in four places: compose, StatefulSet env, committed Secret (base64 of `mysqluser`/`mysqlpw`), and `create-db-secret.sh`; the StatefulSet ignores the Secret that exists. Root password `rootpassword` also hardcoded in `mysql-cli.sh` and `ftgo-flyway/build.gradle` | `docker-compose.yml:8-10,19-20`; `ftgo-mysql-deployment.yml:36-42`; `ftgo-db-secret.yml:4-5`; `misc/create-db-secret.sh:1-3`; `mysql-cli.sh:5`; `ftgo-flyway/build.gradle:7-8` | AB-407 (chart Secret + `existingSecret`), AB-408 (migration creds) |
| D9 | `docker-compose.yml` sets Kafka, Zookeeper, Sleuth and Zipkin env vars for services that don't exist in this repo; nothing in the monolith consumes them | `docker-compose.yml:22-23,25-27` | AB-406 (not carried into chart), AB-412 (compose cleanup) |
| D10 | Compose uses the legacy driver class `com.mysql.jdbc.Driver`; the app ships `mysql-connector-java:8.0.33` and `application.properties` uses `com.mysql.cj.jdbc.Driver` | `docker-compose.yml:21` | AB-406 |
| D11 | `wait-for-services.sh` polls `/health`; Spring Boot 2 serves `/actuator/health` (which the Dockerfile HEALTHCHECK already uses), and polls with `curl` without `-f`, so any HTTP response (including the 404 on `/health`) counts as `connected`: the wait only checks that the port answers, not that the app is healthy | `wait-for-services.sh:10` vs `ftgo-application/Dockerfile:4` | AB-406 (probes), AB-412 (script) |
| D12 | Nothing runs Flyway in-cluster; migrations only run from the host against `DOCKER_HOST_IP` as root, so a k8s deployment would start against an empty schema | `build-and-test-all.sh:42`, `ftgo-flyway/build.gradle:5-9` | AB-408 |
| D13 | `ftgo-application/Dockerfile` requires a prior host `./gradlew assemble` and uses the deprecated `openjdk:8u171-jre-alpine` base; runs as root | `ftgo-application/Dockerfile:1,5` | AB-405 |
| D14 | `mysql-cli.sh` / `wait-for-mysql.sh` pull `mysql:5.7.13` just to run a client, as root | `mysql-cli.sh:6` | AB-407/AB-408 (probes + initContainer) |
| D15 | `build-and-run.sh` recreates MySQL with an empty volume (`docker-compose down -v`) but never runs Flyway; `schema.sql` creates only the database, so the Compose path has no tables until `./gradlew :ftgo-flyway:flywayMigrate` is run manually | `build-and-run.sh:11-17`; `mysql/schema.sql`; `build-and-test-all.sh` | AB-408 (migrations run in-cluster before the app starts) |

## Before evidence: legacy path on kind

Environment: kind v0.24.0, node image `kindest/node:v1.31.0`, kubectl v1.31.0, run from the repo root on `master`.

```
$ kind create cluster --name legacy-asis --image kindest/node:v1.31.0
$ kubectl version | grep Server
Server Version: v1.31.0
$ ./deployment/kubernetes/scripts/kubernetes-deploy-all.sh
secret/ftgo-db-secret created
service/ftgo-mysql created
error: resource mapping not found for name: "ftgo-mysql" namespace: "" from "/dev/fd/63": no matches for kind "StatefulSet" in version "apps/v1beta1"
ensure CRDs are installed first
$ echo $?
1
```

The script exits on the first error (D1). The Secret and headless Service are created, but no StatefulSet, no MySQL pod and no app workload exist. To show the defects behind D1, the follow-up commands below patch the API version in a server-side dry run and replay the rest of the script by hand:

```
+ kubectl get statefulsets,deployments,pods,svc,secrets
NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
service/ftgo-mysql   ClusterIP   None         <none>        3306/TCP   82s
service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP    85s

NAME                    TYPE     DATA   AGE
secret/ftgo-db-secret   Opaque   2      82s
+ sed s#apps/v1beta1#apps/v1# deployment/kubernetes/stateful-services/ftgo-mysql-deployment.yml
+ kubectl apply --dry-run=server -f -
service/ftgo-mysql unchanged (server dry run)
The StatefulSet "ftgo-mysql" is invalid: 
* spec.selector: Required value
* spec.template.metadata.labels: Invalid value: map[string]string{"role":"ftgo-mysql"}: `selector` does not match template `labels`
+ ls '*/src/deployment/kubernetes/*.yml'
ls: cannot access '*/src/deployment/kubernetes/*.yml': No such file or directory
+ kubectl apply -f /dev/fd/63
++ cat '*/src/deployment/kubernetes/*.yml'
cat: '*/src/deployment/kubernetes/*.yml': No such file or directory
error: no objects passed to apply
+ kubectl get pod -l application=ftgo -o name
+ kubectl get pods --selector=svc=ftgo-application -o name
```

Result:

* `apps/v1beta1` StatefulSet rejected (D1). With `apps/v1` it is still invalid: missing `spec.selector` (D2).
* The app manifest glob matches nothing, so `kubectl apply` gets no objects (D6).
* No pods carry `application=ftgo` or `svc=ftgo-application`, so the wait and port-forward scripts have nothing to act on (D7).
* End state: one Secret and one Service; **no MySQL and no `ftgo-application` running**.

## Final status: legacy → Helm parity (AB-412)

Every row from the inventory is either **migrated** (a chart template, value, hook, helm test or Make target now does the job) or **retired** (deleted with the legacy Kubernetes directory in AB-412, with nothing needed in its place). Compose rows are **kept** for inner-loop development: `docker-compose.yml` and its scripts stay, cleaned up (see below).

| ID | Legacy artifact | Status | Replacement |
| --- | --- | --- | --- |
| L1 | `build-and-run.sh` | kept (compose) + migrated | `make kind-demo` |
| L2 | `start-services.sh` | kept (compose) + migrated | `make deploy` (`helm upgrade --install --wait`) |
| L3 | `start-infrastructure-services.sh` | kept (compose) + migrated | `templates/mysql-statefulset.yaml`, `mysql-service.yaml` |
| L4 | `build-and-restart-application.sh` | kept (compose) + migrated | `make images deploy` (unique image tag rolls the Deployment) |
| L5 | `set-env.sh` | kept (compose) | `make e2e` sets `DOCKER_HOST_IP=localhost` |
| L6 | `wait-for-mysql.sh`, `mysql-cli.sh` | kept (compose) + migrated | MySQL startup/readiness probes; Flyway `connectRetries`; `helm --wait` |
| L7 | `wait-for-services.sh` | kept (compose, fixed: D11) + migrated | app startup/readiness/liveness probes; `templates/tests/test-health.yaml` (AB-410) |
| L8 | `docker-compose.yml` | kept (compose, cleaned: D9, D15) + migrated | `app-deployment.yaml`, `app-configmap.yaml`, `db-secret.yaml`, `app.javaOpts` |
| L9 | `mysql/Dockerfile` | kept (compose) + migrated | official `mysql:8.0.39` (`mysql.image.*`) |
| L10 | `mysql/schema.sql` | migrated | `mysql-initdb-configmap.yaml` (reads `files/schema.sql` → `mysql/schema.sql`) |
| L11 | `ftgo-application/Dockerfile` | migrated | multi-stage image (AB-405); `app.image.*` |
| L12 | host `./gradlew :ftgo-flyway:flywayMigrate` | migrated | `ftgo-flyway` image as initContainer or hook Job (`migrations.*`); compose `ftgo-flyway` service |
| L13 | `build-and-test-all.sh` | kept (compose CI) | chart CI pipeline (AB-411) |
| L14 | `run-end-to-end-tests.sh` | kept (compose) + migrated | `make e2e` |
| L15 | legacy MySQL StatefulSet YAML | retired | `mysql-statefulset.yaml`, `mysql-service.yaml` |
| L16 | legacy DB Secret YAML | retired | `db-secret.yaml` / `mysql.auth.existingSecret` |
| L17 | `create-db-secret.sh` | retired | `db-secret.yaml` / `mysql.auth.existingSecret` |
| L18 | `kubernetes-deploy-all.sh` | retired | `make deploy` |
| L19 | `kubernetes-wait-for-ready-pods.sh` | retired | `helm --wait` + probes |
| L20 | `port-forwards.sh` | retired | kind `extraPortMappings` → NodePort 30081 (`app.service.*`) |
| L21 | `kubernetes-kill-port-forwarding.sh` | retired | no port-forwards |
| L22 | `kubernetes-run-end-to-end-tests.sh` | retired | `make e2e`, `helm test` |
| L23 | `kubernetes-deploy-and-test.sh` | retired | `make kind-demo` |
| L24 | `kubernetes-delete-all.sh` | retired | `helm uninstall ftgo` / `make kind-down` |
| L25 | `kubernetes-delete-volumes.sh` | retired | `kubectl delete pvc -l app.kubernetes.io/instance=ftgo,app.kubernetes.io/component=mysql` (README "Uninstall") |

Compose clean-up in AB-412:

- The stale Kafka, Zookeeper, Sleuth and Zipkin variables are removed (D9).
- A one-shot `ftgo-flyway` service, built from the same `ftgo-flyway/Dockerfile` as the chart's migrations image, migrates the schema. `ftgo-application` waits for it to complete successfully, so `build-and-run.sh` no longer leaves an empty schema (D15).
- `wait-for-services.sh` polls `/actuator/health` with `curl -f` (D11).
- The scripts call `docker compose` (Compose v2). The standalone `docker-compose` v1 binary is end-of-life.

The file/line references in the tables above describe the legacy tree as it was on `master` before AB-412. The legacy Kubernetes files remain available in git history.
