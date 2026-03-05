# FTGO Logging Standards

> **Version:** 1.0
> **Status:** Published
> **Applies to:** All FTGO microservices
> **Library:** `shared/ftgo-logging-lib`

---

## Table of Contents

1. [Log Level Guidelines](#1-log-level-guidelines)
2. [What to Log](#2-what-to-log)
3. [What NOT to Log](#3-what-not-to-log)
4. [Structured Logging Format](#4-structured-logging-format)
5. [MDC Fields](#5-mdc-fields)
6. [Per-Environment Configuration](#6-per-environment-configuration)
7. [Sensitive Data Masking](#7-sensitive-data-masking)
8. [Logging Aspect (Method Entry/Exit)](#8-logging-aspect-method-entryexit)
9. [Configuration Reference](#9-configuration-reference)
10. [Examples](#10-examples)

---

## 1. Log Level Guidelines

| Level   | When to Use | Examples |
|---------|------------|----------|
| `ERROR` | Unrecoverable failures that require immediate attention. The operation cannot continue. | Database connection failure, external service unreachable after retries, data corruption detected, unhandled exceptions |
| `WARN`  | Unexpected conditions that the system can recover from, but may indicate a problem. | Deprecated API usage, retry attempt succeeded, fallback triggered, approaching resource limits, validation failures from external input |
| `INFO`  | Normal operational events that confirm the system is working as expected. High-level business events. | Service started/stopped, request processed, order created, payment completed, configuration loaded |
| `DEBUG` | Detailed diagnostic information useful during development and troubleshooting. | Method entry/exit with parameters, SQL queries, cache hits/misses, intermediate calculation results, HTTP request/response details |
| `TRACE` | Very fine-grained diagnostic information. Typically only enabled for specific packages during active debugging. | Loop iterations, byte-level data, full object serialization, step-by-step algorithm execution |

### Rules

- **Production** should run at `INFO` for application code and `ERROR` for framework code.
- **Never** log at `ERROR` for expected business conditions (e.g., validation failures). Use `WARN` or `INFO`.
- **Never** use `System.out.println()` or `System.err.println()`. Always use SLF4J.
- Prefer parameterized logging (`log.info("Order {} created", orderId)`) over string concatenation.
- Guard expensive `DEBUG`/`TRACE` log construction with `log.isDebugEnabled()` / `log.isTraceEnabled()`.

---

## 2. What to Log

### Always Log

| Category | What to Log | Level |
|----------|-------------|-------|
| **Service Lifecycle** | Service startup, shutdown, configuration loaded | `INFO` |
| **Business Events** | Order created, payment processed, delivery dispatched | `INFO` |
| **External Calls** | Outbound HTTP/gRPC calls (URL, method, status, duration) | `INFO` or `DEBUG` |
| **Errors** | All exceptions with stack traces, error codes, context | `ERROR` or `WARN` |
| **Security Events** | Authentication success/failure, authorization denied | `INFO` or `WARN` |
| **Method Entry/Exit** | Service method boundaries (via logging aspect) | `DEBUG` |
| **Performance** | Slow operations exceeding thresholds | `WARN` |
| **State Changes** | Entity state transitions (e.g., order status changes) | `INFO` |

### Log Entry Structure

Every log message should answer: **What happened? To what? Why does it matter?**

```java
// Good: provides context
log.info("Order created: orderId={}, customerId={}, total={}", orderId, customerId, total);

// Bad: no context
log.info("Order created");
```

---

## 3. What NOT to Log

### Never Log

| Category | Examples | Reason |
|----------|----------|--------|
| **Passwords** | User passwords, API secrets, database credentials | Security risk |
| **Tokens** | JWT tokens, OAuth tokens, session tokens, API keys | Security risk |
| **Credit Card Numbers** | Full card numbers, CVV codes | PCI-DSS compliance |
| **PII** | Social Security numbers, full names with addresses, email addresses in bulk | Privacy regulations (GDPR, CCPA) |
| **Full Request/Response Bodies** | Complete HTTP payloads in production | Performance, storage, security |
| **Health Check Details** | Repetitive health check responses | Log noise |

### Masking

The `ftgo-logging-lib` includes automatic masking for common sensitive patterns:
- Credit card numbers are masked: `4111111111111111` becomes `****-****-****-1111`
- Passwords in key-value patterns are masked: `password=secret123` becomes `password=********`
- Bearer tokens are masked: `Bearer eyJhbG...` becomes `Bearer ****`
- SSN patterns are masked: `123-45-6789` becomes `***-**-6789`

If you must log data that may contain sensitive information, use the masking utilities or redact manually.

---

## 4. Structured Logging Format

### JSON Format (Deployed Environments)

All deployed environments (dev, staging, production) use structured JSON logging for machine-parseable output compatible with the EFK (Elasticsearch-Fluentd-Kibana) stack.

```json
{
  "@timestamp": "2026-03-04T22:00:00.000Z",
  "level": "INFO",
  "logger": "n.c.f.order.OrderService",
  "thread": "http-nio-8080-exec-1",
  "message": "Order created: orderId=12345, customerId=678",
  "service": "ftgo-order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi012",
  "correlationId": "req-uuid-001",
  "userId": "user-678",
  "requestId": "req-uuid-001"
}
```

### Human-Readable Format (Local Development)

Local development uses a pattern-based format optimized for developer readability:

```
2026-03-04 22:00:00.000  INFO [ftgo-order-service,abc123def456,789ghi012,req-uuid-001] n.c.f.order.OrderService - Order created: orderId=12345, customerId=678
```

Format: `timestamp level [service,traceId,spanId,correlationId] logger - message`

---

## 5. MDC Fields

All log entries automatically include these MDC (Mapped Diagnostic Context) fields:

| Field | Source | Description |
|-------|--------|-------------|
| `service` | `spring.application.name` or `ftgo.logging.service-name` | Service name |
| `traceId` | Micrometer Tracing / Brave | Distributed trace ID |
| `spanId` | Micrometer Tracing / Brave | Current span ID |
| `correlationId` | `X-Correlation-ID` header or auto-generated UUID | Request correlation ID |
| `userId` | Security context (JWT subject) | Authenticated user ID |
| `requestId` | `X-Request-ID` header or auto-generated UUID | Unique request identifier |

### Setting MDC Fields

Use the `LogContext` utility class for managing MDC in application code:

```java
import net.chrisrichardson.ftgo.logging.context.LogContext;

// Set fields
LogContext.setUserId("user-123");
LogContext.setRequestId("req-456");

// Execute with context (auto-clears after)
LogContext.withContext(() -> {
    // ... your code here
    log.info("Processing order");
}, "user-123", "req-456");

// Clear request context
LogContext.clearRequestContext();
```

---

## 6. Per-Environment Configuration

### Log Level Matrix

| Logger / Package | Local | Dev | Staging | Production |
|-----------------|-------|-----|---------|------------|
| `net.chrisrichardson.ftgo` (app) | `DEBUG` | `INFO` | `INFO` | `INFO` |
| `org.springframework` | `INFO` | `WARN` | `WARN` | `ERROR` |
| `org.hibernate` | `INFO` | `WARN` | `WARN` | `ERROR` |
| `org.apache` | `INFO` | `WARN` | `WARN` | `ERROR` |
| `io.micrometer` | `INFO` | `WARN` | `WARN` | `ERROR` |
| `ROOT` | `INFO` | `WARN` | `WARN` | `WARN` |

### Logging Format per Environment

| Environment | Format | Async | File Output |
|-------------|--------|-------|-------------|
| Local | Human-readable pattern | No | Yes (with rotation) |
| Dev | JSON (structured) | Yes | No (stdout only) |
| Staging | JSON (structured) | Yes | No (stdout only) |
| Production | JSON (structured) | Yes | No (stdout only) |

### Spring Profile Mapping

| Spring Profile | Logging Behavior |
|---------------|-----------------|
| `local` | Human-readable console + file with rotation |
| `dev` | JSON structured console, async |
| `staging` | JSON structured console, async |
| `prod` | JSON structured console, async, stricter levels |
| *(default)* | JSON structured console, async |

---

## 7. Sensitive Data Masking

The library provides a custom Logback layout converter (`SensitiveDataMaskingConverter`) that automatically masks sensitive patterns in log messages.

### Masked Patterns

| Pattern | Example Input | Masked Output |
|---------|---------------|---------------|
| Credit card (13-19 digits) | `4111111111111111` | `****-****-****-1111` |
| Password fields | `"password":"secret"` | `"password":"********"` |
| Bearer tokens | `Bearer eyJhbGci...` | `Bearer ****` |
| SSN | `123-45-6789` | `***-**-6789` |
| Authorization headers | `Authorization: Basic abc` | `Authorization: ********` |

### Integration

The masking converter is automatically registered in the shared `logback-spring.xml`. No additional service-level configuration is required.

---

## 8. Logging Aspect (Method Entry/Exit)

The library provides an AOP-based logging aspect that automatically logs method entry and exit for service methods.

### Configuration

```yaml
ftgo:
  logging:
    aspect:
      enabled: true
      base-packages:
        - net.chrisrichardson.ftgo
      log-level: DEBUG
      include-args: true
      include-result: false
      slow-threshold-ms: 1000
```

### Behavior

- **Entry**: Logs method name and arguments (if `include-args: true`) at configured level
- **Exit**: Logs method name, return type, and execution time at configured level
- **Slow execution**: If execution exceeds `slow-threshold-ms`, logs at `WARN` level
- **Exceptions**: Logs exceptions at `ERROR` level with stack trace

### Example Output

```
DEBUG [ftgo-order-service] n.c.f.logging.aspect.LoggingAspect - --> OrderService.createOrder(customerId=123, items=[...])
DEBUG [ftgo-order-service] n.c.f.logging.aspect.LoggingAspect - <-- OrderService.createOrder() returned in 45ms
WARN  [ftgo-order-service] n.c.f.logging.aspect.LoggingAspect - <-- OrderService.createOrder() SLOW execution: 2345ms (threshold: 1000ms)
```

---

## 9. Configuration Reference

All properties are under the `ftgo.logging` prefix:

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.logging.enabled` | `true` | Enable/disable FTGO logging auto-configuration |
| `ftgo.logging.json-enabled` | `true` | Enable JSON structured logging |
| `ftgo.logging.async-enabled` | `true` | Enable async appender wrapper |
| `ftgo.logging.async-queue-size` | `1024` | Async appender queue size |
| `ftgo.logging.async-discard-threshold` | `0` | Async discard threshold (0 = no discard) |
| `ftgo.logging.include-caller-data` | `false` | Include caller class/method/line in logs |
| `ftgo.logging.service-name` | `${spring.application.name}` | Service name in log output |
| `ftgo.logging.correlation-id-header` | `X-Correlation-ID` | HTTP header for correlation ID |
| `ftgo.logging.masking.enabled` | `true` | Enable sensitive data masking |
| `ftgo.logging.aspect.enabled` | `false` | Enable method entry/exit logging aspect |
| `ftgo.logging.aspect.base-packages` | `net.chrisrichardson.ftgo` | Packages to apply aspect to |
| `ftgo.logging.aspect.log-level` | `DEBUG` | Log level for entry/exit messages |
| `ftgo.logging.aspect.include-args` | `true` | Include method arguments in entry log |
| `ftgo.logging.aspect.include-result` | `false` | Include return value in exit log |
| `ftgo.logging.aspect.slow-threshold-ms` | `1000` | Threshold (ms) for slow execution warning |

---

## 10. Examples

### Basic Service Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(Long customerId, List<OrderItem> items) {
        log.info("Creating order: customerId={}, itemCount={}", customerId, items.size());

        try {
            Order order = orderRepository.save(new Order(customerId, items));
            log.info("Order created: orderId={}, customerId={}, total={}",
                    order.getId(), customerId, order.getTotal());
            return order;
        } catch (Exception e) {
            log.error("Failed to create order: customerId={}, error={}",
                    customerId, e.getMessage(), e);
            throw e;
        }
    }
}
```

### Using LogContext

```java
import net.chrisrichardson.ftgo.logging.context.LogContext;

@RestController
public class OrderController {

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request,
                                              @AuthenticationPrincipal UserDetails user) {
        LogContext.setUserId(user.getUsername());
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.ok(order);
        } finally {
            LogContext.clearRequestContext();
        }
    }
}
```

### Service Configuration (application.yml)

```yaml
spring:
  application:
    name: ftgo-order-service

ftgo:
  logging:
    enabled: true
    service-name: ftgo-order-service
    masking:
      enabled: true
    aspect:
      enabled: true
      base-packages:
        - net.chrisrichardson.ftgo.orderservice
```
