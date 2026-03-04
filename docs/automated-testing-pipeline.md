# Automated Testing Pipeline

This document describes the CI/CD testing pipeline for the FTGO microservices project,
covering unit tests, integration tests, end-to-end (E2E) tests, code coverage, and
parallel execution configuration.

## Overview

The testing pipeline is organized into three tiers, each running as a separate GitHub
Actions workflow:

| Tier | Workflow | Trigger | Dependencies | Timeout |
|------|----------|---------|--------------|---------|
| Unit Tests | `ci-unit-tests.yml` | All pushes/PRs | None (fast) | 10 min/module |
| Integration Tests | `ci-integration-tests.yml` | Path-filtered | MySQL 8.0 service container | 30 min |
| E2E Tests | `ci-e2e-tests.yml` | Path-filtered | Full Docker Compose stack | 45 min |

## Unit Tests

### CI Workflow: `ci-unit-tests.yml`

Provides fast feedback with no external dependencies. Uses a **matrix strategy** to
run tests across all modules in parallel.

**How it works:**
1. **Discover phase** - Scans the repository for modules containing `src/test/java`
2. **Matrix execution** - Each discovered module runs its unit tests as an independent job
3. **Summary phase** - Aggregates results and generates a workflow summary

**Execution command:**
```bash
# Run all unit tests locally (excluding E2E modules)
./gradlew test -x :ftgo-end-to-end-tests:test -x :ftgo-end-to-end-tests-common:test

# Run unit tests for a specific module
./gradlew :shared-ftgo-common:test
./gradlew :services-ftgo-consumer-service:test
```

**Performance target:** < 2 minutes per service module.

### Parallel Execution

Test tasks are configured for parallel execution via:
- `maxParallelForks` - Set to half the available CPU cores (minimum 1)
- `forkEvery = 100` - Forks a new JVM every 100 tests to prevent memory leaks
- Gradle `--parallel` flag - Enables parallel project execution

These settings are applied in both:
- `build.gradle` (root) - for legacy monolith modules
- `FtgoTestingConventionsPlugin` - for new microservice modules

## Integration Tests

### CI Workflow: `ci-integration-tests.yml`

Runs integration tests that require external infrastructure (database, message broker, etc.).

**MySQL Service Container:**
The workflow provisions a MySQL 8.0 service container with:
- Database: `ftgo`
- User: `mysqluser` / `mysqlpw`
- Root password: `rootpassword`
- Health check with 5 retries

**Execution command:**
```bash
# Run integration tests locally (requires MySQL running)
./gradlew integrationTest

# Run integration tests for a specific module
./gradlew :shared-ftgo-domain:integrationTest
```

### Integration Test Source Set

Integration tests live in `src/integration-test/java` within each module. The
`IntegrationTestsPlugin` (legacy) and `FtgoTestingConventionsPlugin` (new modules)
both register this source set with:
- Classpath extending `testImplementation` and `testRuntimeOnly`
- Separate `integrationTest` Gradle task

## End-to-End Tests

### CI Workflow: `ci-e2e-tests.yml`

Runs the full E2E test suite against the complete Docker Compose stack.

**Stack components:**
- MySQL database
- FTGO monolith application (via `ftgo-application`)

**Test modules:**
- `ftgo-end-to-end-tests` - E2E test cases
- `ftgo-end-to-end-tests-common` - Shared E2E utilities

> **Note:** `ftgo-end-to-end-tests-common` has a known build issue due to a missing
> `eventuate-util-test` dependency. This is pre-existing and not a regression.

**Execution command:**
```bash
# Build and start the stack
docker-compose up -d --build

# Run E2E tests
./gradlew :ftgo-end-to-end-tests:test

# Tear down
docker-compose down -v
```

## Code Coverage (JaCoCo)

### Configuration

JaCoCo is configured at two levels:

1. **Root `build.gradle`** - Applies `jacoco` plugin to all subprojects
2. **`FtgoTestingConventionsPlugin`** - Configures JaCoCo for new microservice modules

**JaCoCo version:** 0.8.11

### Coverage Reports

| Task | Description | Report Location |
|------|-------------|-----------------|
| `jacocoTestReport` | Unit test coverage | `build/reports/jacoco/test/` |
| `jacocoIntegrationTestReport` | Integration test coverage | `build/reports/jacoco/jacocoIntegrationTestReport/` |

Reports are generated in both XML and HTML formats. XML reports are suitable for
CI tool integration (e.g., Codecov, SonarQube). HTML reports provide browsable
coverage details.

**Coverage target:** 70%+ line coverage.

### Running Coverage Locally

```bash
# Generate unit test coverage report
./gradlew test jacocoTestReport

# Generate integration test coverage report
./gradlew integrationTest jacocoIntegrationTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html
```

## Test Result Reporting

### Artifacts

All workflows upload test results and coverage reports as GitHub Actions artifacts:

| Artifact | Retention | Contents |
|----------|-----------|----------|
| `unit-test-results-*` | 14 days | JUnit XML + HTML reports |
| `coverage-report-*` | 14 days | JaCoCo XML + HTML reports |
| `integration-test-results` | 14 days | Integration test reports |
| `integration-test-coverage` | 14 days | Integration JaCoCo reports |
| `e2e-test-results` | 14 days | E2E test reports |
| `e2e-docker-logs` | 7 days | Docker Compose logs |

### Test Result Publishing

Test results are published as GitHub check annotations using the
[publish-unit-test-result-action](https://github.com/EnricoMi/publish-unit-test-result-action).
This provides:
- Test counts (passed/failed/skipped) in PR checks
- Individual test failure annotations on the PR
- Trend tracking across workflow runs

### Workflow Summaries

Each workflow generates a summary in the GitHub Actions step summary, including:
- Test result status
- Configuration details
- Test file counts

## Branch Protection

To enforce test passing before merge, configure branch protection rules:

1. Go to **Settings > Branches > Branch protection rules**
2. Add rule for `feat/microservices-migration-*` and `main`
3. Enable **Require status checks to pass before merging**
4. Add required checks:
   - `Unit Tests Summary`
   - `Integration Tests`
   - `End-to-End Tests`

## Concurrency

All test workflows use concurrency groups to cancel in-progress runs when new
commits are pushed:

```yaml
concurrency:
  group: <test-type>-${{ github.ref }}
  cancel-in-progress: true
```

This prevents resource waste from stale test runs.

## Troubleshooting

### Unit tests timing out
- Check `maxParallelForks` setting - reduce if OOM errors occur
- Review `forkEvery` - lower values use more memory but isolate better
- Ensure tests don't have unintended external dependencies

### Integration tests failing to connect to MySQL
- Verify MySQL service container health check passed
- Check `SPRING_DATASOURCE_URL` environment variable
- Ensure the `ftgo` database was created

### E2E tests failing
- Download Docker Compose logs artifact for service-level debugging
- Check application health endpoint: `http://localhost:8081/actuator/health`
- Verify all services started successfully in Docker Compose
