# FTGO Centralized Logging Guide

## Overview

FTGO uses an **EFK (Elasticsearch + Fluentd + Kibana)** stack for centralized log aggregation across all microservices. This replaces the default Spring Boot console-only logging with a structured logging pipeline that enables cross-service log correlation.

### Architecture

```
+------------------+     +------------------+     +------------------+
| Order Service    |     | Consumer Service |     | Restaurant Svc   |
| (JSON stdout)    |     | (JSON stdout)    |     | (JSON stdout)    |
+--------+---------+     +--------+---------+     +--------+---------+
         |                         |                        |
         +------------+------------+------------------------+
                      |
              +-------v--------+
              |    Fluentd     |  Collects, parses, enriches
              |  (DaemonSet)   |  logs from all pods/containers
              +-------+--------+
                      |
              +-------v--------+
              | Elasticsearch  |  Stores, indexes, and enables
              |  (StatefulSet) |  fast full-text search
              +-------+--------+
                      |
              +-------v--------+
              |    Kibana      |  Visualizes logs, dashboards,
              |  (Deployment)  |  and alerting
              +----------------+
```

## Structured Log Format

All FTGO services output structured JSON logs via the `ftgo-logging` shared library. Each log entry contains:

```json
{
  "@timestamp": "2026-03-03T10:15:30.123Z",
  "level": "INFO",
  "logger_name": "com.ftgo.order.OrderService",
  "message": "Order created successfully",
  "thread_name": "http-nio-8080-exec-1",
  "service": "ftgo-order-service",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "requestId": "req-001-002-003",
  "requestMethod": "POST",
  "requestUri": "/orders",
  "clientIp": "10.0.0.1"
}
```

### Standard Fields

| Field | Description | Source |
|-------|-------------|--------|
| `@timestamp` | ISO 8601 timestamp | Logback |
| `level` | Log level (DEBUG, INFO, WARN, ERROR) | Logback |
| `logger_name` | Fully qualified logger name | Logback |
| `message` | Log message | Application |
| `thread_name` | Thread name | JVM |
| `service` | Service name from `spring.application.name` | MDC |
| `correlationId` | Cross-service correlation ID | MDC (from `X-Correlation-ID` header) |
| `traceId` | Distributed trace ID | MDC (from Brave/Zipkin) |
| `spanId` | Span ID within a trace | MDC (from Brave/Zipkin) |
| `requestId` | Per-request unique ID | MDC (from `X-Request-ID` header) |
| `requestMethod` | HTTP method (GET, POST, etc.) | MDC |
| `requestUri` | Request URI path | MDC |
| `clientIp` | Client IP address | MDC |
| `stack_trace` | Exception stack trace (if present) | Logback |

## Integration Guide

### Adding Logging to a Service

1. **Add the dependency** to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':shared:ftgo-logging')
}
```

2. **Set the application name** in `application.properties` or `application.yml`:

```yaml
spring:
  application:
    name: ftgo-order-service
```

3. **Copy the Logback configuration** from the example:

```bash
cp shared/ftgo-logging/src/main/resources/logback-spring-ftgo-example.xml \
   services/ftgo-order-service/src/main/resources/logback-spring.xml
```

4. **That's it!** The auto-configuration handles the rest:
   - `CorrelationIdFilter` automatically populates MDC with request context
   - `ServiceNameInitializer` sets the service name in MDC
   - JSON structured output is enabled by default in `docker` and `production` profiles

### Correlation ID Propagation

When making inter-service HTTP calls, propagate the correlation ID:

```java
// The correlation ID is available from MDC
String correlationId = MDC.get("correlationId");

// Set it on outgoing requests
restTemplate.getInterceptors().add((request, body, execution) -> {
    request.getHeaders().set("X-Correlation-ID",
        MDC.get("correlationId"));
    return execution.execute(request, body);
});
```

### Custom Logging

Use standard SLF4J logging - the structured output is handled automatically:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for consumer: {}", request.getConsumerId());
        // ... business logic
        log.info("Order created: orderId={}", order.getId());
        return order;
    }
}
```

## Local Development

### Starting the EFK Stack

```bash
# Start the logging stack
docker compose -f docker-compose.logging.yml up -d

# Start microservices with logging
docker compose -f docker-compose.microservices.yml -f docker-compose.logging.yml up -d

# Check Elasticsearch health
curl http://localhost:9200/_cluster/health?pretty

# Open Kibana
open http://localhost:5601
```

### Viewing Logs in Kibana

1. Open Kibana at `http://localhost:5601`
2. Navigate to **Stack Management > Data Views** and create a data view:
   - Name: `FTGO Logs`
   - Index pattern: `ftgo-logs-*`
   - Timestamp field: `@timestamp`
3. Go to **Discover** to search and filter logs
4. Import pre-built dashboards:
   - Go to **Stack Management > Saved Objects**
   - Import `infrastructure/logging/kibana/dashboards/service-logs-dashboard.ndjson`

### Searching by Correlation ID

To trace a request across all services:

1. Open **Discover** in Kibana
2. In the search bar, enter: `correlationId: "your-correlation-id-here"`
3. All log entries from all services for that request will be displayed
4. Sort by `@timestamp` to see the request flow chronologically

## Kubernetes Deployment

### Deploy the EFK Stack

```bash
# Development environment
kubectl apply -k infrastructure/kubernetes/overlays/dev/logging/

# Staging environment
kubectl apply -k infrastructure/kubernetes/overlays/staging/logging/

# Production environment
kubectl apply -k infrastructure/kubernetes/overlays/production/logging/
```

### Verify Deployment

```bash
# Check pods
kubectl get pods -n ftgo-logging

# Check Elasticsearch health
kubectl exec -n ftgo-logging elasticsearch-0 -- \
  curl -s http://localhost:9200/_cluster/health?pretty

# Port-forward Kibana for local access
kubectl port-forward -n ftgo-logging svc/kibana 5601:5601
```

## Log Retention Policies

Log retention is configured per environment using Elasticsearch Index Lifecycle Management (ILM):

| Environment | Hot Phase | Warm Phase | Cold Phase | Delete After |
|------------|-----------|------------|------------|--------------|
| Development | 1 day (1 GB max) | - | - | 3 days |
| Staging | 1 day (5 GB max) | 7 days | - | 30 days |
| Production | 1 day (10 GB max) | 7 days | 30 days | 90 days |

ILM policies are defined in `infrastructure/logging/elasticsearch/ilm-policy.json` and applied via the Elasticsearch index template in `infrastructure/logging/elasticsearch/index-template.json`.

### Applying ILM Policies

```bash
# Apply the ILM policy (example for production)
curl -X PUT "http://elasticsearch:9200/_ilm/policy/ftgo-logs-production" \
  -H 'Content-Type: application/json' \
  -d @infrastructure/logging/elasticsearch/ilm-policy.json

# Apply the index template
curl -X PUT "http://elasticsearch:9200/_index_template/ftgo-logs" \
  -H 'Content-Type: application/json' \
  -d @infrastructure/logging/elasticsearch/index-template.json
```

## Alert Rules

Log-based alerts are configured in `infrastructure/logging/alerts/alert-rules.json`. Current alerts:

| Alert | Severity | Trigger |
|-------|----------|---------|
| High Error Rate | Critical | > 10 errors per minute across any service |
| Service Down | Warning | No logs from a service for 5 minutes |
| Exception Spike | Warning | 3x increase in exception rate over 15-min average |
| Slow Request | Info | Requests with > 50 log entries in 10 minutes |

Alerts can be configured to send notifications via Slack webhook. Update the `WEBHOOK_PATH` in the alert rules with your Slack webhook URL.

## Pre-built Kibana Dashboards

The following dashboards are included and can be imported via Kibana's Saved Objects:

### FTGO Service Logs Overview

- **Log Level Distribution** - Pie chart showing breakdown of log levels
- **Logs Per Service** - Histogram of log volume per service over time
- **Error Rate Over Time** - Line chart tracking error rates per service
- **Request Flow by Correlation ID** - Table showing request distribution across services
- **Service Logs** - Searchable table of all log entries

### Importing Dashboards

```bash
# Import via Kibana API
curl -X POST "http://localhost:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  --form file=@infrastructure/logging/kibana/dashboards/service-logs-dashboard.ndjson
```

## Troubleshooting

### Logs Not Appearing in Kibana

1. Check Elasticsearch is healthy: `curl http://localhost:9200/_cluster/health?pretty`
2. Check Fluentd is running: `docker logs ftgo-fluentd`
3. Verify index exists: `curl http://localhost:9200/_cat/indices?v`
4. Check Fluentd can reach Elasticsearch: `docker exec ftgo-fluentd curl http://elasticsearch:9200`

### JSON Parsing Errors in Fluentd

Ensure your service is outputting valid JSON logs. Check with:
```bash
docker logs ftgo-order-service 2>&1 | head -5 | python3 -m json.tool
```

### High Memory Usage

Elasticsearch can be memory-intensive. For local development, reduce heap size:
```yaml
# In docker-compose.logging.yml
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"
```
