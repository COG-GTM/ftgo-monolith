# FTGO Platform - Logging Standards

## Overview

This document defines the logging standards and conventions that all FTGO microservices must follow. Consistent, structured, and actionable logging is essential for debugging, monitoring, and operating a distributed microservices platform.

All services use the shared `ftgo-logging-lib` library, which provides structured JSON logging, MDC context propagation, PII masking, and per-environment configuration out of the box.

## Table of Contents

1. [Log Level Guidelines](#log-level-guidelines)
2. [What to Log](#what-to-log)
3. [What NOT to Log](#what-not-to-log)
4. [MDC Field Standards](#mdc-field-standards)
5. [Structured Log Format](#structured-log-format)
6. [Per-Environment Configuration](#per-environment-configuration)
7. [Sensitive Data Masking](#sensitive-data-masking)
8. [Logback Configuration](#logback-configuration)
9. [Async Logging](#async-logging)
10. [Integration with Distributed Tracing](#integration-with-distributed-tracing)

---

## Log Level Guidelines

Use the following guidelines when choosing a log level. Consistent usage across all services ensures that log searches and alerts are meaningful.

| Level   | When to Use | Examples | Visible In |
|---------|-------------|----------|------------|
| `ERROR` | Unexpected failures that require immediate attention. The operation cannot continue. | Unhandled exceptions, database connection failures, external service unavailable after retries, data corruption detected | All environments |
| `WARN`  | Unexpected conditions that the system can handle but may indicate a problem. The operation continues with degraded behavior. | Slow queries (> threshold), deprecated API usage, retry attempts, fallback logic triggered, cache miss on expected data | All environments |
| `INFO`  | Significant business events and operational milestones. Use sparingly in hot paths. | Service startup/shutdown, order created/completed, payment processed, user authenticated, configuration loaded, scheduled job started/completed | All environments |
| `DEBUG` | Detailed diagnostic information useful for debugging. Should provide enough context to trace the flow of a single request. | Method entry/exit, request/response payloads (sanitized), SQL parameters, cache hits/misses, decision branch taken | Local, Dev |
| `TRACE` | Very fine-grained diagnostic information. Typically only enabled for specific packages during active debugging. | Loop iterations, individual field validations, byte-level data, full object state dumps | Local (targeted) |

### Log Level Decision Tree

```
Is the application unable to continue the current operation?
  YES → ERROR
  NO  → Is this an unexpected condition that may need investigation?
    YES → WARN
    NO  → Is this a significant business event or operational milestone?
      YES → INFO
      NO  → Is this useful diagnostic information for debugging?
        YES → DEBUG
        NO  → TRACE
```

### Key Rules

1. **Never use ERROR for expected conditions** (e.g., validation failures, 404s, business rule violations)
2. **Never use INFO in tight loops** or for per-item processing in batch operations
3. **Always include the `why`** — log messages should explain what happened and why it matters
4. **Use parameterized logging** — use `log.info("Order {} created for user {}", orderId, userId)` instead of string concatenation
5. **Include relevant identifiers** — always include entity IDs, user IDs, or request IDs in log messages

---

## What to Log

### Always Log (INFO)

| Event | Example |
|-------|---------|
| Service startup and shutdown | `"Service ftgo-order-service started on port 8080"` |
| Business events | `"Order ORD-123 created for user USR-456"` |
| External service calls (start + complete) | `"Calling restaurant-service: getRestaurant"` / `"restaurant-service: getRestaurant completed in 45ms"` |
| Authentication events | `"User USR-456 authenticated successfully"` |
| Scheduled job execution | `"Daily report generation started"` / `"Daily report completed: 1,234 records"` |
| Configuration changes | `"Feature flag 'new-checkout' enabled"` |

### Log on Failure (ERROR/WARN)

| Event | Level | Example |
|-------|-------|---------|
| Unhandled exceptions | ERROR | `"Unexpected error processing order ORD-123: NullPointerException"` |
| External service failures | ERROR | `"Failed to call payment-service after 3 retries: Connection refused"` |
| Database errors | ERROR | `"Database query failed: Deadlock detected on table orders"` |
| Validation failures (server-side) | WARN | `"Invalid order request: missing restaurantId"` |
| Retry attempts | WARN | `"Retrying payment-service call (attempt 2/3)"` |
| Circuit breaker state changes | WARN | `"Circuit breaker for restaurant-service OPENED"` |

### Log for Debugging (DEBUG)

| Event | Example |
|-------|---------|
| Method entry/exit | `">>> Entering OrderService.createOrder() with 2 argument(s)"` |
| Request details (sanitized) | `"Processing request: POST /api/orders {restaurantId: 'R-123', items: 3}"` |
| Cache operations | `"Cache hit for restaurant R-123"` / `"Cache miss for menu M-456"` |
| Decision logic | `"Order ORD-123 routed to kitchen queue (priority: HIGH)"` |

---

## What NOT to Log

### Never Log

| Data Type | Why | Instead |
|-----------|-----|---------|
| **Passwords / secrets** | Security breach risk | Log authentication events without credentials |
| **Credit card numbers** | PCI DSS compliance | Use PII masking (automatic via `PiiMaskingConverter`) |
| **API keys / tokens** | Security breach risk | Log token type and last 4 characters at most |
| **Social Security Numbers** | PII / legal compliance | Use PII masking (automatic) |
| **Full email addresses** | GDPR / privacy | Use partial masking (automatic) |
| **Full request/response bodies in production** | Performance + data exposure | Log summaries: status codes, sizes, durations |
| **Health check requests** | Log noise / volume | Filter in Fluentd or exclude in Logback |
| **Sensitive HTTP headers** (`Authorization`, `Cookie`) | Token leakage | Log header names, not values |

### PII Masking

The `ftgo-logging-lib` automatically masks sensitive data via `PiiMaskingConverter`:

```
Input:  "Payment with card 4111-1111-1111-1111 for user john@example.com"
Output: "Payment with card ***MASKED*** for user j***@example.com"

Input:  "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx"
Output: "Authorization: Bearer ***MASKED***"

Input:  "password=myS3cretP@ss"
Output: "password=***MASKED***"
```

---

## MDC Field Standards

All FTGO microservices must include the following MDC (Mapped Diagnostic Context) fields in every log entry. These are automatically managed by `ftgo-logging-lib` filters and the `LogContext` utility.

### Required MDC Fields

| Field | Key | Source | Description |
|-------|-----|--------|-------------|
| **Service Name** | `serviceName` | `spring.application.name` | Identifies the microservice |
| **Trace ID** | `traceId` | Micrometer Tracing (Brave) | Distributed trace identifier |
| **Span ID** | `spanId` | Micrometer Tracing (Brave) | Current span identifier |
| **Correlation ID** | `correlationId` | `X-Correlation-ID` header | Cross-service request correlation from API Gateway |
| **Request ID** | `requestId` | Generated per-request | Unique identifier for the current request |
| **User ID** | `userId` | JWT / Security Context | Authenticated user identifier |

### Automatically Populated Fields

These fields are set automatically by `ftgo-logging-lib` filters:

| Field | Key | Source |
|-------|-----|--------|
| Request Method | `requestMethod` | HTTP request method |
| Request URI | `requestUri` | HTTP request path |
| Remote Address | `remoteAddr` | Client IP (respects `X-Forwarded-For`) |
| User Agent | `userAgent` | HTTP `User-Agent` header |

### Setting MDC Fields Manually

Use the `LogContext` utility class to set MDC fields:

```java
import com.ftgo.common.logging.context.LogContext;

// Individual field setters
LogContext.setUserId("USR-123");
LogContext.setRequestId(UUID.randomUUID().toString());

// Builder pattern
LogContext.builder()
    .userId("USR-123")
    .requestId(UUID.randomUUID().toString())
    .operation("createOrder")
    .apply();

// Always clean up in finally block
try {
    // ... business logic
} finally {
    LogContext.clear();
}
```

---

## Structured Log Format

### JSON Format (Deployed Environments)

All deployed environments (dev, staging, production) use structured JSON logs via `logstash-logback-encoder`:

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger_name": "com.ftgo.order.domain.OrderService",
  "thread_name": "http-nio-8080-exec-1",
  "message": "Order ORD-123 created for user USR-456",
  "service": "ftgo-order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "correlationId": "req-uuid-here",
  "userId": "USR-456",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "requestMethod": "POST",
  "requestUri": "/api/orders",
  "remoteAddr": "10.0.1.50"
}
```

### Human-Readable Format (Local Development)

Local development uses a colorized, human-readable format:

```
2024-01-15 10:30:45.123  INFO [ftgo-order-service,abc123def456,789ghi012,req-uuid-here] [http-nio-8080-exec-1] c.f.o.d.OrderService - Order ORD-123 created for user USR-456
```

Pattern: `timestamp level [service,traceId,spanId,correlationId] [thread] logger - message`

---

## Per-Environment Configuration

### Log Level Matrix

| Package / Logger | Local | Dev / Staging | Production |
|------------------|-------|---------------|------------|
| `com.ftgo` (application) | `DEBUG` | `INFO` | `INFO` |
| `org.springframework` | `INFO` | `WARN` | `ERROR` |
| `org.springframework.web` | `INFO` | `WARN` | `ERROR` |
| `org.hibernate` | `INFO` | `WARN` | `ERROR` |
| `org.hibernate.SQL` | `DEBUG` | `WARN` | `ERROR` |
| `org.apache.http` | `INFO` | `WARN` | `ERROR` |
| `org.elasticsearch` | — | `WARN` | `ERROR` |
| Root logger | `INFO` | `INFO` | `WARN` |

### Environment Configuration Summary

| Setting | Local | Dev / Staging | Production |
|---------|-------|---------------|------------|
| **Log format** | Human-readable (console + file) | JSON (console) | JSON (console) |
| **Async appender** | Disabled | Enabled (queue: 1024) | Enabled (queue: 2048) |
| **File rotation** | Enabled (10MB, 7 days) | Disabled (stdout only) | Disabled (stdout only) |
| **Caller data** | Available | Disabled | Disabled |
| **Discarding threshold** | N/A | 0 (never discard) | 20 (discard low-priority under load) |
| **Never-block mode** | N/A | No | Yes |
| **PII masking** | Enabled | Enabled | Enabled |

### Activating a Profile

Set the active Spring profile via environment variable or property:

```bash
# Environment variable
SPRING_PROFILES_ACTIVE=dev

# System property
-Dspring.profiles.active=prod

# application.properties
spring.profiles.active=local
```

### Per-Environment Properties Files

The `ftgo-logging-lib` provides environment-specific properties:

- `application-logging-local.properties` — Local development defaults
- `application-logging-dev.properties` — Dev/staging defaults
- `application-logging-prod.properties` — Production defaults

These are activated automatically via Spring profiles.

---

## Sensitive Data Masking

### Automatic Masking

The `PiiMaskingConverter` (registered in `logback-spring.xml`) automatically masks:

| Data Type | Pattern | Example |
|-----------|---------|---------|
| Credit card numbers | 13-19 digit sequences | `4111111111111111` → `***MASKED***` |
| Passwords | `password=value` patterns | `password=secret` → `password=***MASKED***` |
| Bearer tokens | `Bearer <token>` | `Bearer eyJ...` → `Bearer ***MASKED***` |
| API keys | `api_key=value` patterns | `api_key=abc123` → `api_key=***MASKED***` |
| SSN | `XXX-XX-XXXX` | `123-45-6789` → `***MASKED***` |
| Email addresses | Partial masking | `john@example.com` → `j***@example.com` |

### JSON Format Masking

For JSON log output, the `PiiMaskingJsonProvider` ensures the `message` field is masked in the JSON structure as well.

### Custom Masking

To add additional masking patterns, extend `PiiMaskingConverter` or add custom patterns in your service's Logback configuration.

---

## Logback Configuration

### Configuration File Location

Each service should have a `logback-spring.xml` in `src/main/resources/`. The service template provides a ready-to-use configuration.

### Configuration Hierarchy

```
logback-spring.xml (service-specific)
  └── Includes Spring Boot defaults
  └── Uses PiiMaskingConverter from ftgo-logging-lib
  └── Uses LogstashEncoder from logstash-logback-encoder
  └── Profile-based appender selection
```

### Key Configuration Elements

1. **PII Masking Converter**: `<conversionRule conversionWord="maskedMsg" converterClass="com.ftgo.common.logging.masking.PiiMaskingConverter"/>`
2. **Service Name Property**: `<springProperty name="serviceName" source="spring.application.name"/>`
3. **Profile-Based Appenders**: Different appenders for `local`, `dev/staging`, `prod`
4. **Async Appender**: Wraps JSON console appender for non-blocking output

---

## Async Logging

### Why Async?

Synchronous logging blocks the application thread while the log event is written. In production with structured JSON output piped to Fluentd, this can introduce latency. The async appender decouples log production from output.

### Configuration

| Parameter | Dev/Staging | Production | Description |
|-----------|-------------|------------|-------------|
| `queueSize` | 1024 | 2048 | Internal ring buffer size |
| `discardingThreshold` | 0 | 20 | When queue fills to 80%, discard TRACE/DEBUG/INFO |
| `maxFlushTime` | 5000ms | 10000ms | Max wait time on shutdown for queue drain |
| `neverBlock` | false | true | Never block the application thread |
| `includeCallerData` | false | false | Disable expensive stack trace capture |

### Local Development

Async logging is **disabled** in local development for immediate console output and simpler debugging.

---

## Integration with Distributed Tracing

The logging library integrates with `ftgo-tracing-lib` (Micrometer Tracing + Brave):

1. **Automatic MDC Population**: Micrometer Tracing automatically puts `traceId` and `spanId` into the MDC
2. **JSON Output**: The Logstash encoder includes all MDC fields in JSON output
3. **Log Correlation**: Search by `traceId` in Kibana to see all log entries across services for a request
4. **Correlation ID**: The API Gateway generates `X-Correlation-ID` which is propagated and logged

### Cross-Service Request Flow

```
1. Client → API Gateway
   - Generates correlationId, starts trace
   - Logs: {service: "ftgo-api-gateway", traceId: "T1", correlationId: "C1"}

2. API Gateway → Order Service
   - Propagates correlationId via header, traceId via B3
   - Logs: {service: "ftgo-order-service", traceId: "T1", correlationId: "C1"}

3. Order Service → Restaurant Service
   - Same traceId, new spanId
   - Logs: {service: "ftgo-restaurant-service", traceId: "T1", correlationId: "C1"}

4. In Kibana: search traceId:"T1" → see all 3 entries in order
```

---

## Quick Reference

### Adding Logging to a New Service

1. Add `ftgo-logging-lib` dependency in `build.gradle`:
   ```groovy
   compile project(":shared:ftgo-logging-lib")
   ```

2. Copy `logback-spring.xml` from the service template to `src/main/resources/`

3. Set `spring.application.name` in `application.properties`

4. Enable logging properties:
   ```properties
   ftgo.logging.enabled=true
   ftgo.logging.json.enabled=true
   ftgo.logging.correlation-id.enabled=true
   ftgo.logging.async.enabled=true
   ```

5. Use `LogContext` for MDC management in your service code

6. Use `LoggingAspect` helpers for consistent method entry/exit logging

### Logging Checklist for Code Reviews

- [ ] Log levels are appropriate (no ERROR for expected conditions)
- [ ] No PII or secrets in log messages
- [ ] Parameterized logging used (no string concatenation)
- [ ] Entity/request IDs included in messages
- [ ] External calls logged with duration
- [ ] Exceptions logged with full stack trace (at ERROR level)
- [ ] MDC context set and cleared properly
- [ ] No logging in tight loops at INFO level
