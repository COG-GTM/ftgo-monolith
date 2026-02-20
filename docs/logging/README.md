# FTGO Centralized Logging Architecture

## Overview

The FTGO centralized logging system provides structured, correlated log collection and analysis across all microservices using the ELK (Elasticsearch, Logstash, Kibana) stack.

## Architecture

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Order Service   │     │ Consumer Service  │     │ Restaurant Svc   │
│  (Logback JSON)  │     │  (Logback JSON)   │     │  (Logback JSON)  │
└────────┬─────────┘     └────────┬──────────┘     └────────┬─────────┘
         │                        │                          │
         │   Structured JSON Logs (with traceId/spanId)      │
         │                        │                          │
         ▼                        ▼                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Logstash                                    │
│  TCP/UDP :5000 (direct)  │  Beats :5044 (Filebeat)                 │
│  - JSON parsing          │  - Docker log collection                │
│  - Trace ID extraction   │  - Field enrichment                     │
│  - Service enrichment    │                                         │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Elasticsearch                                 │
│  Index: ftgo-logs-YYYY.MM.dd                                       │
│  ILM: hot → warm → cold → delete (30d)                             │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Kibana                                     │
│  - Service Logs Overview Dashboard                                  │
│  - Error Trends                                                     │
│  - Log Level Distribution                                           │
│  - Trace Correlation Search                                         │
└─────────────────────────────────────────────────────────────────────┘
```

## Components

### 1. Infrastructure (`infrastructure/logging/`)

| Component       | Version | Purpose                          |
|-----------------|---------|----------------------------------|
| Elasticsearch   | 8.13.0  | Log storage and indexing         |
| Logstash        | 8.13.0  | Log ingestion and transformation |
| Kibana          | 8.13.0  | Visualization and dashboards     |
| Filebeat        | 8.13.0  | Docker container log collection  |

### 2. Shared Library (`libs/ftgo-logging/`)

Spring Boot auto-configuration library providing:

- **Structured JSON logging** via custom Logback encoder
- **Trace ID correlation** with distributed tracing (integrates with `libs/ftgo-tracing/`)
- **Logstash TCP appender** for direct log shipping
- **Request ID propagation** via servlet filter
- **MDC enrichment** with traceId, spanId, requestId, userId

## Getting Started

### Start the ELK Stack

```bash
cd infrastructure/logging
docker compose up -d
```

### Add the Logging Library

Add the dependency to your service's `build.gradle`:

```groovy
implementation project(':libs:ftgo-logging')
```

Or if using the library as a standalone build:

```groovy
implementation 'com.ftgo:ftgo-logging:1.0.0-SNAPSHOT'
```

### Configure Your Service

Add to `application.properties`:

```properties
# Service identification
spring.application.name=order-service
ftgo.logging.service-name=${spring.application.name}

# JSON logging (enabled by default)
ftgo.logging.json-enabled=true

# Trace correlation (enabled by default, requires ftgo-tracing)
ftgo.logging.trace-correlation-enabled=true

# Logstash direct shipping (disabled by default)
ftgo.logging.logstash.enabled=true
ftgo.logging.logstash.host=logstash
ftgo.logging.logstash.port=5000
```

### Using the Logback XML Include

Alternatively, include the provided Logback configuration fragment in your `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="logback/logback-ftgo.xml"/>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
        <!-- Uncomment for Logstash shipping -->
        <!-- <appender-ref ref="ASYNC_LOGSTASH"/> -->
    </root>
</configuration>
```

## Log Format

All services emit structured JSON logs:

```json
{
  "timestamp": "2026-01-15T10:30:45.123+00:00",
  "level": "INFO",
  "logger": "com.ftgo.order.service.OrderService",
  "thread": "http-nio-8080-exec-1",
  "message": "Order created successfully",
  "service": "order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "context": {
    "orderId": "12345"
  }
}
```

## Trace Correlation

The logging library integrates with `libs/ftgo-tracing/` to automatically include trace and span IDs in all log entries via MDC (Mapped Diagnostic Context).

This enables:
- **End-to-end request tracing** across service boundaries
- **Log correlation** in Kibana by searching for a specific `traceId`
- **Linking logs to Jaeger traces** for combined debugging

### How It Works

1. `ftgo-tracing` sets up OpenTelemetry/Micrometer tracing with W3C propagation
2. Spring Boot automatically populates MDC with `traceId` and `spanId`
3. `ftgo-logging` reads these MDC values and includes them in structured JSON output
4. `RequestIdFilter` adds a unique `requestId` (from `X-Request-ID` header or auto-generated)
5. Logstash extracts these fields for Elasticsearch indexing

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.logging.enabled` | `true` | Enable/disable logging auto-configuration |
| `ftgo.logging.json-enabled` | `true` | Enable structured JSON console output |
| `ftgo.logging.trace-correlation-enabled` | `true` | Enable trace ID MDC correlation |
| `ftgo.logging.service-name` | | Service name included in log entries |
| `ftgo.logging.logstash.enabled` | `false` | Enable Logstash TCP appender |
| `ftgo.logging.logstash.host` | `localhost` | Logstash host |
| `ftgo.logging.logstash.port` | `5000` | Logstash TCP port |
| `ftgo.logging.logstash.queue-size` | `512` | Async appender queue size |
| `ftgo.logging.logstash.include-caller-data` | `false` | Include caller class/method info |
| `ftgo.logging.console.enabled` | `true` | Enable JSON console appender |
| `ftgo.logging.console.pretty-print` | `false` | Pretty-print JSON (dev only) |

## Kibana Dashboards

### Importing Dashboards

```bash
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@infrastructure/logging/kibana/dashboards/ftgo-service-logs-dashboard.ndjson
```

### Available Dashboards

1. **FTGO Service Logs Overview** - Main dashboard with log volume over time
2. **Log Volume by Service** - Distribution of logs across services
3. **Error Trends** - Error rate trends per service
4. **Log Level Distribution** - Breakdown by log level
5. **Trace Correlation Search** - Search logs by trace ID, service, or log level

## Index Lifecycle Management

Logs are managed with an ILM policy:

| Phase  | Age   | Actions                          |
|--------|-------|----------------------------------|
| Hot    | 0d    | Active writes, rollover at 1d/10GB |
| Warm   | 2d    | Shrink to 1 shard, force merge   |
| Cold   | 7d    | Reduced priority                 |
| Delete | 30d   | Automatic deletion               |

## Troubleshooting

### Logs Not Appearing in Kibana

1. Check Elasticsearch health: `curl http://localhost:9200/_cluster/health`
2. Check Logstash status: `curl http://localhost:9600`
3. Verify index exists: `curl http://localhost:9200/_cat/indices/ftgo-logs-*`
4. Check Logstash pipeline logs: `docker logs ftgo-logstash`

### Trace IDs Missing

1. Ensure `ftgo-tracing` is on the classpath
2. Verify `ftgo.tracing.enabled=true`
3. Check that `ftgo.logging.trace-correlation-enabled=true`
4. Verify MDC propagation with a test log: check for `traceId` in output
