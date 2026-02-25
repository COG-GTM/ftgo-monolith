# Distributed Tracing Guide

## Overview

The FTGO platform uses **Micrometer Tracing** with the **Brave** bridge and **Zipkin/Jaeger** collectors to provide distributed tracing across all microservices. This replaces the non-functional Spring Cloud Sleuth configuration that existed in the monolith.

### Migration from Sleuth

| Monolith (non-functional)         | Microservices (implemented)                |
|-----------------------------------|--------------------------------------------|
| `spring-cloud-starter-sleuth`     | `micrometer-tracing-bridge-brave`          |
| Zipkin env vars (dead config)     | `zipkin-reporter-brave` + working config   |
| No actual trace propagation       | B3/W3C header propagation across services  |
| No trace collector deployed       | Zipkin and Jaeger in Docker Compose        |

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌───────────────────┐
│  API Gateway    │────▶│  Order Service   │────▶│ Consumer Service  │
│  (propagates    │     │  (creates spans) │     │ (validates)       │
│   trace ctx)    │     └────────┬─────────┘     └───────────────────┘
└─────────────────┘              │
                                 │
                         ┌───────▼──────────┐     ┌───────────────────┐
                         │ Restaurant Svc   │     │ Courier Service   │
                         │ (checks avail.)  │     │ (assigns delivery)│
                         └──────────────────┘     └───────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Zipkin / Jaeger       │
                    │   (trace collector)     │
                    └─────────────────────────┘
```

## Getting Started

### 1. Add Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared:ftgo-tracing-lib")
}
```

### 2. Configure Application Properties

Minimal configuration (defaults work for local development):

```properties
spring.application.name=ftgo-order-service

# Tracing is enabled by default. To disable:
# ftgo.tracing.enabled=false
```

### 3. Start Trace Collector

```bash
# Start Zipkin (recommended for getting started)
docker-compose -f infrastructure/docker/docker-compose.tracing.yml up -d zipkin

# Or start Jaeger (more features, advanced querying)
docker-compose -f infrastructure/docker/docker-compose.tracing.yml up -d jaeger
```

### 4. View Traces

- **Zipkin UI**: http://localhost:9411
- **Jaeger UI**: http://localhost:16686

## Configuration Reference

### FTGO Tracing Properties

| Property                          | Default  | Description                                  |
|-----------------------------------|----------|----------------------------------------------|
| `ftgo.tracing.enabled`            | `true`   | Enable/disable tracing auto-configuration    |
| `ftgo.tracing.custom-spans-enabled` | `true` | Enable custom business spans                 |
| `ftgo.tracing.service-name`       | (auto)   | Override service name (uses spring.application.name) |

### Spring Boot Tracing Properties

| Property                                   | Default                                    | Description                        |
|--------------------------------------------|--------------------------------------------|------------------------------------|
| `management.tracing.sampling.probability`  | `1.0`                                      | Sampling rate (0.0 to 1.0)        |
| `management.tracing.propagation.type`      | `b3`                                       | Propagation format (b3, w3c)      |
| `management.zipkin.tracing.endpoint`       | `http://zipkin:9411/api/v2/spans`          | Zipkin collector endpoint          |

### Environment-Specific Sampling

```properties
# Development (application-dev.properties)
management.tracing.sampling.probability=1.0

# Staging (application-staging.properties)
management.tracing.sampling.probability=0.5

# Production (application-prod.properties)
management.tracing.sampling.probability=0.1
```

## Custom Business Spans

The tracing library provides pre-built span helpers for key business operations.

### Order Flow Spans

```java
@Autowired
private OrderTracingSpans orderSpans;

public Order createOrder(CreateOrderRequest request) {
    Span span = orderSpans.startOrderCreation(null);
    try (Tracer.SpanInScope ws = orderSpans.getTracer().withSpan(span)) {
        // Validate consumer
        Span consumerSpan = orderSpans.startConsumerValidation(request.getConsumerId());
        try (Tracer.SpanInScope cs = orderSpans.getTracer().withSpan(consumerSpan)) {
            consumerService.validateConsumer(request.getConsumerId());
        } finally {
            consumerSpan.end();
        }

        // Check restaurant
        Span restaurantSpan = orderSpans.startRestaurantCheck(request.getRestaurantId());
        try (Tracer.SpanInScope rs = orderSpans.getTracer().withSpan(restaurantSpan)) {
            restaurantService.checkAvailability(request.getRestaurantId());
        } finally {
            restaurantSpan.end();
        }

        // Create order
        Order order = orderRepository.save(new Order(request));
        span.tag("ftgo.order.id", order.getId().toString());
        return order;
    } catch (Exception e) {
        span.error(e);
        throw e;
    } finally {
        span.end();
    }
}
```

### Delivery Flow Spans

```java
@Autowired
private DeliveryTracingSpans deliverySpans;

public void assignDelivery(String orderId) {
    Span span = deliverySpans.startDeliveryAssignment(orderId);
    try (Tracer.SpanInScope ws = deliverySpans.getTracer().withSpan(span)) {
        // Search for available courier
        Span searchSpan = deliverySpans.startCourierSearch(orderId);
        try (Tracer.SpanInScope ss = deliverySpans.getTracer().withSpan(searchSpan)) {
            Courier courier = courierService.findAvailable();
            searchSpan.tag("ftgo.courier.id", courier.getId());
        } finally {
            searchSpan.end();
        }

        // Assign delivery
        delivery.assignCourier(courier);
    } catch (Exception e) {
        span.error(e);
        throw e;
    } finally {
        span.end();
    }
}
```

### Available Span Methods

| Class                  | Method                      | Span Name                        |
|------------------------|-----------------------------|----------------------------------|
| `OrderTracingSpans`    | `startOrderCreation()`      | `ftgo.order.create`              |
| `OrderTracingSpans`    | `startConsumerValidation()` | `ftgo.order.validate-consumer`   |
| `OrderTracingSpans`    | `startRestaurantCheck()`    | `ftgo.order.check-restaurant`    |
| `OrderTracingSpans`    | `startOrderAcceptance()`    | `ftgo.order.accept`              |
| `OrderTracingSpans`    | `startOrderRejection()`     | `ftgo.order.reject`              |
| `DeliveryTracingSpans` | `startCourierSearch()`      | `ftgo.delivery.search-courier`   |
| `DeliveryTracingSpans` | `startDeliveryAssignment()` | `ftgo.delivery.assign`           |
| `DeliveryTracingSpans` | `startDeliveryPickup()`     | `ftgo.delivery.pickup`           |
| `DeliveryTracingSpans` | `startDeliveryCompletion()` | `ftgo.delivery.complete`         |

## Trace Context in Logs

When tracing is enabled, all log lines automatically include `traceId` and `spanId`:

```
2024-03-15 10:30:45.123  INFO [ftgo-order-service,64b8f2e3d1a7c5b0,a1b2c3d4e5f60718] OrderController : Creating order for consumer 12345
2024-03-15 10:30:45.234  INFO [ftgo-order-service,64b8f2e3d1a7c5b0,b2c3d4e5f6071829] ConsumerClient  : Validating consumer 12345
2024-03-15 10:30:45.345  INFO [ftgo-consumer-service,64b8f2e3d1a7c5b0,c3d4e5f607182930] ConsumerController : Consumer 12345 validated
```

Note that the `traceId` (`64b8f2e3d1a7c5b0`) is the same across all services for a single request, while each service/operation gets its own `spanId`.

### Custom Log Pattern

To customize the log pattern, override in your service's `application.properties`:

```properties
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

## API Gateway Integration

The API Gateway automatically propagates trace context to downstream services when configured with the tracing library.

### Configuration

```properties
# API Gateway application.properties
spring.application.name=ftgo-api-gateway

# Propagate both B3 and W3C formats (for compatibility)
management.tracing.propagation.consume=b3,w3c
management.tracing.propagation.produce=b3
```

### Header Propagation

The trace context is propagated via HTTP headers:

| Format | Headers                                                        |
|--------|----------------------------------------------------------------|
| B3     | `X-B3-TraceId`, `X-B3-SpanId`, `X-B3-ParentSpanId`, `X-B3-Sampled` |
| W3C    | `traceparent`, `tracestate`                                    |

## Docker Compose

### Start Tracing Stack

```bash
# Zipkin only
docker-compose -f infrastructure/docker/docker-compose.tracing.yml up -d zipkin

# Jaeger only
docker-compose -f infrastructure/docker/docker-compose.tracing.yml up -d jaeger

# Both (for evaluation)
docker-compose -f infrastructure/docker/docker-compose.tracing.yml up -d

# Full stack (services + monitoring + tracing)
docker-compose -f infrastructure/docker/docker-compose.services.yml \
               -f infrastructure/docker/docker-compose.monitoring.yml \
               -f infrastructure/docker/docker-compose.tracing.yml up -d
```

### Service Configuration for Each Collector

**For Zipkin:**
```properties
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

**For Jaeger (via Zipkin-compatible endpoint):**
```properties
management.zipkin.tracing.endpoint=http://jaeger:9411/api/v2/spans
```

## Kubernetes Deployment

For Kubernetes environments, deploy the trace collector as a separate service:

### Zipkin

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zipkin
  namespace: ftgo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zipkin
  template:
    metadata:
      labels:
        app: zipkin
    spec:
      containers:
        - name: zipkin
          image: openzipkin/zipkin:3.1
          ports:
            - containerPort: 9411
          env:
            - name: STORAGE_TYPE
              value: mem
---
apiVersion: v1
kind: Service
metadata:
  name: zipkin
  namespace: ftgo
spec:
  selector:
    app: zipkin
  ports:
    - port: 9411
      targetPort: 9411
```

### Jaeger

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jaeger
  namespace: ftgo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jaeger
  template:
    metadata:
      labels:
        app: jaeger
    spec:
      containers:
        - name: jaeger
          image: jaegertracing/all-in-one:1.54
          ports:
            - containerPort: 16686
            - containerPort: 4317
            - containerPort: 4318
            - containerPort: 9411
          env:
            - name: COLLECTOR_ZIPKIN_HOST_PORT
              value: ":9411"
            - name: SPAN_STORAGE_TYPE
              value: memory
---
apiVersion: v1
kind: Service
metadata:
  name: jaeger
  namespace: ftgo
spec:
  selector:
    app: jaeger
  ports:
    - name: ui
      port: 16686
      targetPort: 16686
    - name: otlp-grpc
      port: 4317
      targetPort: 4317
    - name: otlp-http
      port: 4318
      targetPort: 4318
    - name: zipkin
      port: 9411
      targetPort: 9411
```

### Service Configuration in Kubernetes

```yaml
# In service deployment
env:
  - name: MANAGEMENT_ZIPKIN_TRACING_ENDPOINT
    value: "http://zipkin.ftgo.svc.cluster.local:9411/api/v2/spans"
  - name: MANAGEMENT_TRACING_SAMPLING_PROBABILITY
    value: "0.1"  # 10% in production
```

## Troubleshooting

### No Traces Appearing

1. Verify the collector is running:
   ```bash
   curl http://localhost:9411/health  # Zipkin
   curl http://localhost:16686/       # Jaeger
   ```

2. Check service logs for trace reporter errors:
   ```bash
   docker logs ftgo-order-service 2>&1 | grep -i "zipkin\|trace\|brave"
   ```

3. Verify sampling is not set to 0:
   ```bash
   curl http://localhost:8080/actuator/env/management.tracing.sampling.probability
   ```

### Traces Not Correlating Across Services

1. Verify propagation headers are being sent:
   ```bash
   curl -v http://localhost:8080/api/orders 2>&1 | grep -i "x-b3\|traceparent"
   ```

2. Ensure all services use the same propagation format (B3 or W3C)

3. Check that `RestTemplate` is created via `RestTemplateBuilder` (not `new RestTemplate()`)

### High Memory Usage from Traces

Reduce sampling rate in production:
```properties
management.tracing.sampling.probability=0.1
```
