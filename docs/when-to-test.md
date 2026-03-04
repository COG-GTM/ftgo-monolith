# When to Write Which Type of Test

## Overview

This guide helps FTGO developers decide which type of test to write for different scenarios. Choosing the right test tier ensures fast feedback, good coverage, and maintainable test suites.

## Decision Matrix

Use this matrix to determine the appropriate test tier for your change:

| What You're Changing                    | Unit Test | Integration Test | Contract Test | E2E Test |
|-----------------------------------------|:---------:|:----------------:|:-------------:|:--------:|
| Business logic in a service class       | **Yes**   | No               | No            | No       |
| Domain entity behavior                  | **Yes**   | No               | No            | No       |
| Value object (Money, Address)           | **Yes**   | No               | No            | No       |
| Validation rules                        | **Yes**   | No               | No            | No       |
| Repository/DAO query                    | No        | **Yes**           | No            | No       |
| Flyway migration script                 | No        | **Yes**           | No            | No       |
| JPA entity mapping                      | No        | **Yes**           | No            | No       |
| Transaction behavior                    | No        | **Yes**           | No            | No       |
| REST controller endpoint                | **Yes**   | Optional          | **Yes**       | No       |
| API request/response format             | No        | No               | **Yes**        | No       |
| API consumed by another FTGO service    | No        | No               | **Yes**        | No       |
| API consumed by external clients        | No        | No               | **Yes**        | No       |
| Cross-service business flow             | No        | No               | No            | **Yes**  |
| Order placement → delivery flow         | No        | No               | No            | **Yes**  |
| Security filter chain                   | **Yes**   | **Yes**           | No            | No       |
| JWT token generation/validation         | **Yes**   | No               | No            | No       |
| Event publishing                        | **Yes**   | Optional          | No            | No       |
| Configuration properties loading        | No        | **Yes**           | No            | No       |

## Decision Flowchart

```
Start: What am I testing?
  │
  ├─ Pure business logic (no I/O)?
  │   └─ → Write a UNIT TEST
  │
  ├─ Database interaction (queries, persistence)?
  │   └─ → Write an INTEGRATION TEST (with Testcontainers)
  │
  ├─ REST API endpoint?
  │   ├─ Testing controller logic? → UNIT TEST (MockMvc)
  │   ├─ Testing request/response format? → CONTRACT TEST
  │   └─ Testing with real DB? → INTEGRATION TEST
  │
  ├─ API consumed by another service?
  │   └─ → Write a CONTRACT TEST
  │
  ├─ Cross-service workflow?
  │   └─ → Write an E2E TEST (sparingly)
  │
  └─ Configuration or framework wiring?
      └─ → Write an INTEGRATION TEST
```

## Detailed Guidelines by Tier

### When to Write Unit Tests

**Always write unit tests for:**

1. **Service layer methods** — Business logic, validation, state transitions
   ```java
   // Example: Order state machine
   @Test
   void shouldRejectApprovalForCancelledOrder() {
       Order order = OrderBuilder.aCancelledOrder().build();
       assertThatThrownBy(() -> orderService.approve(order))
           .isInstanceOf(InvalidOrderStateException.class);
   }
   ```

2. **Domain entity behavior** — Constructors, business methods, invariants
   ```java
   @Test
   void shouldCalculateOrderTotal() {
       Order order = new Order(lineItems);
       assertThat(order.getTotal()).isEqualTo(new Money("29.99"));
   }
   ```

3. **Value objects** — Equality, validation, formatting
   ```java
   @Test
   void shouldFormatMoneyWithTwoDecimals() {
       Money money = new Money("29.9");
       assertThat(money.asString()).isEqualTo("29.90");
   }
   ```

4. **Mappers and converters** — DTO-to-entity mapping
   ```java
   @Test
   void shouldMapCreateOrderRequestToOrder() {
       CreateOrderRequest request = testRequest();
       Order order = orderMapper.toEntity(request);
       assertThat(order.getConsumerId()).isEqualTo(request.getConsumerId());
   }
   ```

**Do NOT write unit tests for:**
- Simple getters/setters without logic
- Framework configuration classes
- Constant definitions
- Trivial delegation methods

### When to Write Integration Tests

**Always write integration tests for:**

1. **Repository queries** — Custom JPQL, native queries, specifications
   ```java
   @Test
   void shouldFindOrdersByConsumerId() {
       orderRepository.save(orderForConsumer(42L));
       List<Order> orders = orderRepository.findByConsumerId(42L);
       assertThat(orders).hasSize(1);
   }
   ```

2. **Database migrations** — Verify Flyway scripts apply correctly
   ```java
   @Test
   void shouldApplyAllMigrations() {
       // Spring Boot auto-applies Flyway on startup
       // If we get here, all migrations passed
       assertThat(orderRepository.count()).isGreaterThanOrEqualTo(0);
   }
   ```

3. **Transaction boundaries** — Rollback behavior, isolation levels
   ```java
   @Test
   void shouldRollbackOnException() {
       assertThatThrownBy(() -> orderService.createWithBadData());
       assertThat(orderRepository.count()).isZero();
   }
   ```

4. **Spring configuration** — Bean wiring, profiles, conditional beans
   ```java
   @Test
   void shouldLoadSecurityConfiguration() {
       assertThat(securityFilterChain).isNotNull();
       assertThat(corsConfigurationSource).isNotNull();
   }
   ```

### When to Write Contract Tests

**Always write contract tests for:**

1. **APIs consumed by other FTGO services** — The producer must guarantee its contract
   ```
   Example: Order Service exposes GET /api/orders/{id}
   Consumer Service calls this API to check order status
   → Write a contract test on the Order Service (producer)
   → Write a stub-based test on the Consumer Service (consumer)
   ```

2. **Breaking API changes** — Adding/removing fields, changing status codes
   ```
   If you rename a JSON field from "orderId" to "id",
   the contract test will fail, preventing a breaking change.
   ```

3. **New API endpoints** — Define the contract before or alongside implementation

**Do NOT write contract tests for:**
- Internal-only endpoints (e.g., actuator health checks)
- APIs that only have external (non-FTGO) consumers
- APIs that are not yet consumed by any service

### When to Write E2E Tests

**Only write E2E tests for:**

1. **Critical business flows** that span multiple services
   - Place Order → Approve → Assign Courier → Deliver
   - Consumer Registration → First Order
   - Restaurant Onboarding → Menu Setup → Receive Order

2. **Smoke tests** for deployment verification
   - Can the system start and process a basic request?

**Do NOT write E2E tests for:**
- Individual service functionality (use unit/integration instead)
- Edge cases and error paths (test at lower tiers)
- Performance or load testing (use dedicated tools)

## Test Writing Checklist

Before writing any test, ask yourself:

1. **Is this already tested?** Check existing tests to avoid duplication.
2. **Am I testing at the right level?** Follow the decision matrix above.
3. **Am I testing behavior, not implementation?** Tests should verify WHAT, not HOW.
4. **Is this test deterministic?** No random data, no time-dependent logic.
5. **Is this test independent?** Tests should not depend on execution order.
6. **Am I using the test library?** Use `ftgo-test-lib` builders and assertions.

## Anti-Patterns to Avoid

### 1. Integration Test for Pure Logic
**Bad:** Using `@SpringBootTest` to test a calculation method.
**Good:** Use a unit test — it runs in milliseconds, not seconds.

### 2. E2E Test for Error Handling
**Bad:** Spinning up all services to test "order with invalid consumer ID."
**Good:** Unit test the service method with a mock repository.

### 3. No Tests at All
**Bad:** "It works on my machine."
**Good:** At minimum, write unit tests for business logic.

### 4. Testing Framework Behavior
**Bad:** Testing that `@Autowired` works or that JPA saves entities.
**Good:** Test YOUR code's behavior with those frameworks.

### 5. Brittle Assertions
**Bad:** Asserting exact JSON strings (breaks on field ordering changes).
**Good:** Assert specific fields: `body("state", equalTo("APPROVED"))`.

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────┐
│                 FTGO Test Selection Guide                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Business logic, domain rules    → UNIT TEST            │
│  Database queries, persistence   → INTEGRATION TEST     │
│  API format, inter-service API   → CONTRACT TEST        │
│  Full user journey               → E2E TEST             │
│                                                         │
│  When in doubt                   → UNIT TEST            │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  Base classes:                                          │
│    AbstractUnitTest          @Tag("unit")               │
│    AbstractIntegrationTest   @Tag("integration")        │
│    AbstractApiTest           @Tag("api")                │
│                                                         │
│  Library: testCompile project(':shared-ftgo-test-lib')  │
└─────────────────────────────────────────────────────────┘
```

## Related Documentation

- [Testing Strategy](testing-strategy.md)
- [JUnit 5 Migration Guide](junit5-migration-guide.md)
- [Testcontainers Guide](testcontainers-guide.md)
- [Automated Testing Pipeline](automated-testing-pipeline.md)
