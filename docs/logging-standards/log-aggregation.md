# Log Aggregation Patterns and Search Queries

This document defines patterns for searching, filtering, and analyzing logs in the FTGO centralized logging system (ELK stack). For infrastructure setup, see [docs/logging/README.md](../logging/README.md).

## Architecture Recap

```
Services (JSON logs) --> Logstash (TCP :5000) --> Elasticsearch --> Kibana
                    \--> Filebeat (Docker) --/
```

- Index pattern: `ftgo-logs-YYYY.MM.dd`
- Retention: 30 days (ILM: hot -> warm -> cold -> delete)

## Kibana Query Language (KQL) Patterns

### By Service

```
service: "order-service"
service: "order-service" or service: "consumer-service"
```

### By Log Level

```
level: "ERROR"
level: "ERROR" or level: "WARN"
not level: "DEBUG" and not level: "TRACE"
```

### By Trace ID (Cross-Service Correlation)

The most powerful query for debugging distributed requests:

```
traceId: "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"
```

This returns all log entries across all services for a single distributed trace.

### By Request ID

```
requestId: "550e8400-e29b-41d4-a716-446655440000"
```

### By User

```
userId: "consumer-123"
```

### By Time Range + Service + Level

```
service: "order-service" and level: "ERROR"
```

Combined with Kibana's time picker for the desired window.

### Full-Text Message Search

```
message: "Failed to create order"
message: "timeout" or message: "connection refused"
```

### By Exception Type

```
exception: "RestaurantNotFoundException"
exception: "OptimisticLockingFailureException"
```

### By Custom Context Fields

```
context.orderId: "ORD-12345"
context.eventType: "ORDER_CREATED"
context.destinationChannel: "order-events"
```

## Common Debugging Workflows

### 1. Trace a Failed Request End-to-End

When an error is reported (e.g., from an alert or user complaint):

1. Find the error log entry:
   ```
   service: "order-service" and level: "ERROR" and message: "Failed*"
   ```

2. Copy the `traceId` from the error entry.

3. Search for all logs in that trace:
   ```
   traceId: "<copied-trace-id>"
   ```

4. Sort by `timestamp` ascending to see the full request flow across services.

5. Cross-reference with Jaeger: open `http://<jaeger-host>:16686/trace/<traceId>` for the distributed trace timeline.

### 2. Investigate Error Spikes

1. In Kibana, open the **FTGO Service Logs Overview** dashboard.
2. Look at the **Error Trends** panel for spikes.
3. Drill down by clicking the spike time range.
4. Filter:
   ```
   level: "ERROR"
   ```
5. Check if errors are concentrated in one service or spread across multiple.
6. Group by `exception` field to identify the most common error type.

### 3. Monitor a Specific Order

```
context.orderId: "ORD-12345"
```

This shows all log entries related to order ORD-12345, regardless of which service produced them (assuming MDC was set properly).

### 4. Find Slow Operations

If services log warnings for slow operations:

```
service: "order-service" and level: "WARN" and message: "Slow*"
```

### 5. Check Service Startup

```
service: "order-service" and message: "Started*"
```

## Elasticsearch Query DSL

For programmatic access or saved searches, use Elasticsearch Query DSL:

### Errors in Last Hour

```json
{
  "query": {
    "bool": {
      "must": [
        { "term": { "level": "ERROR" } },
        { "range": { "timestamp": { "gte": "now-1h" } } }
      ]
    }
  },
  "sort": [{ "timestamp": { "order": "desc" } }]
}
```

### Error Count by Service (Aggregation)

```json
{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        { "term": { "level": "ERROR" } },
        { "range": { "timestamp": { "gte": "now-24h" } } }
      ]
    }
  },
  "aggs": {
    "errors_by_service": {
      "terms": { "field": "service", "size": 20 }
    }
  }
}
```

### Top Exceptions in Last 24 Hours

```json
{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        { "exists": { "field": "exception" } },
        { "range": { "timestamp": { "gte": "now-24h" } } }
      ]
    }
  },
  "aggs": {
    "top_exceptions": {
      "terms": { "field": "exception.keyword", "size": 10 }
    }
  }
}
```

### Logs for a Specific Trace

```json
{
  "query": {
    "term": { "traceId": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6" }
  },
  "sort": [{ "timestamp": { "order": "asc" } }]
}
```

## Alerting Patterns

Configure Kibana alerting (or Elasticsearch Watcher) for these conditions:

### Error Rate Alert

Trigger when error count exceeds threshold per service:

| Condition | Threshold | Window | Severity |
|-----------|-----------|--------|----------|
| Error count per service | > 50 | 5 minutes | Warning |
| Error count per service | > 200 | 5 minutes | Critical |
| Error rate (% of total) | > 5% | 5 minutes | Warning |
| Error rate (% of total) | > 15% | 5 minutes | Critical |

KQL for alert condition:
```
level: "ERROR" and service: "<service-name>"
```

### Missing Logs Alert

Trigger when a service stops sending logs (indicates service is down or logging pipeline is broken):

| Condition | Threshold | Window |
|-----------|-----------|--------|
| Log count per service | < 1 | 10 minutes |

### Exception Spike Alert

Trigger when a specific exception type increases sharply:

| Condition | Threshold | Window |
|-----------|-----------|--------|
| Same exception count | > 20 | 5 minutes |

## Saved Search Templates

Create these saved searches in Kibana for team use:

| Name | Query | Columns |
|------|-------|---------|
| All Errors | `level: "ERROR"` | timestamp, service, message, exception |
| All Warnings | `level: "WARN"` | timestamp, service, message |
| Trace Lookup | `traceId: "<REPLACE>"` | timestamp, service, level, message |
| Order Lookup | `context.orderId: "<REPLACE>"` | timestamp, service, level, message |
| Service Errors (template) | `service: "<REPLACE>" and level: "ERROR"` | timestamp, message, exception, stackTrace |
| Slow Queries | `message: "Slow*" or message: "slow*"` | timestamp, service, message |

## Log Volume Management

### Estimated Volume per Environment

| Environment | Services | Avg Logs/sec | Daily Volume |
|-------------|----------|-------------|-------------|
| Development | 1-3 | 10-50 | ~500 MB |
| Staging | 8-10 | 100-500 | ~5 GB |
| Production | 8-10 | 500-5000 | ~50 GB |

### Volume Reduction Strategies

1. **Log level tuning** - Follow the [log level matrix](log-levels.md) strictly; avoid DEBUG in production.
2. **Sampling** - For extremely high-volume DEBUG logs, consider sampling (log every Nth occurrence).
3. **Rate limiting** - Logback's `TurboFilter` can suppress duplicate messages.
4. **Index lifecycle** - The ILM policy (hot/warm/cold/delete at 30d) manages storage automatically.
5. **Selective indexing** - Use Logstash filters to drop or reduce fields for high-volume, low-value logs.
