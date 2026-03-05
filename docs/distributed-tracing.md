# Distributed Tracing — FTGO Microservices

## Overview

FTGO uses **Micrometer Tracing** with the **Brave** bridge and **Zipkin** as the trace collector to provide distributed tracing across all microservices. This replaces the deprecated Spring Cloud Sleuth configuration and works with Spring Boot 3.x.

### Architecture

```
┌──────────────┐   B3 Headers    ┌──────────────┐
│ Order Service │ ──────────────► │Consumer Svc  │
│ (Brave)       │                 │ (Brave)       │
└──────┬───────┘                 └──────┬───────┘
       │ spans                          │ spans
       ▼                                ▼
┌──────────────────────────────────────────────┐
│              Zipkin Collector                  │
│         http://zipkin:9411/api/v2/spans       │
├──────────────────────────────────────────────┤
│              Zipkin UI                        │
│           http://localhost:9411               │
└──────────────────────────────────────────────┘
```

## Shared Library: `ftgo-tracing-lib`

Located at `shared/ftgo-tracing-lib/`, this library provides auto-configuration that is activated simply by adding it as a dependency.

### Key Components

| Class | Description |
|-------|-------------|
| `FtgoTracingAutoConfiguration` | Configures Brave tracing with Micrometer bridge, B3 propagation, Zipkin reporter, and MDC-based log correlation |
| `FtgoTracingProperties` | Configuration properties under `ftgo.tracing.*` |
| `FtgoTracingLoggingConfiguration` | Documents the recommended logging pattern for trace context |
| `OrderTracingOperations` | Pre-defined custom spans for order business operations |
| `DeliveryTracingOperations` | Pre-defined custom spans for delivery/courier operations |
| `FtgoSpanUtils` | Utility class for wrapping arbitrary operations in custom spans |

### Adding to a Service

Add the dependency to the service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-ftgo-tracing-lib')
}
```

The auto-configuration activates automatically via Spring Boot's `AutoConfiguration.imports`.

## Configuration

### Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.tracing.enabled` | `true` | Enable/disable tracing |
| `ftgo.tracing.sampling-probability` | `1.0` | Sampling rate (0.0–1.0) |
| `ftgo.tracing.zipkin-endpoint` | `http://localhost:9411/api/v2/spans` | Zipkin collector URL |
| `ftgo.tracing.propagation-type` | `B3` | Trace propagation format |
| `ftgo.tracing.service-name` | `${spring.application.name}` | Service name in traces |

### Sampling Strategy

| Environment | Sampling Rate | Rationale |
|-------------|---------------|-----------|
| Development | 1.0 (100%) | Trace every request for debugging |
| Staging | 0.5 (50%) | Balance visibility and overhead |
| Production | 0.1 (10%) | Minimize performance impact |

### Example `application.yml`

```yaml
spring:
  application:
    name: ftgo-order-service

ftgo:
  tracing:
    enabled: true
    sampling-probability: 1.0
    zipkin-endpoint: http://zipkin:9411/api/v2/spans

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

### Environment-Specific Overrides

**Development** (via environment variable or Spring profile):
```yaml
ftgo.tracing.sampling-probability: 1.0
```

**Production**:
```yaml
ftgo.tracing.sampling-probability: 0.1
```

## Trace Context in Logs

When tracing is active, the Brave MDCScopeDecorator injects `traceId` and `spanId` into the SLF4J MDC. Configure the logging pattern to include these:

```
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

Example log output:
```
2024-03-15 10:30:45.123  INFO [ftgo-order-service,64a8f2bc1e3d4a5b,7c2e1f3a4b5d6e7f] OrderController - Creating order for consumer 123
2024-03-15 10:30:45.234 DEBUG [ftgo-order-service,64a8f2bc1e3d4a5b,8d3f2a4b5c6e7f8a] OrderService - Validating order items
```

The `traceId` (`64a8f2bc1e3d4a5b`) is consistent across all services handling the same request, while `spanId` changes for each operation.

## Custom Spans

### Using Pre-defined Operations

```java
@RestController
public class OrderController {

    private final OrderTracingOperations orderTracing;

    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        Span span = orderTracing.startOrderCreation();
        try {
            Order order = orderService.create(request);
            span.tag("order.id", order.getId());
            return order;
        } catch (Exception e) {
            orderTracing.endSpanWithError(span, e);
            throw e;
        } finally {
            orderTracing.endSpan(span);
        }
    }
}
```

### Using FtgoSpanUtils

```java
// Wrap a void operation
FtgoSpanUtils.executeInSpan(tracer, "order.validate", () -> {
    validator.validate(order);
});

// Wrap a returning operation with tags
Order result = FtgoSpanUtils.executeInSpan(tracer, "order.create",
    "consumer.id", consumerId, () -> {
        return orderService.createOrder(request);
    });
```

## Infrastructure

### Docker Compose (Local Development)

Start the tracing stack:

```bash
# Tracing only
docker-compose -f docker-compose-tracing.yml up -d

# Full stack (app + metrics + tracing)
docker-compose -f docker-compose.yml \
  -f docker-compose-metrics.yml \
  -f docker-compose-tracing.yml up -d
```

Access Zipkin UI at: **http://localhost:9411**

### Kubernetes

Deploy Zipkin and tracing configuration:

```bash
# Deploy Zipkin
kubectl apply -f deploy/zipkin/deployment.yml

# Apply tracing ConfigMap
kubectl apply -f deploy/zipkin/configmap.yml

# Access Zipkin UI
kubectl port-forward svc/zipkin 9411:9411
```

Environment-specific ConfigMaps provide different sampling rates:
- `ftgo-tracing-config-dev` — 100% sampling
- `ftgo-tracing-config-prod` — 10% sampling

### Service Environment Variables

For Docker Compose or Kubernetes, add these environment variables to FTGO services:

```yaml
FTGO_TRACING_ENABLED: "true"
FTGO_TRACING_ZIPKIN_ENDPOINT: "http://zipkin:9411/api/v2/spans"
FTGO_TRACING_SAMPLING_PROBABILITY: "1.0"  # or "0.1" for production
```

## Trace Propagation

Traces propagate across service boundaries using **B3 multi-header** format:

| Header | Description |
|--------|-------------|
| `X-B3-TraceId` | 128-bit trace identifier |
| `X-B3-SpanId` | 64-bit span identifier |
| `X-B3-ParentSpanId` | Parent span identifier |
| `X-B3-Sampled` | Sampling decision (1 = sampled) |

These headers are automatically injected into outgoing HTTP requests and extracted from incoming requests when using Spring's `RestTemplate` or `WebClient` with tracing instrumentation.

## Migration from Spring Cloud Sleuth

This implementation replaces the dead Sleuth configuration in `docker-compose.yml`:

| Old (Sleuth — dead config) | New (Micrometer Tracing) |
|---------------------------|--------------------------|
| `SPRING_SLEUTH_ENABLED` | `FTGO_TRACING_ENABLED` |
| `SPRING_SLEUTH_SAMPLER_PROBABILITY` | `FTGO_TRACING_SAMPLING_PROBABILITY` |
| `SPRING_ZIPKIN_BASE_URL` | `FTGO_TRACING_ZIPKIN_ENDPOINT` |

The old Sleuth environment variables in `docker-compose.yml` can be removed as part of the monolith decomposition.

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| `io.micrometer:micrometer-tracing` | 1.2.5 | Tracing API abstraction |
| `io.micrometer:micrometer-tracing-bridge-brave` | 1.2.5 | Brave integration bridge |
| `io.zipkin.reporter2:zipkin-reporter-brave` | 3.3.0 | Zipkin span reporter |
| `io.zipkin.reporter2:zipkin-sender-urlconnection` | 3.3.0 | HTTP span sender |
