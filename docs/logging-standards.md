# FTGO Logging Standards

## Overview

All FTGO microservices use structured JSON logging via SLF4J with Logback, enabling consistent log aggregation and search in the ELK/EFK stack.

## Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| ERROR | Unrecoverable failures, data loss risk | Database connection failure, unhandled exception |
| WARN | Recoverable issues, degraded state | Circuit breaker open, retry exhausted, slow query |
| INFO | Business events, state transitions | Order created, payment processed, service started |
| DEBUG | Diagnostic detail for troubleshooting | Request/response payloads, SQL queries |
| TRACE | Very fine-grained, framework internals | Only in development |

## Structured Logging Format

All log output is JSON. Use MDC (Mapped Diagnostic Context) for request-scoped fields:

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "logger": "n.c.f.order.OrderService",
  "message": "Order created successfully",
  "service": "ftgo-order-service",
  "traceId": "abc123def456",
  "spanId": "789ghi",
  "orderId": 12345,
  "consumerId": 678,
  "environment": "production"
}
```

## Logback Configuration

Each service includes `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <springProfile name="!local">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeContext>false</includeContext>
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>requestId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>

    <springProfile name="local">
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

## Logging Best Practices

1. **Use parameterized logging** — avoid string concatenation
   ```java
   // Good
   log.info("Order {} created for consumer {}", orderId, consumerId);
   // Bad
   log.info("Order " + orderId + " created for consumer " + consumerId);
   ```

2. **Include context in error logs** — always log the exception
   ```java
   log.error("Failed to process order {}", orderId, exception);
   ```

3. **Never log sensitive data** — PII, passwords, tokens, credit cards
   ```java
   // Bad
   log.info("User authenticated with token {}", token);
   // Good
   log.info("User {} authenticated successfully", userId);
   ```

4. **Use MDC for request context** — set at the start of each request
   ```java
   MDC.put("orderId", String.valueOf(orderId));
   MDC.put("consumerId", String.valueOf(consumerId));
   try {
       // business logic
   } finally {
       MDC.clear();
   }
   ```

5. **Log business events at INFO** — state transitions, key operations
6. **Log technical detail at DEBUG** — SQL, HTTP calls, serialization
7. **Keep messages concise and searchable** — use consistent prefixes

## Log Shipping

Services send logs to Fluentd via Docker log driver:

```yaml
logging:
  driver: fluentd
  options:
    fluentd-address: "localhost:24224"
    tag: "ftgo.{{.Name}}"
```

Fluentd forwards to Elasticsearch. Kibana provides search and dashboards at `http://kibana:5601`.
