# FTGO Platform - Automated Testing Pipeline

## Overview

The FTGO platform uses a three-tier automated testing strategy executed via GitHub Actions CI pipelines. Each tier provides progressively broader validation with increasing execution time and infrastructure requirements.

| Tier | Workflow | Dependencies | Target Time | Trigger |
|------|----------|-------------|-------------|---------|
| Unit Tests | `ci-test-unit.yml` | None | < 2 min/service | Push & PR |
| Integration Tests | `ci-test-integration.yml` | MySQL (service container) | < 10 min/service | Push & PR |
| End-to-End Tests | `ci-test-e2e.yml` | Full Docker Compose stack | < 20 min | Manual (workflow_dispatch) |

## Test Tiers

### Tier 1: Unit Tests

**Workflow:** `.github/workflows/ci-test-unit.yml`

Unit tests run without any external dependencies and provide the fastest feedback loop.

**Execution strategy:**
- Shared libraries run in a single job with `--parallel` flag
- Microservices run in a matrix strategy (one job per service) for parallel execution
- Both new (`services/`) and legacy module paths are tested

**Modules tested:**
- Shared libraries: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util`, all API contracts
- Microservices: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`

**Output:**
- JUnit XML test results (published as GitHub Check annotations)
- HTML test reports (uploaded as artifacts)
- JaCoCo code coverage reports (XML + HTML)

### Tier 2: Integration Tests

**Workflow:** `.github/workflows/ci-test-integration.yml`

Integration tests validate service behavior with a real MySQL database using GitHub Actions service containers.

**Execution strategy:**
- Matrix strategy: one job per service
- MySQL 8.0 service container provisioned per job
- Flyway migrations applied before tests run

**Environment:**
- MySQL 8.0 with database `ftgo`
- Credentials: `mysqluser` / `mysqlpw`
- Connection: `jdbc:mysql://localhost:3306/ftgo`

**Source set:** `src/integration-test/java` (configured by `ftgo.testing-conventions` plugin)

### Tier 3: End-to-End Tests

**Workflow:** `.github/workflows/ci-test-e2e.yml`

E2E tests validate the complete application stack using Docker Compose.

**Current status:** Manual trigger only (`workflow_dispatch`) due to pre-existing build failure in `ftgo-end-to-end-tests-common` (missing `eventuate-util-test` dependency).

**Execution flow:**
1. Build application (excluding broken E2E modules)
2. Start Docker Compose stack (MySQL + FTGO application)
3. Wait for MySQL and application health checks
4. Run Flyway migrations
5. Execute E2E tests
6. Collect Docker Compose logs
7. Tear down stack

**To enable automatic triggers:** Resolve the `eventuate-util-test` dependency in `ftgo-end-to-end-tests-common`, then uncomment the `push`/`pull_request` triggers in the workflow.

## Test Reporting

### Workflow: `ci-test-report.yml`

Aggregates test results and coverage reports from unit and integration test workflows.

**Triggered by:** Completion of `CI: Unit Tests` or `CI: Integration Tests` workflows.

**Reports generated:**
- Aggregated test result counts per tier
- JaCoCo coverage report inventory
- GitHub Step Summary with dashboard view
- Combined artifacts retained for 30 days

### JUnit Test Results

All test workflows publish JUnit XML results using the `EnricoMi/publish-unit-test-result-action@v2` action. This provides:
- Test result annotations on GitHub Check runs
- Per-commit test counts (passed, failed, skipped)
- Test duration tracking

### Artifact Retention

| Artifact Type | Retention |
|--------------|-----------|
| Test reports (HTML/XML) | 14 days |
| Coverage reports (JaCoCo) | 14 days |
| Aggregated reports | 30 days |
| Docker Compose logs (E2E) | 7 days |

## Code Coverage

### Configuration

Code coverage is provided by JaCoCo, configured in the `ftgo.testing-conventions` Gradle convention plugin.

**JaCoCo version:** 0.8.11

**Coverage target:** 70% instruction coverage (enforced via `jacocoTestCoverageVerification` task)

**Reports generated:**
- XML reports (for CI tooling integration)
- HTML reports (for human review)

### Running Coverage Locally

```bash
# Run unit tests with coverage
./gradlew test jacocoTestReport

# Run integration tests with coverage
./gradlew integrationTest jacocoIntegrationTestReport

# Verify coverage meets threshold
./gradlew jacocoTestCoverageVerification
```

## JUnit 5 Configuration

### New Microservices

New microservices use JUnit 5 (Jupiter) via the `ftgo.testing-conventions` convention plugin:

```groovy
plugins {
    id 'ftgo.testing-conventions'
}
```

This provides:
- `useJUnitPlatform()` on all test tasks
- JUnit 5 Jupiter dependencies (via version catalog)
- Integration test source set (`src/integration-test/java`)

### Legacy Modules

Legacy modules use JUnit 4 with the JUnit Vintage engine for backward compatibility (configured in root `build.gradle`):

```groovy
dependencies {
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.junit.vintage:junit-vintage-engine:5.10.2'
}
```

This allows legacy JUnit 4 tests to run on the JUnit 5 platform without modification.

## Parallel Test Execution

### Gradle-Level Parallelism

Enabled in `gradle.properties`:
```properties
org.gradle.parallel=true
```

This runs independent Gradle tasks across modules concurrently.

### Test-Level Parallelism

Configured in `ftgo.testing-conventions`:
```groovy
tasks.withType(Test).configureEach {
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    forkEvery = 100
}
```

- `maxParallelForks`: Uses half the available CPU cores for test forking
- `forkEvery`: Restarts test JVMs every 100 test classes to prevent memory leaks

### CI-Level Parallelism

- **Unit tests:** Matrix strategy runs services in parallel jobs
- **Integration tests:** Matrix strategy runs services in parallel jobs (each with own MySQL)
- **Shared libraries:** Single job with `--parallel` Gradle flag

## Branch Protection

To enforce test quality, configure the following required status checks in GitHub branch protection settings for `feat/microservices-migration-v3` (and later `main`):

| Check Name | Workflow |
|-----------|----------|
| `Unit Tests Summary` | `ci-test-unit.yml` |
| `Integration Tests Summary` | `ci-test-integration.yml` |

These summary jobs aggregate results from all matrix jobs, providing a single pass/fail gate for branch protection.

## Troubleshooting

### Unit tests fail with "class not found"
- Ensure `./gradlew clean` is run first
- Check that the module's `build.gradle` applies the correct convention plugin

### Integration tests fail to connect to MySQL
- Verify the MySQL service container is healthy in CI logs
- Check `SPRING_DATASOURCE_URL` environment variable
- Ensure Flyway migrations completed successfully

### E2E tests time out waiting for application
- Check Docker Compose logs artifact for application startup errors
- Verify the application health endpoint URL is correct
- Increase the wait timeout if the application takes longer to start

### JaCoCo reports are empty
- Ensure tests actually executed (check JUnit XML results)
- Verify `jacocoTestReport` task runs after `test` task
- Check that the `jacoco` plugin is applied (via `ftgo.testing-conventions`)
