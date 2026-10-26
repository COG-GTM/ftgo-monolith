# 0001. Helm chart for deployment, with kind as the local/demo target

- **Status:** Proposed
- **Date:** 2026-09-30
- **ARB ticket:** TO BE CREATED (no ARB project exists on cog-gtm.atlassian.net yet; tracked from Jira AB-404, epic AB-402)
- **Authors:** Devin (on behalf of the AB-402 epic owner)
- **Owning team:** TBD — owner to confirm before ARB (FTGO monolith team)
- **Related ADRs:** none (first ADR in this repository)

## Context

`ftgo-monolith` (Spring Boot 2.0.3, Java 8, Gradle 4.10.2 wrapper) is deployed today in three
inconsistent ways:

- `docker-compose.yml` builds `mysql/Dockerfile` (`FROM mysql:5.7.13` + `mysql/schema.sql`) and
  `ftgo-application/Dockerfile`, with credentials inline (`rootpassword`, `mysqluser`/`mysqlpw`).
- `deployment/kubernetes/` holds plain YAML applied by shell scripts
  (`deployment/kubernetes/scripts/kubernetes-deploy-all.sh`). The manifests use removed APIs
  (`apps/v1beta1` StatefulSet), unpinned images (`msapatterns/mysql:latest`,
  `imagePullPolicy: Always`), and a committed base64 Secret (`ftgo-db-secret.yml`).
- Schema migrations (`ftgo-flyway`, Gradle Flyway plugin 6.0.0, `V1__create_ftgo_db.sql`,
  `V2__add_courier_optimization_and_api_tracking.sql`) are run by hand from a developer machine
  with root credentials; nothing in the Kubernetes path runs them.

The Kubernetes path does not apply on any current Kubernetes (1.16+ removed `apps/v1beta1`), and
none of the three paths is versioned or upgradeable as a unit or can be rolled back. Epic AB-402 replaces them with a
single Helm chart, demoed on a local kind cluster.

Constraints:

- The application already ships `mysql-connector-java:8.0.33`, driver `com.mysql.cj.jdbc.Driver`,
  and `allowPublicKeyRetrieval=true` in `spring.datasource.url`.
- Hibernate is 5.2.17.Final (via Spring Boot 2.0.3), which has no `MySQL8Dialect` (added in 5.3).
- `mysql/schema.sql` only creates the `ftgo` database and grants `mysqluser` on it; all tables
  come from the Flyway migrations.
- The end-to-end tests hardcode application port 8081; the in-container health endpoint is
  `/actuator/health` on port 8080.
- The demo must run fully offline after images are pulled/built: no external chart repositories,
  no registry push.

ARB triggers: **T1** (new deployable units: Helm release with app Deployment, MySQL StatefulSet,
Flyway migration initContainer/Job; new container images `ftgo-application`, `ftgo-flyway`),
**T2** (changed data store: MySQL engine 5.7 -> 8.0 and its provisioning/initialisation path),
**T7** (major runtime/tooling upgrades: MySQL 5.7 -> 8.0, Flyway 6 -> 10; introduction of Helm and
kind). Not triggered: T3, T4, T5, T6, T8, T9 (see "Architecture review" in the AB-404 PR).

## Decision

We will package and deploy `ftgo-monolith` as **one umbrella Helm chart at
`deployment/helm/ftgo/`** that owns the application Deployment/Service, an **in-chart MySQL 8.0
StatefulSet** (official `mysql:8.0.39`, `mysql/schema.sql` delivered as a ConfigMap mounted into
`/docker-entrypoint-initdb.d/`), and **Flyway migrations** packaged as a dedicated `ftgo-flyway`
image (`FROM flyway/flyway:<pinned 10.x>` + the SQL files) that runs as an **initContainer on the
app Deployment by default**, with `migrations.mode: hook` (pre-install/pre-upgrade Job) as an
alternative. Database credentials come from a **chart-managed Secret with demo defaults**,
overridable with **`mysql.auth.existingSecret`**; no real credentials are committed. **kind** is the
local/demo target, with pinned tool versions (kind v0.24.0,
`kindest/node:v1.31.0@sha256:53df588e04085fd41ae12de0c3fe4c72f7013bba32a20e7325357a1ac94ba865`,
kubectl v1.31.x, Helm v3.16.x, kubeconform validating Kubernetes 1.31.0). Images
`ftgo-application:<git-sha>` / `:dev` and `ftgo-flyway:*` are built locally and loaded with
`kind load docker-image` into a cluster named `ftgo`, using `imagePullPolicy: IfNotPresent`. The app
is reachable on `http://localhost:8081` via a NodePort Service and a kind `extraPortMappings` entry
(see decision 7).

### Decision details

#### 1. Packaging: one umbrella Helm chart

| Alternative | Pros | Cons | Why rejected |
| --- | --- | --- | --- |
| Do nothing (keep scripts + Compose + current YAML) | No work | Manifests do not apply on any supported Kubernetes (`apps/v1beta1`); `latest` images; committed Secret; no migrations; no rollback | Broken today; fails the epic goal |
| Keep plain YAML, fixed up (`apps/v1`, pinned images, script-driven) | No new tool; smallest diff | No parameterisation (image tag, credentials, storage) without `sed`/`envsubst`; no release history, `upgrade`/`rollback`, or `helm test`; ordering (DB -> migrate -> app) stays in shell scripts | Keeps the scripts the epic is meant to retire |
| Kustomize (base + `kind` overlay) | Built into kubectl; declarative overlays; no templating language | No release object, so no atomic upgrade/rollback or hooks; value-driven toggles (e.g. `migrations.mode`, `existingSecret`) become patch sprawl; no packaging/versioning | Upgrade/rollback and toggles are core to AB-410 and the demo |
| **Helm, one umbrella chart (chosen)** | Single versioned artifact; values-driven config; `helm upgrade --atomic`, `helm rollback`, `helm test`, hooks; `helm lint`/`template` + kubeconform in CI | Templating complexity; Helm becomes a required tool | — |

Separate charts per component (app / MySQL / Flyway) were not pursued: the three are released and
rolled back together, and splitting them re-introduces cross-release ordering.

#### 2. MySQL inside the chart: in-chart templates vs Bitnami subchart

| Alternative | Pros | Cons | Why rejected |
| --- | --- | --- | --- |
| **In-chart MySQL templates (chosen)** | No external chart repo or dependency download (offline demo); official `mysql` image; keeps the existing `schema.sql` init via `/docker-entrypoint-initdb.d`; small, readable templates | We own the StatefulSet/Service/PVC templates; single replica, no replication/backup | — |
| Bitnami `mysql` subchart | Production-grade: replication, metrics, backups, hardened defaults | External dependency (`helm dependency update`, OCI registry pull), Bitnami image and values conventions differ from the official image, heavier for a demo; Bitnami's public catalogue/image-distribution terms have been changing, adding supply-chain risk to a demo | Chosen as the **production-grade alternative**, not for the demo |
| Managed database (e.g. RDS) | Real HA/backups | Not available on kind; out of epic scope; would trigger T2/T5 on cloud infra | Out of scope for local/demo |

No custom MySQL image is built for Kubernetes: `mysql/schema.sql` becomes ConfigMap content in
AB-407. The Compose-only `mysql/Dockerfile` is retired with the legacy path in AB-412.

#### 3. MySQL version: move to 8.0 (`mysql:8.0.39`)

Decision: **MySQL 8.0**, official image pinned to the exact tag `mysql:8.0.39`.

Justification:

- MySQL 5.7 reached end of life (Oracle) in October 2023; 5.7.13 is years behind on security fixes.
- The application already uses Connector/J 8.0.33 and `com.mysql.cj.jdbc.Driver`, which support
  MySQL 8's default `caching_sha2_password` authentication; `allowPublicKeyRetrieval=true` (with
  `useSSL=false`) is exactly the setting needed for that plugin over a non-TLS connection.
- Flyway is moving to a pinned 10.x Docker image (the Gradle plugin 6.0.0 remains only for local
  developer use until AB-412), which supports MySQL 8.0.
- Verified empirically (see the AB-404 PR for command output):
  - `mysql:8.0.39` initialises with the current `mysql/schema.sql` (database `ftgo`,
    `GRANT ALL ... TO 'mysqluser'@'%'`) via `/docker-entrypoint-initdb.d`.
  - `flyway/flyway:10.17.3` applies V1 and V2 cleanly (`flyway_schema_history` at v2).
  - The application (Java 8, Hibernate 5.2.17.Final, `MySQL5Dialect` auto-selected) starts,
    reports `{"status":"UP"}`, and completes the create consumer -> restaurant -> order -> courier
    -> accept -> preparing -> ready -> picked up -> delivered flow against MySQL 8.0.39.

Caveats (not blockers):

- Hibernate 5.2 has no `MySQL8Dialect`; `MySQL5Dialect`/`MySQL57Dialect` work for the SQL this app
  issues. Revisit if/when Spring Boot is upgraded.
- MySQL 8 defaults to `utf8mb4` / `utf8mb4_0900_ai_ci` (5.7: `latin1`). The schema declares no
  charset, so new tables pick up `utf8mb4`; this is desirable and does not affect the demo.
- On current `master`, `POST /orders` fails with `Unknown column 'latitude'` on **any** MySQL
  version because the V2 migration adds `delivery_address_latitude/longitude` while the entity maps
  `latitude/longitude`. This is an application bug, not a MySQL 8 incompatibility; it is fixed by
  PR COG-GTM/ftgo-monolith#348 and must land before AB-409's end-to-end tests can pass.

Staying on 5.7 was rejected: EOL, no benefit for this codebase, and the official `mysql:5.7`
images are no longer maintained.

#### 4. Migrations: Flyway initContainer (default) vs Helm hook Job

| Alternative | Pros | Cons | Why rejected |
| --- | --- | --- | --- |
| **initContainer on the app Deployment (default)** | App container never starts against an unmigrated schema; works identically on `install` and `upgrade`; no hook lifecycle or leftover Jobs; failures surface as `Init:CrashLoopBackOff` on the pod being rolled out, and `helm upgrade --atomic` rolls back | Runs once per pod start (Flyway is idempotent and takes a DB lock, so concurrent replicas are safe but redundant); migration image is coupled to the app pod | — |
| Helm pre-install/pre-upgrade hook Job (`migrations.mode: hook`) | Runs exactly once per release; decouples migration from pod restarts; common in production | On first `install` the hook runs before the in-chart MySQL StatefulSet exists (hooks run before regular resources), so it needs retry/wait logic or MySQL outside the release; hook Jobs are not rolled back by `helm rollback`; hook resources need delete policies | Kept as an opt-in alternative (`migrations.mode: hook`), mainly for use with an external/existing database |
| Keep running Flyway manually via Gradle | No change | Needs root creds and network access to the cluster DB from a laptop; not reproducible | Does not meet the one-command deploy goal |
| Hibernate `ddl-auto` | No extra image | Unsafe for existing data; diverges from the Flyway history | Rejected |

Flyway connects as the app user `mysqluser` to database `ftgo` (it only needs privileges on
`ftgo`, which `schema.sql` grants), not root.

#### 5. Secrets: chart-managed demo Secret with `existingSecret` override

- By default the chart renders one Secret with demo values (root password, `mysqluser` password),
  clearly labelled as demo-only in `values.yaml`.
- Setting `mysql.auth.existingSecret: <name>` makes the chart render no Secret and reference the
  named Secret instead (created out-of-band, e.g. `kubectl create secret` or an external secret
  operator). Keys are documented in `values.yaml`.
- The committed `deployment/kubernetes/stateful-services/ftgo-db-secret.yml` is removed with the
  legacy path (AB-412). No real credentials are ever committed; only well-known demo defaults.

Rejected: committing base64 Secrets (status quo — base64 is not encryption); requiring a Secret to
exist before install (breaks the one-command demo); Sealed Secrets / SOPS / External Secrets
(adds controllers or keys the demo does not need — a valid production follow-up via
`existingSecret`).

#### 6. Pinned tool versions for the demo

| Tool | Version | Notes |
| --- | --- | --- |
| kind | v0.24.0 | Release binary verified downloadable |
| kind node image | `kindest/node:v1.31.0@sha256:53df588e04085fd41ae12de0c3fe4c72f7013bba32a20e7325357a1ac94ba865` | Digest from the kind v0.24.0 release notes; manifest verified with `docker manifest inspect` |
| kubectl | v1.31.x | Within kubectl's +/-1 minor skew of the 1.31 node |
| Helm | v3.16.x | Supports Kubernetes 1.31 |
| kubeconform | validates against Kubernetes 1.31.0 | Used in CI (AB-411) with `helm template` output |
| MySQL | `mysql:8.0.39` | Exact tag |
| Flyway | `flyway/flyway:10.x` pinned to an exact tag in AB-408 (10.17.3 used for verification) | |

#### 7. Local access: NodePort + kind `extraPortMappings`

kind's `extraPortMappings` forwards a host port to a port on the kind node container, not to a
Service, so the kind values expose the app through a `NodePort` Service with a fixed `nodePort`
(default `30081`, configurable; `service.type` stays `ClusterIP` by default outside kind), and the
kind cluster config maps `containerPort: 30081` -> `hostPort: 8081` with
`listenAddress: 127.0.0.1`. This gives a stable `http://localhost:8081` for the e2e tests with no
long-running process.

| Alternative | Pros | Cons | Why rejected |
| --- | --- | --- | --- |
| **NodePort + `extraPortMappings` (chosen)** | No extra components; survives pod restarts; one-command deploy | Port fixed at cluster-create time; kind-specific values | — |
| Ingress controller (ingress-nginx) + `extraPortMappings` 80/443 | Production-like routing | Extra controller to install and pin; host routing not needed for one app | Overkill for the demo |
| `kubectl port-forward svc/ftgo 8081:8080` | No cluster config | Foreground process that dies on pod restarts/rollouts; breaks during `helm upgrade` demos | Kept only as a runbook fallback |

Images are built locally (`scripts/build-images.sh`, AB-405) and loaded with
`kind load docker-image` into the kind cluster `ftgo`; no registry is involved, hence
`imagePullPolicy: IfNotPresent`.

## Alternatives considered

| Alternative | Pros | Cons | Why rejected |
| --- | --- | --- | --- |
| Do nothing | No effort | Current manifests do not apply on supported Kubernetes; committed creds; no migrations | Broken; see decision 1 |
| Fixed-up plain YAML + scripts | No new tooling | No parameterisation, upgrade/rollback, or tests | See decision 1 |
| Kustomize | In kubectl; overlays | No release/rollback/hooks | See decision 1 |
| Helm + Bitnami MySQL subchart | Production-grade DB features | External dependency; heavier; not offline | Production alternative; see decision 2 |
| Stay on MySQL 5.7 | Matches Compose today | EOL; no benefit | See decision 3 |
| Flyway as Helm hook Job (default) | Runs once per release | Ordering problem with in-chart DB on install; not rolled back | Kept as opt-in `migrations.mode: hook`; see decision 4 |
| minikube / k3d / Docker Desktop Kubernetes as local target | Also local Kubernetes | Not uniformly available in CI; kind is the upstream-conformance tool with pinned node images | kind chosen for reproducibility in CI and demo |

## Architecture

```mermaid
C4Container
    title FTGO monolith on kind via Helm (deployment/helm/ftgo)
    Person(user, "Demo user / e2e tests", "curl, EndToEndTests on localhost:8081")
    Person(dev, "Developer", "scripts/build-images.sh, helm, kind")
    System_Boundary(kind, "kind cluster 'ftgo' (kindest/node v1.31.0)") {
        System_Boundary(rel, "Helm release 'ftgo' (chart deployment/helm/ftgo)") {
            Container(app, "ftgo-application", "Deployment, Spring Boot 2.0.3 / Java 8", "REST API :8080, /actuator/health")
            Container(flyway, "ftgo-flyway", "initContainer (default) or hook Job, Flyway 10.x", "Applies V1..Vn to ftgo")
            ContainerDb(mysql, "ftgo-mysql", "StatefulSet + PVC, mysql:8.0.39", "Database ftgo; init from schema.sql ConfigMap")
            Container(secret, "DB credentials", "Secret (chart demo default or existingSecret)", "root + mysqluser passwords")
        }
    }
    System_Ext(hub, "Docker Hub", "mysql, flyway/flyway, kindest/node base images (pinned)")
    Rel(user, app, "HTTP (no auth): localhost:8081 -> kind extraPortMappings -> NodePort 30081 -> :8080")
    Rel(flyway, mysql, "MySQL protocol :3306 / mysqluser password")
    Rel(app, mysql, "JDBC (Connector/J 8.0.33) :3306 / mysqluser password")
    Rel(app, secret, "env from secretKeyRef")
    Rel(flyway, secret, "env from secretKeyRef")
    Rel(dev, rel, "helm install/upgrade/rollback/test via kube-apiserver (kubeconfig)")
    Rel(dev, hub, "docker pull / build (HTTPS)")
```

Flow: `helm install` -> MySQL StatefulSet starts and runs `schema.sql` on an empty volume ->
app pod's `ftgo-flyway` initContainer waits for MySQL and migrates -> app container starts ->
readiness on `/actuator/health`.

## Non-functional requirements

| NFR | Target | How met |
| --- | --- | --- |
| Availability SLO | N/A — local/demo environment, no SLO | Single replica app and MySQL; liveness/readiness probes restart failed pods |
| p95 latency | N/A — demo; unchanged from current app behaviour | No change to application code path |
| RPO / RTO | N/A for demo (data is disposable); RTO for a fresh environment: TBD — owner to confirm (target "one command, a few minutes") | PVC survives pod restarts and `helm upgrade`; `helm uninstall` + cluster delete discards data by design |
| Peak load | N/A — single demo user + e2e tests | — |
| Scaling model | Manual `replicaCount`; MySQL fixed at 1 | initContainer migrations are idempotent and Flyway-locked, so app replicas > 1 are safe |
| Data retention | Demo data only; lifetime of the kind cluster | No backups; Bitnami/managed DB is the production path |
| Deploy reproducibility | Identical result on any machine with Docker | All tools and images pinned (node image by digest) |

## Security & compliance

- **Data classification:** Demo/synthetic data only (fake consumers, restaurants, orders). No PII
  or production data; classification unchanged from today.
- **Encryption at rest:** None (kind PVC on the host's Docker volume). Acceptable for demo data.
  Production would require an encrypted storage class or managed DB.
- **Encryption in transit:** None inside the cluster (JDBC `useSSL=false`, as today). Traffic
  never leaves the local kind network; the app is exposed only on `localhost:8081`.
- **AuthN / AuthZ:** Application API is unauthenticated (unchanged). DB access via `mysqluser`
  password scoped to `ftgo.*`; Flyway no longer uses root.
- **Secrets:** Chart-managed Secret with demo defaults; `mysql.auth.existingSecret` for anything
  beyond demo. Removes the committed `ftgo-db-secret.yml`. No real credentials in git.
- **Audit logging:** N/A — local demo; Kubernetes audit logging not enabled on kind.
- **Data residency / regions:** N/A — runs on the developer/CI machine; no cloud resources.
- **Policy sections satisfied:** No AWS/CDK/Terraform resources, so `approved-infra.yaml` (T5) is
  not engaged. Images pinned by exact tag (node image by digest); no `latest`.
- **Threats considered:** supply chain (pinned tags/digest; no third-party charts); credential
  leakage (no real creds committed; override path); accidental exposure (kind `extraPortMappings`
  uses `listenAddress: 127.0.0.1` — implemented in the kind config in AB-409).

## Cost

| Item | Assumption | Monthly estimate |
| --- | --- | --- |
| kind cluster | Runs on existing developer laptops / CI runners | $0 incremental |
| CI (AB-411) | Adds `helm lint` / `helm template` / kubeconform, and possibly a kind job, to existing CI | TBD — owner to confirm CI minutes; expected well under $500/mo |
| Container registry | None (images loaded with `kind load`) | $0 |
| **Total** | | **~$0 new recurring spend (< $2k/mo threshold)** |

## Operations

- **On-call rotation:** N/A — demo environment has no on-call. Owning team for the chart: TBD —
  owner to confirm before ARB.
- **Runbook:** `docs/` demo runbook delivered in AB-413; chart usage in the chart README (AB-406).
- **Dashboards / alarms:** N/A — demo. `helm test` (AB-410) provides a post-deploy smoke check;
  `/actuator/health` backs readiness/liveness probes.
- **Rollback plan:** `helm rollback ftgo <revision>` (or `helm upgrade --atomic`, which rolls back
  automatically on failure). Schema migrations are forward-only: a rollback restores the previous
  app image but not the schema, so migrations must stay backward compatible (expand/contract). The
  environment can always be recreated with `kind delete cluster --name ftgo` + one-command deploy.
  The legacy scripts stay in the repo until AB-412, so the epic itself can be reverted by
  reverting its PRs.
- **Migration / cut-over plan:** AB-405 images -> AB-406 chart + app -> AB-407 MySQL -> AB-408
  Flyway -> AB-409 kind one-command deploy + e2e -> AB-410 upgrade/rollback/`helm test` -> AB-411
  CI -> AB-412 retire legacy scripts/YAML/Compose MySQL image -> AB-413 demo runbook. Chart PRs
  must not merge before ARB outcome is recorded on AB-404.

## Policy exceptions requested

| Rule | Resource | Justification | Compensating control | Expiry |
| --- | --- | --- | --- | --- |
| none | | | | |

## Consequences

- Positive: one versioned, reproducible deployment artifact; working Kubernetes path on current
  Kubernetes; migrations automated and ordered; no committed credentials; supported MySQL
  version; upgrade/rollback/test built in.
- Negative / risks: Helm and kind become required tools; in-chart MySQL is not production-grade
  (no HA/backups); forward-only migrations limit what `helm rollback` can undo; Hibernate 5.2 uses
  `MySQL5Dialect` against MySQL 8.
- Follow-ups: land PR COG-GTM/ftgo-monolith#348 before AB-409 e2e; production deployment (Bitnami
  or managed DB, TLS, External Secrets) needs its own ADR; consider moving the local Gradle Flyway
  plugin (6.0.0) to match the image's Flyway major.

## Open questions

- Owning team and on-call for the chart (TBD — owner to confirm before ARB).
- Reviewers: Platform (Kubernetes/Helm packaging) and Data (MySQL 5.7 -> 8.0) — confirm with ARB.
- ARB project: none exists on cog-gtm.atlassian.net; where should the ARB ticket be filed?
- CI cost of a kind-based job in AB-411 (TBD — owner to confirm).
