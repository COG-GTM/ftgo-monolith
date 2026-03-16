# CI Build Pipeline

This document describes the Continuous Integration (CI) build pipeline for the FTGO microservices, implemented as a GitHub Actions workflow in `.github/workflows/ci-build.yml`.

---

## Overview

The CI pipeline provides independent, per-service Gradle builds that trigger only when relevant code changes are detected. This ensures fast feedback loops and avoids unnecessary builds when unrelated parts of the codebase change.

### Key Features

- **Per-service build jobs** — each microservice builds and tests independently
- **Path-based filtering** — only affected services are rebuilt on each commit
- **Shared dependency awareness** — changes to `shared-libraries/`, `buildSrc/`, or root build files trigger all service builds
- **Gradle dependency caching** — speeds up repeat builds via `actions/cache`
- **Test report artifacts** — HTML test reports are uploaded on build failures for debugging
- **Build summaries** — each job writes a summary to the GitHub Actions step summary

---

## Trigger Rules

The workflow runs on:

| Event | Branches | Condition |
|-------|----------|-----------|
| `push` | `feat/microservices-migration`, `main`, `master` | Files changed in watched paths |
| `pull_request` | `feat/microservices-migration`, `main`, `master` | Files changed in watched paths |

### Watched Paths

| Path Pattern | Description |
|-------------|-------------|
| `services/**` | Microservice source code, Dockerfiles, k8s manifests |
| `shared-libraries/**` | Shared library modules |
| `buildSrc/**` | Convention plugins and build logic |
| `build.gradle` | Root build configuration |
| `settings.gradle` | Multi-module project settings |
| `gradle.properties` | Build properties |
| `.github/workflows/ci-build.yml` | The CI workflow itself |

---

## Change Detection Logic

The `detect-changes` job determines which services need to be built:

1. **Service-specific changes**: If files under `services/<service-name>/` changed, only that service is built.
2. **Shared changes**: If files under `shared-libraries/`, `buildSrc/`, root build files, or the CI workflow itself changed, **all** services are rebuilt (since they may depend on these shared resources).
3. **PR vs Push**: On push events, changed files are detected via `HEAD~1..HEAD`. On pull requests, the diff is computed between the PR base and head SHAs.

### Example Scenarios

| Change | Services Built |
|--------|---------------|
| `services/order-service/src/...` | order-service only |
| `services/consumer-service/src/...` + `services/courier-service/src/...` | consumer-service + courier-service |
| `shared-libraries/ftgo-common/...` | All 4 services |
| `buildSrc/...` | All 4 services |
| `build.gradle` | All 4 services |

---

## Build Jobs

Each service has a dedicated build job that runs independently and in parallel:

| Job | Gradle Task | Condition |
|-----|------------|-----------|
| `build-order-service` | `:services:order-service:build` | order-service changed or shared changed |
| `build-consumer-service` | `:services:consumer-service:build` | consumer-service changed or shared changed |
| `build-restaurant-service` | `:services:restaurant-service:build` | restaurant-service changed or shared changed |
| `build-courier-service` | `:services:courier-service:build` | courier-service changed or shared changed |

### Build Steps (per service)

1. **Checkout** — full repository checkout via `actions/checkout@v4`
2. **JDK 8 setup** — Temurin JDK 8 via `actions/setup-java@v4` (project requirement)
3. **Gradle cache** — caches `~/.gradle/caches` and `~/.gradle/wrapper` keyed on `*.gradle` files and wrapper properties
4. **Gradle build** — runs `./gradlew :services:<service>:build` excluding end-to-end test modules
5. **Test report upload** — on failure, uploads HTML test reports as artifacts (retained 14 days)
6. **Build summary** — writes job status to GitHub Step Summary

---

## CI Summary Job

The `ci-summary` job runs after all build jobs (using `if: always()`) and:

- Aggregates pass/fail/skipped status for each service
- Reports whether each service was triggered
- Fails the overall workflow if any triggered build failed

This provides a single check status for the entire CI pipeline.

---

## Gradle Caching

Caching is configured to speed up subsequent builds:

- **Cache paths**: `~/.gradle/caches`, `~/.gradle/wrapper`
- **Cache key**: `<os>-gradle-<hash of all *.gradle files and wrapper properties>`
- **Restore keys**: Falls back to `<os>-gradle-` prefix if exact key not found

This means the first build populates the cache, and subsequent builds reuse downloaded dependencies.

---

## Test Reports

When a build fails, HTML test reports are uploaded as GitHub Actions artifacts:

| Service | Artifact Name | Path |
|---------|--------------|------|
| order-service | `order-service-test-reports` | `services/order-service/build/reports/tests/` |
| consumer-service | `consumer-service-test-reports` | `services/consumer-service/build/reports/tests/` |
| restaurant-service | `restaurant-service-test-reports` | `services/restaurant-service/build/reports/tests/` |
| courier-service | `courier-service-test-reports` | `services/courier-service/build/reports/tests/` |

Artifacts are retained for **14 days** and can be downloaded from the workflow run's Artifacts section.

---

## Concurrency

The workflow uses GitHub Actions concurrency control:

```yaml
concurrency:
  group: ci-build-${{ github.ref }}
  cancel-in-progress: true
```

This ensures that if a new commit is pushed while a build is in progress for the same branch/PR, the in-progress run is cancelled in favor of the newer one.

---

## Relationship to Other Workflows

| Workflow | Purpose | Trigger |
|----------|---------|---------|
| **CI Build** (`ci-build.yml`) | Compile and test services | PR and push to migration branch |
| **Docker Build** (`docker-build.yml`) | Build and push container images | Push/PR with `services/**` changes |
| **Deploy** (`deploy.yml`) | Deploy to Kubernetes environments | Push to migration branch (k8s paths) or manual |

The CI Build workflow is designed to run alongside (not replace) the Docker Build workflow. CI Build validates compilation and tests, while Docker Build handles container image creation.

---

## Adding a New Service

When adding a new microservice to the CI pipeline:

1. Add a new build job in `ci-build.yml` following the existing pattern
2. Add the service to the `detect-changes` job's service list
3. Add the service to the `ci-summary` job's `needs` list and summary table
4. Update badge references in `README.md` if needed

---

## Troubleshooting

### Build not triggered
- Verify your changes are in a watched path (see [Watched Paths](#watched-paths))
- Check that the PR targets one of the configured branches
- Look at the `detect-changes` job output to see which services were detected

### Gradle cache miss
- The cache key is based on all `*.gradle` files — changes to any Gradle file invalidate the cache
- First build after a cache miss will be slower as dependencies are re-downloaded

### Test failures
- Download the test report artifact from the workflow run
- Open the HTML report in a browser for detailed failure information
- Run the failing test locally: `./gradlew :services:<service>:test`
