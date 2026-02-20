# Log Level Guidelines

This document defines when to use each log level and the recommended log level configuration per environment.

## Log Level Definitions

| Level | Purpose | Example |
|-------|---------|---------|
| `ERROR` | Unrecoverable failures requiring immediate attention. The current operation cannot continue. | Database connection failure, message queue unavailable, unhandled exception |
| `WARN` | Unexpected conditions that are recoverable. The system continues but something may need attention. | Circuit breaker opened, retry attempt, deprecated API usage, slow query (>500ms) |
| `INFO` | Significant business events and operational milestones. | Order created, payment processed, service started, configuration loaded |
| `DEBUG` | Detailed diagnostic information useful during development and troubleshooting. | Method entry/exit with parameters, intermediate computation results, cache hit/miss |
| `TRACE` | Very fine-grained diagnostic output. Rarely enabled outside local development. | Full request/response bodies, serialization details, framework internals |

## Environment Configuration Matrix

### Root Logger Levels

| Logger / Package | Development | Staging | Production |
|-----------------|-------------|---------|------------|
| Root logger | `DEBUG` | `INFO` | `INFO` |
| `com.ftgo` | `DEBUG` | `INFO` | `INFO` |
| `com.ftgo.*.repository` | `DEBUG` | `INFO` | `WARN` |
| `org.springframework` | `INFO` | `WARN` | `WARN` |
| `org.springframework.web` | `DEBUG` | `INFO` | `WARN` |
| `org.springframework.security` | `DEBUG` | `INFO` | `WARN` |
| `org.hibernate.SQL` | `DEBUG` | `WARN` | `ERROR` |
| `org.hibernate.type.descriptor.sql` | `TRACE` | `WARN` | `ERROR` |
| `org.apache.kafka` | `INFO` | `WARN` | `WARN` |
| `io.micrometer` | `INFO` | `WARN` | `WARN` |
| `net.logstash` | `INFO` | `WARN` | `ERROR` |
| `org.flywaydb` | `INFO` | `INFO` | `WARN` |
| `io.github.resilience4j` | `DEBUG` | `INFO` | `WARN` |

### Appender Configuration per Environment

| Appender | Development | Staging | Production |
|----------|-------------|---------|------------|
| Console (plain text, pretty-print) | Enabled | Disabled | Disabled |
| Console (JSON) | Disabled | Enabled | Enabled |
| Logstash TCP | Disabled | Enabled | Enabled |
| File (rolling) | Optional | Disabled | Disabled |

## Guidelines for Developers

### When to Log at Each Level

#### ERROR

Use `ERROR` only for conditions that represent a **failure of the current operation** and likely require human intervention.

```java
logger.error("Failed to process payment for orderId={}: {}", orderId, e.getMessage(), e);
```

Rules:
- Always include the exception as the last argument so the stack trace is captured.
- Include enough context (IDs, operation name) to locate the issue.
- Do not use `ERROR` for expected conditions (e.g., validation failures, 404 responses).

#### WARN

Use `WARN` for conditions that are **unexpected but recoverable**.

```java
logger.warn("Circuit breaker open for restaurant-service, falling back to cache");
logger.warn("Slow query detected: {}ms for findOrdersByConsumer(consumerId={})", elapsed, consumerId);
```

Rules:
- Use for retry scenarios, circuit breaker state changes, and degraded operation.
- Use for slow operations that exceed SLO thresholds.
- Do not use `WARN` for normal business-logic paths.

#### INFO

Use `INFO` for **significant business events** and operational state changes.

```java
logger.info("Order created: orderId={}, consumerId={}, restaurantId={}", orderId, consumerId, restaurantId);
logger.info("Service started on port {}", port);
```

Rules:
- Log at service startup/shutdown with configuration summary.
- Log successful completion of key business operations with relevant IDs.
- Keep INFO volume manageable: avoid logging per-item in bulk operations.

#### DEBUG

Use `DEBUG` for **diagnostic detail** useful during development.

```java
logger.debug("Resolving restaurant menu: restaurantId={}, menuVersion={}", restaurantId, version);
logger.debug("Cache lookup for key={}: hit={}", cacheKey, cacheHit);
```

Rules:
- Safe to be verbose; this level is disabled in staging/production by default.
- Include method parameters and intermediate results.
- Use for tracing code paths through conditionals and loops.

#### TRACE

Use `TRACE` only for **extremely detailed diagnostics**.

```java
logger.trace("Serialized order payload: {}", jsonPayload);
```

Rules:
- Rarely enabled; only for deep debugging of specific subsystems.
- Suitable for full request/response bodies, serialization output, and framework-level tracing.

### Anti-Patterns

| Anti-Pattern | Correct Approach |
|-------------|-----------------|
| `logger.error("User not found")` for expected 404 | Use `DEBUG` or `INFO`; return 404 normally |
| `logger.info(...)` inside tight loops | Use `DEBUG` or log a summary after the loop |
| Logging sensitive data (passwords, tokens, PII) | Mask or omit; see [Sensitive Data](#sensitive-data) |
| Logging without context IDs | Always include `orderId`, `consumerId`, or similar |
| Using string concatenation: `logger.info("Order " + id)` | Use parameterized logging: `logger.info("Order {}", id)` |
| Catching and logging then re-throwing | Log at the point where the exception is handled, not where it passes through |

### Sensitive Data

Never log:
- Passwords, API keys, tokens (JWT, OAuth)
- Credit card numbers, CVVs, bank account details
- Personal Identifiable Information (full SSN, full address unless required for debugging)
- Session IDs or authentication cookies

If you must reference sensitive entities, log a masked or hashed form:

```java
logger.info("Payment authorized for card ending in {}", last4Digits);
```

## Applying Log Levels in Spring Boot

Use Spring profile-specific configuration in `application-{profile}.properties`:

```properties
# application-dev.properties
logging.level.root=DEBUG
logging.level.com.ftgo=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# application-staging.properties
logging.level.root=INFO
logging.level.com.ftgo=INFO
logging.level.org.hibernate.SQL=WARN

# application-prod.properties
logging.level.root=INFO
logging.level.com.ftgo=INFO
logging.level.org.hibernate.SQL=ERROR
```

Alternatively, configure levels in the Logback configuration templates provided in `config/logback/`.
