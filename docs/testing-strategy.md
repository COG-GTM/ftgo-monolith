# FTGO Testing Strategy

## Overview

This document defines the comprehensive testing strategy for the FTGO microservices platform. It establishes the testing pyramid, guidelines for each test tier, and standards for test quality across all bounded contexts.

## Testing Pyramid

The FTGO testing strategy follows the testing pyramid model, which prioritizes fast, isolated tests at the base and reserves expensive end-to-end tests for critical paths.

```
            /‾‾‾‾‾‾‾‾\
           /   E2E     \        ~5%  - Critical user journeys
          /   Tests      \
         /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
        /  Contract Tests   \   ~5%  - Service API contracts
       /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
      /  Integration Tests    \ ~20% - DB, external services
     /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
    /      Unit Tests            \ ~70% - Business logic, domain
   /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
```

### Target Distribution

| Test Tier        | Target % | Execution Time | Scope                           |
|------------------|----------|----------------|---------------------------------|
| Unit Tests       | ~70%     | < 1 second     | Single class/method             |
| Integration Tests| ~20%     | < 30 seconds   | Component + infrastructure      |
| Contract Tests   | ~5%      | < 10 seconds   | API contract verification       |
| E2E Tests        | ~5%      | < 5 minutes    | Full user journey               |

## Test Tiers

### 1. Unit Tests (~70%)

**Purpose:** Verify individual classes and methods in isolation.

**Characteristics:**
- No Spring context, no database, no external services
- Mocked dependencies via Mockito
- Fast execution (milliseconds per test)
- Run on every commit

**Technology Stack:**
- JUnit 5 (Jupiter)
- Mockito 5.x with `@ExtendWith(MockitoExtension.class)`
- AssertJ for fluent assertions
- Test data builders from `ftgo-test-lib`

**Location:** `services/<service>/src/test/java/`

**Naming Convention:** `*Test.java` (e.g., `OrderServiceTest.java`)

**Example Pattern:**
```java
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("should create order with valid input")
    void shouldCreateOrderWithValidInput() {
        // Given
        given(orderRepository.save(any())).willReturn(testOrder());

        // When
        Order result = orderService.createOrder(createOrderRequest());

        // Then
        assertThat(result.getState()).isEqualTo(APPROVAL_PENDING);
        verify(orderRepository).save(any());
    }
}
```

### 2. Integration Tests (~20%)

**Purpose:** Verify component interactions with real infrastructure (database, message broker).

**Characteristics:**
- Full or partial Spring Boot context
- Real database via Testcontainers (MySQL)
- Tests repository queries, Flyway migrations, transaction behavior
- Run on PR creation and merge

**Technology Stack:**
- JUnit 5 + `@SpringBootTest`
- Testcontainers for MySQL (replaces Docker Compose)
- `@DynamicPropertySource` for runtime configuration
- Spring Test `@Transactional` for test isolation

**Location:** `services/<service>/src/integration-test/java/` or `services/<service>/src/test/java/` with `@Tag("integration")`

**Naming Convention:** `*IntegrationTest.java` (e.g., `OrderRepositoryIntegrationTest.java`)

**Example Pattern:**
```java
@Tag("integration")
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistAndRetrieveOrder() {
        Order saved = orderRepository.save(newOrder());
        Optional<Order> found = orderRepository.findById(saved.getId());
        assertThat(found).isPresent();
    }
}
```

### 3. Contract Tests (~5%)

**Purpose:** Verify that service APIs maintain their contracts between producer and consumer.

**Characteristics:**
- Producer side: auto-generated tests from contract DSL
- Consumer side: stub-based verification
- No running services required
- Catches breaking API changes early

**Technology Stack:**
- Spring Cloud Contract (Groovy DSL)
- Spring Cloud Contract Stub Runner
- Rest-Assured for HTTP verification

**Location:** `services/<service>/src/test/resources/contracts/` (contract definitions)

**See:** [Contract Testing Approach](#contract-testing-approach) below for detailed workflow.

### 4. End-to-End Tests (~5%)

**Purpose:** Verify critical user journeys across the entire system.

**Characteristics:**
- Full system deployment (all services running)
- Tests real HTTP calls between services
- Slow execution — reserved for critical paths only
- Run before release/deploy

**Technology Stack:**
- Rest-Assured for API-level E2E
- Docker Compose or Kubernetes for environment orchestration

**Location:** `ftgo-end-to-end-tests/`

**Guidelines:**
- Keep E2E tests minimal — only test critical business flows
- Each E2E test should cover a complete user journey (e.g., "place order → approve → deliver")
- Use polling/retry for async operations
- Set generous timeouts for CI environments

## Shared Test Library (`shared/ftgo-test-lib/`)

The `ftgo-test-lib` shared library provides reusable test infrastructure:

| Package      | Contents                                              |
|--------------|-------------------------------------------------------|
| `builders`   | Test data builders (Order, Consumer, Restaurant, Courier) |
| `assertions` | Custom domain assertions (Money, Order, Address)      |
| `containers` | Testcontainers config (MySQL singleton)               |
| `config`     | Base test classes (AbstractUnitTest, AbstractIntegrationTest, AbstractApiTest) |
| `templates`  | Copy-and-adapt test templates for each tier           |

### Adding as a Dependency

```groovy
// In your service's build.gradle
testCompile project(':shared-ftgo-test-lib')
```

### Using Test Data Builders

```java
// Create test data with sensible defaults
Map<String, Object> order = OrderBuilder.anOrder()
    .withConsumerId(42L)
    .withOrderTotal(new BigDecimal("25.99"))
    .build();

// Use presets for common scenarios
Map<String, Object> approved = OrderBuilder.anApprovedOrder().build();
Map<String, Object> cancelled = OrderBuilder.aCancelledOrder().build();
```

### Using Custom Assertions

```java
// Domain-specific assertions
FtgoAssertions.assertOrder(order)
    .hasState("APPROVED")
    .hasConsumerId(42L)
    .hasLineItemCount(2);

// Money assertions
MoneyAssertions.assertMoneyEquals(actual, expected);
MoneyAssertions.assertMoneyInRange(amount, min, max);
```

## Contract Testing Approach

### Overview

Contract testing ensures that services can communicate correctly without requiring them to be deployed together. FTGO uses Spring Cloud Contract for producer-driven contract testing.

### Workflow

1. **Producer defines contracts** — Groovy DSL files under `src/test/resources/contracts/`
2. **Tests are auto-generated** — Spring Cloud Contract generates JUnit tests from contracts
3. **Stubs are published** — Producer publishes WireMock stubs as artifacts
4. **Consumer verifies** — Consumer uses Stub Runner to test against the stubs

### Contract Structure

```
services/ftgo-order-service/
  src/test/resources/contracts/
    order/
      shouldReturnOrderById.groovy
      shouldCreateOrder.groovy
      shouldReturn404ForMissingOrder.groovy
```

### Example Contract (Groovy DSL)

```groovy
Contract.make {
    description "should return order by id"
    request {
        method GET()
        url "/api/orders/1"
        headers {
            contentType applicationJson()
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            orderId: 1,
            state: "APPROVAL_PENDING",
            orderTotal: "29.99"
        ])
    }
}
```

### When to Write Contract Tests

- When a service exposes an API consumed by another FTGO service
- When changing an existing API that other services depend on
- When adding new fields to API responses
- When deprecating or removing API endpoints

## CI/CD Integration

### Pipeline Stages

```
┌──────────┐    ┌───────────────┐    ┌──────────────┐    ┌─────────┐
│  Unit     │───→│  Integration  │───→│  Contract     │───→│  E2E    │
│  Tests    │    │  Tests        │    │  Tests        │    │  Tests  │
│  (every   │    │  (every PR)   │    │  (every PR)   │    │  (pre-  │
│  commit)  │    │               │    │               │    │  deploy)│
└──────────┘    └───────────────┘    └──────────────┘    └─────────┘
```

### Gradle Task Mapping

| Test Tier    | Gradle Task           | Tag Filter          |
|--------------|-----------------------|---------------------|
| Unit         | `./gradlew test`      | `@Tag("unit")`      |
| Integration  | `./gradlew integrationTest` | `@Tag("integration")` |
| Contract     | `./gradlew contractTest` | `@Tag("contract")`   |
| E2E          | `./gradlew e2eTest`   | `@Tag("e2e")`       |

### Code Coverage

- Target: **70%+ line coverage** per service
- Tool: JaCoCo (configured via `FtgoTestingConventionsPlugin`)
- Reports: XML + HTML (published as CI artifacts)
- Coverage gates enforced in `integrationTest` and `test` tasks

## Test Quality Standards

### Naming Conventions

- **Test classes:** `<ClassUnderTest>Test.java` (unit), `<ClassUnderTest>IntegrationTest.java` (integration)
- **Test methods:** Use `@DisplayName` with descriptive sentences
- **Method names:** `shouldDoSomethingWhenCondition()` pattern

### Structure

Every test should follow the **Arrange-Act-Assert** (or Given-When-Then) pattern:

```java
@Test
@DisplayName("should reject order when consumer not found")
void shouldRejectOrderWhenConsumerNotFound() {
    // Arrange (Given)
    given(consumerRepository.findById(999L)).willReturn(Optional.empty());

    // Act (When) + Assert (Then)
    assertThatThrownBy(() -> orderService.createOrder(invalidRequest()))
        .isInstanceOf(ConsumerNotFoundException.class);
}
```

### What NOT to Test

- Framework code (Spring Boot internals, JPA implementation details)
- Trivial getters/setters (unless they contain logic)
- Configuration classes (unless they have conditional logic)
- Third-party library behavior

## Related Documentation

- [JUnit 4 to JUnit 5 Migration Guide](junit5-migration-guide.md)
- [Testcontainers Guide](testcontainers-guide.md)
- [When to Write Which Test](when-to-test.md)
- [Automated Testing Pipeline](automated-testing-pipeline.md)
- [Code Review Guidelines](code-review-guidelines.md)
