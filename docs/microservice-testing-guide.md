# Microservice Testing Guide

Comprehensive testing strategy and guidelines for the FTGO monolith-to-microservices migration. This document covers test categories, naming conventions, mocking strategy, test data management, and performance testing.

For CI pipeline details and parallelization, see [Testing Strategy](testing-strategy.md).

---

## Table of Contents

1. [Testing Strategy Overview](#testing-strategy-overview)
2. [Test Categories and When to Use Each](#test-categories-and-when-to-use-each)
3. [Test Naming Conventions](#test-naming-conventions)
4. [Mocking Strategy](#mocking-strategy)
5. [Test Data Management](#test-data-management)
6. [Contract Testing](#contract-testing)
7. [Performance and Load Testing](#performance-and-load-testing)
8. [Test Organization and Structure](#test-organization-and-structure)
9. [JUnit 5 Migration Path](#junit-5-migration-path)
10. [References](#references)

---

## Testing Strategy Overview

The FTGO project follows the **test pyramid** model to balance speed, cost, and confidence:

```
            /  E2E Tests  \              Few, slow, highest confidence
           / Contract Tests \            API compatibility verification
          / Integration Tests \          Real dependencies (DB, Spring)
         /    Unit Tests        \        Many, fast, fully isolated
        /__________________________\
```

### Guiding Principles

| Principle | Description |
|-----------|-------------|
| **Test at the lowest level possible** | Prefer unit tests over integration tests. Only move up the pyramid when lower-level tests cannot verify the behavior. |
| **Fast feedback** | Unit tests run in seconds; integration tests in minutes. Developers should run unit tests before every commit. |
| **Isolation** | Each test must be independent. No shared mutable state. No ordering dependencies between tests. |
| **Determinism** | Tests must produce the same result every time. Avoid time-dependent logic, random data, or external service calls in unit tests. |
| **Readability** | Tests are documentation. A reader should understand the expected behavior from the test name and structure alone. |

### Test Tiers Summary

| Tier | Framework | Speed | Dependencies | Location | Gradle Task |
|------|-----------|-------|-------------|----------|-------------|
| Unit | JUnit 4 + Mockito | ms | None (mocked) | `src/test/java/` | `test` |
| Integration | JUnit 4 + Spring Boot Test | seconds | MySQL, Spring Context | `src/integration-test/java/` | `integrationTest` |
| Contract | Spring Cloud Contract / Pact | seconds | Stub server | `src/test/java/.../contract/` | `contractTest` |
| E2E | JUnit 4 + Rest-Assured | minutes | Full application stack | `ftgo-end-to-end-tests/` | `:ftgo-end-to-end-tests:test` |

---

## Test Categories and When to Use Each

### Unit Tests

**Purpose**: Verify a single class or method in isolation.

**When to write unit tests**:
- Domain entity business logic (`Order.cancel()`, `Money.add()`)
- Service-layer methods with mocked repositories
- Value object behavior (equality, serialization, formatting)
- Input validation and error handling
- State machine transitions (`OrderState` changes)
- Utility/helper methods

**When NOT to write unit tests**:
- Simple getters/setters with no logic
- Spring configuration classes
- Data-only DTOs
- Anything that requires a database or network

**Existing examples in FTGO**:
- `MoneyTest` — Value object arithmetic and comparison
- `MoneySerializationTest` — Jackson serialization with custom module
- `OrderControllerTest` — Controller with mocked services (standalone MockMvc)

**Template**: [`test-templates/UnitTestTemplate.java`](../test-templates/UnitTestTemplate.java)

### Integration Tests

**Purpose**: Verify that components work together with real dependencies.

**When to write integration tests**:
- JPA repository custom queries against a real MySQL database
- Spring context loading and bean wiring validation
- Flyway migration script verification
- Transaction boundary behavior (commit/rollback)
- JSON serialization through the full HTTP stack
- Security filter chain behavior

**When NOT to write integration tests**:
- Pure business logic (use unit tests)
- Cross-service workflows (use E2E tests)
- API contract compatibility (use contract tests)

**Existing examples in FTGO**:
- `FtgoApplicationTest` — Full Spring Boot context test extending `AbstractEndToEndTests`
- `IntegrationTestsPlugin` — Gradle plugin creating the `integration-test` source set

**Template**: [`test-templates/IntegrationTestTemplate.java`](../test-templates/IntegrationTestTemplate.java)

### Contract Tests

**Purpose**: Verify API compatibility between service consumer and provider during migration.

**When to write contract tests**:
- Extracting a service from the monolith (verify the new API matches existing behavior)
- Any service-to-service REST API call
- When multiple teams own different services
- Before changing any public API response format

**When NOT to write contract tests**:
- Internal service logic
- Database schema changes
- UI interactions

**Template**: [`test-templates/ContractTestTemplate.java`](../test-templates/ContractTestTemplate.java)

### End-to-End (E2E) Tests

**Purpose**: Verify complete user workflows across the full application stack.

**When to write E2E tests**:
- Critical user journeys (create order -> accept -> prepare -> deliver)
- Cross-service data consistency
- API gateway routing
- Authentication/authorization flows

**When NOT to write E2E tests**:
- Individual component behavior (use unit/integration tests)
- Edge cases and error paths (test these at lower levels)
- Performance benchmarks (use dedicated load tests)

**Existing examples in FTGO**:
- `AbstractEndToEndTests` — Complete order lifecycle test

---

## Test Naming Conventions

### Class Naming

| Test Type | Pattern | Example |
|-----------|---------|---------|
| Unit test | `{ClassUnderTest}Test` | `OrderServiceTest` |
| Integration test | `{ClassUnderTest}IntegrationTest` | `OrderRepositoryIntegrationTest` |
| REST API test | `{Controller}Test` or `{Controller}ApiTest` | `OrderControllerTest` |
| Contract test | `{Service}ContractTest` | `OrderServiceContractTest` |
| E2E test | `{Feature}EndToEndTest` | `OrderLifecycleEndToEndTest` |

### Method Naming

Use the `should{ExpectedBehavior}` prefix. This is the established convention throughout the FTGO codebase.

```java
// Good - describes expected behavior
@Test
public void shouldCreateOrderSuccessfully() { ... }

@Test
public void shouldReturnEmptyWhenOrderNotFound() { ... }

@Test
public void shouldThrowWhenConsumerIdIsInvalid() { ... }

@Test
public void shouldTransitionFromApprovedToAccepted() { ... }
```

For more complex scenarios, use `should{Behavior}When{Condition}`:

```java
@Test
public void shouldRejectOrderWhenRestaurantIsClosed() { ... }

@Test
public void shouldReturnTotalWithTaxWhenDeliveryAddressIsInState() { ... }
```

**Anti-patterns to avoid**:

```java
// Bad - test names
@Test public void test1() { ... }                    // Non-descriptive
@Test public void testCreateOrder() { ... }          // "test" prefix is redundant
@Test public void createOrderWorks() { ... }         // Not a "should" statement
@Test public void shouldWork() { ... }               // Too vague
```

### Package Naming

Mirror the production code package structure:

```
Production:  net.chrisrichardson.ftgo.orderservice.domain.OrderService
Unit test:   net.chrisrichardson.ftgo.orderservice.domain.OrderServiceTest
Web test:    net.chrisrichardson.ftgo.orderservice.web.OrderControllerTest
```

---

## Mocking Strategy

### When to Mock

| Scenario | Mock? | Reason |
|----------|-------|--------|
| Repository in service unit test | Yes | Isolate business logic from DB |
| Service in controller unit test | Yes | Test HTTP layer independently |
| External HTTP client | Yes | Avoid network calls in tests |
| Value objects (Money, Address) | No | They are simple and deterministic |
| Domain entities | No | Test with real objects; they have no external deps |
| Spring context in unit test | No | Don't load Spring for unit tests |
| Database in integration test | No | Use real DB for integration tests |

### Mockito Patterns (JUnit 4)

#### Inline mocks (preferred in FTGO)

The FTGO codebase uses `Mockito.mock()` in `@Before` methods rather than annotations:

```java
// Preferred pattern (matches OrderControllerTest)
private OrderService orderService;
private OrderRepository orderRepository;

@Before
public void setUp() {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
}
```

#### Annotation-based mocks (alternative)

```java
// Alternative using @MockitoRule (JUnit 4) instead of @RunWith(MockitoJUnitRunner.class)
// Use this when already using @RunWith(SpringRunner.class)
@Rule
public MockitoRule mockitoRule = MockitoJUnit.rule();

@Mock
private OrderRepository orderRepository;

@InjectMocks
private OrderService orderService;
```

#### Common Mockito operations

```java
// Stubbing return values
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(repository.findById(anyLong())).thenReturn(Optional.empty());
when(service.create(any(CreateRequest.class))).thenReturn(newEntity);

// Stubbing void methods
doNothing().when(repository).delete(any());
doThrow(new RuntimeException("DB error")).when(repository).save(any());

// Verification
verify(repository).save(any(Order.class));
verify(repository, times(1)).findById(1L);
verify(repository, never()).delete(any());

// Argument capture
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(repository).save(captor.capture());
assertEquals("expected", captor.getValue().getName());
```

### Spring Boot Test Mocking

For integration tests that need to replace specific beans:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceIntegrationTest {

    // Replaces the real bean in the Spring context
    @MockBean
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;  // Uses real bean with mocked payment

    @Test
    public void shouldProcessOrderWithMockedPayment() {
        when(paymentService.charge(any())).thenReturn(PaymentResult.success());
        // test with real OrderService but mocked PaymentService
    }
}
```

---

## Test Data Management

### Object Mother Pattern

The FTGO project uses the **Object Mother** pattern for creating test data. This centralizes test fixture creation, making tests more readable and reducing duplication.

**Existing example**: `OrderDetailsMother`

```java
public class OrderDetailsMother {
    public static long CONSUMER_ID = 1511300065921L;
    public static final int CHICKEN_VINDALOO_QUANTITY = 5;
    public static long ORDER_ID = 99L;
    public static Order CHICKEN_VINDALOO_ORDER = makeAjantaOrder();
    public static final OrderState CHICKEN_VINDALOO_ORDER_STATE = OrderState.APPROVED;

    private static Order makeAjantaOrder() {
        Order order = new Order(CONSUMER_ID, restaurant, chickenVindalooLineItems());
        order.setId(ORDER_ID);
        return order;
    }
}
```

### Guidelines for Object Mother Classes

| Guideline | Description |
|-----------|-------------|
| One per aggregate | Create one Mother per domain aggregate (e.g., `OrderMother`, `ConsumerMother`, `RestaurantMother`) |
| Static factory methods | Use `static` methods that return fully constructed objects |
| Descriptive names | Name constants after the scenario: `APPROVED_ORDER`, `CANCELLED_ORDER` |
| Composable | Mothers can reference other Mothers (e.g., `OrderMother` uses `RestaurantMother`) |
| No randomness | Use fixed, deterministic values for reproducibility |

### Naming Convention for Object Mothers

```
{AggregateName}Mother        -> OrderMother, ConsumerMother
{AggregateName}DetailsMother -> OrderDetailsMother (includes related DTOs)
```

### Test Data for Integration Tests

Integration tests with real databases should manage data carefully:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional  // Rolls back after each test
public class OrderRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void shouldFindOrdersByConsumer() {
        // Arrange - use EntityManager to insert test data
        Consumer consumer = entityManager.persist(new Consumer("John", "Doe"));
        entityManager.persist(new Order(consumer.getId(), ...));
        entityManager.flush();

        // Act & Assert
        List<Order> orders = orderRepository.findByConsumerId(consumer.getId());
        assertEquals(1, orders.size());
        // Transaction rolls back automatically - no cleanup needed
    }
}
```

### Test Data for E2E Tests

E2E tests create data through REST APIs (pattern from `AbstractEndToEndTests`):

```java
// Create test data via API calls (not direct DB insertion)
int consumerId = createConsumer();          // POST /consumers
int restaurantId = createRestaurant();       // POST /restaurants
int orderId = createOrder(consumerId, restaurantId); // POST /orders
```

### Shared Test Utilities

The `ftgo-test-util` module provides shared test helpers:

```java
// FtgoTestUtil - shared assertion helpers
FtgoTestUtil.assertPresent(optionalValue);  // Assert Optional is present
```

When adding new shared test utilities, add them to `ftgo-test-util` rather than duplicating across modules.

---

## Contract Testing

### When to Introduce Contract Tests

Contract tests become critical during the microservices extraction phase:

```
Phase 1: Monolith (no contract tests needed)
Phase 2: First service extracted -> Add contracts for extracted APIs
Phase 3: Multiple services -> Contracts between all service pairs
Phase 4: Full microservices -> Contract tests as part of every service CI
```

### Recommended Approach

For the FTGO project (all Java, all Spring Boot), **Spring Cloud Contract** is recommended. See [`test-templates/ContractTestTemplate.java`](../test-templates/ContractTestTemplate.java) for implementation details.

### Contract Test Workflow

```
Provider changes API
       |
       v
Update contract definition (Groovy DSL)
       |
       v
Run provider contract verification (auto-generated tests)
       |
       v
Publish stubs to artifact repository
       |
       v
Consumer runs tests against new stubs
       |
       v
Both pass? -> Merge. Either fails? -> Fix before merge.
```

---

## Performance and Load Testing

### When to Performance Test

| Phase | What to Test | Tool |
|-------|-------------|------|
| Pre-migration | Baseline monolith performance | JMeter or Gatling |
| During extraction | Compare service performance to monolith baseline | Gatling |
| Post-migration | Full microservices system under load | Gatling + distributed |

### Recommended Tools

#### Gatling (Recommended)

Gatling provides Scala-based DSL for expressive load tests and produces detailed HTML reports.

```scala
// Example Gatling simulation for FTGO Order API
class OrderSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8081")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val createOrderScenario = scenario("Create Order")
    .exec(
      http("Create Consumer")
        .post("/consumers")
        .body(StringBody("""{"name":{"firstName":"John","lastName":"Doe"}}"""))
        .check(jsonPath("$.consumerId").saveAs("consumerId"))
    )
    .pause(1)
    .exec(
      http("Create Order")
        .post("/orders")
        .body(StringBody(session =>
          s"""{"consumerId":${session("consumerId").as[Int]},
              "restaurantId":1,
              "lineItems":[{"menuItemId":"1","quantity":2}]}"""))
        .check(status.is(200))
    )

  setUp(
    createOrderScenario.inject(
      rampUsersPerSec(1) to 50 during (60 seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(3000),
     global.successfulRequests.percent.gt(99.0)
   )
}
```

**Integration with Gradle**:

```groovy
// build.gradle for a performance test module
plugins {
    id 'io.gatling.gradle' version '3.3.1'
}

dependencies {
    gatling 'io.gatling.highcharts:gatling-charts-highcharts:3.3.1'
}
```

#### JMeter (Alternative)

JMeter provides a GUI-based test builder and is suitable for teams less familiar with code-based load testing.

```bash
# Run JMeter test plan from command line
jmeter -n -t order-load-test.jmx -l results.jtl -e -o reports/
```

### Performance Test Thresholds

| Metric | Target | Critical |
|--------|--------|----------|
| P50 response time | < 200ms | < 500ms |
| P95 response time | < 500ms | < 2000ms |
| P99 response time | < 1000ms | < 3000ms |
| Error rate | < 0.1% | < 1% |
| Throughput (orders/sec) | > 100 | > 50 |

### Performance Testing in CI

Performance tests should NOT run on every PR. Instead:

1. **Nightly builds**: Run full load test suite against a staging environment
2. **Release gates**: Run before every production release
3. **On-demand**: Trigger manually for performance-sensitive changes

```yaml
# Example CI trigger (reference only - do not add to .github/workflows/)
on:
  schedule:
    - cron: '0 2 * * *'  # Nightly at 2 AM UTC
  workflow_dispatch:       # Manual trigger
```

---

## Test Organization and Structure

### Directory Layout per Service

```
services/<service-name>/
  src/
    main/java/                          # Production code
    test/java/                          # Unit tests + REST API tests
      net/chrisrichardson/ftgo/<service>/
        domain/
          {Entity}Test.java             # Domain logic unit tests
          {Service}Test.java            # Service-layer unit tests
          {Entity}Mother.java           # Test data factories
        web/
          {Controller}Test.java         # REST API tests (MockMvc)
        contract/
          {Service}ContractBase.java    # Contract test base class
    integration-test/java/              # Integration tests (IntegrationTestsPlugin)
      net/chrisrichardson/ftgo/<service>/
        {Repository}IntegrationTest.java
        {Service}IntegrationTest.java
    integration-test/resources/
      application-test.properties       # Test-specific DB config
```

### Test Class Structure (AAA Pattern)

All tests should follow the **Arrange-Act-Assert** pattern:

```java
@Test
public void shouldCalculateOrderTotal() {
    // Arrange - set up preconditions
    Order order = OrderMother.createWithLineItems(
        new LineItem("item-1", new Money("10.00"), 2),
        new LineItem("item-2", new Money("5.00"), 1)
    );

    // Act - invoke the behavior under test
    Money total = order.getOrderTotal();

    // Assert - verify the outcome
    assertEquals(new Money("25.00"), total);
}
```

### Test Independence

Each test must be able to run:
- In any order
- In isolation
- Repeatedly with the same result

**Anti-patterns to avoid**:
- Tests that depend on other tests running first
- Static mutable state shared between tests
- Tests that rely on database state from previous tests
- Tests that depend on system clock or random values

---

## JUnit 5 Migration Path

The FTGO codebase currently uses **JUnit 4.12**. New microservices (e.g., `ftgo-security-lib`) have started using JUnit 5. The migration path is:

### Phase 1: Dual Support (Current)

Run JUnit 4 and JUnit 5 tests side by side using the `junit-vintage-engine`:

```groovy
dependencies {
    testCompile 'junit:junit:4.12'                          // Existing JUnit 4
    testCompile 'org.junit.jupiter:junit-jupiter:5.3.2'     // New JUnit 5
    testRuntime 'org.junit.vintage:junit-vintage-engine:5.3.2' // Run JUnit 4 on JUnit 5 platform
}

test {
    useJUnitPlatform()  // Enable JUnit Platform (runs both 4 and 5)
}
```

### Phase 2: Migrate Existing Tests

Replace JUnit 4 constructs with JUnit 5 equivalents:

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` | Or remove entirely (Spring Boot 2.1+ auto-detects) |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` | |
| `@Before` | `@BeforeEach` | |
| `@BeforeClass` | `@BeforeAll` | |
| `@After` | `@AfterEach` | |
| `@Test(expected = X.class)` | `assertThrows(X.class, () -> ...)` | More precise exception testing |
| `@Rule` / `@ClassRule` | `@ExtendWith` | Extensions replace rules |
| `Assert.assertEquals()` | `Assertions.assertEquals()` | Package change |
| `@Ignore` | `@Disabled` | |

### Phase 3: Remove JUnit 4

Once all tests are migrated, remove the vintage engine and JUnit 4 dependency.

---

## References

- [Testing Strategy](testing-strategy.md) — CI pipeline, parallelization, execution order
- [Test Reporting](test-reporting.md) — Report formats and artifact management
- [Quality Gates](quality-gates.md) — Static analysis and code quality thresholds
- [JaCoCo Configuration](jacoco-configuration.md) — Code coverage setup
- [Test Templates](../test-templates/README.md) — Reusable test file templates
- [Microservices Patterns](https://microservices.io/patterns/testing/) — Testing patterns for microservices
- [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) — Contract testing framework
- [Gatling](https://gatling.io/) — Performance testing tool
