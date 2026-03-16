# Testing Strategy

This document describes the automated testing strategy for the FTGO monolith and microservices migration project, including test categories, execution strategy, and parallelization approach.

---

## Overview

The FTGO project uses a three-tier testing strategy aligned with the [test pyramid](https://martinfowler.com/bliki/TestPyramid.html):

```
         /  E2E Tests  \          <- Few, slow, high confidence
        / Integration    \        <- Moderate, with real dependencies
       /   Unit Tests      \      <- Many, fast, isolated
      /______________________\
```

Each tier has a dedicated CI workflow under `.github/workflows/`:

| Tier | Workflow | File | Trigger Frequency |
|------|----------|------|-------------------|
| Unit | Unit Tests | `test-unit.yml` | Every push and PR |
| Integration | Integration Tests | `test-integration.yml` | Every push and PR |
| E2E | E2E Tests | `test-e2e.yml` | PR only + manual dispatch |

---

## Test Categories

### Unit Tests

**Purpose**: Validate individual components in isolation without external dependencies.

| Property | Value |
|----------|-------|
| Framework | JUnit 4.12 (monolith modules), JUnit 5 (new microservices) |
| Source location | `src/test/java/` |
| Gradle task | `test` |
| External deps | None (all mocked) |
| Execution time | Seconds per module |

**What to test**:
- Domain entity business logic (e.g., `Order.cancel()`, `Money.add()`)
- Service layer methods with mocked repositories
- Input validation and error handling
- Value object equality and serialization
- State machine transitions

**What NOT to test at this level**:
- Database queries (use integration tests)
- REST API endpoint behavior (use integration tests)
- Cross-service workflows (use E2E tests)

### Integration Tests

**Purpose**: Validate component interactions with real external dependencies (database, Spring context).

| Property | Value |
|----------|-------|
| Framework | JUnit 4.12 / JUnit 5 with Spring Boot Test |
| Source location | `src/integration-test/java/` (via `IntegrationTestsPlugin`) |
| Gradle task | `integrationTest` |
| External deps | MySQL 5.7 (service container) |
| Execution time | Minutes per module |

**What to test**:
- JPA repository queries against a real MySQL database
- Spring context loading and bean wiring
- REST controller endpoint behavior with `@SpringBootTest`
- Database migration scripts (Flyway)
- Transaction boundaries and rollback behavior
- JSON serialization/deserialization through the full stack

**Integration Test Source Set**:

The `IntegrationTestsPlugin` in `buildSrc/` creates a separate source set:

```
src/
  main/java/          # Production code
  test/java/          # Unit tests
  integration-test/
    java/             # Integration test classes
    resources/        # Integration test configs (e.g., application-test.properties)
```

The integration test classpath extends the test classpath, so all test dependencies are available.

### End-to-End (E2E) Tests

**Purpose**: Validate complete user workflows across the full application stack.

| Property | Value |
|----------|-------|
| Framework | JUnit 4.12 with REST-Assured |
| Source location | `ftgo-end-to-end-tests/` |
| Gradle task | `:ftgo-end-to-end-tests:test` |
| External deps | Full application stack (all services + MySQL) |
| Execution time | 5-15 minutes |

**What to test**:
- Complete order lifecycle (create -> accept -> prepare -> deliver)
- Cross-service data consistency
- API Gateway routing and authentication
- Error propagation across service boundaries

---

## Test Execution Strategy

### Execution Order in CI

Tests execute in dependency order across separate workflow files:

```
test-unit.yml          (fastest, runs first)
    |
test-integration.yml   (requires MySQL, runs in parallel with unit)
    |
test-e2e.yml           (requires full stack, runs on PR only)
```

Unit and integration tests run independently and in parallel. E2E tests are triggered separately and require a successful build.

### Per-Service Parallelization

Each workflow uses a change-detection strategy to run tests only for affected modules:

1. **`detect-changes` job** — Analyzes the git diff to determine which services/modules changed.
2. **Per-service jobs** — Run in parallel, each testing only the affected service.
3. **Summary job** — Aggregates results and fails if any individual job failed.

```
detect-changes
    |
    +-- test-order-service      (parallel)
    +-- test-consumer-service   (parallel)
    +-- test-restaurant-service (parallel)
    +-- test-courier-service    (parallel)
    +-- test-monolith-modules   (parallel)
    |
unit-test-summary
```

### Shared Dependency Awareness

Changes to shared infrastructure trigger all test jobs:

| Changed Path | Triggers |
|-------------|----------|
| `services/order-service/` | order-service only |
| `shared-libraries/*` | All services + monolith modules |
| `buildSrc/*` | All services + monolith modules |
| `build.gradle` | All services + monolith modules |
| `settings.gradle` | All services + monolith modules |
| `gradle.properties` | All services + monolith modules |

### Concurrency Control

Each workflow uses GitHub Actions concurrency groups to cancel redundant runs:

```yaml
concurrency:
  group: test-unit-${{ github.ref }}
  cancel-in-progress: true
```

This ensures that pushing a new commit while tests are running cancels the in-progress run, saving CI minutes.

---

## Parallelization Details

### Job-Level Parallelism

GitHub Actions runs independent jobs in parallel by default. Our strategy maximizes this:

- **5 parallel unit test jobs** (4 services + monolith modules)
- **5 parallel integration test jobs** (4 services + monolith modules)
- Each job gets its own runner, JDK, and (for integration) MySQL instance

### Gradle-Level Parallelism

Within each job, Gradle can parallelize test execution:

```bash
# Parallel test execution within a module (configured in build.gradle)
test {
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
}
```

> **Note**: Gradle parallel test execution (`maxParallelForks`) is configured in the build plugins, not in the CI workflows. The workflows simply invoke the Gradle tasks.

### Test Forking Strategy

| Test Type | Fork Strategy | Reason |
|-----------|--------------|--------|
| Unit | `maxParallelForks = N/2` | Tests are isolated; safe to parallelize |
| Integration | `maxParallelForks = 1` | Shared database state; must be sequential |
| E2E | `maxParallelForks = 1` | Shared application state; must be sequential |

---

## Test Data Management

### Unit Tests

- Use object mothers and test fixtures (e.g., `OrderDetailsMother`)
- No shared state between tests
- Each test creates its own data

### Integration Tests

- Database is initialized by MySQL service container
- Each test class should use `@Transactional` with rollback for isolation
- Flyway migrations create the schema; tests populate test data

### E2E Tests

- Full application stack with clean database
- Test data created via REST API calls during test setup
- Tests should be idempotent and order-independent

---

## Local Test Execution

Developers can run tests locally using the following commands:

```bash
# Unit tests (all modules except e2e and application)
./gradlew test \
  -x :ftgo-end-to-end-tests-common:test \
  -x :ftgo-end-to-end-tests:test \
  -x :ftgo-application:test

# Integration tests (requires MySQL running locally)
./gradlew integrationTest \
  -x :ftgo-end-to-end-tests-common:integrationTest \
  -x :ftgo-end-to-end-tests:integrationTest

# Single service unit tests
./gradlew :services:order-service:test

# Single service integration tests
./gradlew :services:order-service:integrationTest

# E2E tests (requires full application stack running)
./gradlew :ftgo-end-to-end-tests:test
```

---

## Workflow Relationships

| Workflow | Purpose | Depends On |
|----------|---------|------------|
| `ci-build.yml` | Compile and basic test | None |
| `test-unit.yml` | Comprehensive unit tests | None |
| `test-integration.yml` | Database integration tests | MySQL service |
| `test-e2e.yml` | Full-stack validation | All services built |
| `docker-build.yml` | Container images | None |
| `deploy.yml` | Kubernetes deployment | Docker images |

---

## Adding Tests for a New Service

When adding a new microservice:

1. **Unit tests**: Add test classes under `services/<name>/src/test/java/`
2. **Integration tests**: Apply `IntegrationTestsPlugin` and add classes under `services/<name>/src/integration-test/java/`
3. **CI workflows**: Add a new job in both `test-unit.yml` and `test-integration.yml` following the existing pattern
4. **Update summary jobs**: Add the new service to `detect-changes` outputs and summary job `needs`

---

## References

- [CI Build Pipeline](ci-pipeline.md) — Existing build workflow documentation
- [Quality Gates](quality-gates.md) — Static analysis and quality enforcement
- [Test Reporting](test-reporting.md) — Report formats and artifact management
- [JaCoCo Configuration](jacoco-configuration.md) — Code coverage setup
