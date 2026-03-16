# Distributed Tracing Setup

## Overview

The FTGO platform uses **Spring Cloud Sleuth** with **Zipkin/Jaeger** for distributed
tracing across microservices. The `ftgo-tracing-lib` shared library provides:

- Automatic trace context propagation via **B3 headers**
- Trace-to-log correlation (`traceId` / `spanId` in SLF4J MDC)
- Custom span creation utilities (`SpanHelper`)
- Spring Boot auto-configuration (just add the dependency)

## Quick Start

### 1. Add the dependency

In a service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-tracing-lib')
}
```

That's it. The library auto-configures itself via `spring.factories`.

### 2. Start a local tracing backend

```bash
docker-compose -f deployment/tracing/docker-compose-tracing.yml up -d
```

| Service | URL                      |
|---------|--------------------------|
| Zipkin  | http://localhost:9411     |
| Jaeger  | http://localhost:16686    |

### 3. Run your service

Traces are sent to Zipkin at `http://localhost:9411` by default. Override with:

```properties
spring.zipkin.base-url=http://your-zipkin-host:9411/
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.tracing.enabled` | `true` | Master switch for tracing |
| `spring.sleuth.sampler.probability` | `1.0` | Sampling rate (0.0 - 1.0) |
| `spring.sleuth.propagation.type` | `B3` | Propagation format |
| `spring.zipkin.enabled` | `true` | Enable Zipkin reporter |
| `spring.zipkin.base-url` | `http://localhost:9411/` | Zipkin collector URL |
| `spring.zipkin.sender.type` | `web` | Transport type (`web`, `kafka`, `rabbit`) |
| `ftgo.tracing.log-correlation.enabled` | `true` | Inject traceId/spanId into MDC |

### Production recommendations

```properties
# Sample 10% of requests in production
spring.sleuth.sampler.probability=0.1

# Point to cluster-internal Zipkin
spring.zipkin.base-url=http://zipkin.ftgo-tracing.svc.cluster.local:9411/
```

## How It Works

### Trace Context Propagation (B3)

Spring Cloud Sleuth automatically instruments:
- Incoming HTTP requests (extracts B3 headers)
- `RestTemplate` outbound calls (injects B3 headers)
- Spring MVC handler mappings

B3 headers propagated on every inter-service call:
- `X-B3-TraceId` - 128-bit trace identifier
- `X-B3-SpanId` - 64-bit span identifier
- `X-B3-ParentSpanId` - parent span (if applicable)
- `X-B3-Sampled` - sampling decision (`1` or `0`)

### Trace-to-Log Correlation

The library injects `traceId` and `spanId` into the SLF4J MDC. The existing
`ftgo-logging-lib` logback configuration already includes these MDC keys in its
JSON output, so logs are automatically correlated with traces.

Example log line (JSON format):
```json
{
  "timestamp": "2026-03-16T12:00:00.000Z",
  "level": "INFO",
  "logger": "n.c.f.order.OrderService",
  "message": "Order created",
  "traceId": "463ac35c9f6413ad48485a3953bb6124",
  "spanId": "a2fb4a1d1a96d312",
  "service": "order-service"
}
```

Example log line (console format):
```
2026-03-16 12:00:00.000 [http-nio-8080-exec-1] [463ac35c9f6413ad48485a3953bb6124] INFO  OrderService - Order created
```

### Custom Spans with SpanHelper

Inject `SpanHelper` to instrument business operations:

```java
@Service
public class OrderService {

    private final SpanHelper spanHelper;

    public OrderService(SpanHelper spanHelper) {
        this.spanHelper = spanHelper;
    }

    public Order createOrder(CreateOrderRequest request) throws Exception {
        return spanHelper.executeInSpan("createOrder", () -> {
            // All work here is captured in a child span
            validateConsumer(request.getConsumerId());
            return persistOrder(request);
        });
    }

    public void processPayment(Order order) {
        spanHelper.executeInSpanWithAccess("processPayment", span -> {
            span.tag("order.id", String.valueOf(order.getId()));
            span.tag("payment.amount", order.getTotal().toString());
            paymentGateway.charge(order);
        });
    }
}
```

Available methods:
- `executeInSpan(name, Callable)` - traced call returning a value
- `executeInSpan(name, Runnable)` - traced void operation
- `executeInSpanWithAccess(name, SpanConsumer)` - void with span tag access
- `executeInSpanWithAccess(name, SpanFunction)` - return value with span tag access
- `currentTraceId()` / `currentSpanId()` - read current trace context

## Kubernetes Deployment

Apply the manifests in order:

```bash
kubectl apply -f deployment/tracing/kubernetes/namespace.yaml
kubectl apply -f deployment/tracing/kubernetes/zipkin-deployment.yaml
kubectl apply -f deployment/tracing/kubernetes/zipkin-service.yaml
kubectl apply -f deployment/tracing/kubernetes/jaeger-deployment.yaml
kubectl apply -f deployment/tracing/kubernetes/jaeger-service.yaml
```

### Verify

```bash
kubectl -n ftgo-tracing get pods
kubectl -n ftgo-tracing get svc
```

### Accessing the UIs

Port-forward for local access:

```bash
# Zipkin UI
kubectl -n ftgo-tracing port-forward svc/zipkin 9411:9411

# Jaeger UI
kubectl -n ftgo-tracing port-forward svc/jaeger 16686:16686
```

### Service configuration for K8s

In each service's Kubernetes ConfigMap or `application-k8s.properties`:

```properties
spring.zipkin.base-url=http://zipkin.ftgo-tracing.svc.cluster.local:9411/
spring.sleuth.sampler.probability=0.1
```

## Architecture

```
                          +-----------+
                          |  Zipkin   |
  Service A ---B3 hdrs--> |  :9411    | <-- traces
  Service B ---B3 hdrs--> |           |
  Service C ---B3 hdrs--> +-----------+
                                |
                          +-----v-----+
                          |  Jaeger   |
                          |  :16686   | <-- UI / query
                          +-----------+
```

Both Zipkin and Jaeger accept Zipkin-format spans. Services send to Zipkin by
default; Jaeger is deployed alongside as an alternative UI / query engine since
it accepts the same wire format via its Zipkin-compatible collector.

## Troubleshooting

### No traces appearing in Zipkin/Jaeger

1. Verify `spring.zipkin.base-url` points to the correct host
2. Check that `spring.zipkin.enabled=true`
3. Confirm `spring.sleuth.sampler.probability` is > 0
4. Inspect application logs for Zipkin reporter errors

### Missing traceId in logs

1. Ensure `ftgo.tracing.log-correlation.enabled=true`
2. Verify log pattern includes `%X{traceId}` or that the JSON encoder
   includes `traceId` in its MDC key list

### High latency overhead

- Reduce sampling rate: `spring.sleuth.sampler.probability=0.01`
- Switch to async reporter: `spring.zipkin.sender.type=kafka`
