# FTGO Distributed Tracing Architecture

## Overview

FTGO uses **Micrometer Tracing** with the **Brave bridge** and **Zipkin** as the trace collector to provide distributed tracing across all microservices. This enables end-to-end request tracking, latency analysis, and service dependency visualization.

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   API       │────▶│   Order     │────▶│  Consumer   │
│   Gateway   │     │   Service   │     │  Service    │
│             │     │             │     │             │
│ traceId: abc│     │ traceId: abc│     │ traceId: abc│
│ spanId: 001 │     │ spanId: 002 │     │ spanId: 003 │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                    │
       │    B3 Headers     │    B3 Headers      │
       │  (X-B3-TraceId,   │  (X-B3-TraceId,   │
       │   X-B3-SpanId)    │   X-B3-SpanId)    │
       │                   │                    │
       ▼                   ▼                    ▼
┌─────────────────────────────────────────────────────┐
│                    Zipkin Server                     │
│              http://zipkin:9411                      │
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Collector│  │ Storage  │  │ Query & UI       │  │
│  │ (HTTP)   │  │ (Memory/ │  │ (Port 9411)      │  │
│  │          │  │  ES)     │  │                   │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Components

### Shared Tracing Library (`shared/ftgo-tracing/`)

The shared library provides:

- **`FtgoTracingAutoConfiguration`** - Spring Boot auto-configuration that registers tracing beans
- **`FtgoTracingProperties`** - Configuration properties under the `ftgo.tracing.*` prefix
- **`TracingContextPropagator`** - Utilities for trace context propagation across service boundaries
- **`BusinessSpanCreator`** - Standard span naming conventions for FTGO business operations
- **`TracingConstants`** - Shared constants for MDC keys, headers, and defaults

### Convention Plugin (`build-logic/ftgo.tracing-conventions.gradle`)

Gradle convention plugin that adds tracing dependencies to any service:
- `micrometer-tracing-bridge-brave`
- `zipkin-reporter-brave`
- `zipkin-sender-urlconnection`

## Trace Propagation

### B3 Propagation (Default)

FTGO uses **B3 propagation** format, which is the standard for Brave/Zipkin:

| Header | Purpose |
|--------|---------|
| `X-B3-TraceId` | 128-bit trace identifier |
| `X-B3-SpanId` | 64-bit span identifier |
| `X-B3-ParentSpanId` | 64-bit parent span identifier |
| `X-B3-Sampled` | Sampling decision (1 = sampled) |
| `b3` | Single-header format (compact) |

### W3C Trace Context (Alternative)

Can be configured by setting `ftgo.tracing.propagation-type=W3C`:

| Header | Purpose |
|--------|---------|
| `traceparent` | Trace ID, span ID, flags |
| `tracestate` | Vendor-specific data |

## Configuration

### Application Properties

```yaml
# Enable/disable tracing
ftgo.tracing.enabled: true

# Sampling probability (0.0 to 1.0)
# 1.0 = 100% sampling (development)
# 0.1 = 10% sampling (production)
ftgo.tracing.sampling-probability: 1.0

# Zipkin collector endpoint
ftgo.tracing.zipkin-endpoint: http://zipkin:9411/api/v2/spans

# Propagation format: B3 or W3C
ftgo.tracing.propagation-type: B3
```

### Spring Boot Actuator Integration

Micrometer Tracing integrates with Spring Boot Actuator. The following management properties are set by the tracing defaults:

```yaml
management:
  tracing:
    sampling:
      probability: ${ftgo.tracing.sampling-probability:1.0}
    propagation:
      type: B3
  zipkin:
    tracing:
      endpoint: ${ftgo.tracing.zipkin-endpoint:http://localhost:9411/api/v2/spans}
```

### Sampling Strategy

| Environment | Probability | Rationale |
|------------|-------------|-----------|
| Development | 1.0 (100%) | Full visibility for debugging |
| Staging | 0.5 (50%) | Balance between visibility and overhead |
| Production | 0.1 (10%) | Minimize performance impact |

## Log Integration

Trace context is automatically included in all log lines via the SLF4J MDC:

### Structured JSON (Production)

```json
{
  "@timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "logger": "com.ftgo.order.OrderService",
  "message": "Order created successfully",
  "service": "order-service",
  "traceId": "a1b2c3d4e5f67890a1b2c3d4e5f67890",
  "spanId": "1234567890abcdef",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Plain Text (Development)

```
2024-01-15 10:30:00.000  INFO [order-service,a1b2c3d4e5f67890,1234567890abcdef] OrderService - Order created
```

## Custom Spans for Business Operations

### Using @Observed Annotation (Recommended)

```java
@Observed(name = "ftgo.order.create",
          contextualName = "create-order",
          lowCardinalityKeyValues = {"order.type", "delivery"})
public Order createOrder(CreateOrderRequest request) {
    // Business logic - automatically traced
}
```

### Using ObservationRegistry Programmatically

```java
@Autowired
private ObservationRegistry observationRegistry;

@Autowired
private BusinessSpanCreator spanCreator;

public Order createOrder(CreateOrderRequest request) {
    return Observation.createNotStarted(spanCreator.orderSpanName("create"), observationRegistry)
        .lowCardinalityKeyValue("order.type", "delivery")
        .observe(() -> {
            // Business logic
            return doCreateOrder(request);
        });
}
```

### Standard Span Names

| Span Name | Service | Operation |
|-----------|---------|-----------|
| `ftgo.order.create` | Order Service | Create new order |
| `ftgo.order.accept` | Order Service | Accept order |
| `ftgo.order.cancel` | Order Service | Cancel order |
| `ftgo.order.deliver` | Order Service | Mark order delivered |
| `ftgo.consumer.validate` | Consumer Service | Validate consumer |
| `ftgo.consumer.register` | Consumer Service | Register consumer |
| `ftgo.restaurant.findAvailable` | Restaurant Service | Find restaurants |
| `ftgo.restaurant.acceptOrder` | Restaurant Service | Accept order |
| `ftgo.courier.assign` | Courier Service | Assign courier |
| `ftgo.courier.updateLocation` | Courier Service | Update location |
| `ftgo.gateway.route` | API Gateway | Route request |

## API Gateway Trace Propagation

The API Gateway is configured to propagate trace context to downstream services. Micrometer Tracing automatically instruments:

1. **Incoming requests** - Creates a root span for each request entering the gateway
2. **Outgoing requests** - Propagates trace headers (B3/W3C) to downstream services
3. **Service routing** - Creates child spans for each route decision

Services should configure their Zipkin endpoint to point to the same Zipkin instance:

```yaml
# In each service's application.yml
ftgo:
  tracing:
    zipkin-endpoint: http://zipkin:9411/api/v2/spans  # Docker Compose
    # zipkin-endpoint: http://zipkin.ftgo-tracing:9411/api/v2/spans  # Kubernetes
```

## Infrastructure

### Local Development (Docker Compose)

```bash
# Start Zipkin for local development
docker compose -f docker-compose.tracing.yml up -d

# Start with microservices
docker compose -f docker-compose.microservices.yml -f docker-compose.tracing.yml up -d

# Access Zipkin UI
open http://localhost:9411
```

### Kubernetes

```bash
# Deploy tracing infrastructure (dev)
kubectl apply -k infrastructure/kubernetes/overlays/dev/tracing/

# Deploy tracing infrastructure (production)
kubectl apply -k infrastructure/kubernetes/overlays/production/tracing/

# Access Zipkin UI (port-forward)
kubectl port-forward -n ftgo-tracing svc/zipkin 9411:9411
open http://localhost:9411
```

### Environment-Specific Configuration

| Component | Dev | Staging | Production |
|-----------|-----|---------|------------|
| Storage | In-memory | In-memory | Elasticsearch |
| Replicas | 1 | 1 | 2 |
| Memory | 256Mi | 512Mi | 1Gi |
| CPU | 250m | 500m | 1000m |

## Troubleshooting

### Traces Not Appearing in Zipkin

1. Verify Zipkin is running: `curl http://localhost:9411/health`
2. Check service configuration: ensure `ftgo.tracing.zipkin-endpoint` is correct
3. Verify sampling: ensure `ftgo.tracing.sampling-probability` is > 0
4. Check network connectivity between service and Zipkin

### Missing Trace Context in Logs

1. Verify `micrometer-tracing-bridge-brave` is on the classpath
2. Check that the logging pattern includes `%X{traceId}` and `%X{spanId}`
3. Ensure the ftgo-tracing library auto-configuration is active

### High Latency from Tracing

1. Reduce sampling probability in production
2. Consider async reporting (default with Brave)
3. Monitor Zipkin server resource usage
