# Log Format Standards

All FTGO services must emit structured JSON logs in staging and production environments. This document defines the canonical JSON schema, required and optional fields, and formatting conventions.

## JSON Log Schema

### Required Fields

Every log entry **must** contain the following top-level fields:

| Field | Type | Source | Description |
|-------|------|--------|-------------|
| `timestamp` | `string` | Logback | ISO 8601 with milliseconds and timezone: `yyyy-MM-dd'T'HH:mm:ss.SSSXXX` |
| `level` | `string` | Logback | Log level: `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` |
| `logger` | `string` | Logback | Fully qualified logger name (class name) |
| `thread` | `string` | Logback | Thread name |
| `message` | `string` | Application | Human-readable log message |
| `service` | `string` | Configuration | Service name from `spring.application.name` |

### Correlation Fields

These fields are populated automatically by `libs/ftgo-logging` and `libs/ftgo-tracing` via MDC:

| Field | Type | Source | Description |
|-------|------|--------|-------------|
| `traceId` | `string` | ftgo-tracing | Distributed trace identifier (W3C trace ID) |
| `spanId` | `string` | ftgo-tracing | Current span identifier |
| `requestId` | `string` | ftgo-logging | Unique request ID (from `X-Request-ID` header or auto-generated UUID) |
| `userId` | `string` | Application/MDC | Authenticated user identifier (when available) |

### Optional Fields

| Field | Type | Source | Description |
|-------|------|--------|-------------|
| `context` | `object` | MDC (overflow) | Additional MDC key-value pairs not in the standard set |
| `exception` | `string` | Logback | Exception class and message (when throwable is present) |
| `stackTrace` | `string` | Logback | Full stack trace string (when throwable is present) |

## Canonical JSON Example

### Standard Log Entry

```json
{
  "timestamp": "2026-01-15T10:30:45.123+00:00",
  "level": "INFO",
  "logger": "com.ftgo.order.service.OrderService",
  "thread": "http-nio-8080-exec-1",
  "message": "Order created successfully",
  "service": "order-service",
  "traceId": "a]b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5",
  "spanId": "1a2b3c4d5e6f7a8b",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "consumer-123"
}
```

### Log Entry with Exception

```json
{
  "timestamp": "2026-01-15T10:31:02.456+00:00",
  "level": "ERROR",
  "logger": "com.ftgo.order.service.OrderService",
  "thread": "http-nio-8080-exec-3",
  "message": "Failed to create order",
  "service": "order-service",
  "traceId": "f1e2d3c4b5a6f7e8d9c0b1a2f3e4d5c6",
  "spanId": "8a7b6c5d4e3f2a1b",
  "requestId": "661f9511-f3ac-52e5-b827-557766551111",
  "userId": "consumer-456",
  "exception": "com.ftgo.order.exception.RestaurantNotFoundException: Restaurant 789 not found",
  "stackTrace": "com.ftgo.order.exception.RestaurantNotFoundException: Restaurant 789 not found\\n\\tat com.ftgo.order.service.OrderService.createOrder(OrderService.java:45)\\n\\t..."
}
```

### Log Entry with Custom Context

```json
{
  "timestamp": "2026-01-15T10:32:15.789+00:00",
  "level": "INFO",
  "logger": "com.ftgo.order.messaging.OrderEventPublisher",
  "thread": "order-event-publisher-1",
  "message": "Order event published",
  "service": "order-service",
  "traceId": "c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9",
  "spanId": "2b3c4d5e6f7a8b9c",
  "requestId": "772a0622-g4bd-63f6-c938-668877662222",
  "context": {
    "orderId": "ORD-12345",
    "eventType": "ORDER_CREATED",
    "destinationChannel": "order-events"
  }
}
```

## Field Naming Conventions

- Use **camelCase** for all field names.
- Top-level standard fields use the names defined in this document exactly.
- Custom context fields should be descriptive and use domain-specific prefixes when ambiguous.
- Do not use dots in field names (Elasticsearch maps them to nested objects).

| Convention | Example | Anti-Pattern |
|-----------|---------|-------------|
| camelCase | `orderId` | `order_id`, `OrderId` |
| Descriptive | `restaurantName` | `name`, `rn` |
| No dots | `orderStatus` | `order.status` |
| ID suffix for identifiers | `consumerId` | `consumer`, `consumerIdentifier` |

## Timestamp Format

All timestamps must use ISO 8601 format with millisecond precision and timezone offset:

```
yyyy-MM-dd'T'HH:mm:ss.SSSXXX
```

Example: `2026-01-15T10:30:45.123+00:00`

This is configured in `libs/ftgo-logging` via the `FtgoJsonEncoder` and the Logstash encoder's `timestampPattern`.

## Implementation

The `FtgoJsonEncoder` in `libs/ftgo-logging` produces this format automatically. When using the `logback-ftgo.xml` include fragment or the `LogstashEncoder`, the format is applied through configuration.

To ensure compliance, services should:

1. Include `libs/ftgo-logging` as a dependency.
2. Use one of the Logback templates from `config/logback/`.
3. Set `spring.application.name` to populate the `service` field.
4. Use SLF4J parameterized logging for all log statements.

### SLF4J Usage

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public void createOrder(String orderId, String consumerId) {
        logger.info("Order created: orderId={}, consumerId={}", orderId, consumerId);
    }
}
```

### Adding Custom Context via MDC

```java
import org.slf4j.MDC;

try {
    MDC.put("orderId", orderId);
    MDC.put("eventType", "ORDER_CREATED");
    logger.info("Processing order event");
} finally {
    MDC.remove("orderId");
    MDC.remove("eventType");
}
```

Custom MDC fields appear under the `context` object in the JSON output.

## Elasticsearch Field Mapping

The JSON fields map to Elasticsearch index fields as follows:

| JSON Field | Elasticsearch Type | Indexed | Notes |
|-----------|-------------------|---------|-------|
| `timestamp` | `date` | Yes | Primary time field |
| `level` | `keyword` | Yes | Used for filtering |
| `logger` | `keyword` | Yes | Used for filtering |
| `thread` | `keyword` | Yes | |
| `message` | `text` | Yes | Full-text searchable |
| `service` | `keyword` | Yes | Used for filtering/aggregation |
| `traceId` | `keyword` | Yes | Used for correlation |
| `spanId` | `keyword` | Yes | Used for correlation |
| `requestId` | `keyword` | Yes | Used for correlation |
| `userId` | `keyword` | Yes | Used for filtering |
| `exception` | `text` | Yes | Full-text searchable |
| `stackTrace` | `text` | Yes | Full-text searchable |
| `context.*` | `keyword` | Yes | Dynamic mapping |
