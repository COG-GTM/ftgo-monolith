# FTGO API Contract Testing Strategy

> Version: 1.0  
> Status: Approved  
> Last Updated: 2024-03-01  
> Applies To: All FTGO microservices

## Overview

API contract testing ensures that microservices can communicate reliably by verifying that API providers and consumers agree on request/response formats. This is critical in a microservices architecture where services are developed and deployed independently.

## Strategy: Consumer-Driven Contract Testing

FTGO uses **consumer-driven contract testing** (CDC) with [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) as the primary framework.

### Why Consumer-Driven Contracts?

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **Spring Cloud Contract** | Native Spring integration, Groovy/YAML DSL, stub generation | Spring-specific | **Selected** |
| Pact | Language-agnostic, Pact Broker | Extra infrastructure (broker) | Alternative |
| Manual integration tests | Simple | Slow, brittle, hard to maintain | Rejected |

---

## Architecture

```
┌─────────────────┐     Contract      ┌─────────────────┐
│  Consumer        │ ←─── Stubs ────→ │  Provider        │
│  (Order Service) │                   │  (Restaurant     │
│                  │     Verified      │   Service)       │
│  Uses generated  │     against       │  Runs contract   │
│  stubs for tests │     real API      │  verification    │
└─────────────────┘                    └─────────────────┘
```

### Contract Flow

1. **Consumer** defines the contract (expected request/response)
2. **Contract** is stored in the provider's API module (e.g., `shared/ftgo-restaurant-service-api/`)
3. **Provider** runs contract verification tests to ensure compliance
4. **Stubs** are generated and published for consumer-side testing

---

## Contract Location

Contracts are stored in the shared API modules under `shared/`:

```
shared/
  ftgo-order-service-api/
    src/
      contractTest/
        resources/
          contracts/
            orders/
              shouldReturnOrderById.groovy
              shouldCreateOrder.groovy
              shouldReturn404ForNonExistentOrder.groovy
  ftgo-restaurant-service-api/
    src/
      contractTest/
        resources/
          contracts/
            restaurants/
              shouldReturnRestaurantById.groovy
              shouldReturnRestaurantMenu.groovy
```

---

## Contract Format

### Groovy DSL Example

```groovy
// shouldReturnOrderById.groovy
Contract.make {
    description "should return order by ID"
    
    request {
        method GET()
        url "/api/v1/orders/123"
        headers {
            accept(applicationJson())
        }
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            status: "success",
            data: [
                orderId: 123,
                status: "PENDING",
                totalAmount: [
                    amount: "29.99"
                ],
                createdAt: $(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z'))
            ],
            timestamp: $(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]+)?Z')),
            path: "/api/v1/orders/123"
        ])
    }
}
```

### Error Contract Example

```groovy
// shouldReturn404ForNonExistentOrder.groovy
Contract.make {
    description "should return 404 for non-existent order"
    
    request {
        method GET()
        url "/api/v1/orders/999999"
        headers {
            accept(applicationJson())
        }
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body([
            status: "error",
            error: [
                code: "RESOURCE_NOT_FOUND",
                message: $(regex('.+'))
            ],
            timestamp: $(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]+)?Z')),
            path: "/api/v1/orders/999999"
        ])
    }
}
```

---

## Build Configuration

### Provider Side (Service)

```groovy
// services/ftgo-order-service/build.gradle
plugins {
    id 'org.springframework.cloud.contract' version '4.1.0'
}

contracts {
    testFramework = TestFramework.JUNIT5
    baseClassForTests = 'com.ftgo.order.contract.BaseContractTest'
    contractsPath = "contracts"
}

dependencies {
    testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
}
```

### Consumer Side (Dependent Service)

```groovy
// services/ftgo-restaurant-service/build.gradle (if it consumes order API)
dependencies {
    testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-stub-runner'
}
```

### Consumer Test Example

```java
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.ftgo:ftgo-order-service-api:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class OrderServiceClientTest {

    @Test
    void shouldGetOrderById() {
        // Given stubs are running on port 8090
        
        // When calling the order service
        ResponseEntity<ApiResponse<OrderDTO>> response = restTemplate
            .getForEntity("http://localhost:8090/api/v1/orders/123", ...);
        
        // Then the response matches the contract
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("success");
    }
}
```

---

## Contract Verification in CI/CD

### Pipeline Steps

```yaml
# In CI pipeline for each service
steps:
  - name: Run unit tests
    run: ./gradlew test

  - name: Run contract verification
    run: ./gradlew contractTest

  - name: Publish stubs
    run: ./gradlew publishStubsToScm
    if: branch == 'main'
```

### Failure Handling

| Scenario | Action |
|----------|--------|
| Provider breaks contract | CI fails, provider must fix before merge |
| Consumer needs new contract | Consumer creates PR to provider's API module |
| Contract needs updating | Both provider and consumer teams coordinate |

---

## Best Practices

1. **Contracts live with the API definition** - Store contracts in `shared/ftgo-*-service-api/` modules
2. **Consumer writes first** - The consuming team proposes the contract
3. **Keep contracts minimal** - Test the contract, not the business logic
4. **Use regex for dynamic values** - Timestamps, IDs, etc. should use patterns
5. **Follow the standard envelope** - All contracts must use `ApiResponse`/`ApiErrorResponse` format
6. **Version contracts with the API** - Contracts in `v1/` and `v2/` subdirectories
7. **Run in CI** - Contract tests run on every PR and merge to main

---

## References

- [FTGO REST API Standards](api-standards.md)
- [Spring Cloud Contract Documentation](https://docs.spring.io/spring-cloud-contract/reference/)
- [Consumer-Driven Contracts (Martin Fowler)](https://martinfowler.com/articles/consumerDrivenContracts.html)
- [Pact Documentation](https://docs.pact.io/) (alternative framework)
