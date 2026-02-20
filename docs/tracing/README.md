# Distributed Tracing Guide

## Overview

FTGO uses distributed tracing to correlate requests across microservice boundaries. The tracing infrastructure is built on:

- **Micrometer Tracing** - Vendor-neutral tracing facade
- **OpenTelemetry** - Instrumentation and export protocol
- **Jaeger** - Trace collection, storage, and visualization

## Architecture

```
Service A ──> Service B ──> Service C
    │              │              │
    └──────────────┴──────────────┘
                   │
            Trace Context
         (W3C / B3 headers)
                   │
                   ▼
              Jaeger Collector
            (Zipkin / OTLP)
                   │
                   ▼
              Jaeger Storage
                   │
                   ▼
              Jaeger Query UI
           (http://localhost:16686)
```

## How It Works

1. **Trace Creation**: When a request enters the first service, a new trace is created with a unique trace ID
2. **Context Propagation**: Trace context is propagated via HTTP headers (W3C `traceparent` by default)
3. **Span Creation**: Each service creates spans for its operations within the trace
4. **Export**: Spans are exported to Jaeger via Zipkin-compatible or OTLP endpoints
5. **Visualization**: Jaeger UI allows viewing and searching traces across services

## Setup

### 1. Add the Library Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-tracing')
}
```

### 2. Configure the Service

In `application.yml`:

```yaml
spring:
  application:
    name: order-service

ftgo:
  tracing:
    enabled: true
    service-name: order-service
    sampling-probability: 1.0
    exporter:
      type: ZIPKIN
      zipkin-endpoint: http://jaeger:9411/api/v2/spans
```

### 3. Start Jaeger

For local development:

```bash
cd infrastructure/tracing
docker-compose up -d
```

For Kubernetes:

```bash
kubectl apply -f infrastructure/tracing/kubernetes/
```

### 4. View Traces

Open the Jaeger UI at http://localhost:16686

## Configuration Reference

### Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.tracing.enabled` | `true` | Enable/disable tracing |
| `ftgo.tracing.service-name` | `${spring.application.name}` | Service name in traces |
| `ftgo.tracing.sampling-probability` | `1.0` | Sampling rate (0.0 to 1.0) |
| `ftgo.tracing.propagation.type` | `W3C` | Propagation format: W3C, B3, B3_MULTI |
| `ftgo.tracing.propagation.baggage-enabled` | `true` | Enable baggage propagation |
| `ftgo.tracing.exporter.type` | `ZIPKIN` | Exporter: ZIPKIN, OTLP, LOGGING |
| `ftgo.tracing.exporter.zipkin-endpoint` | `http://localhost:9411/api/v2/spans` | Zipkin endpoint |
| `ftgo.tracing.exporter.otlp-endpoint` | `http://localhost:4317` | OTLP gRPC endpoint |

### Propagation Headers

#### W3C (Default)

```
traceparent: 00-<trace-id>-<span-id>-<flags>
tracestate: <key>=<value>
```

#### B3 Single Header

```
b3: <trace-id>-<span-id>-<sampling>-<parent-span-id>
```

#### B3 Multi-Header

```
X-B3-TraceId: <trace-id>
X-B3-SpanId: <span-id>
X-B3-ParentSpanId: <parent-span-id>
X-B3-Sampled: 1
```

### Baggage Headers

Custom context fields propagated across services:

```
x-request-id: <request-id>
x-correlation-id: <correlation-id>
```

## Production Recommendations

### Sampling

For production, reduce sampling to avoid excessive trace volume:

```yaml
ftgo:
  tracing:
    sampling-probability: 0.1  # Sample 10% of traces
```

### Exporter

Use OTLP for production deployments with Jaeger:

```yaml
ftgo:
  tracing:
    exporter:
      type: OTLP
      otlp-endpoint: http://jaeger-collector.ftgo-tracing:4317
```

### Kubernetes Service Discovery

Services connect to Jaeger via Kubernetes service DNS:

- Zipkin: `http://jaeger-collector.ftgo-tracing:9411/api/v2/spans`
- OTLP gRPC: `http://jaeger-collector.ftgo-tracing:4317`

## Troubleshooting

### No traces appearing in Jaeger

1. Verify Jaeger is running: `curl http://localhost:14269/`
2. Check service configuration for correct exporter endpoint
3. Ensure `ftgo.tracing.enabled=true`
4. Verify sampling probability is > 0

### Missing spans in a trace

1. Ensure all services use the same propagation type
2. Check that trace context headers are forwarded by any proxies or gateways
3. Verify the `ftgo-tracing` library is included in all services
