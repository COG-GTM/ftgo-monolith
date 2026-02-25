# Centralized Logging with EFK Stack

## Overview

The FTGO platform uses an **EFK stack** (Elasticsearch, Fluentd, Kibana) for centralized log aggregation across all microservices. The `ftgo-logging-lib` shared library provides structured JSON logging, MDC context propagation, and correlation ID support.

## Architecture

```
Service (JSON stdout) -> Fluentd -> Elasticsearch -> Kibana
                          |
                     (K8s: DaemonSet reads container logs)
                     (Docker: fluentd log driver)
```

### Components

| Component       | Role                          | Port  |
|-----------------|-------------------------------|-------|
| Elasticsearch   | Log storage and search engine | 9200  |
| Fluentd         | Log collector and forwarder   | 24224 |
| Kibana          | Log visualization and dashboards | 5601 |

## Shared Logging Library (`ftgo-logging-lib`)

### Adding to a Service

Add the dependency in your service's `build.gradle`:

```groovy
dependencies {
    compile project(":shared:ftgo-logging-lib")
}
```

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.logging.enabled` | `true` | Enable/disable logging library |
| `ftgo.logging.json.enabled` | `true` | Enable structured JSON output |
| `ftgo.logging.json.include-caller-data` | `false` | Include class/method/line in logs |
| `ftgo.logging.correlation-id.enabled` | `true` | Enable correlation ID propagation |
| `ftgo.logging.correlation-id.header-name` | `X-Correlation-ID` | HTTP header for correlation ID |
| `ftgo.logging.correlation-id.mdc-key` | `correlationId` | MDC key for correlation ID |
| `ftgo.logging.correlation-id.generate-if-missing` | `true` | Auto-generate if no header present |
| `ftgo.logging.async.enabled` | `true` | Enable async appender (non-blocking) |
| `ftgo.logging.async.queue-size` | `1024` | Async appender queue size |
| `ftgo.logging.async.discarding-threshold` | `0` | Threshold for discarding low-priority logs |
| `ftgo.logging.elasticsearch.enabled` | `false` | Enable direct ES shipping (prefer Fluentd) |

### JSON Log Format

All services output structured JSON logs with these fields:

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "service": "ftgo-order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "correlationId": "req-uuid-here",
  "message": "Order created successfully",
  "logger_name": "com.ftgo.order.service.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "requestMethod": "POST",
  "requestUri": "/api/orders",
  "remoteAddr": "10.0.1.50"
}
```

### MDC Fields

The library automatically populates these MDC fields:

| Field | Source | Description |
|-------|--------|-------------|
| `service` | `spring.application.name` | Service name |
| `traceId` | Micrometer Tracing (Brave) | Distributed trace ID |
| `spanId` | Micrometer Tracing (Brave) | Current span ID |
| `correlationId` | `X-Correlation-ID` header | Request correlation ID from API Gateway |
| `requestMethod` | HTTP request | GET, POST, PUT, DELETE |
| `requestUri` | HTTP request | Request path |
| `remoteAddr` | HTTP request / X-Forwarded-For | Client IP address |

### Correlation ID Flow

```
Client -> API Gateway (generates X-Correlation-ID)
       -> CorrelationIdFilter (extracts to MDC)
       -> Service logic (logs include correlationId)
       -> Response (X-Correlation-ID header echoed back)
```

## Local Development (Docker Compose)

### Starting the EFK Stack

```bash
# Start EFK stack only
docker-compose -f infrastructure/docker/docker-compose.logging.yml up -d

# Start with all infrastructure
docker-compose -f infrastructure/docker/docker-compose.services.yml \
               -f infrastructure/docker/docker-compose.monitoring.yml \
               -f infrastructure/docker/docker-compose.tracing.yml \
               -f infrastructure/docker/docker-compose.logging.yml up -d
```

### Accessing Kibana

1. Open http://localhost:5601
2. Go to **Management > Stack Management > Index Patterns**
3. Create index pattern: `ftgo-logs-*`
4. Set time field: `@timestamp`
5. Go to **Discover** to search logs

### Useful Kibana Queries

```
# All logs from a specific service
service: "ftgo-order-service"

# All logs for a specific trace
traceId: "abc123def456"

# Error logs across all services
level: "ERROR"

# Correlation ID search (cross-service request flow)
correlationId: "req-uuid-here"

# Errors in a specific time range
level: "ERROR" AND @timestamp >= "2024-01-15T10:00:00"

# Combined filter: errors in order service
service: "ftgo-order-service" AND level: "ERROR"
```

### Configuring Services to Use Fluentd Log Driver

In `docker-compose.services.yml`, add to each service:

```yaml
services:
  ftgo-order-service:
    logging:
      driver: fluentd
      options:
        fluentd-address: localhost:24224
        tag: ftgo.{{.Name}}
        fluentd-async: "true"
```

## Kubernetes Deployment

### EFK Stack Components

The EFK stack is deployed via Kustomize manifests:

```
infrastructure/k8s/base/logging/
  ├── kustomization.yaml
  ├── elasticsearch/
  │   ├── statefulset.yaml      # Single-node ES (scale in prod)
  │   ├── service.yaml
  │   ├── configmap.yaml        # ILM policies, index templates
  │   └── kustomization.yaml
  ├── fluentd/
  │   ├── daemonset.yaml        # Runs on every node
  │   ├── configmap.yaml        # Fluentd configuration
  │   ├── serviceaccount.yaml   # RBAC for pod log access
  │   └── kustomization.yaml
  └── kibana/
      ├── deployment.yaml
      ├── service.yaml
      ├── configmap.yaml        # Dashboard definitions
      └── kustomization.yaml
```

### Log Retention Policies

Retention is configured per environment via Elasticsearch ILM (Index Lifecycle Management):

| Environment | Retention | Rollover | Warm Tier |
|-------------|-----------|----------|-----------|
| Development | 7 days    | 2 GB / 1 day | None |
| Staging     | 30 days   | 5 GB / 1 day | 7 days |
| Production  | 90 days   | 10 GB / 1 day | 7 days (+ cold at 30 days) |

Retention patches are in:
- `infrastructure/k8s/overlays/dev/patches/logging-retention.yaml`
- `infrastructure/k8s/overlays/staging/patches/logging-retention.yaml`
- `infrastructure/k8s/overlays/prod/patches/logging-retention.yaml`

### Error Rate Alerts

Elasticsearch Watcher is configured to alert when error rates spike:
- **Threshold**: > 50 errors in 5 minutes
- **Check interval**: Every 5 minutes
- **Configuration**: `kibana/configmap.yaml` (`error-rate-alert.json`)

## Integration with Distributed Tracing

The logging library integrates with `ftgo-tracing-lib` (Micrometer Tracing):

1. **traceId** and **spanId** are automatically added to MDC by Micrometer Tracing
2. The JSON encoder includes all MDC fields in log output
3. In Kibana, search by `traceId` to see all log entries for a request flow across services

### Cross-Service Request Flow Example

```
1. API Gateway receives request
   -> Generates correlationId, starts trace
   -> Logs: {service: "ftgo-api-gateway", traceId: "abc", correlationId: "xyz"}

2. Order Service processes request
   -> Receives correlationId via header, traceId via B3 propagation
   -> Logs: {service: "ftgo-order-service", traceId: "abc", correlationId: "xyz"}

3. Restaurant Service called
   -> Same traceId propagated, new spanId
   -> Logs: {service: "ftgo-restaurant-service", traceId: "abc", correlationId: "xyz"}

4. In Kibana: search traceId: "abc" to see all 3 entries
```

## Performance Considerations

- **Async Appender**: Logs are written asynchronously to avoid blocking application threads
- **Queue Size**: Default 1024 entries; increase for high-throughput services
- **Discarding Threshold**: Set to 0 (never discard) by default; increase in production to drop DEBUG/TRACE under load
- **Fluentd Buffering**: File-based buffer with 5-second flush interval prevents log loss
- **Elasticsearch Resources**: Development uses 512MB heap; production should use 2GB+ with dedicated nodes
