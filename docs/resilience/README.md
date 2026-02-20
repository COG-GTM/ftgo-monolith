# FTGO Resilience Library

The `ftgo-resilience` library provides health checks, service discovery, and resilience patterns for FTGO microservices.

## Features

### Health Checks

Spring Boot Actuator health endpoints with custom health indicators:

- **Database Health**: Validates database connectivity, reports product name, version, and response time
- **Messaging Health**: Tracks messaging broker availability with programmatic status updates
- **External Service Health**: Monitors external service dependencies with per-service tracking
- **Circuit Breaker Health**: Reports circuit breaker states from the Resilience4j registry
- **Service Discovery Health**: Reports registered service instances and their health

#### Endpoints

| Endpoint | Description |
|---|---|
| `/actuator/health` | Aggregate health status with all indicators |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |

#### Configuration

```yaml
ftgo:
  resilience:
    health-check:
      enabled: true
      database-enabled: true
      messaging-enabled: true
      external-services-enabled: true
      timeout-ms: 5000
```

### Circuit Breaker (Resilience4j)

Prevents cascading failures by short-circuiting calls to failing services.

#### Default Configuration

| Property | Default | Description |
|---|---|---|
| `failure-rate-threshold` | 50% | Failure rate to open the circuit |
| `slow-call-rate-threshold` | 80% | Slow call rate to open the circuit |
| `slow-call-duration-threshold` | 2s | Duration threshold for slow calls |
| `sliding-window-size` | 10 | Number of calls in the sliding window |
| `minimum-number-of-calls` | 5 | Minimum calls before calculating failure rate |
| `wait-duration-in-open-state` | 30s | Time to wait before transitioning to half-open |
| `permitted-calls-in-half-open` | 3 | Calls allowed in half-open state |

#### Pre-configured Circuit Breakers

- `orderService` — Order service calls
- `restaurantService` — Restaurant service calls
- `consumerService` — Consumer service calls
- `courierService` — Courier service calls
- `externalPayment` — External payment gateway (stricter: 30% threshold, 20 window, 60s wait)

#### Configuration

```yaml
ftgo:
  resilience:
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      sliding-window-size: 10
      wait-duration-in-open-state-ms: 30000
```

### Retry Pattern

Automatically retries failed operations with exponential backoff.

#### Default Configuration

| Property | Default | Description |
|---|---|---|
| `max-attempts` | 3 | Maximum retry attempts |
| `wait-duration` | 500ms | Initial wait between retries |
| `multiplier` | 2.0 | Exponential backoff multiplier |

#### Retried Exceptions

- `IOException`
- `TimeoutException`
- `ResourceAccessException`

#### Ignored Exceptions

- `IllegalArgumentException`

#### Configuration

```yaml
ftgo:
  resilience:
    retry:
      enabled: true
      max-attempts: 3
      wait-duration-ms: 500
      multiplier: 2.0
```

### Bulkhead Pattern

Limits concurrent access to downstream services to prevent resource exhaustion.

#### Default Configuration

| Property | Default | Description |
|---|---|---|
| `max-concurrent-calls` | 25 | Maximum concurrent calls |
| `max-wait-duration` | 500ms | Maximum wait time for permission |

#### Configuration

```yaml
ftgo:
  resilience:
    bulkhead:
      enabled: true
      max-concurrent-calls: 25
      max-wait-duration-ms: 500
```

### Service Discovery

Kubernetes-native service discovery with a service registry.

#### Default Registered Services

- `order-service` (port 8080)
- `restaurant-service` (port 8080)
- `consumer-service` (port 8080)
- `courier-service` (port 8080)

#### Configuration

```yaml
ftgo:
  resilience:
    discovery:
      enabled: true
      type: kubernetes
```

## Kubernetes Integration

The library integrates with Kubernetes health probes configured in deployment manifests:

```yaml
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: 10
  periodSeconds: 5
  failureThreshold: 30

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
  periodSeconds: 5
  failureThreshold: 3
```

## Usage

### Adding the Dependency

```groovy
dependencies {
    implementation project(':libs:ftgo-resilience')
}
```

### Using Circuit Breaker

```java
@Autowired
@Qualifier("orderServiceCircuitBreaker")
private CircuitBreaker circuitBreaker;

public Order getOrder(String orderId) {
    return CircuitBreaker.decorateSupplier(circuitBreaker,
            () -> orderClient.getOrder(orderId)).get();
}
```

### Using Retry

```java
@Autowired
@Qualifier("orderServiceRetry")
private Retry retry;

public Order getOrder(String orderId) {
    return Retry.decorateSupplier(retry,
            () -> orderClient.getOrder(orderId)).get();
}
```

### Using Bulkhead

```java
@Autowired
@Qualifier("orderServiceBulkhead")
private Bulkhead bulkhead;

public Order getOrder(String orderId) {
    return Bulkhead.decorateSupplier(bulkhead,
            () -> orderClient.getOrder(orderId)).get();
}
```

### Combining Patterns

```java
Supplier<Order> decorated = Decorators.ofSupplier(
        () -> orderClient.getOrder(orderId))
    .withCircuitBreaker(circuitBreaker)
    .withBulkhead(bulkhead)
    .withRetry(retry)
    .decorate();
```

## Metrics

All resilience patterns publish metrics to Micrometer, available at `/actuator/prometheus`:

- `resilience4j_circuitbreaker_*` — Circuit breaker state, call counts, failure rates
- `resilience4j_retry_*` — Retry attempt counts, success/failure
- `resilience4j_bulkhead_*` — Concurrent call counts, available permissions
