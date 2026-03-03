# FTGO Microservices Testing Strategy

## Overview

This document defines the comprehensive testing strategy for the FTGO microservices migration. It establishes the testing pyramid, defines test types and their purposes, provides guidelines for when to write which type of test, and documents the contract testing approach.

## Table of Contents

1. [Testing Pyramid](#testing-pyramid)
2. [Test Types](#test-types)
3. [When to Write Which Test](#when-to-write-which-test)
4. [Contract Testing Approach](#contract-testing-approach)
5. [Test Utility Library](#test-utility-library)
6. [Mocking vs Real Dependencies](#mocking-vs-real-dependencies)
7. [Test Organization](#test-organization)
8. [Coverage Goals](#coverage-goals)
9. [CI/CD Integration](#cicd-integration)

---

## Testing Pyramid

The FTGO testing strategy follows a standard testing pyramid with four tiers. The pyramid shape reflects the ideal distribution of tests: many fast, cheap unit tests at the base; fewer, slower integration and contract tests in the middle; and a small number of expensive E2E tests at the top.

```
           /\
          /  \        E2E Tests (< 5%)
         /    \       Critical user journeys only
        /------\
       /        \     Contract Tests (~ 10%)
      /          \    API contracts between services
     /------------\
    /              \  Integration Tests (~ 15-20%)
   /                \ Real DB, containers, service interactions
  /------------------\
 /                    \  Unit Tests (>= 70%)
/______________________\ Domain logic, services, utilities
```

### Distribution Targets

| Test Type    | Coverage Target | Execution Time | Run Frequency     |
|-------------|----------------|----------------|-------------------|
| Unit         | >= 70% line    | < 5 min total  | Every commit      |
| Integration  | Key paths      | < 10 min total | Every PR          |
| Contract     | All API endpoints | < 5 min    | Every PR          |
| E2E          | Critical paths | < 15 min       | Pre-release/nightly |

---

## Test Types

### 1. Unit Tests (JUnit 5 + Mockito)

**Purpose:** Verify individual classes and methods in isolation.

**Characteristics:**
- No external dependencies (database, network, filesystem)
- All collaborators are mocked
- Fast execution (milliseconds per test)
- Deterministic (no flaky tests)

**What to test:**
- Domain entity behavior (state transitions, calculations, validation)
- Service layer business logic
- Utility/helper classes
- DTOs and value object serialization
- Exception handling

**Framework:** JUnit 5 (Jupiter) + Mockito + AssertJ

**Location:** `src/test/java/`

**Example:**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("should create order in APPROVED state")
    void shouldCreateOrderInApprovedState() {
        Order order = OrderBuilder.anOrder().build();
        assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    }
}
```

### 2. Integration Tests (Spring Boot + Testcontainers)

**Purpose:** Verify that multiple components work together with real external dependencies.

**Characteristics:**
- Uses real databases via Testcontainers
- Tests repository queries against real MySQL
- Verifies Spring context loading and wiring
- Slower than unit tests but catches wiring issues

**What to test:**
- Repository methods with real database
- Service-to-repository integration
- Spring configuration and bean wiring
- Database migration scripts (Flyway)
- JPA entity mappings and queries

**Framework:** Spring Boot Test + Testcontainers + JUnit 5

**Location:** `src/integration-test/java/` (separate source set)

**Example:**
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = MySqlTestContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        MySqlTestContainer.registerProperties(registry, mysql);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistAndRetrieveOrder() {
        Order order = OrderBuilder.anOrder().build();
        Order saved = orderRepository.save(order);
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }
}
```

### 3. Contract Tests (Spring Cloud Contract / Pact)

**Purpose:** Verify that API contracts between services are not broken.

**Characteristics:**
- Provider tests verify the service satisfies its contracts
- Consumer tests verify the client can consume the provider's API
- Contracts are shared artifacts (stubs/pacts)
- Catch breaking changes before deployment

**What to test:**
- REST API request/response structure
- HTTP status codes for all scenarios
- JSON serialization format
- Error response format
- API versioning compatibility

**Framework:** Spring Cloud Contract (primary) or Pact (cross-language)

**Location:** `src/test/resources/contracts/` (contract definitions) + `src/test/java/` (base classes)

**Example contract (Groovy DSL):**
```groovy
Contract.make {
    description "should return order by ID"
    request {
        method GET()
        url "/orders/1"
    }
    response {
        status OK()
        headers { contentType applicationJson() }
        body([orderId: 1, state: "APPROVED"])
    }
}
```

### 4. E2E Tests (Docker Compose + Rest-Assured)

**Purpose:** Verify critical user journeys through the entire system.

**Characteristics:**
- Full system deployed (all services, databases, message brokers)
- Tests real HTTP calls against running services
- Slowest and most expensive tests
- Only cover critical business paths

**What to test:**
- Order creation flow (consumer -> restaurant -> order -> courier)
- Payment processing flow
- User authentication flow
- Cross-service data consistency

**Framework:** Rest-Assured + Docker Compose + JUnit 5

**Location:** `ftgo-end-to-end-tests/`

**Example:**
```java
@Test
void shouldCreateOrderAndAccept() {
    // Create consumer
    long consumerId = createConsumer();
    // Create restaurant
    long restaurantId = createRestaurant();
    // Place order
    long orderId = createOrder(consumerId, restaurantId);
    // Verify order state
    verifyOrderState(orderId, "APPROVED");
    // Accept order
    acceptOrder(orderId);
    verifyOrderState(orderId, "ACCEPTED");
}
```

---

## When to Write Which Test

### Decision Matrix

| Scenario | Unit | Integration | Contract | E2E |
|----------|------|-------------|----------|-----|
| New domain entity method | **Yes** | No | No | No |
| New service method | **Yes** | Maybe | No | No |
| New repository query | No | **Yes** | No | No |
| New REST endpoint | **Yes** (controller) | No | **Yes** | No |
| New inter-service call | No | No | **Yes** | Maybe |
| Bug fix (domain logic) | **Yes** | No | No | No |
| Bug fix (data issue) | No | **Yes** | No | No |
| Bug fix (integration) | No | Maybe | Maybe | **Yes** |
| Refactoring | **Yes** (existing) | Existing | Existing | No |
| Database migration | No | **Yes** | No | No |
| Security endpoint | **Yes** | **Yes** | No | No |
| Critical business flow | **Yes** | Maybe | Maybe | **Yes** |

### Rules of Thumb

1. **Always start with a unit test.** If you can't unit test it, the design may need improvement.
2. **Add integration tests for data access.** Any repository method that does more than simple CRUD deserves an integration test.
3. **Add contract tests for every public API.** Any endpoint that another service calls must have a contract test.
4. **Add E2E tests only for critical paths.** If a failure in this flow would wake someone at 3 AM, it needs an E2E test.
5. **Never duplicate coverage.** If a unit test already covers the logic, don't write an integration test for the same assertion.

### Test-First vs Test-After

- **Domain logic:** Write tests first (TDD). Domain entities and services benefit most from TDD.
- **Controller/API:** Write contract first, then implement. Contract-first development ensures API stability.
- **Integration:** Write after implementation. Integration tests verify wiring, which requires working code.
- **E2E:** Write during feature completion. E2E tests verify the full flow works end-to-end.

---

## Contract Testing Approach

### Strategy: Consumer-Driven Contracts

We adopt **consumer-driven contract testing** where the consumer (client) defines what it expects from the provider (server). This ensures that providers don't break their consumers.

### Recommended Tools

| Tool | Use Case | Pros | Cons |
|------|----------|------|------|
| **Spring Cloud Contract** | Spring-to-Spring services | Native Spring integration, Groovy DSL | Java/JVM only |
| **Pact** | Cross-language services | Language-agnostic, Pact Broker | More setup required |

**Recommendation:** Use Spring Cloud Contract for service-to-service communication within the FTGO ecosystem. Consider Pact if non-JVM consumers are introduced.

### Contract Testing Workflow

```
1. Consumer defines expected interactions (contract)
   ↓
2. Contract is shared with provider (via artifact/broker)
   ↓
3. Provider runs contract tests against its implementation
   ↓
4. Provider generates stubs from verified contracts
   ↓
5. Consumer runs tests against provider stubs
   ↓
6. Both sides are verified compatible
```

### Provider Verification

Each service must:
1. Define a base test class for contract verification
2. Set up mock data that satisfies all contracts
3. Run generated contract tests in CI

### Consumer Verification

Each service client must:
1. Use `@AutoConfigureStubRunner` to load provider stubs
2. Test all client methods against stubs
3. Fail fast if provider contract changes

### Contract Versioning

- Contracts are versioned with the service version
- Breaking changes require a major version bump
- Backward-compatible changes (adding fields) are non-breaking
- Removing or renaming fields is always a breaking change

---

## Test Utility Library

The `shared/ftgo-test-utils` library provides reusable test utilities across all services.

### Test Data Builders

| Builder | Domain Class | Default Values |
|---------|-------------|----------------|
| `OrderBuilder` | `Order` | consumerId=1, default restaurant, 1 line item |
| `ConsumerBuilder` | `Consumer` | firstName="John", lastName="Doe" |
| `RestaurantBuilder` | `Restaurant` | name="Ajanta", Oakland address, 1 menu item |
| `CourierBuilder` | `Courier` | firstName="Mike", lastName="Driver", Oakland address |
| `MenuItemBuilder` | `MenuItem` | id="1", name="Chicken Vindaloo", price="12.34" |
| `OrderLineItemBuilder` | `OrderLineItem` | menuItemId="1", quantity=1 |
| `AddressBuilder` | `Address` | 123 Main St, Oakland, CA 94611 |
| `PersonNameBuilder` | `PersonName` | John Doe |
| `MoneyBuilder` | `Money` | amount="10.00" |

### Usage

```java
// Add dependency
testCompile project(':shared:ftgo-test-utils')

// Use builders
Order order = OrderBuilder.anOrder()
    .withConsumerId(42L)
    .withOrderId(1L)
    .build();
```

### Custom Assertions

- `OrderAssertions` - Domain-specific assertions for Order state, totals, consumers
- `MoneyAssertions` - Assertions for Money equality, zero checks, comparisons

### Testcontainers Configuration

- `MySqlTestContainer` - Pre-configured MySQL container with singleton support
- `TestApplicationConfig` - Shared constants (profiles, timeouts, default IDs)

---

## Mocking vs Real Dependencies

### When to Mock

| Dependency | Mock in Unit Tests | Mock in Integration Tests | Mock in E2E Tests |
|-----------|-------------------|--------------------------|-------------------|
| Repository | **Yes** | No (use Testcontainers) | No |
| Other service | **Yes** | **Yes** (or use stubs) | No |
| External API | **Yes** | **Yes** (use WireMock) | **Yes** (use sandbox) |
| Message broker | **Yes** | No (use Testcontainers) | No |
| Clock/time | **Yes** | Maybe | No |
| Random/UUID | **Yes** | No | No |

### Guidelines

1. **Mock at boundaries.** Mock the dependency interface, not the implementation.
2. **Don't mock what you don't own.** If you mock a third-party library, you might miss behavior changes. Wrap it in an adapter and mock the adapter.
3. **Don't mock value objects.** `Money`, `Address`, `PersonName` should be real instances.
4. **Don't mock domain entities.** Use builders to create real domain objects.
5. **Mock services, not repositories, in controller tests.** The controller layer should only know about services.

---

## Test Organization

### Directory Structure

```
service-name/
├── src/
│   ├── main/java/          # Production code
│   ├── test/java/           # Unit tests + API tests
│   │   ├── domain/          # Domain entity tests
│   │   ├── service/         # Service layer tests
│   │   ├── web/             # Controller/API tests
│   │   └── contracts/       # Contract base classes
│   ├── test/resources/
│   │   ├── contracts/       # Contract definitions (.groovy)
│   │   └── application-unit-test.yml
│   ├── integration-test/
│   │   ├── java/            # Integration tests
│   │   │   ├── repository/  # Repository integration tests
│   │   │   └── service/     # Service integration tests
│   │   └── resources/
│   │       └── application-integration-test.yml
```

### Naming Conventions

| Test Type | Class Suffix | Example |
|----------|-------------|---------|
| Unit test | `Test` | `OrderServiceTest` |
| Integration test | `IntegrationTest` | `OrderRepositoryIntegrationTest` |
| Contract test | `ContractBase` | `OrderContractBase` |
| API test | `ApiTest` | `OrderControllerApiTest` |
| E2E test | `EndToEndTest` | `OrderFlowEndToEndTest` |

### Test Method Naming

Use descriptive method names with the pattern:
```
should[ExpectedBehavior]When[Condition]()
```

Examples:
- `shouldCreateOrderWhenConsumerIsValid()`
- `shouldReturn404WhenOrderNotFound()`
- `shouldThrowExceptionWhenInvalidState()`

---

## Coverage Goals

### JaCoCo Configuration

Coverage is enforced via the `ftgo.testing-conventions` Gradle plugin:

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.70  // 70% line coverage minimum
            }
        }
    }
}
```

### Excluded from Coverage

The following are excluded from coverage metrics (configured in `ftgo.testing-conventions.gradle`):
- Configuration classes (`*Configuration`, `*Config`)
- Application entry points (`*Application`, `*Main`)
- DTOs and request/response models
- Generated code

### Coverage by Layer

| Layer | Minimum Coverage | Rationale |
|-------|-----------------|-----------|
| Domain entities | 90% | Core business logic, must be thoroughly tested |
| Service layer | 80% | Business rules and orchestration |
| Controller layer | 70% | Request/response handling |
| Repository layer | N/A | Tested via integration tests |
| Configuration | N/A | Excluded from coverage |

---

## CI/CD Integration

### Pipeline Stages

```
1. Compile & Lint
   ↓
2. Unit Tests + Coverage (ci-unit-tests.yml)
   ↓
3. Contract Tests
   ↓
4. Integration Tests (ci-integration-tests.yml)
   ↓
5. Build Docker Images
   ↓
6. E2E Tests (ci-e2e-tests.yml)
   ↓
7. Deploy
```

### CI Configuration

- **Unit tests:** Run on every push and PR (`ci-unit-tests.yml`)
- **Integration tests:** Run on PR and merge to main (`ci-integration-tests.yml`)
- **E2E tests:** Run on merge to main and release branches (`ci-e2e-tests.yml`)

### Failure Policies

- **Unit test failure:** Block merge, must fix immediately
- **Integration test failure:** Block merge, investigate within 24 hours
- **Contract test failure:** Block merge, coordinate with affected teams
- **E2E test failure:** Block release, investigate immediately
- **Flaky test:** Quarantine after 3 flakes, fix within 1 sprint

---

## Migration from Monolith Testing

### Current State (Monolith)

| Aspect | Current | Target |
|--------|---------|--------|
| Test framework | JUnit 4.12 | JUnit 5.10 (Jupiter) |
| Mocking | Mockito (manual) | Mockito + `@ExtendWith` |
| Assertions | JUnit + Hamcrest | AssertJ (fluent) |
| API testing | Rest-Assured 2.9 | Rest-Assured 5.4 |
| Database tests | Manual MySQL setup | Testcontainers |
| Contract tests | None | Spring Cloud Contract |
| Test data | Mother objects | Builder pattern |
| Coverage | No enforcement | JaCoCo >= 70% |

### Migration Path

See [JUnit 5 Migration Guide](./junit5-migration-guide.md) for detailed migration patterns.

### Gaps Being Addressed

1. **Contract tests** - New capability for microservices inter-service communication
2. **Testcontainers** - Replacing manual Docker setup for integration tests
3. **Performance tests** - Future: add Gatling/JMeter for load testing
4. **Security tests** - Future: add OWASP ZAP for security scanning
5. **Chaos tests** - Future: add Chaos Monkey for resilience testing
