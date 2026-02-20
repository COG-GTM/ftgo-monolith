# ftgo-tracing

Shared distributed tracing library for FTGO microservices. Provides auto-configuration for Micrometer Tracing with OpenTelemetry bridge and Zipkin/Jaeger exporters.

## Features

- Spring Boot auto-configuration for distributed tracing
- Micrometer Tracing with OpenTelemetry bridge
- Zipkin and OTLP (Jaeger) span exporters
- Configurable trace propagation (W3C, B3, B3 Multi-Header)
- Baggage propagation for cross-service context
- Correlation ID injection into logs

## Usage

Add as a dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-tracing')
}
```

Or use the version catalog bundle:

```groovy
dependencies {
    implementation libs.bundles.tracing
}
```

## Configuration

All properties are optional with sensible defaults:

```yaml
ftgo:
  tracing:
    enabled: true
    service-name: ${spring.application.name}
    sampling-probability: 1.0
    propagation:
      type: W3C          # W3C, B3, B3_MULTI
      baggage-enabled: true
    exporter:
      type: ZIPKIN        # ZIPKIN, OTLP, LOGGING
      zipkin-endpoint: http://localhost:9411/api/v2/spans
      otlp-endpoint: http://localhost:4317
```

### Exporter Types

| Type    | Description                              | Default Endpoint                          |
|---------|------------------------------------------|-------------------------------------------|
| ZIPKIN  | Zipkin-compatible (works with Jaeger)    | `http://localhost:9411/api/v2/spans`      |
| OTLP    | OpenTelemetry Protocol (gRPC)            | `http://localhost:4317`                   |
| LOGGING | Console logging (development only)       | N/A                                       |

### Propagation Types

| Type     | Header Format                              |
|----------|--------------------------------------------|
| W3C      | `traceparent`, `tracestate` (default)      |
| B3       | Single `b3` header                         |
| B3_MULTI | `X-B3-TraceId`, `X-B3-SpanId`, etc.       |

## Log Correlation

Trace and span IDs are automatically injected into log output via MDC. The default logging pattern includes:

```
[application-name, traceId, spanId]
```

## Infrastructure

See `infrastructure/tracing/` for Docker Compose and Kubernetes deployment manifests for Jaeger.
