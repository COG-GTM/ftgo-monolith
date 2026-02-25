# FTGO Platform - Comprehensive Testing Strategy

> Version: 1.0
> Status: Approved
> Last Updated: 2024-03-01
> Applies To: All FTGO microservices

## Overview

This document defines the comprehensive testing strategy for the FTGO microservices platform. It establishes the testing pyramid, guidelines for each test tier, and best practices for writing maintainable, reliable tests during and after the monolith-to-microservices migration.

## Testing Pyramid

The FTGO platform follows the testing pyramid model to balance confidence, speed, and maintainability:

```
        /‾‾‾‾‾‾‾‾‾‾\
       /   E2E Tests  \         ~5% of tests
      /   (Critical     \       Full stack validation
     /     Paths Only)    \     Target: < 20 min
    /──────────────────────\
   /    Contract Tests       \   ~10% of tests
  /   (Service-to-Service     \  API compatibility
 /     API Agreements)          \ Target: < 5 min
/────────────────────────────────\
|      Integration Tests          |  ~15% of tests
|   (Database + Spring Context)   |  Real dependencies
|   Target: < 10 min/service      |  Testcontainers
|──────────────────────────────────|
|          Unit Tests              |  ~70% of tests
|   (Domain Logic, Services)       |  Mocked dependencies
|   Target: < 2 min/service        |  Fast feedback
\__________________________________/
```

### Coverage Targets

| Tier | Coverage Target | Execution Time | Dependencies |
|------|----------------|----------------|-------------|
| Unit Tests | 70% instruction coverage (enforced by JaCoCo) | < 2 min/service | None (mocked) |
| Integration Tests | Key repository and configuration paths | < 10 min/service | MySQL via Testcontainers |
| Contract Tests | All inter-service API contracts | < 5 min/service | Spring Cloud Contract stubs |
| E2E Tests | Critical business flows only | < 20 min total | Full Docker Compose stack |

## Test Framework Stack

### Current Stack (New Microservices)

| Tool | Version | Purpose |
|------|---------|---------|
| JUnit 5 (Jupiter) | 5.10.2 | Test framework |
| Mockito | 5.10.0 | Mocking framework |
| AssertJ | 3.25.3 | Fluent assertions |
| Rest-Assured | 5.4.0 | REST API testing |
| Testcontainers | 1.19.6 | Container-based integration testing |
| Spring Boot Test | 3.2.3 | Spring context testing |
| Spring Cloud Contract | 4.1.x | Contract testing |
| JaCoCo | 0.8.11 | Code coverage |

### Legacy Stack (Monolith Modules)

| Tool | Version | Purpose |
|------|---------|---------|
| JUnit 4 | 4.13.2 | Test framework (via JUnit Vintage Engine) |
| Rest-Assured | 3.0.6 | REST API testing |
| Spring Boot Test | 2.7.18 | Spring context testing |

---

## Tier 1: Unit Tests

### Purpose

Unit tests validate individual classes and methods in isolation. They are the foundation of the testing pyramid and provide the fastest feedback loop.

### What to Test

- **Domain entities**: State transitions, business rules, validation logic
- **Value objects**: Equality, arithmetic operations, serialization
- **Domain services**: Business logic orchestration with mocked dependencies
- **Controllers**: Request mapping, response formatting (with MockMvc)
- **Mappers/Converters**: DTO-to-entity transformations
- **Utility classes**: Helper functions, formatters

### What NOT to Unit Test

- Framework configuration (Spring context loading, bean wiring)
- Simple getters/setters without logic
- Third-party library behavior
- Database queries (use integration tests instead)

### Conventions

1. **Test class naming**: `{ClassName}Test.java`
2. **Test method naming**: `should{ExpectedBehavior}` or `should{ExpectedBehavior}When{Condition}`
3. **Location**: `src/test/java/com/ftgo/{service}/{layer}/`
4. **Arrange-Act-Assert**: Use the AAA pattern consistently
5. **One assertion per concept**: Each test should verify one logical concept (multiple related assertions are fine)

### JUnit 5 Annotations

```java
@Test                          // Basic test method
@DisplayName("description")    // Human-readable test name
@Nested                        // Group related tests
@ParameterizedTest             // Data-driven tests
@ValueSource / @CsvSource      // Parameter sources
@BeforeEach / @AfterEach       // Per-test setup/teardown
@BeforeAll / @AfterAll         // Per-class setup/teardown (static)
@Tag("unit")                   // Test categorization
@Disabled("reason")            // Skip test with reason
```

### Example: Domain Entity Unit Test

```java
package com.ftgo.order.domain;

import com.ftgo.common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order")
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = OrderTestBuilder.anOrder().withDefaults().build();
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {
        @Test
        @DisplayName("should be in PENDING state")
        void shouldBeInPendingState() {
            assertThat(order.getState()).isEqualTo(OrderState.PENDING);
        }

        @Test
        @DisplayName("should calculate order total from line items")
        void shouldCalculateOrderTotal() {
            assertThat(order.getOrderTotal()).isEqualTo(new Money("25.00"));
        }
    }

    @Nested
    @DisplayName("when cancelling")
    class WhenCancelling {
        @Test
        @DisplayName("should transition to CANCELLED state")
        void shouldTransitionToCancelled() {
            order.cancel();
            assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
        }

        @Test
        @DisplayName("should reject cancellation of non-cancellable order")
        void shouldRejectCancellationOfDeliveredOrder() {
            order.markDelivered();
            assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(UnsupportedStateTransitionException.class);
        }
    }
}
```

### Example: Service Unit Test with Mockito

```java
package com.ftgo.order.domain;

import com.ftgo.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("should create order when restaurant exists")
    void shouldCreateOrder() {
        // Arrange
        var restaurant = RestaurantTestBuilder.aRestaurant().withDefaults().build();
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var request = new CreateOrderRequest(1L, 1L, List.of(
            new MenuItemIdAndQuantity("item1", 2)));
        Order order = orderService.createOrder(request);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getRestaurant()).isEqualTo(restaurant);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("should throw when restaurant not found")
    void shouldThrowWhenRestaurantNotFound() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(
            new CreateOrderRequest(1L, 999L, List.of())))
            .isInstanceOf(RestaurantNotFoundException.class);
    }
}
```

---

## Tier 2: Integration Tests

### Purpose

Integration tests validate the interaction between components with real external dependencies (database, message broker, etc.) using Testcontainers.

### What to Test

- **Repository layer**: JPA queries, custom queries, pagination
- **Spring configuration**: Bean wiring, property loading, auto-configuration
- **Database migrations**: Flyway migration correctness
- **Security filter chain**: Authentication/authorization flow
- **API endpoints**: Full request-response cycle with Spring context

### Source Set

Integration tests live in a separate source set:

```
src/
  integration-test/
    java/com/ftgo/{service}/
      repository/       # Repository integration tests
      api/              # API integration tests
      config/           # Configuration tests
    resources/
      application-integration-test.properties
```

### Running Integration Tests

```bash
# Run integration tests for a specific service
./gradlew :services:ftgo-order-service:integrationTest

# Run with coverage report
./gradlew :services:ftgo-order-service:integrationTest jacocoIntegrationTestReport
```

### Testcontainers Configuration

All integration tests use Testcontainers for database dependencies instead of requiring an external Docker Compose stack:

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration-test")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ftgo")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

### Shared Testcontainers Configuration

To avoid duplicating container setup, use a shared abstract base class:

```java
public abstract class AbstractMySqlIntegrationTest {

    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ftgo")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
```

### Example: Repository Integration Test

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("should persist and retrieve order")
    void shouldPersistAndRetrieveOrder() {
        Order order = OrderTestBuilder.anOrder().withDefaults().build();

        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderTotal()).isEqualTo(order.getOrderTotal());
    }
}
```

---

## Tier 3: Contract Tests

### Purpose

Contract tests verify that microservices agree on API request/response formats. They prevent breaking changes when services evolve independently.

FTGO uses **consumer-driven contract testing** with Spring Cloud Contract. See [API Contract Testing](api-contract-testing.md) for the full contract testing strategy.

### Key Points

- Contracts are stored in shared API modules (`shared/ftgo-*-service-api/`)
- Provider services run contract verification tests
- Consumer services test against generated stubs
- Contracts follow the Groovy DSL format

### Example: Contract Verification Base Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        // Set up common stubs for contract verification
    }
}
```

---

## Tier 4: End-to-End Tests

### Purpose

E2E tests validate critical business flows across the entire platform stack. They run against a full Docker Compose environment.

### What to Test (Critical Paths Only)

1. **Order lifecycle**: Create order -> Accept -> Prepare -> Deliver
2. **Consumer registration**: Register -> Verify -> Place first order
3. **Restaurant onboarding**: Create restaurant -> Set menu -> Receive order

### What NOT to E2E Test

- Edge cases (use unit/integration tests)
- Error handling (use unit tests)
- Performance (use dedicated performance tests)
- Security rules (use integration tests)

### Running E2E Tests

```bash
# Start full stack
docker-compose up -d

# Wait for services
./wait-for-services.sh

# Run E2E tests
./run-end-to-end-tests.sh
```

---

## JUnit 4 to JUnit 5 Migration Guide

### Why Migrate?

| Feature | JUnit 4 | JUnit 5 |
|---------|---------|---------|
| Architecture | Monolithic | Modular (Jupiter + Platform + Vintage) |
| Nested tests | Not supported | `@Nested` for test grouping |
| Parameterized tests | Limited | Rich `@ParameterizedTest` support |
| Extensions | `@RunWith` / `@Rule` | `@ExtendWith` (composable) |
| Display names | Not supported | `@DisplayName` for readable output |
| Assertions | `Assert.assertEquals()` | `Assertions.assertEquals()` or AssertJ |
| Conditional tests | Not supported | `@EnabledIf`, `@DisabledOnOs`, etc. |
| Lifecycle | `@Before` / `@After` | `@BeforeEach` / `@AfterEach` |

### Migration Checklist

1. **Import changes:**

   ```java
   // JUnit 4
   import org.junit.Test;
   import org.junit.Before;
   import org.junit.After;
   import static org.junit.Assert.*;

   // JUnit 5
   import org.junit.jupiter.api.Test;
   import org.junit.jupiter.api.BeforeEach;
   import org.junit.jupiter.api.AfterEach;
   import static org.assertj.core.api.Assertions.*;  // preferred
   ```

2. **Annotation changes:**

   | JUnit 4 | JUnit 5 |
   |---------|---------|
   | `@Before` | `@BeforeEach` |
   | `@After` | `@AfterEach` |
   | `@BeforeClass` | `@BeforeAll` |
   | `@AfterClass` | `@AfterAll` |
   | `@Ignore` | `@Disabled("reason")` |
   | `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
   | `@Rule ExpectedException` | `assertThrows()` or `assertThatThrownBy()` |

3. **Exception testing:**

   ```java
   // JUnit 4
   @Test(expected = IllegalArgumentException.class)
   public void shouldThrow() { ... }

   // JUnit 5 (with AssertJ - preferred)
   @Test
   void shouldThrow() {
       assertThatThrownBy(() -> service.doSomething())
           .isInstanceOf(IllegalArgumentException.class)
           .hasMessage("expected message");
   }
   ```

4. **Parameterized tests:**

   ```java
   // JUnit 5
   @ParameterizedTest
   @ValueSource(strings = {"PENDING", "APPROVED", "CANCELLED"})
   @DisplayName("should parse valid order states")
   void shouldParseValidOrderState(String state) {
       assertThat(OrderState.valueOf(state)).isNotNull();
   }

   @ParameterizedTest
   @CsvSource({
       "10, 2, 20",
       "5, 3, 15",
       "0, 10, 0"
   })
   @DisplayName("should calculate line item total")
   void shouldCalculateLineItemTotal(int price, int quantity, int expected) {
       var lineItem = new OrderLineItem("id", "name", new Money(price), quantity);
       assertThat(lineItem.getTotal()).isEqualTo(new Money(expected));
   }
   ```

5. **Backward compatibility**: Legacy JUnit 4 tests continue to work via the JUnit Vintage Engine configured in the root `build.gradle`.

---

## When to Mock vs. Use Real Dependencies

### Decision Matrix

| Dependency Type | Unit Test | Integration Test | E2E Test |
|----------------|-----------|------------------|----------|
| Repository/DAO | Mock | Real (Testcontainers) | Real |
| External HTTP API | Mock (WireMock/stubs) | Mock (WireMock) | Real |
| Message broker | Mock | Testcontainers or embedded | Real |
| Domain service | Mock | Real | Real |
| Spring context | No context | Full context | Full context |
| Database | Mock | Real (Testcontainers MySQL) | Real (Docker Compose) |
| File system | Mock or temp files | Real | Real |
| Time/Clock | Mock (`Clock.fixed()`) | Mock or real | Real |

### Guidelines

1. **Mock at the boundary**: Mock external dependencies, not internal collaborators
2. **Prefer real over mock**: If it's fast and deterministic, use the real thing
3. **Never mock domain objects**: Use test builders to create real instances
4. **Mock time in unit tests**: Use `java.time.Clock` for deterministic testing
5. **Don't mock what you don't own**: Wrap third-party APIs, then mock the wrapper

---

## Test Data Management

### Test Data Builders

Use the Builder pattern for creating test data. This makes tests readable and maintainable:

```java
public class OrderTestBuilder {
    private Long consumerId = 1L;
    private Restaurant restaurant = RestaurantTestBuilder.aRestaurant().build();
    private List<OrderLineItem> lineItems = defaultLineItems();
    private OrderState state = OrderState.PENDING;

    public static OrderTestBuilder anOrder() {
        return new OrderTestBuilder();
    }

    public OrderTestBuilder withDefaults() {
        return this;
    }

    public OrderTestBuilder withConsumerId(Long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderTestBuilder withRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

    public Order build() {
        return new Order(consumerId, restaurant, lineItems);
    }

    private static List<OrderLineItem> defaultLineItems() {
        return List.of(
            new OrderLineItem("item1", "Burger", new Money(10), 2),
            new OrderLineItem("item2", "Fries", new Money(5), 1)
        );
    }
}
```

### Object Mother Pattern (Legacy)

The existing monolith uses the Object Mother pattern (`RestaurantMother`, `OrderDetailsMother`). New microservices should prefer the Builder pattern but may use Object Mothers for backward compatibility.

---

## Test Configuration

### Application Properties for Tests

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
logging.level.com.ftgo=DEBUG

# src/integration-test/resources/application-integration-test.properties
# Database configured via Testcontainers @DynamicPropertySource
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.ftgo=DEBUG
logging.level.org.springframework.test=DEBUG
```

### Test Profiles

| Profile | Usage | Database |
|---------|-------|----------|
| `test` | Unit tests (default) | H2 in-memory or mocked |
| `integration-test` | Integration tests | MySQL via Testcontainers |
| `contract-test` | Contract verification | Mocked services |
| `e2e` | End-to-end tests | Real MySQL via Docker Compose |

---

## CI Pipeline Integration

Tests are executed in the CI pipeline via GitHub Actions workflows:

| Workflow | Tier | Trigger | Details |
|----------|------|---------|---------|
| `ci-test-unit.yml` | Unit | Push & PR | Matrix strategy per service |
| `ci-test-integration.yml` | Integration | Push & PR | MySQL service container |
| `ci-test-e2e.yml` | E2E | Manual | Full Docker Compose stack |
| `ci-test-report.yml` | Reporting | After unit/integration | Aggregated results |

See [Testing Pipeline](testing-pipeline.md) for detailed CI configuration.

---

## Security Testing

### What to Test

1. **Authentication**: JWT token validation, expired tokens, missing tokens
2. **Authorization**: Role-based access control, endpoint permissions
3. **Input validation**: SQL injection, XSS, request body validation
4. **CORS**: Cross-origin request handling

### Example: Security Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should reject unauthenticated requests to protected endpoints")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should allow admin access to admin endpoints")
    void shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
            .andExpect(status().isOk());
    }
}
```

---

## Performance Testing (Future)

> **Status**: Planned for a future phase

### Recommended Tools

- **Gatling**: Load testing with Scala DSL
- **JMH**: Microbenchmarking for critical paths
- **k6**: HTTP load testing with JavaScript

### Key Metrics to Track

- Response time (p50, p95, p99)
- Throughput (requests/second)
- Error rate under load
- Resource utilization (CPU, memory, connections)

---

## Resilience Testing (Future)

> **Status**: Planned for a future phase

### Recommended Approaches

- **Chaos Monkey for Spring Boot**: Random latency, exceptions
- **Testcontainers Toxiproxy**: Network fault injection
- **Circuit breaker testing**: Verify Resilience4j fallback behavior

---

## File Structure Reference

```
services/ftgo-{service}-service/
  src/
    test/
      java/com/ftgo/{service}/
        domain/
          {Entity}Test.java              # Domain unit tests
          {Service}Test.java             # Service unit tests with mocks
        web/
          {Controller}Test.java          # Controller unit tests
        repository/
          (empty - use integration tests for repos)
        testdata/
          {Entity}TestBuilder.java       # Test data builders
      resources/
        application-test.properties      # Test configuration
    integration-test/
      java/com/ftgo/{service}/
        repository/
          {Repository}IntegrationTest.java  # Repository tests with Testcontainers
        api/
          {Feature}ApiIntegrationTest.java  # API integration tests
        config/
          AbstractMySqlIntegrationTest.java # Shared Testcontainers config
      resources/
        application-integration-test.properties
```

---

## Quick Reference: Test Naming Cheat Sheet

| Type | File Name | Method Name | Location |
|------|-----------|-------------|----------|
| Unit | `OrderTest.java` | `shouldCalculateTotal()` | `src/test/java/.../domain/` |
| Unit (Controller) | `OrderControllerTest.java` | `shouldReturnOrderById()` | `src/test/java/.../web/` |
| Integration | `OrderRepositoryIntegrationTest.java` | `shouldPersistOrder()` | `src/integration-test/java/.../repository/` |
| Contract | `BaseContractTest.java` | (generated by framework) | `src/contractTest/java/` |
| E2E | `OrderLifecycleE2ETest.java` | `shouldCompleteOrderFlow()` | `ftgo-end-to-end-tests/` |

---

## References

- [Testing Pipeline CI Configuration](testing-pipeline.md)
- [API Contract Testing Strategy](api-contract-testing.md)
- [API Standards](api-standards.md)
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Code quality and review guidelines
- [Spring Boot Testing Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
