# Test Data Management Strategy

This document defines how test data is created, managed, and cleaned up across all test levels in the FTGO platform.

## Table of Contents

- [Principles](#principles)
- [Test Data Creation Patterns](#test-data-creation-patterns)
  - [Test Data Factory](#test-data-factory)
  - [Builder Pattern](#builder-pattern)
  - [Random Data Generation](#random-data-generation)
- [Data Lifecycle by Test Level](#data-lifecycle-by-test-level)
- [Database Cleanup Strategies](#database-cleanup-strategies)
- [Test Fixtures](#test-fixtures)
- [Sensitive Data Handling](#sensitive-data-handling)
- [Shared Test Data Library](#shared-test-data-library)

---

## Principles

1. **Tests own their data.** Each test creates the data it needs and does not depend on data from other tests.
2. **No shared mutable state.** Tests must not rely on insertion order or data created by other test classes.
3. **Deterministic by default.** Use fixed values for assertions; use random data only for uniqueness constraints.
4. **Clean up after yourself.** Integration tests must leave the database in a clean state for the next test.
5. **Use factories, not raw constructors.** Centralize test object creation in factory classes to reduce duplication and improve maintainability.

---

## Test Data Creation Patterns

### Test Data Factory

The `TestDataFactory` class in `libs/ftgo-test-util` provides static factory methods for creating domain objects with sensible defaults.

```java
import com.ftgo.testutil.TestDataFactory;

// Create with defaults
Money price = TestDataFactory.money();
Address address = TestDataFactory.address();

// Create with overrides
Money customPrice = TestDataFactory.money(new BigDecimal("25.99"));
```

**Guidelines:**
- Factory methods return objects with valid, realistic defaults
- Use overloaded methods or builder wrappers for customization
- Keep factory methods close to the domain they represent
- Add new factory methods as new domain objects are introduced

### Builder Pattern

For complex domain objects, use the builder pattern with a test-specific builder that provides defaults:

```java
Order order = OrderTestBuilder.anOrder()
    .withConsumerId(123L)
    .withRestaurantId(456L)
    .withLineItems(List.of(
        TestDataFactory.orderLineItem("Pizza", 2, "12.99")
    ))
    .build();
```

**Guidelines:**
- Name builders as `<Entity>TestBuilder`
- Provide an `a<Entity>()` or `an<Entity>()` static entry point
- Default all fields to valid values
- Use method chaining for fluent configuration

### Random Data Generation

The `RandomDataGenerator` class in `libs/ftgo-test-util` generates unique values for fields that require uniqueness (IDs, emails, etc.).

```java
import com.ftgo.testutil.RandomDataGenerator;

String uniqueEmail = RandomDataGenerator.email();
Long uniqueId = RandomDataGenerator.id();
String uniqueName = RandomDataGenerator.name("Restaurant");
```

**Guidelines:**
- Use random data only for uniqueness constraints, not for assertion values
- Always use `RandomDataGenerator` instead of `java.util.Random` directly
- Random data should still be realistic (valid email format, reasonable price ranges)

---

## Data Lifecycle by Test Level

### Unit Tests

| Aspect     | Strategy                                              |
|------------|-------------------------------------------------------|
| Creation   | In-memory objects via factories and builders           |
| Storage    | None (mocked repositories)                            |
| Cleanup    | Garbage collected automatically                       |
| Isolation  | Guaranteed by mocking; no shared state                |

### Integration Tests

| Aspect     | Strategy                                              |
|------------|-------------------------------------------------------|
| Creation   | Factories + JPA `save()` in `@BeforeEach`             |
| Storage    | Testcontainers MySQL (ephemeral per test class)       |
| Cleanup    | `DatabaseCleanupExtension` truncates tables           |
| Isolation  | Each test starts with a clean database                |

### Contract Tests

| Aspect     | Strategy                                              |
|------------|-------------------------------------------------------|
| Creation   | Inline JSON or factory-built DTOs                     |
| Storage    | None (tests verify schemas, not persistence)          |
| Cleanup    | Not applicable                                        |
| Isolation  | Each contract is independent                          |

### E2E Tests

| Aspect     | Strategy                                              |
|------------|-------------------------------------------------------|
| Creation   | REST API calls to create prerequisite data            |
| Storage    | Shared Testcontainers MySQL across all E2E tests      |
| Cleanup    | Full database reset between test classes              |
| Isolation  | Ordered test execution within a class                 |

---

## Database Cleanup Strategies

### Strategy 1: Truncate Tables (Recommended for Integration Tests)

Use the `DatabaseCleanupExtension` from `libs/ftgo-test-util`:

```java
@ExtendWith(DatabaseCleanupExtension.class)
@SpringBootTest
@Tag("integration")
class OrderRepositoryIntegrationTest {
    // Tables are truncated before each test
}
```

The extension:
1. Discovers all tables in the schema
2. Disables foreign key checks
3. Truncates all tables
4. Re-enables foreign key checks

### Strategy 2: Transaction Rollback (Alternative for Simple Tests)

Use `@Transactional` on the test class to automatically roll back after each test:

```java
@SpringBootTest
@Transactional
@Tag("integration")
class OrderServiceIntegrationTest {
    // Each test runs in a transaction that is rolled back
}
```

**Limitation:** Does not work when tests trigger asynchronous operations or when the code under test manages its own transactions.

### Strategy 3: Recreate Schema (For E2E Tests)

For E2E tests, use Hibernate's `ddl-auto=create-drop` or Flyway migrations to recreate the schema:

```yaml
# application-integration-test.yml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## Test Fixtures

Test fixtures are pre-defined sets of related test data used across multiple tests.

### Organization

```
libs/ftgo-test-util/
  src/main/java/com/ftgo/testutil/
    TestDataFactory.java          # Simple object creation
    RandomDataGenerator.java      # Unique value generation
    DatabaseCleanupExtension.java # DB cleanup JUnit extension
    JsonHelper.java               # JSON serialization helpers
```

### Fixture Naming

| Pattern                  | Purpose                              | Example                    |
|--------------------------|--------------------------------------|----------------------------|
| `TestDataFactory`        | Static factory for domain objects    | `TestDataFactory.money()`  |
| `<Entity>TestBuilder`    | Fluent builder for complex objects   | `OrderTestBuilder`         |
| `<Entity>TestFixtures`   | Pre-built object sets                | `RestaurantTestFixtures`   |
| `RandomDataGenerator`    | Unique random values                 | `RandomDataGenerator.id()` |

---

## Sensitive Data Handling

- Never use real customer data in tests
- Never commit credentials, tokens, or secrets in test fixtures
- Use generated placeholder values for PII fields
- Store test-specific secrets in environment variables or Spring profiles
- The `integration-test` profile should use Testcontainers connection properties, never real infrastructure credentials

---

## Shared Test Data Library

All shared test utilities live in `libs/ftgo-test-util/`. Services depend on it via:

```groovy
dependencies {
    testImplementation project(':libs:ftgo-test-util')
}
```

This library provides:

| Class                       | Purpose                                     |
|-----------------------------|---------------------------------------------|
| `TestDataFactory`           | Domain object creation with defaults        |
| `RandomDataGenerator`       | Unique value generation for IDs, emails     |
| `DatabaseCleanupExtension`  | JUnit 5 extension for table truncation      |
| `JsonHelper`                | JSON serialization/deserialization helpers   |
| `TestContainersConfig`      | Shared Testcontainers configuration         |
| `MockSecurityContext`       | Security context setup for authenticated tests |

---

## Related Documents

- [Testing Strategy](TESTING-STRATEGY.md)
- [Testing Pipeline README](README.md)
- [ftgo-test-util Library](../../libs/ftgo-test-util/)
