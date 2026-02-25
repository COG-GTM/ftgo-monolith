# FTGO Platform - Testing Guidelines

> Version: 1.0
> Status: Approved
> Last Updated: 2024-03-01
> Applies To: All FTGO microservices

## When to Write Which Type of Test

This quick-reference guide helps developers decide which test tier to use for different scenarios.

## Decision Flowchart

```
Is the code under test...

├─ Pure business logic (no I/O)?
│  └─ → Write a UNIT TEST
│
├─ A Spring controller (request mapping, validation)?
│  └─ → Write a UNIT TEST with @WebMvcTest or MockMvc
│
├─ A JPA repository (queries, persistence)?
│  └─ → Write an INTEGRATION TEST with Testcontainers
│
├─ Spring configuration (bean wiring, properties)?
│  └─ → Write an INTEGRATION TEST with @SpringBootTest
│
├─ An API consumed by another service?
│  └─ → Write a CONTRACT TEST with Spring Cloud Contract
│
├─ A critical end-to-end business flow?
│  └─ → Write an E2E TEST (use sparingly)
│
├─ Security rules (auth, authorization)?
│  └─ → Write an INTEGRATION TEST with @SpringBootTest + MockMvc
│
└─ Anything else?
   └─ → Start with a UNIT TEST, escalate if needed
```

## Test Tier Quick Reference

### Unit Tests — The Default Choice

**Write a unit test when:**
- Testing domain entity behavior (state transitions, calculations, validation)
- Testing service orchestration logic (with mocked dependencies)
- Testing DTO/mapper transformations
- Testing controller request/response mapping (with MockMvc)
- Testing utility/helper classes
- Testing error handling and edge cases

**Do NOT unit test:**
- Simple getters/setters without logic
- Framework behavior (Spring auto-configuration, JPA cascades)
- Database query correctness (use integration tests)

**Example scenarios:**
| Scenario | Test Type |
|----------|-----------|
| Order total calculation | Unit test (domain) |
| Order state machine transitions | Unit test (domain) |
| OrderService.createOrder() orchestration | Unit test (service, mock repo) |
| OrderController GET /orders/{id} | Unit test (controller, MockMvc) |
| Money serialization/deserialization | Unit test (value object) |
| Input validation (blank name, negative amount) | Unit test (parameterized) |

### Integration Tests — Real Dependencies

**Write an integration test when:**
- Testing JPA repository custom queries
- Testing database migration correctness (Flyway)
- Testing Spring Security filter chain behavior
- Testing Spring configuration / auto-configuration
- Testing the full request→service→repository→database flow (sparingly)
- Testing Testcontainers-managed infrastructure

**Do NOT integration test:**
- Business logic that can be tested with unit tests
- Error handling that can be tested with mocks
- Every CRUD endpoint (one representative test is enough)

**Example scenarios:**
| Scenario | Test Type |
|----------|-----------|
| Custom JPQL query returns correct results | Integration test |
| Flyway migration creates expected schema | Integration test |
| JWT filter rejects expired tokens | Integration test |
| Full POST→GET lifecycle for an entity | Integration test (sparingly) |

### Contract Tests — Service-to-Service Agreements

**Write a contract test when:**
- Your service exposes a REST API consumed by other FTGO services
- Your service consumes a REST API from another FTGO service
- API request/response formats are changing
- You want to verify backward compatibility

**Do NOT contract test:**
- Internal-only APIs (not consumed by other services)
- Third-party external APIs (use WireMock instead)
- Business logic (contracts test format, not behavior)

**Example scenarios:**
| Scenario | Test Type |
|----------|-----------|
| Order service GET /orders/{id} response format | Contract test (provider) |
| Restaurant service client expects menu format | Contract test (consumer) |
| New field added to API response | Contract test (verify compatibility) |

### E2E Tests — Critical Paths Only

**Write an E2E test when:**
- Testing a critical business flow that spans multiple services
- Validating infrastructure integration (Docker, networking)
- Smoke-testing a deployment

**Do NOT E2E test:**
- Edge cases or error scenarios
- Individual service behavior
- Performance (use dedicated performance tests)

**Example scenarios:**
| Scenario | Test Type |
|----------|-----------|
| Complete order lifecycle (create → deliver) | E2E test |
| Consumer registration → first order | E2E test |
| Restaurant menu update propagation | E2E test |

## Bounded Context Test Examples

### Order Service

| Layer | Test | Tier | Location |
|-------|------|------|----------|
| Domain | Order state transitions | Unit | `src/test/java/.../domain/OrderTest.java` |
| Domain | Order total calculation | Unit | `src/test/java/.../domain/OrderTest.java` |
| Service | OrderService.createOrder | Unit (mock repo) | `src/test/java/.../domain/OrderServiceTest.java` |
| Web | GET /orders/{id} | Unit (MockMvc) | `src/test/java/.../web/OrderControllerTest.java` |
| Web | POST /orders validation | Unit (MockMvc) | `src/test/java/.../web/OrderControllerTest.java` |
| Repository | findByConsumerIdAndStatus | Integration | `src/integration-test/.../repository/OrderRepositoryIT.java` |
| API | Full CRUD lifecycle | Integration | `src/integration-test/.../api/OrderApiIT.java` |
| Contract | GET /orders/{id} response | Contract | `src/test/.../web/OrderContractBase.java` |
| E2E | Complete order flow | E2E | `ftgo-end-to-end-tests/` |

### Consumer Service

| Layer | Test | Tier | Location |
|-------|------|------|----------|
| Domain | Consumer creation/validation | Unit | `src/test/.../domain/ConsumerTest.java` |
| Domain | ConsumerService.register | Unit (mock repo) | `src/test/.../domain/ConsumerServiceTest.java` |
| Web | POST /consumers | Unit (MockMvc) | `src/test/.../web/ConsumerControllerTest.java` |
| Repository | findByEmail | Integration | `src/integration-test/.../repository/ConsumerRepositoryIT.java` |
| Contract | GET /consumers/{id} | Contract | `src/test/.../web/ConsumerContractBase.java` |

### Restaurant Service

| Layer | Test | Tier | Location |
|-------|------|------|----------|
| Domain | Restaurant menu management | Unit | `src/test/.../domain/RestaurantTest.java` |
| Domain | RestaurantService.create | Unit (mock repo) | `src/test/.../domain/RestaurantServiceTest.java` |
| Web | GET /restaurants/{id}/menu | Unit (MockMvc) | `src/test/.../web/RestaurantControllerTest.java` |
| Repository | findByNameContaining | Integration | `src/integration-test/.../repository/RestaurantRepositoryIT.java` |
| Contract | GET /restaurants/{id} | Contract | `src/test/.../web/RestaurantContractBase.java` |

### Courier Service

| Layer | Test | Tier | Location |
|-------|------|------|----------|
| Domain | Courier availability | Unit | `src/test/.../domain/CourierTest.java` |
| Domain | Delivery assignment logic | Unit | `src/test/.../domain/DeliveryServiceTest.java` |
| Repository | findAvailableCouriers | Integration | `src/integration-test/.../repository/CourierRepositoryIT.java` |
| Contract | GET /couriers/{id}/availability | Contract | `src/test/.../web/CourierContractBase.java` |

## Test Quality Checklist

Before submitting a PR, verify:

- [ ] Every new public method has at least one test
- [ ] Tests follow the naming convention: `should{Behavior}[When{Condition}]`
- [ ] Tests use `@DisplayName` for readability
- [ ] Tests use AssertJ for assertions (not JUnit Assert)
- [ ] Mocks are used only for external dependencies, not domain objects
- [ ] Test data uses builders, not raw constructors
- [ ] No test depends on execution order
- [ ] No test depends on external state (database, file system, network)
- [ ] Integration tests use Testcontainers, not external Docker Compose
- [ ] Coverage meets the 70% threshold

## Anti-Patterns to Avoid

| Anti-Pattern | Why It's Bad | What to Do Instead |
|-------------|-------------|-------------------|
| Testing implementation details | Breaks on refactoring | Test behavior/outcomes |
| One giant test method | Hard to understand failures | One concept per test |
| Shared mutable state between tests | Flaky, order-dependent tests | Fresh fixtures in @BeforeEach |
| Mocking domain objects | Hides bugs, tests become trivial | Use real domain objects via builders |
| Integration test for business logic | Slow feedback, unnecessary complexity | Use unit tests |
| No assertions | Test always passes | Every test must assert something |
| Catching exceptions to assert | Misses the exception type | Use assertThatThrownBy() |
| Magic numbers/strings | Unclear test intent | Use descriptive constants or builders |

## References

- [FTGO Testing Strategy](testing-strategy.md) — Full testing pyramid and framework details
- [FTGO JUnit 5 Migration Guide](junit5-migration-guide.md) — Step-by-step migration instructions
- [FTGO Testing Pipeline](testing-pipeline.md) — CI/CD pipeline configuration
- [FTGO API Contract Testing](api-contract-testing.md) — Contract testing strategy
