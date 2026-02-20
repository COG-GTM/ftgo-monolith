# FTGO Testing Pipeline

This document describes the automated testing pipeline for the FTGO microservices platform.

> **See also:**
> - [Testing Strategy](TESTING-STRATEGY.md) -- comprehensive testing strategy covering all test levels
> - [Test Data Management](TEST-DATA-MANAGEMENT.md) -- test data creation, lifecycle, and cleanup
> - [Test Templates](templates/) -- reusable test class templates for each layer
> - [ftgo-test-util](../../libs/ftgo-test-util/) -- shared test utility classes

## Pipeline Overview

The test pipeline runs in three sequential stages via GitHub Actions:

```
Unit Tests → Integration Tests → E2E Tests
```

Each stage must pass before the next stage begins. Coverage reports are generated and uploaded as artifacts at each stage.

**Workflow file:** `.github/workflows/test-pipeline.yml`

---

## Stage 1: Unit Tests

Unit tests run in isolation without external dependencies. They validate individual classes, methods, and business logic.

### Running Locally

```bash
# Run unit tests for a specific service
./gradlew :services:order-service:test

# Run unit tests for all services (skipping integration tests)
./gradlew test -x integrationTest
```

### Convention

- Located in `src/test/java/` under the standard package structure
- Do **not** require `@Tag` annotation (they are the default)
- Use Mockito for mocking dependencies
- Use AssertJ for fluent assertions
- Follow the naming pattern: `*Test.java`

### Example

```java
package com.ftgo.order.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        // Given ... When ... Then ...
    }
}
```

---

## Stage 2: Integration Tests

Integration tests verify that components work together with real infrastructure (MySQL, Kafka) via Testcontainers.

### Running Locally

```bash
# Run integration tests for a specific service
./gradlew :services:order-service:integrationTest

# Run integration tests for all services
./gradlew integrationTest
```

**Prerequisite:** Docker must be running on your machine.

### Convention

- Located in `src/test/java/<package>/integration/`
- **Must** be annotated with `@Tag("integration")`
- Extend `AbstractIntegrationTest` (or configure Testcontainers directly)
- Use `@DynamicPropertySource` to inject container connection properties
- Follow the naming pattern: `*IntegrationTest.java`

### Testcontainers Setup

The `ftgo.testing-conventions` Gradle plugin automatically includes:

| Container | Image | Purpose |
|-----------|-------|---------|
| MySQL | `mysql:8.0` | Database persistence |
| Kafka | `confluentinc/cp-kafka:7.5.0` | Event messaging |

### Example

```java
package com.ftgo.order.integration;

import com.ftgo.order.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }
}
```

### Shared Base Class

Each service provides an `AbstractIntegrationTest` in `src/test/java/<package>/support/` that:
- Starts MySQL and Kafka containers via `@Container`
- Injects connection properties via `@DynamicPropertySource`
- Activates the `integration-test` Spring profile

---

## Stage 3: E2E Tests

End-to-end tests validate complete workflows across multiple services using a shared Docker network.

### Running Locally

```bash
./gradlew :services:e2e-tests:test
```

**Prerequisite:** Docker must be running on your machine.

### Convention

- Located in `services/e2e-tests/src/test/java/com/ftgo/e2e/`
- **Must** be annotated with `@Tag("e2e")`
- Extend `E2ETestBase` for shared infrastructure
- Use REST Assured for HTTP assertions
- Follow the naming pattern: `*E2ETest.java`

### Shared Infrastructure

The `E2ETestBase` class provides:
- A shared Docker network for inter-service communication
- MySQL container with all service databases pre-created
- Kafka container for event-driven communication
- REST Assured base configuration

### Example

```java
package com.ftgo.e2e;

import com.ftgo.e2e.support.E2ETestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class OrderFlowE2ETest extends E2ETestBase {

    @Test
    void mysqlContainerIsRunning() {
        assertThat(MYSQL.isRunning()).isTrue();
    }
}
```

---

## Test Coverage

JaCoCo is configured via the `ftgo.testing-conventions` convention plugin for all modules.

### Reports

| Report | Location | Format |
|--------|----------|--------|
| Unit test coverage | `build/reports/jacoco/test/` | HTML + XML |
| Integration test coverage | `build/reports/jacoco/integrationTest/` | HTML + XML |

### Generating Reports Locally

```bash
# Unit test coverage
./gradlew :services:order-service:jacocoTestReport

# Integration test coverage
./gradlew :services:order-service:jacocoIntegrationTestReport
```

### CI Artifacts

Coverage reports are uploaded as GitHub Actions artifacts with 14-day retention:
- `coverage-report-<module>` — unit test coverage
- `integration-coverage-report-<module>` — integration test coverage
- `all-coverage-reports` — aggregated coverage (30-day retention)

---

## Adding New Tests

### Adding a Unit Test

1. Create a test class in `services/<service>/src/test/java/com/ftgo/<service>/`
2. Use `@ExtendWith(MockitoExtension.class)` for mocked tests
3. Name the file `*Test.java`
4. Run: `./gradlew :services:<service>:test`

### Adding an Integration Test

1. Create a test class in `services/<service>/src/test/java/com/ftgo/<service>/integration/`
2. Add `@Tag("integration")` to the class
3. Extend `AbstractIntegrationTest` or configure Testcontainers directly
4. Add `application-integration-test.yml` if not present
5. Name the file `*IntegrationTest.java`
6. Run: `./gradlew :services:<service>:integrationTest`

### Adding an E2E Test

1. Create a test class in `services/e2e-tests/src/test/java/com/ftgo/e2e/`
2. Add `@Tag("e2e")` to the class
3. Extend `E2ETestBase`
4. Name the file `*E2ETest.java`
5. Run: `./gradlew :services:e2e-tests:test`

### Adding a New Service to the Pipeline

1. Add the service to the `unit-tests` matrix in `.github/workflows/test-pipeline.yml`
2. Add the service to the `integration-tests` matrix
3. Create `AbstractIntegrationTest` base class in the service
4. Add `application-integration-test.yml` test profile

---

## Gradle Convention Plugins

The testing infrastructure is managed by convention plugins in `build-logic/`:

| Plugin | Purpose |
|--------|---------|
| `ftgo.testing-conventions` | JUnit 5, Mockito, AssertJ, REST Assured, Testcontainers, JaCoCo |
| `ftgo.java-conventions` | Java 17 toolchain, compiler options |

Apply these in your service's `build.gradle`:

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
}
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Testcontainers can't connect to Docker | Ensure Docker daemon is running: `docker info` |
| Integration tests fail with connection refused | Check `@DynamicPropertySource` maps container ports correctly |
| Coverage report missing | Ensure `jacocoTestReport` task runs after `test` |
| CI times out | Check if Docker image pulls are slow; consider caching |
| `integrationTest` runs unit tests too | Ensure integration tests have `@Tag("integration")` |
