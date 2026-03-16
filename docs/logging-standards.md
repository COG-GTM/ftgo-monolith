# FTGO Logging Standards

This document defines the logging standards that all FTGO microservices must follow. Consistent logging practices are critical for debugging, monitoring, auditing, and maintaining production systems.

## Table of Contents

1. [Log Level Guidelines](#log-level-guidelines)
2. [What to Log](#what-to-log)
3. [What NOT to Log](#what-not-to-log)
4. [Structured Logging Format](#structured-logging-format)
5. [Per-Environment Configuration](#per-environment-configuration)
6. [MDC Context Fields](#mdc-context-fields)
7. [Sensitive Data Masking](#sensitive-data-masking)
8. [Best Practices](#best-practices)

---

## Log Level Guidelines

### TRACE
- **Purpose**: Finest-grained diagnostic information.
- **When to use**:
  - Method entry/exit with parameter values (handled by `LoggingAspect`)
  - Detailed loop iterations or algorithm steps
  - Wire-level protocol data (HTTP headers, serialized payloads)
- **Environment**: Never enabled in production. Use only in local development for deep debugging.
- **Example**:
  ```java
  logger.trace("Resolving restaurant menu items for restaurantId={}", restaurantId);
  ```

### DEBUG
- **Purpose**: Detailed diagnostic information useful during development.
- **When to use**:
  - Variable values at important decision points
  - SQL queries and cache hits/misses
  - Configuration values loaded at startup
  - Request/response payloads for inter-service calls
- **Environment**: Enabled in `local` and `dev` profiles. Disabled in `staging` and `prod`.
- **Example**:
  ```java
  logger.debug("Order state transition: {} -> {} for orderId={}", oldState, newState, orderId);
  ```

### INFO
- **Purpose**: General operational events that confirm the system is working as expected.
- **When to use**:
  - Application startup and shutdown
  - Service registration/deregistration
  - Successful completion of significant business operations (order created, payment processed)
  - Configuration changes or feature flag toggles
  - Scheduled job execution start/completion
- **Environment**: Enabled in all environments.
- **Example**:
  ```java
  logger.info("Order {} created for consumer {} at restaurant {}", orderId, consumerId, restaurantId);
  ```

### WARN
- **Purpose**: Potentially harmful situations that do not prevent the current operation from completing.
- **When to use**:
  - Deprecated API usage
  - Retry attempts (with retry count)
  - Approaching resource limits (connection pool, memory, disk)
  - Fallback logic activation (circuit breaker open, default values used)
  - Missing optional configuration (using defaults)
- **Environment**: Enabled in all environments.
- **Example**:
  ```java
  logger.warn("Retry attempt {}/{} for payment processing, orderId={}", attempt, maxRetries, orderId);
  ```

### ERROR
- **Purpose**: Error events that prevent a specific operation from completing. The application may still be able to serve other requests.
- **When to use**:
  - Unhandled exceptions
  - Failed external service calls (after all retries exhausted)
  - Data integrity violations
  - Failed business rule validations that indicate a bug
  - Security violations (unauthorized access attempts)
- **Always include**: The exception object as the last parameter so the full stack trace is logged.
- **Environment**: Enabled in all environments. Should trigger alerts in production.
- **Example**:
  ```java
  logger.error("Failed to process payment for orderId={}, consumerId={}", orderId, consumerId, exception);
  ```

---

## What to Log

### Always Log
- **Business events**: Order creation, state transitions, payment processing, delivery status changes.
- **Authentication events**: Login success/failure, token refresh, session creation/invalidation.
- **External service calls**: Outbound HTTP calls with URL, method, response status, and latency.
- **Error conditions**: All caught exceptions with context (what operation failed and why).
- **Performance data**: Request duration for slow operations (above configurable threshold).
- **Configuration at startup**: Active profiles, feature flags, and connection pool sizes.
- **Lifecycle events**: Application start, stop, health check status changes.

### Log Format
Always use parameterized logging (SLF4J `{}` placeholders) instead of string concatenation:

```java
// GOOD - parameterized (no string allocation if level is disabled)
logger.info("Order {} created for consumer {}", orderId, consumerId);

// BAD - string concatenation (always allocates)
logger.info("Order " + orderId + " created for consumer " + consumerId);
```

---

## What NOT to Log

### Never Log (Compliance & Security)
| Data Type | Examples | Reason |
|-----------|----------|--------|
| **Passwords / secrets** | User passwords, API keys, JWT signing keys | Security breach risk |
| **Full credit card numbers** | `4111111111111111` | PCI-DSS compliance |
| **Social Security Numbers** | `123-45-6789` | PII / regulatory compliance |
| **Authentication tokens** | Bearer tokens, session IDs, OAuth tokens | Session hijacking risk |
| **Database connection strings** | JDBC URLs with credentials | Credential exposure |
| **Personal health information** | Medical records, diagnoses | HIPAA compliance |
| **Full email addresses** | In bulk/list operations | GDPR/privacy regulations |

### Avoid Logging
- **Request/response bodies** in production (use DEBUG level only).
- **Stack traces for expected exceptions** (e.g., validation errors) - use WARN with a message instead.
- **High-frequency repetitive messages** - use sampling or rate limiting.
- **Binary data** - log metadata (size, type) instead of content.

### Sensitive Data Masking
The `ftgo-logging-lib` provides a `SensitiveDataMaskingConverter` that automatically masks sensitive patterns in log messages. See [Sensitive Data Masking](#sensitive-data-masking) for details.

---

## Structured Logging Format

All services use structured JSON logging in deployed environments. The standard fields are:

```json
{
  "@timestamp": "2024-01-15T10:30:00.123Z",
  "@version": "1",
  "message": "Order 12345 created for consumer 67890",
  "logger_name": "n.c.f.order.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "service": "order-service",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "serviceName": "order-service",
  "traceId": "64-char-hex-trace-id",
  "spanId": "16-char-hex-span-id",
  "requestMethod": "POST",
  "requestUri": "/orders",
  "clientIp": "10.0.0.1",
  "userId": "consumer-67890"
}
```

### Required MDC Fields

Every log entry in a request context must include:

| Field | Source | Description |
|-------|--------|-------------|
| `serviceName` | `spring.application.name` | Identifies the originating service |
| `correlationId` | HTTP header or generated | End-to-end request correlation |
| `traceId` | Spring Cloud Sleuth | Distributed trace identifier |
| `spanId` | Spring Cloud Sleuth | Current span identifier |

---

## Per-Environment Configuration

### Log Levels by Environment

| Logger / Environment | `local` | `dev` | `staging` | `prod` |
|----------------------|---------|-------|-----------|--------|
| Root logger | `INFO` | `INFO` | `INFO` | `INFO` |
| `net.chrisrichardson.ftgo` | `DEBUG` | `DEBUG` | `INFO` | `INFO` |
| `org.springframework` | `WARN` | `WARN` | `WARN` | `WARN` |
| `org.hibernate` | `WARN` | `WARN` | `WARN` | `WARN` |
| `org.apache` | `WARN` | `WARN` | `WARN` | `WARN` |

### Output Format by Environment

| Environment | Format | Appender | Async |
|-------------|--------|----------|-------|
| `local` / `dev` | Human-readable (pattern) | Console + Rolling File | No (console), Yes (file) |
| `staging` | JSON | Async Console | Yes |
| `prod` / `k8s` | JSON | Async Console | Yes |

### Spring Profile Mapping

Set via `spring.profiles.active`:

```properties
# Local development
spring.profiles.active=local

# Deployed environments
spring.profiles.active=prod
```

### Dynamic Log Level Changes

Use Spring Boot Actuator to change log levels at runtime without restart:

```bash
# View current log level
curl http://localhost:8081/actuator/loggers/net.chrisrichardson.ftgo

# Change log level
curl -X POST http://localhost:8081/actuator/loggers/net.chrisrichardson.ftgo \
  -H 'Content-Type: application/json' \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## MDC Context Fields

The logging library automatically manages MDC fields through `CorrelationIdFilter` and `LogContext`.

### Setting Custom MDC Fields

Use `LogContext` to add business context to logs:

```java
import net.chrisrichardson.ftgo.logging.LogContext;

// In a service method
try (LogContext ctx = LogContext.create()
        .userId(authenticatedUserId)
        .put("orderId", orderId)
        .put("restaurantId", restaurantId)
        .apply()) {
    logger.info("Processing order");
    // All logs within this block include orderId and restaurantId
}
// MDC fields are automatically cleaned up
```

### Thread Propagation

When dispatching work to thread pools, use `MdcContextHolder` to propagate context:

```java
import net.chrisrichardson.ftgo.logging.MdcContextHolder;

executor.submit(MdcContextHolder.wrapWithContext(() -> {
    // MDC context is available in this thread
    logger.info("Async task completed");
}));
```

---

## Sensitive Data Masking

The `SensitiveDataMaskingConverter` automatically masks the following patterns in all log messages:

| Pattern | Example Input | Masked Output |
|---------|---------------|---------------|
| Credit card numbers | `4111111111111111` | `411111******1111` |
| SSN | `123-45-6789` | `***-**-6789` |
| Passwords in key-value pairs | `password=secret123` | `password=********` |
| Bearer tokens | `Bearer eyJhbGc...` | `Bearer [MASKED]` |
| Email addresses | `user@example.com` | `u***@example.com` |

Masking is applied transparently at the Logback encoder level and does not require any code changes.

---

## Best Practices

### 1. Use SLF4J Logger
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

### 2. Check Log Level for Expensive Operations
```java
if (logger.isDebugEnabled()) {
    logger.debug("Order details: {}", expensiveSerialize(order));
}
```

### 3. Always Include Context
```java
// GOOD - includes business context
logger.error("Failed to create order for consumer={}, restaurant={}", consumerId, restaurantId, ex);

// BAD - no context
logger.error("Order creation failed", ex);
```

### 4. Use Structured Key-Value Pairs
```java
// GOOD - parseable by log aggregation tools
logger.info("Order created: orderId={}, total={}, items={}", orderId, total, itemCount);

// BAD - inconsistent format
logger.info("Created an order with ID " + orderId + " totaling $" + total);
```

### 5. Log at the Right Level
- Don't log expected validation failures as ERROR (use WARN or DEBUG).
- Don't log routine operations as WARN (use INFO or DEBUG).
- Reserve ERROR for conditions that need attention.

### 6. One Log Statement per Event
Avoid logging the same event at multiple levels or in multiple places. Log once at the most appropriate point.

### 7. Include Exception Objects
```java
// GOOD - full stack trace is captured
logger.error("Payment failed for orderId={}", orderId, exception);

// BAD - only message, no stack trace
logger.error("Payment failed: " + exception.getMessage());
```
