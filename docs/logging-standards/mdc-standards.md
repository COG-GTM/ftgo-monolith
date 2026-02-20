# MDC (Mapped Diagnostic Context) Standards

This document defines the standard MDC fields used across all FTGO microservices, how they are populated, and guidelines for adding custom MDC fields.

## Overview

MDC (Mapped Diagnostic Context) is a thread-local map managed by SLF4J that enriches every log entry with contextual data. In FTGO, MDC fields are the primary mechanism for correlating logs across services and providing context for debugging.

The `libs/ftgo-logging` library automatically includes MDC fields in the structured JSON output via the `FtgoJsonEncoder` and `LogstashEncoder`.

## Standard MDC Fields

### Automatically Populated Fields

These fields are set by the FTGO libraries and should not be manually overridden:

| MDC Key | Source | Lifecycle | Description |
|---------|--------|-----------|-------------|
| `traceId` | `libs/ftgo-tracing` (Micrometer/OpenTelemetry) | Request scope | W3C trace identifier propagated across service boundaries |
| `spanId` | `libs/ftgo-tracing` (Micrometer/OpenTelemetry) | Span scope | Current span identifier within the trace |
| `requestId` | `libs/ftgo-logging` (`RequestIdFilter`) | Request scope | Unique request ID from `X-Request-ID` header or auto-generated UUID |

### Application-Populated Fields

These fields should be set by application code at appropriate points:

| MDC Key | When to Set | Lifecycle | Description |
|---------|------------|-----------|-------------|
| `userId` | After authentication | Request scope | Authenticated user/consumer ID |
| `orderId` | During order processing | Operation scope | Order identifier for order-related operations |
| `consumerId` | After authentication or from request | Request/operation scope | Consumer identifier |
| `restaurantId` | During restaurant operations | Operation scope | Restaurant identifier |
| `deliveryId` | During delivery operations | Operation scope | Delivery identifier |

## MDC Lifecycle Management

### Request-Scoped Fields

Request-scoped MDC fields are set at the start of an HTTP request and cleared when the request completes. The `RequestIdFilter` in `libs/ftgo-logging` handles `requestId` automatically.

For `userId`, set it in a security filter or interceptor after authentication:

```java
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserMdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            MDC.put("userId", auth.getName());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
        }
    }
}
```

### Operation-Scoped Fields

Operation-scoped MDC fields are set for a specific business operation and cleared afterward. Always use try-finally to ensure cleanup:

```java
public void processOrder(String orderId, String consumerId) {
    MDC.put("orderId", orderId);
    MDC.put("consumerId", consumerId);
    try {
        logger.info("Processing order");
        // ... business logic ...
        logger.info("Order processed successfully");
    } finally {
        MDC.remove("orderId");
        MDC.remove("consumerId");
    }
}
```

### Async / Thread-Crossing Fields

MDC is thread-local, so it does not automatically propagate to child threads or async tasks. Use these patterns:

#### TaskDecorator for @Async Methods

```java
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

Register the decorator with your async executor:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
```

#### CompletableFuture

```java
Map<String, String> mdcContext = MDC.getCopyOfContextMap();

CompletableFuture.supplyAsync(() -> {
    if (mdcContext != null) {
        MDC.setContextMap(mdcContext);
    }
    try {
        logger.info("Async operation executing");
        return doWork();
    } finally {
        MDC.clear();
    }
});
```

#### Kafka Consumers

For message-driven operations, extract correlation IDs from message headers and set MDC.

> **Note:** Kafka consumers are an exception to the "don't manually set `traceId`" rule
> because there is no HTTP request context for the tracing library to instrument.
> In this case, manually restoring the `traceId` from the message header is required
> to maintain cross-service correlation.

```java
@KafkaListener(topics = "order-events")
public void handleOrderEvent(ConsumerRecord<String, String> record) {
    Header traceHeader = record.headers().lastHeader("traceId");
    if (traceHeader != null) {
        MDC.put("traceId", new String(traceHeader.value(), StandardCharsets.UTF_8));
    }
    MDC.put("orderId", record.key());
    try {
        logger.info("Processing order event: type={}", extractEventType(record));
        // ... handle event ...
    } finally {
        MDC.clear();
    }
}
```

## Custom MDC Fields

### Naming Conventions

| Rule | Example | Anti-Pattern |
|------|---------|-------------|
| Use camelCase | `orderId` | `order_id`, `OrderId` |
| Use `Id` suffix for identifiers | `restaurantId` | `restaurant`, `restId` |
| Be descriptive | `paymentMethod` | `pm`, `method` |
| Avoid generic names | `orderStatus` | `status`, `state` |
| No dots in keys | `eventType` | `event.type` |

### Service-Specific MDC Fields

Each service may define additional MDC fields relevant to its domain. Register them in the table below when adding new fields:

| Service | MDC Key | Description |
|---------|---------|-------------|
| order-service | `orderId` | Order identifier |
| order-service | `orderStatus` | Current order state |
| consumer-service | `consumerId` | Consumer identifier |
| restaurant-service | `restaurantId` | Restaurant identifier |
| restaurant-service | `menuItemId` | Menu item identifier |
| delivery-service | `deliveryId` | Delivery identifier |
| delivery-service | `courierId` | Courier/driver identifier |
| kitchen-service | `ticketId` | Kitchen ticket identifier |
| accounting-service | `paymentId` | Payment transaction identifier |
| api-gateway | `routeId` | Gateway route identifier |
| api-gateway | `clientIp` | Client IP (masked in production) |

## MDC and the JSON Output

Standard MDC fields (`traceId`, `spanId`, `requestId`, `userId`) are promoted to top-level JSON fields by the `FtgoJsonEncoder`. All other MDC fields appear under the `context` object:

```json
{
  "timestamp": "2026-01-15T10:30:45.123+00:00",
  "level": "INFO",
  "message": "Order created",
  "service": "order-service",
  "traceId": "abc123",
  "spanId": "def456",
  "requestId": "550e8400-...",
  "userId": "consumer-123",
  "context": {
    "orderId": "ORD-12345",
    "restaurantId": "REST-789"
  }
}
```

## Best Practices

1. **Always clean up** - Use try-finally to remove MDC fields after the scope ends.
2. **Set early, remove late** - Populate MDC as soon as context is available, remove it at the outermost scope.
3. **Don't overwrite standard fields** - Never manually set `traceId`, `spanId`, or `requestId`; let the libraries handle them.
4. **Keep values short** - MDC values are included in every log line; avoid large strings.
5. **No sensitive data** - Never put passwords, tokens, or PII into MDC.
6. **Propagate across threads** - Use `MdcTaskDecorator` or manual `MDC.getCopyOfContextMap()` for async operations.
7. **Propagate across services** - Use message headers (Kafka, HTTP) to pass correlation IDs across service boundaries.
