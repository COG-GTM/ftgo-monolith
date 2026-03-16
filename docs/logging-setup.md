# Centralized Logging Setup (EFK Stack)

This document describes the centralized logging infrastructure for FTGO microservices using the **EFK stack** (Elasticsearch, Fluentd, Kibana).

## Architecture Overview

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Order Service │   │Consumer Svc  │   │Restaurant Svc│   ...
│  (JSON logs)  │   │  (JSON logs)  │   │  (JSON logs)  │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                  │                   │
       └──────────────────┼───────────────────┘
                          │
                   ┌──────▼───────┐
                   │   Fluentd    │  (Log aggregator)
                   │  Port 24224  │
                   └──────┬───────┘
                          │
                   ┌──────▼───────┐
                   │Elasticsearch │  (Log storage & search)
                   │  Port 9200   │
                   └──────┬───────┘
                          │
                   ┌──────▼───────┐
                   │   Kibana     │  (Visualization)
                   │  Port 5601   │
                   └──────────────┘
```

## Components

### 1. ftgo-logging-lib (Shared Library)

Located at `shared-libraries/ftgo-logging-lib/`, this library provides:

- **Structured JSON logging** via Logback with `logstash-logback-encoder`
- **Correlation ID propagation** across services via HTTP headers (`X-Correlation-ID`)
- **MDC (Mapped Diagnostic Context)** for enriching log entries with request metadata
- **Spring Boot auto-configuration** for zero-config integration

#### Key Classes

| Class | Purpose |
|-------|---------|
| `LoggingAutoConfiguration` | Auto-configures correlation ID filter and RestTemplate interceptor |
| `CorrelationIdFilter` | Servlet filter that extracts/generates correlation IDs per request |
| `CorrelationIdInterceptor` | RestTemplate interceptor for propagating correlation IDs |
| `MdcContextHolder` | Utility for managing MDC in multi-threaded environments |
| `LoggingConstants` | MDC keys and header constants |

#### Integration

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-logging-lib')
}
```

The library auto-configures itself via Spring Boot's `spring.factories`. No additional configuration needed.

#### Logging Profiles

| Profile | Output Format | Use Case |
|---------|--------------|----------|
| `default`, `local`, `dev` | Human-readable console | Local development |
| `prod`, `staging`, `k8s` | JSON (stdout) | Production / EFK ingestion |
| `file` | JSON (file + stdout) | When persistent file logs needed |

Set the profile via:
```bash
spring.profiles.active=prod
```

#### MDC Fields

All log entries are enriched with:

| MDC Key | Description |
|---------|-------------|
| `correlationId` | Unique request trace ID (propagated across services) |
| `serviceName` | Name of the originating service |
| `requestMethod` | HTTP method (GET, POST, etc.) |
| `requestUri` | Request URI path |
| `clientIp` | Client IP address |
| `userId` | Authenticated user ID (when set) |
| `traceId` | Distributed trace ID (when set) |
| `spanId` | Distributed span ID (when set) |

### 2. EFK Stack (Docker Compose)

Located at `deployment/logging/docker-compose-logging.yml`.

#### Quick Start (Local Development)

```bash
# Start the EFK stack
docker-compose -f deployment/logging/docker-compose-logging.yml up -d

# Wait for services to be healthy, then apply ILM policy
./deployment/logging/elasticsearch/apply-ilm-policy.sh

# Import Kibana dashboards
./deployment/logging/kibana/import-dashboards.sh

# Verify Elasticsearch is running
curl http://localhost:9200/_cluster/health?pretty

# Access Kibana
open http://localhost:5601
```

#### Running FTGO Services with EFK

To send application logs to Fluentd, configure your service's Docker log driver:

```yaml
services:
  your-service:
    logging:
      driver: fluentd
      options:
        fluentd-address: localhost:24224
        tag: ftgo.{{.Name}}
```

Or run with the `prod` profile to output JSON logs directly to stdout (collected by Fluentd in Kubernetes).

### 3. Fluentd Configuration

#### Docker Compose (Local)

Located at `deployment/logging/fluentd/conf/fluent.conf`:
- Accepts logs on TCP/UDP port 24224
- Parses JSON-formatted log messages
- Concatenates multi-line stack traces
- Forwards to Elasticsearch with `ftgo-logs-` prefix

#### Kubernetes

Located at `deployment/logging/fluentd/kubernetes/fluentd-daemonset.yml`:
- Deploys as a DaemonSet (one pod per node)
- Collects logs from `/var/log/containers/ftgo-*.log`
- Enriches with Kubernetes metadata (pod name, namespace, labels)
- Forwards to Elasticsearch with `ftgo-k8s-logs-` prefix

```bash
# Deploy to Kubernetes
kubectl apply -f deployment/logging/fluentd/kubernetes/fluentd-daemonset.yml
kubectl apply -f deployment/logging/fluentd/kubernetes/elasticsearch-statefulset.yml
```

### 4. Kibana Dashboards

Pre-configured dashboards are located at `deployment/logging/kibana/dashboards.ndjson`.

#### Available Visualizations

| Dashboard | Description |
|-----------|-------------|
| FTGO - Service Logs Dashboard | Main dashboard with all visualizations |
| FTGO - Log Volume Over Time | Histogram of log volume per service |
| FTGO - Error Rate by Service | Donut chart of errors per service |
| FTGO - Log Levels Distribution | Pie chart of log level distribution |
| FTGO - Correlation ID Search | Table for tracing requests by correlation ID |

#### Import Dashboards

```bash
./deployment/logging/kibana/import-dashboards.sh [KIBANA_URL]
```

### 5. Log Retention Policies

Located at `deployment/logging/elasticsearch/index-lifecycle-policy.json`.

#### Retention Schedule

| Phase | Age | Actions |
|-------|-----|---------|
| **Hot** | 0-7 days | Active writes, rollover at 5GB or 1 day |
| **Warm** | 7-30 days | Shrink to 1 shard, force merge |
| **Cold** | 30-90 days | Frozen (read-only, minimal resources) |
| **Delete** | 90+ days | Indices permanently deleted |

#### Apply Retention Policy

```bash
./deployment/logging/elasticsearch/apply-ilm-policy.sh [ELASTICSEARCH_URL]
```

This creates both the ILM policy and an index template that automatically applies it to all `ftgo-logs-*` and `ftgo-k8s-logs-*` indices.

## Structured Log Format (JSON)

When running with the `prod` profile, log entries are formatted as:

```json
{
  "@timestamp": "2024-01-15T10:30:00.123Z",
  "@version": "1",
  "message": "Order created successfully",
  "logger_name": "n.c.f.order.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "service": "order-service",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "serviceName": "order-service",
  "requestMethod": "POST",
  "requestUri": "/orders",
  "clientIp": "10.0.0.1"
}
```

## Correlation ID Flow

1. Client sends request (optionally with `X-Correlation-ID` header)
2. `CorrelationIdFilter` extracts or generates a correlation ID
3. Correlation ID is stored in MDC and included in all log entries
4. `CorrelationIdInterceptor` propagates the ID to downstream service calls
5. All services in the request chain share the same correlation ID
6. Use Kibana to search by `correlationId` to trace a request across all services

## Troubleshooting

### Elasticsearch not starting
```bash
# Check if vm.max_map_count is set (required for ES)
sysctl vm.max_map_count
# If too low, set it:
sudo sysctl -w vm.max_map_count=262144
```

### Fluentd not connecting to Elasticsearch
```bash
# Check Fluentd logs
docker logs ftgo-fluentd

# Verify Elasticsearch is reachable
curl http://localhost:9200/_cluster/health?pretty
```

### No logs appearing in Kibana
1. Check that the index pattern `ftgo-logs-*` exists in Kibana > Management > Index Patterns
2. Verify logs are being indexed: `curl http://localhost:9200/ftgo-logs-*/_count`
3. Ensure services are configured to output JSON logs (use `prod` profile)
