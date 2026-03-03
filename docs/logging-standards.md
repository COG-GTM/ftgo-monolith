# FTGO Logging Standards

## Overview

This document defines the logging standards and conventions that all FTGO microservices must follow. These standards ensure consistent, searchable, and actionable log output across the platform.

All services MUST use the `shared/ftgo-logging` library, which provides structured logging, MDC propagation, sensitive data masking, and per-environment configuration out of the box.

---

## 1. Log Levels

### Level Definitions

| Level   | Purpose                                                        | Examples                                                        |
|---------|----------------------------------------------------------------|-----------------------------------------------------------------|
| `ERROR` | Unrecoverable failures requiring immediate attention           | Database connection lost, payment processing failure, unhandled exception |
| `WARN`  | Unexpected conditions that are recoverable                     | Retry attempt, deprecated API usage, fallback triggered, slow query |
| `INFO`  | Significant business events and state transitions              | Order created, payment processed, service started, user authenticated |
| `DEBUG` | Detailed diagnostic information for troubleshooting            | Method entry/exit, request/response payloads, cache hit/miss    |
| `TRACE` | Very fine-grained diagnostic data (rarely used in production)  | Loop iterations, field-level value changes, wire-level protocol data |

### Level Selection Guidelines

- **Default to `INFO`** for business-significant operations (order lifecycle, authentication events, configuration changes).
- Use `WARN` when something unexpected happened but the system can continue normally.
- Use `ERROR` only for conditions that require human intervention or indicate data loss.
- Use `DEBUG` for information useful during development and troubleshooting.
- Use `TRACE` sparingly; it generates high volume and should only be enabled temporarily for specific investigations.
- **Never** log at `INFO` or above inside tight loops or high-frequency operations.

---

## 2. What to Log

### MUST Log

- Service startup and shutdown events (`INFO`)
- Incoming HTTP requests and responses (status code, duration) (`INFO`)
- Business state transitions (order created, payment completed) (`INFO`)
- Authentication and authorization events (`INFO`)
- External service calls (target, duration, status) (`INFO` / `WARN` on failure)
- Configuration values at startup (non-sensitive) (`INFO`)
- Retry attempts and circuit breaker state changes (`WARN`)
- Unhandled exceptions with full stack traces (`ERROR`)

### SHOULD Log

- Cache hits and misses (`DEBUG`)
- Database query execution (query type, duration) (`DEBUG`)
- Method entry and exit for key service methods (`DEBUG`)
- Request/response bodies for debugging (at `DEBUG` only, with masking)
- Scheduled task execution (`INFO`)

### MUST NOT Log

- **Passwords** or password hashes
- **Credit card numbers** (full or partial beyond last 4 digits)
- **API keys**, tokens, or secrets
- **Social Security Numbers** or government-issued IDs
- **Personal health information** (PHI)
- **Personally Identifiable Information** (PII) such as full names combined with addresses, email addresses in plain text, or phone numbers
- Session tokens or authentication cookies
- Database connection strings with credentials
- Encryption keys or certificates

> **Note:** The `ftgo-logging` library includes automatic sensitive data masking as a safety net, but developers MUST NOT rely on it as the sole protection. Avoid logging sensitive data in the first place.

---

## 3. Structured Log Format

All services use structured JSON logging in deployed environments. Every log entry includes the following standard fields:

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
  "userId": "user-42"
}
```

### Standard MDC Fields

| MDC Key         | Description                                  | Set By                        |
|-----------------|----------------------------------------------|-------------------------------|
| `correlationId` | Cross-service correlation ID                 | `CorrelationIdFilter`         |
| `traceId`       | Distributed trace ID (Brave/Zipkin)          | Tracing library auto-config   |
| `spanId`        | Span ID within a trace                       | Tracing library auto-config   |
| `serviceName`   | Name of the service producing the log entry  | `ServiceNameInitializer`      |
| `requestId`     | Per-request unique ID                        | `CorrelationIdFilter`         |
| `userId`        | Authenticated user identifier                | Application code / `LogContext` |

### Setting MDC Fields in Application Code

Use the `LogContext` utility to manage MDC fields:

```java
import com.ftgo.logging.LogContext;

// Set user context after authentication
LogContext.setUserId("user-42");

// Set custom context fields
LogContext.put("orderId", "order-123");

// Clear all FTGO MDC fields (typically done by filters automatically)
LogContext.clear();
```

---

## 4. Sensitive Data Masking

The `ftgo-logging` library includes a `SensitiveDataMaskingConverter` that automatically masks sensitive patterns in log output:

| Data Type        | Pattern Example          | Masked Output              |
|------------------|--------------------------|----------------------------|
| Credit card      | `4111111111111111`       | `************1111`         |
| Password fields  | `password=secret123`     | `password=********`        |
| Bearer tokens    | `Bearer eyJhbGciOi...`  | `Bearer [MASKED]`          |
| Authorization    | `Authorization: Basic x` | `Authorization: [MASKED]`  |

Masking is applied at the Logback encoder level before log output, providing defense-in-depth. However, developers should still follow the "MUST NOT Log" rules above.

---

## 5. Per-Environment Configuration

### Log Levels by Environment

| Environment       | Root Level | FTGO Packages | Frameworks (Spring, Hibernate) |
|-------------------|------------|---------------|-------------------------------|
| `local` / `dev`   | `DEBUG`    | `DEBUG`       | `WARN`                        |
| `docker`          | `INFO`     | `INFO`        | `WARN`                        |
| `staging`         | `INFO`     | `INFO`        | `WARN`                        |
| `production`      | `INFO`     | `INFO`        | `ERROR`                       |

### Output Format by Environment

| Environment       | Format         | Appender        |
|-------------------|----------------|-----------------|
| `local` / `dev`   | Human-readable | `CONSOLE_PLAIN` |
| `docker`          | JSON           | `ASYNC_JSON`    |
| `staging`         | JSON           | `ASYNC_JSON`    |
| `production`      | JSON           | `ASYNC_JSON`    |

### Async Logging

In deployed environments (`docker`, `staging`, `production`), logging uses an async appender to avoid blocking application threads. Configuration:

- Queue size: 1024
- Discard threshold: 0 (never discard)
- Never block: true (drop logs rather than block application threads under extreme load)

---

## 6. Logging Aspect (Method Entry/Exit)

The `LoggingAspect` provides automatic method entry/exit logging for classes annotated with `@Loggable` or within `@Service`, `@RestController`, and `@Repository` annotated classes.

### Usage

```java
import com.ftgo.logging.annotation.Loggable;

@Loggable
@Service
public class OrderService {
    // Method entry/exit will be logged at DEBUG level automatically
    public Order createOrder(CreateOrderRequest request) {
        // ...
    }
}
```

### What Gets Logged

- **Entry**: Method name and parameter types (not values, to avoid PII leakage)
- **Exit**: Method name, return type, and execution duration in milliseconds
- **Exception**: Method name, exception type, and message (at `WARN` level)

---

## 7. Logback Configuration

### Including FTGO Defaults

Every service should use the shared `logback-spring.xml` configuration. The library provides a complete `logback-spring.xml` that can be included on the classpath:

```xml
<configuration>
    <include resource="ftgo-logback-defaults.xml" />

    <!-- Profile-specific configuration is included automatically -->
    <!-- Override only if service-specific customization is needed -->
</configuration>
```

### File Rotation

For environments that write to files (when Fluentd is not available):

- Max file size: 100 MB
- Max history: 30 days
- Total size cap: 1 GB
- Files are compressed (.gz) after rotation

---

## 8. Best Practices

### DO

- Use SLF4J parameterized logging: `log.info("Order {} created for consumer {}", orderId, consumerId)`
- Include relevant context (IDs, counts, durations) in log messages
- Log at the appropriate level (see Level Definitions above)
- Use `LogContext` to set MDC fields for cross-cutting context
- Log the start and end of significant operations with timing information
- Include exception objects as the last argument: `log.error("Failed to process order {}", orderId, exception)`

### DON'T

- Don't use string concatenation: `log.info("Order " + orderId + " created")` (inefficient)
- Don't log sensitive data (see MUST NOT Log section)
- Don't log at `INFO` or above in tight loops
- Don't catch and log exceptions without rethrowing or handling them (log-and-swallow anti-pattern)
- Don't use `System.out.println()` or `System.err.println()` for logging
- Don't use `e.printStackTrace()` - use `log.error("message", e)` instead
- Don't create a new Logger for each method call; use a class-level static field
