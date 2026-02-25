# FTGO Resilience, Health Checks & Service Discovery

## Overview

The `ftgo-resilience-lib` provides comprehensive health checks, service discovery abstractions, and resilience patterns for all FTGO microservices. It is located at `shared/ftgo-resilience-lib`.

## Quick Start

### 1. Add Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    compile project(":shared:ftgo-resilience-lib")
}
```

### 2. Configuration

The library auto-configures with sensible defaults. Customize in `application.properties`:

```properties
# Enable/disable resilience (default: true)
ftgo.resilience.enabled=true

# Health probes (K8s)
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true

# Graceful shutdown
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

## Health Checks

### Endpoints

| Endpoint | Purpose | K8s Probe |
|----------|---------|-----------|
| `/actuator/health` | Overall health | - |
| `/actuator/health/readiness` | Readiness probe | `readinessProbe` |
| `/actuator/health/liveness` | Liveness probe | `livenessProbe` |

### Health Indicators

- **ServiceHealthIndicator**: Reports service status and circuit breaker states
- **DownstreamServiceHealthIndicator**: Checks connectivity to dependent services
- **CircuitBreakerHealthIndicator**: Reports detailed circuit breaker metrics
- **DataSource** (Spring Boot auto): Database connectivity
- **DiskSpace** (Spring Boot auto): Available disk space

### Readiness vs Liveness

- **Readiness**: Includes DB, disk space, downstream services. Failing readiness removes the pod from the Service's endpoints (no traffic).
- **Liveness**: Basic application health only. Failing liveness causes the pod to be restarted.

### Registering Downstream Services

```java
@Autowired
private DownstreamServiceHealthIndicator healthIndicator;

@PostConstruct
public void registerDependencies() {
    healthIndicator.registerDownstreamService(
        "consumer-service",
        "http://ftgo-consumer-service.ftgo.svc.cluster.local:8082/actuator/health");
}
```

## Service Discovery

### Kubernetes DNS-Based Discovery

Services discover each other via K8s DNS names:

```
{service-name}.{namespace}.svc.cluster.local
```

Examples:
- `ftgo-order-service.ftgo.svc.cluster.local:8081`
- `ftgo-consumer-service.ftgo.svc.cluster.local:8082`
- `ftgo-restaurant-service.ftgo.svc.cluster.local:8083`
- `ftgo-courier-service.ftgo.svc.cluster.local:8084`

### Using ServiceEndpointResolver

```java
@Autowired
private ServiceEndpointResolver resolver;

public void callOrderService() {
    String url = resolver.resolveUrl("order-service", "/api/orders");
    // url = http://ftgo-order-service.ftgo.svc.cluster.local:8081/api/orders
}
```

### Local Development Override

```java
@Autowired
private KubernetesServiceRegistry registry;

@PostConstruct
public void configureLocalDev() {
    registry.setOverrideUrl("order-service", "http://localhost:8081");
}
```

### Configuration

```properties
ftgo.resilience.discovery.namespace=ftgo
ftgo.resilience.discovery.cluster-domain=svc.cluster.local
```

## Resilience Patterns

### Circuit Breaker

Opens after 5 consecutive failures, half-opens after 30 seconds.

```java
@CircuitBreaker(name = "consumer-service", fallbackMethod = "fallback")
public Consumer validateConsumer(long consumerId) {
    // Call consumer service
}

public Consumer fallback(long consumerId, Exception e) {
    // Fallback logic
}
```

**Configuration:**

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.circuit-breaker.failure-rate-threshold` | 50 | Failure rate % to open |
| `ftgo.resilience.circuit-breaker.minimum-number-of-calls` | 5 | Min calls before evaluation |
| `ftgo.resilience.circuit-breaker.wait-duration-in-open-state-seconds` | 30 | Wait before half-open |
| `ftgo.resilience.circuit-breaker.sliding-window-size` | 10 | Window size |
| `ftgo.resilience.circuit-breaker.permitted-number-of-calls-in-half-open-state` | 3 | Calls in half-open |

### Retry

3 attempts with exponential backoff (1s, 2s, 4s).

```java
@Retry(name = "consumer-service", fallbackMethod = "fallback")
public Consumer getConsumer(long consumerId) {
    // Call consumer service
}
```

**Configuration:**

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.retry.max-attempts` | 3 | Max retry attempts |
| `ftgo.resilience.retry.initial-interval-millis` | 1000 | Initial backoff (1s) |
| `ftgo.resilience.retry.multiplier` | 2.0 | Backoff multiplier |
| `ftgo.resilience.retry.max-interval-millis` | 8000 | Max backoff (8s) |

### Bulkhead

Limits concurrent calls to each downstream service.

```java
@Bulkhead(name = "consumer-service", fallbackMethod = "fallback")
public Consumer getConsumer(long consumerId) {
    // Call consumer service
}
```

**Configuration:**

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.bulkhead.max-concurrent-calls` | 25 | Max concurrent calls |
| `ftgo.resilience.bulkhead.max-wait-duration-millis` | 500 | Max wait for permit |

### Rate Limiter

Protects against cascading failures.

```java
@RateLimiter(name = "consumer-service", fallbackMethod = "fallback")
public Consumer getConsumer(long consumerId) {
    // Call consumer service
}
```

**Configuration:**

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.resilience.rate-limiter.limit-for-period` | 50 | Calls per period |
| `ftgo.resilience.rate-limiter.limit-refresh-period-millis` | 1000 | Refresh period |
| `ftgo.resilience.rate-limiter.timeout-duration-millis` | 500 | Timeout for permit |

### Combining Patterns

```java
@CircuitBreaker(name = "consumer-service", fallbackMethod = "fallback")
@Retry(name = "consumer-service")
@Bulkhead(name = "consumer-service")
@RateLimiter(name = "consumer-service")
public Consumer getConsumer(long consumerId) {
    // Retry -> CircuitBreaker -> RateLimiter -> Bulkhead -> actual call
}
```

## Graceful Shutdown

Zero-downtime deployments with:

1. **Spring Boot**: `server.shutdown=graceful` drains in-flight requests
2. **K8s preStop hook**: `sleep 10` allows load balancer to deregister pod
3. **terminationGracePeriodSeconds**: 45s total budget

### K8s Deployment Configuration

```yaml
containers:
  - name: my-service
    lifecycle:
      preStop:
        exec:
          command: ["sh", "-c", "sleep 10"]
spec:
  terminationGracePeriodSeconds: 45
```

## Metrics

Resilience4j metrics are automatically exposed via Micrometer at `/actuator/prometheus`:

- `resilience4j_circuitbreaker_*` - Circuit breaker state, failure rate, calls
- `resilience4j_retry_*` - Retry attempts, successes, failures
- `resilience4j_bulkhead_*` - Available permits, concurrent calls
- `resilience4j_ratelimiter_*` - Available permissions, waiting threads

## Kubernetes Configuration

### Probes

All services use:
- **Readiness**: `/actuator/health/readiness` (30s initial delay, 10s period)
- **Liveness**: `/actuator/health/liveness` (60s initial delay, 15s period)

### Service Discovery

Services use K8s ClusterIP services with DNS:
```
ftgo-{service-name}.ftgo.svc.cluster.local:{port}
```

## Architecture

```
shared/ftgo-resilience-lib/
  src/main/java/com/ftgo/common/resilience/
    config/           - Auto-configuration and properties
    circuitbreaker/   - Circuit breaker registry and config
    retry/            - Retry registry and config
    bulkhead/         - Bulkhead registry and config
    ratelimiter/      - Rate limiter registry and config
    health/           - Health indicators and probe config
    discovery/        - K8s service registry and resolver
    shutdown/         - Graceful shutdown handler
    metrics/          - Micrometer metrics binding
```
