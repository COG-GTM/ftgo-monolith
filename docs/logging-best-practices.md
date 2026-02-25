# FTGO Platform - Logging Best Practices Guide

## Overview

This guide provides practical coding patterns and best practices for logging in FTGO microservices. It complements the [Logging Standards](logging-standards.md) document with concrete code examples and anti-patterns.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Logger Declaration](#logger-declaration)
3. [Parameterized Logging](#parameterized-logging)
4. [MDC Context Management](#mdc-context-management)
5. [Method Entry/Exit Logging](#method-entryexit-logging)
6. [Exception Logging](#exception-logging)
7. [External Service Call Logging](#external-service-call-logging)
8. [Business Event Logging](#business-event-logging)
9. [Sensitive Data Handling](#sensitive-data-handling)
10. [Performance Considerations](#performance-considerations)
11. [Testing Log Output](#testing-log-output)
12. [Common Anti-Patterns](#common-anti-patterns)

---

## Getting Started

### 1. Add the Logging Library

In your service's `build.gradle`:

```groovy
dependencies {
    compile project(":shared:ftgo-logging-lib")
}
```

### 2. Add Logback Configuration

Copy `logback-spring.xml` from the service template to your service's `src/main/resources/`:

```bash
cp services/ftgo-service-template/src/main/resources/logback-spring.xml \
   services/ftgo-your-service/src/main/resources/logback-spring.xml
```

### 3. Configure Properties

In your service's `application.properties`:

```properties
spring.application.name=ftgo-your-service

ftgo.logging.enabled=true
ftgo.logging.json.enabled=true
ftgo.logging.correlation-id.enabled=true
ftgo.logging.async.enabled=true
```

---

## Logger Declaration

### Correct Pattern

Declare a private static final logger per class using SLF4J:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // ...
}
```

### Anti-Patterns

```java
// BAD: Non-static logger (creates a new logger per instance)
private final Logger log = LoggerFactory.getLogger(getClass());

// BAD: Using System.out/System.err
System.out.println("Order created");

// BAD: Using java.util.logging directly
java.util.logging.Logger.getLogger("OrderService").info("Order created");

// BAD: Hardcoded logger name (fragile on refactoring)
private static final Logger log = LoggerFactory.getLogger("OrderService");
```

---

## Parameterized Logging

### Always Use Parameterized Messages

SLF4J's parameterized logging defers string construction until the message is actually logged, avoiding unnecessary object creation when the level is disabled.

```java
// GOOD: Parameterized logging
log.info("Order {} created for user {} with {} items", orderId, userId, itemCount);

// GOOD: With complex objects
log.debug("Processing order: id={}, status={}, total={}", order.getId(), order.getStatus(), order.getTotal());
```

### Anti-Patterns

```java
// BAD: String concatenation (always evaluated, even if DEBUG is disabled)
log.debug("Processing order: " + order.toString());

// BAD: String.format (always evaluated)
log.info(String.format("Order %s created for user %s", orderId, userId));

// BAD: Unnecessary guard when using parameterized logging
if (log.isDebugEnabled()) {
    log.debug("Order {} processed", orderId); // Guard is redundant here
}

// GOOD: Guard IS appropriate for expensive computations
if (log.isDebugEnabled()) {
    log.debug("Order details: {}", expensiveSerialize(order));
}
```

---

## MDC Context Management

### Using LogContext

The `LogContext` utility provides a clean API for MDC management:

```java
import com.ftgo.common.logging.context.LogContext;

@RestController
public class OrderController {

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails user) {

        // Set MDC context for this request
        LogContext.builder()
            .userId(user.getUsername())
            .requestId(UUID.randomUUID().toString())
            .operation("createOrder")
            .apply();

        try {
            log.info("Creating order for restaurant {}", request.getRestaurantId());
            Order order = orderService.createOrder(request);
            log.info("Order {} created successfully", order.getId());
            return ResponseEntity.ok(toResponse(order));
        } finally {
            LogContext.clear(); // Always clean up!
        }
    }
}
```

### Context Propagation Across Threads

When passing work to another thread, capture and restore the MDC context:

```java
import com.ftgo.common.logging.context.LogContext;
import java.util.Map;

// Capture context before submitting to executor
Map<String, String> contextSnapshot = LogContext.snapshot();

executor.submit(() -> {
    LogContext.restore(contextSnapshot);
    try {
        // MDC context is now available in this thread
        log.info("Processing async task for order {}", orderId);
    } finally {
        LogContext.clearAll();
    }
});
```

### Anti-Patterns

```java
// BAD: Setting MDC directly without cleanup
MDC.put("userId", userId);
// ... business logic
// FORGOT to call MDC.remove("userId") — MDC leaks to next request!

// BAD: Not using try/finally
LogContext.setUserId(userId);
orderService.createOrder(request); // If this throws, MDC leaks
LogContext.clear();

// GOOD: Always use try/finally
LogContext.setUserId(userId);
try {
    orderService.createOrder(request);
} finally {
    LogContext.clear();
}
```

---

## Method Entry/Exit Logging

### Using LoggingAspect Utilities

The `LoggingAspect` class provides consistent entry/exit logging:

```java
import com.ftgo.common.logging.aspect.LoggingAspect;

public class OrderService {

    public Order createOrder(CreateOrderRequest request) {
        LoggingAspect.logEntry(getClass(), "createOrder", request);
        try {
            Order order = processOrder(request);
            LoggingAspect.logExit(getClass(), "createOrder", order);
            return order;
        } catch (Exception e) {
            LoggingAspect.logException(getClass(), "createOrder", e);
            throw e;
        }
    }
}
```

### Using with Spring AOP (Service-Specific)

Create a service-specific aspect for automatic method logging:

```java
import com.ftgo.common.logging.aspect.LoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(name = "ftgo.logging.aspect.enabled", havingValue = "true")
public class ServiceLoggingAspect {

    @Around("within(com.ftgo.order.domain..*)")
    public Object logDomainMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return LoggingAspect.logMethodExecution(
            joinPoint.getTarget().getClass(),
            joinPoint.getSignature().getName(),
            joinPoint.getArgs(),
            joinPoint::proceed
        );
    }
}
```

Enable in `application.properties`:

```properties
ftgo.logging.aspect.enabled=true
```

---

## Exception Logging

### Log Exceptions with Full Stack Trace

```java
// GOOD: Full exception as last parameter (SLF4J captures stack trace)
try {
    paymentService.processPayment(orderId, amount);
} catch (PaymentException e) {
    log.error("Payment failed for order {}: {}", orderId, e.getMessage(), e);
    throw new OrderCreationException("Payment failed", e);
}
```

### Logging at the Right Level

```java
// ERROR: Unexpected, system-level failures
catch (DatabaseException e) {
    log.error("Database error while creating order {}", orderId, e);
}

// WARN: Expected failure with recovery
catch (OptimisticLockException e) {
    log.warn("Concurrent modification on order {}, retrying", orderId, e);
    return retryOperation();
}

// Don't log AND throw without context
catch (Exception e) {
    // BAD: Just re-logging the same exception up the stack
    log.error("Error", e);
    throw e;

    // GOOD: Add context, or let the caller log it
    log.error("Failed to process order {} during payment step", orderId, e);
    throw new OrderProcessingException("Payment step failed for order " + orderId, e);
}
```

### Anti-Patterns

```java
// BAD: Swallowing exceptions silently
try {
    riskyOperation();
} catch (Exception e) {
    // Silently ignored - bugs will be invisible!
}

// BAD: Logging exception message without stack trace
log.error("Error: " + e.getMessage()); // Missing stack trace!

// BAD: Logging at wrong level
catch (ValidationException e) {
    log.error("Validation failed", e); // Should be WARN, not ERROR
}

// BAD: Duplicate logging (both catch blocks log the same thing)
try {
    methodA(); // methodA already logs the error
} catch (Exception e) {
    log.error("Error in methodA", e); // Duplicate log entry
}
```

---

## External Service Call Logging

### Pattern for Inter-Service Calls

```java
import com.ftgo.common.logging.aspect.LoggingAspect;

public class RestaurantServiceClient {

    public Restaurant getRestaurant(String restaurantId) {
        LoggingAspect.logExternalCall(getClass(), "restaurant-service", "getRestaurant");
        long startTime = System.currentTimeMillis();

        try {
            Restaurant restaurant = restTemplate.getForObject(
                restaurantServiceUrl + "/api/restaurants/" + restaurantId,
                Restaurant.class);

            long duration = System.currentTimeMillis() - startTime;
            LoggingAspect.logExternalCallComplete(getClass(), "restaurant-service",
                "getRestaurant", duration);

            return restaurant;
        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("External call failed: restaurant-service getRestaurant " +
                "for restaurantId={} after {}ms", restaurantId, duration, e);
            throw e;
        }
    }
}
```

### Output

```
INFO  ---> Calling external service: restaurant-service operation: getRestaurant
INFO  <--- External service: restaurant-service operation: getRestaurant completed in 45ms
```

---

## Business Event Logging

### Pattern for Business Events

Use the `LoggingAspect.logBusinessEvent()` helper for consistent business event logging:

```java
import com.ftgo.common.logging.aspect.LoggingAspect;

public class OrderService {

    public Order createOrder(CreateOrderRequest request) {
        // ... create order logic
        Order order = orderRepository.save(newOrder);

        LoggingAspect.logBusinessEvent(getClass(), "ORDER_CREATED",
            String.format("orderId=%s, userId=%s, restaurantId=%s, total=%.2f",
                order.getId(), order.getUserId(), order.getRestaurantId(), order.getTotal()));

        return order;
    }

    public void cancelOrder(String orderId, String reason) {
        // ... cancel logic

        LoggingAspect.logBusinessEvent(getClass(), "ORDER_CANCELLED",
            String.format("orderId=%s, reason=%s", orderId, reason));
    }
}
```

### Output

```
INFO  [BUSINESS_EVENT] ORDER_CREATED - orderId=ORD-123, userId=USR-456, restaurantId=R-789, total=25.50
INFO  [BUSINESS_EVENT] ORDER_CANCELLED - orderId=ORD-123, reason=customer_request
```

---

## Sensitive Data Handling

### Automatic Masking

The `PiiMaskingConverter` in `logback-spring.xml` automatically masks common PII patterns. You do not need to manually mask these in your code.

### Manual Sanitization for Complex Objects

For objects that may contain sensitive data, create sanitized toString methods:

```java
// GOOD: Sanitized logging
log.info("Processing payment: {}", payment.toLogString());

// In Payment class:
public String toLogString() {
    return String.format("Payment{id=%s, amount=%.2f, cardLast4=%s, status=%s}",
        id, amount, cardNumber.substring(cardNumber.length() - 4), status);
}

// BAD: Logging full object with sensitive fields
log.info("Processing payment: {}", payment); // May expose card number!
```

### Never Log These

```java
// BAD: Logging passwords
log.debug("Authenticating user {} with password {}", username, password);

// BAD: Logging full tokens
log.debug("Using token: {}", jwtToken);

// GOOD: Log token metadata only
log.debug("Authenticating user {} with token type {}", username, tokenType);
```

---

## Performance Considerations

### 1. Use Appropriate Log Levels

```java
// BAD: INFO in a loop (generates massive log volume in production)
for (OrderItem item : order.getItems()) {
    log.info("Processing item: {}", item.getId());
}

// GOOD: DEBUG in loops, INFO for summary
log.info("Processing {} items for order {}", order.getItems().size(), order.getId());
for (OrderItem item : order.getItems()) {
    log.debug("Processing item: {}", item.getId());
}
log.info("All {} items processed for order {}", order.getItems().size(), order.getId());
```

### 2. Guard Expensive Operations

```java
// GOOD: Guard expensive serialization
if (log.isDebugEnabled()) {
    log.debug("Full order state: {}", objectMapper.writeValueAsString(order));
}

// GOOD: Guard expensive computation
if (log.isTraceEnabled()) {
    log.trace("Order validation details: {}", computeValidationReport(order));
}
```

### 3. Avoid Logging in Hot Paths

```java
// BAD: Logging in a frequently called utility method
public Money calculateTax(Money amount, TaxRate rate) {
    log.debug("Calculating tax for {} at rate {}", amount, rate); // Called 1000x per request!
    return amount.multiply(rate.getValue());
}

// GOOD: Log at the caller level
log.debug("Calculating taxes for order {} with {} line items", orderId, items.size());
Money totalTax = items.stream()
    .map(item -> calculateTax(item.getPrice(), item.getTaxRate()))
    .reduce(Money.ZERO, Money::add);
log.debug("Total tax for order {}: {}", orderId, totalTax);
```

### 4. Use Async Logging in Production

Async logging is configured automatically via `logback-spring.xml` for deployed environments. No code changes needed.

---

## Testing Log Output

### Verifying Log Messages in Tests

Use SLF4J Test or Logback's `ListAppender` to capture and assert log output:

```java
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@Test
void shouldLogOrderCreation() {
    // Arrange: capture log output
    Logger logger = (Logger) LoggerFactory.getLogger(OrderService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    // Act
    orderService.createOrder(request);

    // Assert
    assertThat(listAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .anyMatch(msg -> msg.contains("Order") && msg.contains("created"));

    // Cleanup
    logger.detachAppender(listAppender);
}
```

### Verifying PII Masking

```java
@Test
void shouldMaskCreditCardInLogs() {
    Logger logger = (Logger) LoggerFactory.getLogger(PaymentService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    // Act: log a message with a credit card number
    paymentService.processPayment("4111111111111111", amount);

    // Assert: credit card should be masked in logs
    assertThat(listAppender.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .noneMatch(msg -> msg.contains("4111111111111111"));
}
```

---

## Common Anti-Patterns

### 1. Log and Throw

```java
// BAD: Logging and re-throwing creates duplicate log entries
catch (Exception e) {
    log.error("Failed to create order", e);
    throw e; // Caller will also log this!
}

// GOOD: Either log OR throw, not both (unless adding new context)
catch (Exception e) {
    throw new OrderCreationException("Failed to create order " + orderId, e);
}
// Let GlobalExceptionHandler log it once at the boundary
```

### 2. Swallowing Exceptions

```java
// BAD: Silent catch
catch (IOException e) {
    // nothing here
}

// GOOD: At minimum, log a warning
catch (IOException e) {
    log.warn("Non-critical IO error during cleanup: {}", e.getMessage());
}
```

### 3. Logging Sensitive Data

```java
// BAD: Full request body may contain passwords, tokens
log.debug("Request body: {}", requestBody);

// GOOD: Log safe summary
log.debug("Request: method={}, uri={}, contentLength={}", method, uri, contentLength);
```

### 4. Inconsistent Log Formats

```java
// BAD: Inconsistent message formats
log.info("order created: " + orderId);
log.info("ORDER_CREATED orderId=" + orderId);
log.info("[Order] Created: {}", orderId);

// GOOD: Consistent format
log.info("Order {} created for user {}", orderId, userId);
```

### 5. Missing Context

```java
// BAD: No context
log.error("Operation failed");

// GOOD: Full context
log.error("Failed to create order for user {} at restaurant {}: {}",
    userId, restaurantId, e.getMessage(), e);
```

---

## Summary

| Do | Don't |
|----|-------|
| Use SLF4J parameterized logging | Use string concatenation in log messages |
| Use `LogContext` for MDC management | Set MDC directly without cleanup |
| Log at appropriate levels | Use ERROR for expected conditions |
| Include entity IDs and context | Log bare "Error occurred" messages |
| Use `LoggingAspect` helpers | Write inconsistent entry/exit patterns |
| Guard expensive debug operations | Log complex objects at INFO level |
| Clean up MDC in finally blocks | Let MDC leak across requests |
| Use automatic PII masking | Log passwords, tokens, or card numbers |
| Log external call durations | Ignore inter-service call monitoring |
| Test log output in unit tests | Assume logging works without verification |
