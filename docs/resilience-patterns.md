# FTGO Resilience Patterns

## Overview

The `ftgo-resilience-lib` shared library provides resilience patterns for inter-service communication in the FTGO microservices platform. It is built on [Resilience4j](https://resilience4j.readme.io/) and integrates with Spring Boot Actuator, Micrometer metrics, and Kubernetes health probes.

## Components

### 1. Circuit Breaker

Prevents cascade failures by stopping calls to failing downstream services.

**Default Configuration:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `failureRateThreshold` | 50% | Failure rate to trigger OPEN state |
| `slidingWindowSize` | 10 | Number of calls in the sliding window |
| `minimumNumberOfCalls` | 5 | Minimum calls before calculating failure rate |
| `waitDurationInOpenState` | 30s | Duration before transitioning to HALF_OPEN |
| `permittedNumberOfCallsInHalfOpenState` | 3 | Test calls in HALF_OPEN state |
| `automaticTransitionFromOpenToHalfOpenEnabled` | true | Auto-transition from OPEN to HALF_OPEN |

**State Machine:**
```
CLOSED → (failure rate exceeds threshold) → OPEN
OPEN → (wait duration expires) → HALF_OPEN
HALF_OPEN → (test calls succeed) → CLOSED
HALF_OPEN → (test calls fail) → OPEN
```

**Configuration:**
```properties
ftgo.resilience.circuit-breaker.failure-rate-threshold=50
ftgo.resilience.circuit-breaker.sliding-window-size=10
ftgo.resilience.circuit-breaker.wait-duration-in-open-state=30s
```

### 2. Retry with Exponential Backoff

Automatically retries failed operations with increasing delays.

**Default Configuration:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxAttempts` | 3 | Maximum retry attempts |
| `waitDuration` | 1s | Initial wait before first retry |
| `multiplier` | 2.0 | Exponential backoff multiplier |
| `exponentialBackoff` | true | Enable exponential backoff |

**Backoff Schedule:** 1s → 2s → 4s

**Configuration:**
```properties
ftgo.resilience.retry.max-attempts=3
ftgo.resilience.retry.wait-duration=1s
ftgo.resilience.retry.multiplier=2.0
```

### 3. Bulkhead

Limits concurrent calls to downstream services to prevent thread pool exhaustion.

**Default Configuration:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxConcurrentCalls` | 25 | Maximum concurrent calls per service |
| `maxWaitDuration` | 0s | Wait time for a permit (0 = fail immediately) |

**Configuration:**
```properties
ftgo.resilience.bulkhead.max-concurrent-calls=25
ftgo.resilience.bulkhead.max-wait-duration=0s
```

### 4. Rate Limiter

Controls the rate of outbound requests to downstream services.

**Default Configuration:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `limitForPeriod` | 50 | Permissions per refresh period |
| `limitRefreshPeriod` | 1s | Duration of one refresh period |
| `timeoutDuration` | 500ms | Max wait for a permission |

**Configuration:**
```properties
ftgo.resilience.rate-limiter.limit-for-period=50
ftgo.resilience.rate-limiter.limit-refresh-period=1s
ftgo.resilience.rate-limiter.timeout-duration=500ms
```

## Health Checks

### Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Overall health status |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |

### Health Indicator Groups

**Liveness Group** (`/actuator/health/liveness`):
- `livenessState` — JVM liveness
- `ping` — Basic connectivity

**Readiness Group** (`/actuator/health/readiness`):
- `readinessState` — Application readiness
- `db` — Database connectivity
- `diskSpace` — Disk space availability
- `downstreamServices` — Circuit breaker states of downstream services

### Downstream Service Health

The `downstreamServices` health indicator maps circuit breaker states to health:
- **CLOSED** → UP (healthy)
- **HALF_OPEN** → UP with warning
- **OPEN** → DOWN (unhealthy, triggers readiness failure)

## Kubernetes Integration

### Service Discovery (DNS-based)

Services discover each other via K8s DNS:
```
http://<service-name>.<namespace>.svc.cluster.local:<port>
```

**Configuration:**
```properties
ftgo.discovery.namespace=ftgo-dev
ftgo.discovery.cluster-domain=cluster.local
ftgo.discovery.services.order-service.host=ftgo-order-service
ftgo.discovery.services.order-service.port=8081
```

**Usage:**
```java
@Autowired
private ServiceResolver serviceResolver;

String url = serviceResolver.resolve("order-service");
// → http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081
```

### Probes

All services configure K8s probes:

```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: http
  initialDelaySeconds: 15
  failureThreshold: 20

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
  initialDelaySeconds: 60
  periodSeconds: 15

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
  initialDelaySeconds: 30
  periodSeconds: 10
```

### Graceful Shutdown

Zero-downtime deployments use a multi-step shutdown:

1. K8s sends SIGTERM to pod
2. `preStop` hook runs `sleep 5` (allows endpoint deregistration)
3. Spring Boot graceful shutdown stops accepting new requests
4. Active requests drain within 30s timeout
5. Pod terminates

**K8s Config:**
```yaml
terminationGracePeriodSeconds: 45
lifecycle:
  preStop:
    exec:
      command: ["sh", "-c", "sleep 5"]
```

**Spring Boot Config:**
```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

## Metrics

All resilience patterns expose metrics via Micrometer (available at `/actuator/prometheus`):

- **Circuit Breaker:** `resilience4j_circuitbreaker_*` (state, failure rate, calls)
- **Retry:** `resilience4j_retry_*` (attempt counts, success/failure rates)
- **Bulkhead:** `resilience4j_bulkhead_*` (available permits, active calls)
- **Rate Limiter:** `resilience4j_ratelimiter_*` (available permissions, waiting threads)

## Quick Start

### 1. Add Dependency

In your service's `build.gradle`:
```groovy
compile project(':shared-ftgo-resilience-lib')
```

### 2. Configuration

The library auto-configures with sensible defaults. Override in `application.properties` as needed.

### 3. Disable (if needed)

```properties
ftgo.resilience.enabled=false
```
