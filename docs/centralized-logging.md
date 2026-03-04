# Centralized Logging with EFK Stack

## Overview

FTGO uses an **EFK stack** (Elasticsearch + Fluentd + Kibana) for centralized log aggregation across all microservices. The `ftgo-logging-lib` shared library provides structured JSON logging using Logback with the `logstash-logback-encoder`, ensuring consistent log format and correlation ID propagation across all services.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Order Service   │     │ Consumer Service │     │ Restaurant Svc  │
│  (JSON stdout)   │     │  (JSON stdout)   │     │  (JSON stdout)  │
└────────┬─────────┘     └────────┬─────────┘     └────────┬────────┘
         │                        │                         │
         └────────────────────────┼─────────────────────────┘
                                  │
                         ┌────────▼────────┐
                         │     Fluentd      │
                         │  (DaemonSet /    │
                         │   Log Collector) │
                         └────────┬─────────┘
                                  │
                         ┌────────▼────────┐
                         │  Elasticsearch   │
                         │  (Log Storage &  │
                         │   Search Engine) │
                         └────────┬─────────┘
                                  │
                         ┌────────▼────────┐
                         │     Kibana       │
                         │  (Visualization  │
                         │   & Dashboards)  │
                         └─────────────────┘
```

## Structured Log Format

All FTGO services output structured JSON logs with the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `@timestamp` | ISO-8601 | Event timestamp |
| `level` | string | Log level (INFO, WARN, ERROR, DEBUG) |
| `service` | string | Service name (e.g., `ftgo-order-service`) |
| `traceId` | string | Distributed trace ID (from Brave/Micrometer) |
| `spanId` | string | Span ID (from Brave/Micrometer) |
| `correlationId` | string | API Gateway correlation ID |
| `message` | string | Log message |
| `logger` | string | Logger class name |
| `thread` | string | Thread name |
| `stackTrace` | string | Exception stack trace (if present) |
| `userId` | string | Authenticated user ID (if available) |
| `requestMethod` | string | HTTP method (GET, POST, etc.) |
| `requestUri` | string | Request URI path |

### Example Log Entry

```json
{
  "@timestamp": "2024-03-15T10:30:45.123Z",
  "level": "INFO",
  "service": "ftgo-order-service",
  "traceId": "64f8c2b1a3d4e5f6",
  "spanId": "a1b2c3d4e5f6a7b8",
  "correlationId": "req-abc-123-def-456",
  "message": "Order created successfully",
  "logger": "n.c.f.o.s.OrderService",
  "thread": "http-nio-8081-exec-1",
  "userId": "user-789",
  "requestMethod": "POST",
  "requestUri": "/api/orders"
}
```

## Shared Logging Library (`ftgo-logging-lib`)

### Adding to Your Service

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-ftgo-logging-lib')
}
```

The library auto-configures via Spring Boot auto-configuration. No additional setup is required.

### Configuration Properties

Configure via `application.yml` or environment variables:

```yaml
ftgo:
  logging:
    enabled: true                        # Enable/disable logging auto-config
    json-enabled: true                   # Enable structured JSON output
    async-enabled: true                  # Enable async appender (recommended)
    async-queue-size: 1024               # Async queue capacity
    async-discard-threshold: 0           # 0 = never discard
    include-caller-data: false           # Include class/method/line (perf cost)
    service-name: ftgo-order-service     # Overrides spring.application.name
    correlation-id-header: X-Correlation-ID  # Header for correlation ID
```

### Correlation ID Propagation

The `CorrelationIdFilter` automatically:
1. Extracts the `X-Correlation-ID` header from incoming requests
2. Generates a UUID if no header is present
3. Places the correlation ID in SLF4J MDC for log inclusion
4. Sets the correlation ID in the response header for downstream tracking

### MDC Context

The `MdcContextLifecycle` class manages MDC fields:

```java
@Autowired
private MdcContextLifecycle mdcContext;

// Set additional context
mdcContext.setUserId("user-123");
mdcContext.setRequestId("req-456");

// Clean up after request
mdcContext.clearRequestContext();
```

### Async Appender

The async appender wraps the JSON console appender for non-blocking log writes:
- **Queue size**: 1024 (configurable)
- **Never blocks**: Application threads are never blocked by log I/O
- **Discard threshold**: 0 (no logs discarded by default)

## Local Development

### Starting the EFK Stack

```bash
# Start EFK stack only
docker-compose -f docker-compose-logging.yml up -d

# Start with services
docker-compose -f docker-compose-services.yml -f docker-compose-logging.yml up -d

# Full observability stack
docker-compose -f docker-compose-services.yml \
  -f docker-compose-metrics.yml \
  -f docker-compose-tracing.yml \
  -f docker-compose-logging.yml up -d
```

### Setting Up Index Lifecycle Management

After the EFK stack is running, configure ILM policies:

```bash
# For development (7-day retention)
./deploy/efk/setup-ilm.sh dev

# For staging (30-day retention)
./deploy/efk/setup-ilm.sh staging

# For production (90-day retention)
./deploy/efk/setup-ilm.sh prod
```

### Importing Kibana Dashboards

Import the pre-configured dashboards:

```bash
# Service Logs Overview
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deploy/efk/kibana/dashboards/service-logs-overview.ndjson

# Request Flow Tracing
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deploy/efk/kibana/dashboards/request-flow-tracing.ndjson

# Error Aggregation
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@deploy/efk/kibana/dashboards/error-aggregation.ndjson
```

### Endpoints

| Service | URL | Description |
|---------|-----|-------------|
| Elasticsearch | http://localhost:9200 | Search API |
| Kibana | http://localhost:5601 | Log visualization UI |
| Fluentd | localhost:24224 | Log forward input |

## Kibana Dashboards

### Service Logs Overview

Shows aggregated log data across all FTGO services:
- **Log Volume by Service**: Histogram of log counts per service over time
- **Error Rate by Service**: Line chart of error frequency per service
- **Log Level Distribution**: Pie chart of INFO/WARN/ERROR/DEBUG proportions
- **Top Error Messages**: Table of most frequent error messages

### Request Flow Tracing

Enables distributed request tracing:
- **Trace Search**: Search logs by `traceId` to see all entries across services
- **Service Interaction Timeline**: Visual timeline of service calls
- **Correlation ID Lookup**: Find all logs for a single API request

### Error Aggregation

Focused error analysis:
- **Error Count Trend**: Area chart of errors over time by service
- **Error Percentage**: Metric showing error rate per service
- **Error/Warning Logs**: Detailed view with stack traces

## Log Retention Policies

Retention is configured per environment via Elasticsearch ILM:

| Environment | Retention | Rollover Size | Notes |
|-------------|-----------|---------------|-------|
| Development | 7 days | 5 GB | Single-node, minimal resources |
| Staging | 30 days | 10 GB | Warm phase after 7 days |
| Production | 90 days | 20 GB | Warm phase 7d, cold phase 30d |

## Log-Based Alerts

Pre-configured alert rules in `deploy/efk/kibana/alerts/error-rate-alerts.json`:

| Alert | Threshold | Window | Severity |
|-------|-----------|--------|----------|
| High Error Rate | > 50 errors | 5 min | Warning |
| Critical Error Spike | > 200 errors | 5 min | Critical |
| Service Log Silence | < 1 log | 10 min | Warning |
| Unhandled Exceptions | > 10 with stack trace | 5 min | Warning |

## Kubernetes Deployment

The EFK stack is deployed via Helm chart or Kustomize:

### Helm

```bash
# Dev environment
helm upgrade --install ftgo deploy/helm/ftgo \
  -f deploy/helm/ftgo/values-dev.yaml \
  -n ftgo-dev

# Production
helm upgrade --install ftgo deploy/helm/ftgo \
  -f deploy/helm/ftgo/values-prod.yaml \
  -n ftgo-prod
```

### Kustomize

```bash
kubectl kustomize deploy/k8s/overlays/dev | kubectl apply -f -
```

### Components Deployed

- **Elasticsearch StatefulSet**: Persistent log storage with PVC
- **Fluentd DaemonSet**: One pod per node for log collection
- **Kibana Deployment**: Web UI for log visualization
- **ServiceAccount + RBAC**: Fluentd access to K8s API for metadata enrichment

## Searching Logs

### By Trace ID

Find all log entries for a distributed request:

```
traceId: "64f8c2b1a3d4e5f6"
```

### By Correlation ID

Find all logs from a single API Gateway request:

```
correlationId: "req-abc-123-def-456"
```

### By Service and Level

Find errors in a specific service:

```
service: "ftgo-order-service" AND level: "ERROR"
```

### By Time Range and Stack Trace

Find recent exceptions:

```
level: "ERROR" AND _exists_: stackTrace
```

## Integration with Distributed Tracing

The logging library works alongside `ftgo-tracing-lib`. When both are on the classpath:

1. Brave MDCScopeDecorator injects `traceId` and `spanId` into SLF4J MDC
2. `ftgo-logging-lib` includes these MDC fields in JSON output
3. Searching by `traceId` in Kibana shows all log entries for a trace
4. Click through to Zipkin for visual trace timeline

## Performance Considerations

- **Async Appender**: Enabled by default to avoid blocking application threads
- **Never Block**: The async appender is configured with `neverBlock=true`
- **Queue Sizing**: Default 1024 entries; increase for high-throughput services
- **Discard Threshold**: Set to 0 (no discarding); adjust in production if needed
- **Caller Data**: Disabled by default (expensive to compute); enable only for debugging
