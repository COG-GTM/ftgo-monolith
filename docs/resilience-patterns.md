# FTGO Resilience Patterns Guide

## Overview

This document describes the resilience patterns, health checks, service discovery, and graceful shutdown mechanisms implemented across FTGO microservices. These patterns ensure reliable inter-service communication and zero-downtime deployments.

## Architecture

```
                    ┌─────────────────────────────────────────┐
                    │           API Gateway                     │
                    │  ┌─────────┐ ┌─────────┐ ┌───────────┐ │
                    │  │Circuit  │ │Rate     │ │Health     │ │
                    │  │Breaker  │ │Limiter  │ │Check      │ │
                    │  └────┬────┘ └────┬────┘ └─────┬─────┘ │
                    └───────┼───────────┼────────────┼────────┘
                            │           │            │
              ┌─────────────┼───────────┼────────────┼─────────────┐
              │             │           │            │             │
     ┌────────▼──────┐ ┌───▼────────┐ ┌▼──────────┐ ┌▼──────────┐
     │ Order Service │ │ Consumer   │ │Restaurant │ │ Courier   │
     │               │ │ Service    │ │ Service   │ │ Service   │
     │ ┌───────────┐ │ │            │ │           │ │           │
     │ │Resilience │ │ │ Resilience │ │Resilience │ │Resilience │
     │ │Patterns:  │ │ │ Patterns   │ │Patterns   │ │Patterns   │
     │ │- CB       │ │ │            │ │           │ │           │
     │ │- Retry    │ │ │            │ │           │ │           │
     │ │- Bulkhead │ │ │            │ │           │ │           │
     │ │- RateLim  │ │ │            │ │           │ │           │
     │ └───────────┘ │ │            │ │           │ │           │
     └───────────────┘ └────────────┘ └───────────┘ └───────────┘
              │             │            │             │
              └─────────────┴────────────┴─────────────┘
                            │
                     ┌──────▼──────┐
                     │   MySQL DB  │
                     └─────────────┘
```

## Shared Resilience Library (`shared/ftgo-resilience`)

The `ftgo-resilience` shared library provides a common resilience foundation for all FTGO microservices. It includes:

- **Health Indicators** - Custom health checks for databases, disk space, business logic, and dependent services
- **Resilience4j Configuration** - Default circuit breaker, retry, bulkhead, and rate limiter settings
- **Service Discovery** - Kubernetes DNS-based service discovery
- **Graceful Shutdown** - Connection draining for zero-downtime deployments
- **Micrometer Integration** - Resilience metrics exposed via Prometheus

### Adding to a Service

Add the dependency in the service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared:ftgo-resilience')
}
```

The library auto-configures via Spring Boot's `spring.factories` mechanism.

---

## 1. Health Checks

### Health Indicators

Each service exposes detailed health information via Spring Boot Actuator:

| Indicator | Name | Description | Probe Group |
|-----------|------|-------------|-------------|
| Database | `ftgoDatabase` | Validates DB connectivity with `SELECT 1` query | Readiness |
| Disk Space | `ftgoDiskSpace` | Checks free disk space >= 100 MB threshold | Liveness |
| Business Health | `ftgoServiceBusiness` | Extensible service-specific business checks | - |
| Dependent Services | `ftgoDependentService` | Checks downstream service availability via HTTP | Readiness |

### Health Endpoint Response

```json
GET /actuator/health
{
  "status": "UP",
  "components": {
    "ftgoDatabase": {
      "status": "UP",
      "details": {
        "database": "available",
        "validationQuery": "SELECT 1",
        "responseTimeMs": 3
      }
    },
    "ftgoDiskSpace": {
      "status": "UP",
      "details": {
        "total": "50.00 GB",
        "free": "35.20 GB",
        "threshold": "100.00 MB"
      }
    },
    "ftgoServiceBusiness": {
      "status": "UP",
      "details": {
        "description": "Business health checks passed"
      }
    },
    "ftgoDependentService": {
      "status": "UP",
      "details": {
        "order-service": "UP",
        "consumer-service": "UP"
      }
    }
  }
}
```

### Configuration

Health check behavior is configured via environment variables in Kubernetes ConfigMaps:

```yaml
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS: "always"
MANAGEMENT_ENDPOINT_HEALTH_SHOW_COMPONENTS: "always"
MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED: "true"
MANAGEMENT_HEALTH_LIVENESSSTATE_ENABLED: "true"
MANAGEMENT_HEALTH_READINESSSTATE_ENABLED: "true"
```

---

## 2. Kubernetes Probes

### Probe Configuration

All services use three types of Kubernetes probes:

| Probe | Path | Purpose | Timing |
|-------|------|---------|--------|
| **Startup** | `/actuator/health` | Allows slow startup without killing the pod | Initial: 10s, Period: 5s, Failures: 30 |
| **Liveness** | `/actuator/health/liveness` | Restarts the pod if it becomes unresponsive | Initial: 60s, Period: 15s, Failures: 3 |
| **Readiness** | `/actuator/health/readiness` | Removes pod from Service load balancing | Initial: 30s, Period: 10s, Failures: 3 |

### Probe Flow

```
Pod Start
    │
    ▼
┌─────────────────┐
│  Startup Probe   │ ◄── Checks /actuator/health every 5s
│  (up to 150s)    │     Allows JVM warmup and Spring context init
└────────┬────────┘
         │ Success
         ▼
┌─────────────────┐     ┌──────────────────┐
│ Liveness Probe   │     │ Readiness Probe   │
│ (every 15s)      │     │ (every 10s)       │
│                  │     │                   │
│ Checks:          │     │ Checks:           │
│ - JVM alive      │     │ - DB connectivity │
│ - Disk space     │     │ - Dependencies UP │
│                  │     │                   │
│ Failure → Restart│     │ Failure → Remove  │
│                  │     │   from Service    │
└─────────────────┘     └──────────────────┘
```

---

## 3. Circuit Breaker

### Configuration

The circuit breaker prevents cascading failures by stopping calls to unhealthy services.

| Parameter | Default Value | Description |
|-----------|--------------|-------------|
| `slidingWindowType` | `COUNT_BASED` | Uses call count (not time) for failure calculation |
| `slidingWindowSize` | `10` | Number of calls in the sliding window |
| `failureRateThreshold` | `50%` | Percentage of failures to trip the breaker |
| `minimumNumberOfCalls` | `5` | Minimum calls before calculating failure rate |
| `waitDurationInOpenState` | `30s` | Time to wait before transitioning to half-open |
| `permittedCallsInHalfOpen` | `3` | Calls allowed in half-open state to test recovery |
| `automaticTransition` | `true` | Auto-transition from open to half-open |

### State Machine

```
         5+ failures (≥50%)
    ┌──────────────────────┐
    │                      ▼
 CLOSED ──────────────► OPEN
    ▲                      │
    │                      │ 30s wait
    │                      ▼
    │              ┌──── HALF_OPEN
    │              │       │
    │   Success    │       │ Failure
    └──────────────┘       │
                           │
                    Back to OPEN
```

### Per-Service Instances

Each downstream service has its own circuit breaker instance:

```yaml
resilience4j.circuitbreaker.instances:
  order-service:
    base-config: default
  consumer-service:
    base-config: default
  restaurant-service:
    base-config: default
  courier-service:
    base-config: default
```

---

## 4. Retry with Exponential Backoff

### Configuration

| Parameter | Default Value | Description |
|-----------|--------------|-------------|
| `maxAttempts` | `3` | Total number of attempts (1 initial + 2 retries) |
| `waitDuration` | `1s` | Base wait time between retries |
| `exponentialBackoffMultiplier` | `2` | Multiplier for backoff (1s → 2s → 4s) |
| `retryExceptions` | `IOException, TimeoutException, ResourceAccessException` | Exceptions that trigger retry |

### Retry Timeline

```
Request ──► Attempt 1 ──► FAIL ──► Wait 1s ──► Attempt 2 ──► FAIL ──► Wait 2s ──► Attempt 3
                                                                                      │
                                                                              Success or Final Failure
```

---

## 5. Bulkhead

### Configuration

The bulkhead pattern limits concurrent calls to prevent resource exhaustion.

| Parameter | Default Value | Description |
|-----------|--------------|-------------|
| `maxConcurrentCalls` | `25` | Maximum concurrent calls per downstream service |
| `maxWaitDuration` | `500ms` | Maximum time to wait for permission to execute |

### Behavior

```
Incoming Requests
       │
       ▼
┌──────────────────┐
│    Bulkhead       │
│  (max 25 calls)   │
│                   │
│  Slots: [■■■■□□□] │ ◄── 4 of 25 in use
│                   │
│  If full:         │
│  Wait up to 500ms │
│  Then reject      │
└──────────────────┘
```

---

## 6. Rate Limiter

### Configuration

| Parameter | Default Value | Description |
|-----------|--------------|-------------|
| `limitForPeriod` | `50` | Maximum calls per refresh period |
| `limitRefreshPeriod` | `1s` | Period after which the limit resets |
| `timeoutDuration` | `500ms` | Maximum wait time for permission |

---

## 7. Service Discovery (Kubernetes DNS)

### How It Works

In Kubernetes, each Service object gets a DNS entry automatically:

```
{service-name}.{namespace}.svc.{cluster-domain}
```

### FTGO Service DNS Names

| Service | DNS Name | URL |
|---------|----------|-----|
| Order Service | `ftgo-order-service.ftgo.svc.cluster.local` | `http://ftgo-order-service.ftgo.svc.cluster.local:8080` |
| Consumer Service | `ftgo-consumer-service.ftgo.svc.cluster.local` | `http://ftgo-consumer-service.ftgo.svc.cluster.local:8080` |
| Restaurant Service | `ftgo-restaurant-service.ftgo.svc.cluster.local` | `http://ftgo-restaurant-service.ftgo.svc.cluster.local:8080` |
| Courier Service | `ftgo-courier-service.ftgo.svc.cluster.local` | `http://ftgo-courier-service.ftgo.svc.cluster.local:8080` |
| API Gateway | `ftgo-api-gateway.ftgo.svc.cluster.local` | `http://ftgo-api-gateway.ftgo.svc.cluster.local:8080` |

### Configuration

```yaml
ftgo:
  service-discovery:
    namespace: ftgo
    cluster-domain: cluster.local
    default-port: 8080
```

### Programmatic Access

```java
@Autowired
private KubernetesServiceDiscovery serviceDiscovery;

String orderServiceUrl = serviceDiscovery.getServiceUrl("ftgo-order-service");
// Returns: http://ftgo-order-service.ftgo.svc.cluster.local:8080
```

---

## 8. Graceful Shutdown

### Zero-Downtime Deployment Flow

```
1. K8s sends SIGTERM to pod
         │
         ▼
2. preStop hook: sleep 5s          ◄── Allows LB to deregister pod
         │
         ▼
3. Spring receives shutdown signal
         │
         ▼
4. Readiness probe → DOWN           ◄── Pod removed from Service endpoints
         │
         ▼
5. GracefulShutdownConfiguration
   waits for pre-wait period (5s)   ◄── Extra buffer for LB propagation
         │
         ▼
6. Spring drains active requests
   (timeout: 30s per phase)         ◄── In-flight requests complete
         │
         ▼
7. Application context closes
         │
         ▼
8. If not stopped after 60s:
   K8s sends SIGKILL                ◄── terminationGracePeriodSeconds
```

### Configuration

```yaml
# Spring Boot graceful shutdown
server.shutdown: graceful
spring.lifecycle.timeout-per-shutdown-phase: 30s

# Custom pre-wait for LB deregistration
ftgo.shutdown.pre-wait-seconds: 5

# Kubernetes
terminationGracePeriodSeconds: 60
lifecycle.preStop.exec.command: ["sh", "-c", "sleep 5"]
```

---

## 9. Environment-Specific Configuration

### Dev Environment

Relaxed thresholds for debugging and development:

| Setting | Value | Reason |
|---------|-------|--------|
| Circuit Breaker Failure Rate | 80% | More tolerant of failures |
| Wait Duration in Open State | 10s | Faster recovery for testing |
| Retry Max Attempts | 5 | More retries for unstable dev services |
| Bulkhead Max Concurrent | 50 | Higher limit for load testing |
| Rate Limiter Limit | 100/s | Higher throughput for testing |
| Health Details | `always` | Full visibility for debugging |

### Staging Environment

Production-like configuration for realistic testing:

| Setting | Value | Reason |
|---------|-------|--------|
| Circuit Breaker Failure Rate | 50% | Matches production |
| Wait Duration in Open State | 30s | Matches production |
| Retry Max Attempts | 3 | Matches production |
| Bulkhead Max Concurrent | 25 | Matches production |
| Rate Limiter Limit | 50/s | Matches production |
| Health Details | `always` | Full visibility for QA |

### Production Environment

Strict thresholds for reliability:

| Setting | Value | Reason |
|---------|-------|--------|
| Circuit Breaker Failure Rate | 50% | Standard reliability threshold |
| Wait Duration in Open State | 30s | Sufficient recovery time |
| Retry Max Attempts | 3 | Balanced retry strategy |
| Bulkhead Max Concurrent | 25 | Resource protection |
| Rate Limiter Limit | 50/s | Overload prevention |
| Health Details | `when_authorized` | Security: details only for authenticated users |

---

## 10. Micrometer Metrics

All resilience patterns automatically expose metrics via Micrometer/Prometheus:

### Circuit Breaker Metrics

```
resilience4j_circuitbreaker_state{name="order-service"} 0  # 0=CLOSED, 1=OPEN, 2=HALF_OPEN
resilience4j_circuitbreaker_calls_total{name="order-service", kind="successful"} 150
resilience4j_circuitbreaker_calls_total{name="order-service", kind="failed"} 3
resilience4j_circuitbreaker_failure_rate{name="order-service"} 2.0
```

### Retry Metrics

```
resilience4j_retry_calls_total{name="order-service", kind="successful_without_retry"} 140
resilience4j_retry_calls_total{name="order-service", kind="successful_with_retry"} 8
resilience4j_retry_calls_total{name="order-service", kind="failed_with_retry"} 2
```

### Bulkhead Metrics

```
resilience4j_bulkhead_available_concurrent_calls{name="order-service"} 21
resilience4j_bulkhead_max_allowed_concurrent_calls{name="order-service"} 25
```

### Rate Limiter Metrics

```
resilience4j_ratelimiter_available_permissions{name="order-service"} 45
resilience4j_ratelimiter_waiting_threads{name="order-service"} 0
```

---

## 11. File Structure

```
shared/ftgo-resilience/
├── build.gradle
└── src/main/
    ├── java/com/ftgo/resilience/
    │   ├── config/
    │   │   ├── ResilienceAutoConfiguration.java    # Auto-config entry point
    │   │   ├── ResilienceConfiguration.java        # CB, retry, bulkhead, rate limiter beans
    │   │   └── HealthIndicatorConfiguration.java   # Health indicator beans
    │   ├── health/
    │   │   ├── DatabaseHealthIndicator.java         # DB connectivity check
    │   │   ├── DiskSpaceHealthIndicator.java        # Disk space check
    │   │   ├── ServiceBusinessHealthIndicator.java  # Business health check
    │   │   └── DependentServiceHealthIndicator.java # Downstream service check
    │   ├── discovery/
    │   │   └── KubernetesServiceDiscovery.java      # K8s DNS service discovery
    │   └── shutdown/
    │       └── GracefulShutdownConfiguration.java   # Graceful shutdown handler
    └── resources/
        ├── META-INF/spring.factories                # Auto-configuration registration
        └── application-resilience.yml               # Default resilience properties

build-logic/src/main/groovy/
└── ftgo.resilience-conventions.gradle              # Convention plugin for services

infrastructure/kubernetes/
├── base/
│   ├── {service}/configmap.yml                     # Resilience env vars per service
│   └── {service}/deployment.yml                    # Probes + graceful shutdown
└── overlays/
    ├── dev/kustomization.yml                       # Relaxed resilience for dev
    ├── staging/kustomization.yml                   # Production-like resilience
    └── prod/kustomization.yml                      # Strict resilience for prod
```

## Related Documentation

- [API Gateway Guide](api-gateway.md) - Gateway-level circuit breakers and rate limiting
- [Logging Guide](logging-guide.md) - Structured logging for resilience events
- [Security Configuration](security-configuration.md) - Authentication for health endpoints
