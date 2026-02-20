# FTGO Testing Strategy

This document defines the comprehensive testing strategy for the FTGO microservices platform, covering all test levels, tooling, conventions, and quality gates.

## Table of Contents

- [Testing Pyramid](#testing-pyramid)
- [Test Levels](#test-levels)
  - [Unit Tests](#unit-tests)
  - [Integration Tests](#integration-tests)
  - [Contract Tests](#contract-tests)
  - [End-to-End Tests](#end-to-end-tests)
- [Test Tooling](#test-tooling)
- [Naming Conventions](#naming-conventions)
- [Coverage Requirements](#coverage-requirements)
- [CI Pipeline Integration](#ci-pipeline-integration)
- [Test Environment Strategy](#test-environment-strategy)
- [Performance Testing Guidelines](#performance-testing-guidelines)

---

## Testing Pyramid

The FTGO platform follows the standard test pyramid to balance confidence, speed, and maintainability:

```
        /  E2E   \          Few, slow, high confidence
       / Contract \         API boundary verification
      / Integration \       Real infrastructure via Testcontainers
     /   Unit Tests   \     Many, fast, isolated
    ---------------------
```

| Level       | Ratio  | Speed     | Scope                          |
|-------------|--------|-----------|--------------------------------|
| Unit        | ~70%   | < 1s each | Single class or method         |
| Integration | ~20%   | < 30s     | Service + database/messaging   |
| Contract    | ~5%    | < 10s     | API request/response schemas   |
| E2E         | ~5%    | < 2 min   | Multi-service business flows   |

---

## Test Levels

### Unit Tests

Unit tests validate individual classes in isolation by mocking all collaborators.

**Scope:** Business logic in service, repository, and controller layers.

**Principles:**
- Test one behavior per test method
- Follow the Given-When-Then (Arrange-Act-Assert) pattern
- Mock all external dependencies using Mockito
- Use AssertJ for fluent, readable assertions
- Keep tests deterministic with no shared mutable state

**Layer-Specific Guidelines:**

| Layer      | What to test                                   | What to mock                       |
|------------|------------------------------------------------|------------------------------------|
| Service    | Business rules, validation, state transitions  | Repositories, external clients     |
| Repository | Custom queries, projections, specifications    | Nothing (use in-memory DB or mock) |
| Controller | Request mapping, validation, response codes    | Services (via `@WebMvcTest`)       |

**Convention:**
- File location: `src/test/java/<package>/`
- Naming: `*Test.java`
- No `@Tag` annotation required (default test suite)

**Templates:** See [ServiceTestTemplate](templates/ServiceTestTemplate.java), [RepositoryTestTemplate](templates/RepositoryTestTemplate.java), [ControllerTestTemplate](templates/ControllerTestTemplate.java)

---

### Integration Tests

Integration tests verify that components work together with real infrastructure (MySQL, Kafka) provisioned via Testcontainers.

**Scope:** Service layer with real database, repository with real MySQL, messaging with real Kafka.

**Principles:**
- Use Testcontainers to provision infrastructure per test class
- Reuse container instances across tests in the same class via `@Container` (static)
- Use `@DynamicPropertySource` to inject container connection properties
- Activate the `integration-test` Spring profile
- Extend `AbstractIntegrationTest` for shared container setup
- Clean up test data between tests

**Convention:**
- File location: `src/test/java/<package>/integration/`
- Naming: `*IntegrationTest.java`
- Must have `@Tag("integration")`
- Extend `AbstractIntegrationTest` from the service or `libs/ftgo-test-util`

**Containers:**

| Container | Image                        | Purpose              |
|-----------|------------------------------|----------------------|
| MySQL     | `mysql:8.0`                  | Database persistence |
| Kafka     | `confluentinc/cp-kafka:7.5.0`| Event messaging      |

**Templates:** See [IntegrationTestTemplate](templates/IntegrationTestTemplate.java)

---

### Contract Tests

Contract tests verify that API producers and consumers agree on request/response schemas without requiring both services to run simultaneously.

**Scope:** REST API endpoints, event message schemas.

**Approach:** Consumer-Driven Contract Testing using Spring Cloud Contract or Pact.

**Principles:**
- Consumer defines expected interactions (contracts)
- Producer verifies contracts against its implementation
- Contracts are versioned alongside service code
- Contract tests run as part of the CI pipeline
- Fail the build if a contract is broken

**REST API Contracts:**
- Verify request/response body schemas
- Verify HTTP status codes
- Verify required headers
- Verify error response formats

**Event Contracts:**
- Verify event payload schemas
- Verify event metadata (type, source, timestamp)
- Verify serialization/deserialization compatibility

**Convention:**
- File location: `src/test/java/<package>/contract/`
- Naming: `*ContractTest.java`
- Contracts stored in `src/test/resources/contracts/`

**Templates:** See [ConsumerContractTestTemplate](templates/ConsumerContractTestTemplate.java), [ProducerContractTestTemplate](templates/ProducerContractTestTemplate.java)

---

### End-to-End Tests

E2E tests validate complete business workflows across multiple services.

**Scope:** Multi-service flows such as order creation, payment processing, delivery assignment.

**Principles:**
- Use a shared Docker network for inter-service communication
- Pre-create all service databases in a single MySQL container
- Use REST Assured for HTTP assertions
- Focus on critical business paths only
- Avoid testing edge cases already covered by lower-level tests

**Convention:**
- File location: `services/e2e-tests/src/test/java/com/ftgo/e2e/`
- Naming: `*E2ETest.java`
- Must have `@Tag("e2e")`
- Extend `E2ETestBase`

---

## Test Tooling

| Tool             | Version  | Purpose                                |
|------------------|----------|----------------------------------------|
| JUnit 5          | 5.10.2   | Test framework                         |
| Mockito          | 5.11.0   | Mocking framework                      |
| AssertJ          | 3.25.3   | Fluent assertions                      |
| REST Assured     | 5.4.0    | HTTP API testing                       |
| Testcontainers   | 1.19.7   | Docker-based infrastructure in tests   |
| JaCoCo           | 0.8.11   | Code coverage measurement              |
| Spring Boot Test | 3.2.5    | Spring context testing                 |
| Spring Cloud Contract / Pact | - | Contract testing              |

All versions are managed centrally via `gradle/libs.versions.toml` and the `ftgo.testing-conventions` Gradle convention plugin in `build-logic/`.

---

## Naming Conventions

| Element          | Convention                        | Example                             |
|------------------|-----------------------------------|-------------------------------------|
| Unit test class  | `<Class>Test`                     | `OrderServiceTest`                  |
| Integration test | `<Class>IntegrationTest`          | `OrderRepositoryIntegrationTest`    |
| Contract test    | `<Api>ContractTest`               | `OrderApiContractTest`              |
| E2E test         | `<Flow>E2ETest`                   | `OrderFlowE2ETest`                  |
| Test method      | `should<Action>When<Condition>`   | `shouldCreateOrderWhenValid`        |
| Test fixture     | `<Entity>TestFixtures`            | `OrderTestFixtures`                 |
| Test builder     | `<Entity>TestBuilder`             | `OrderTestBuilder`                  |

---

## Coverage Requirements

| Metric            | Minimum | Target |
|-------------------|---------|--------|
| Line coverage     | 70%     | 85%    |
| Branch coverage   | 60%     | 75%    |
| Method coverage   | 70%     | 85%    |

Coverage is enforced per module via JaCoCo in the `ftgo.testing-conventions` plugin. Reports are generated for both unit and integration test suites.

**Exclusions from coverage:**
- Configuration classes (`*Configuration.java`, `*Config.java`)
- Application entry points (`*Application.java`)
- DTOs and value objects with only getters/setters
- Generated code (MapStruct mappers, Lombok-generated code)

---

## CI Pipeline Integration

The testing pipeline runs in three sequential stages via GitHub Actions (`.github/workflows/test-pipeline.yml`):

```
Unit Tests  ──►  Integration Tests  ──►  E2E Tests
   (matrix)          (matrix)            (single)
```

Each stage must pass before the next begins. Coverage reports are uploaded as GitHub Actions artifacts.

**Stage Configuration:**

| Stage       | Gradle Task       | Tag Filter       | Docker Required |
|-------------|-------------------|------------------|-----------------|
| Unit        | `test`            | (none)           | No              |
| Integration | `integrationTest` | `integration`    | Yes             |
| E2E         | `test`            | `e2e`            | Yes             |

**Quality Gates:**
- All tests must pass
- Coverage thresholds must be met
- No test flakiness (retry once, fail on second failure)

---

## Test Environment Strategy

| Environment | Infrastructure     | Data                  | Purpose                    |
|-------------|--------------------|-----------------------|----------------------------|
| Local       | Testcontainers     | Factories / builders  | Developer workflow         |
| CI          | Testcontainers     | Factories / builders  | Automated verification     |
| Staging     | Managed services   | Seed scripts          | Pre-production validation  |
| Production  | Managed services   | Real data             | Smoke tests only           |

---

## Performance Testing Guidelines

Performance tests are not part of the standard CI pipeline but should be run before major releases.

**Tools:** JMH for microbenchmarks, Gatling or k6 for load testing.

**Targets:**

| Metric             | Threshold        |
|--------------------|------------------|
| API response time  | p95 < 200ms      |
| Throughput         | > 100 req/s      |
| Error rate         | < 0.1%           |
| Database query     | < 50ms average   |

---

## Related Documents

- [Testing Pipeline README](README.md) -- CI pipeline details and running tests locally
- [Test Data Management Strategy](TEST-DATA-MANAGEMENT.md)
- [Test Templates](templates/) -- Reusable test class templates for each layer
- [ftgo-test-util Library](../../libs/ftgo-test-util/) -- Shared test utility classes
