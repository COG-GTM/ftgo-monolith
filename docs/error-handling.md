# FTGO Error Handling Guide

## Overview

The `ftgo-error-handling` shared library provides centralized, standardized error handling
for all FTGO microservices. It uses Spring's `@ControllerAdvice` mechanism to intercept
exceptions globally and convert them into a consistent JSON error response format.

## Quick Start

Add the library as a dependency in your service's `build.gradle`:

```groovy
dependencies {
    compile project(":shared:ftgo-error-handling")
}
```

That's it. The library uses Spring Boot auto-configuration to register the
`GlobalExceptionHandler` automatically when the library is on the classpath.

### Disabling Auto-Configuration

To disable the global error handler (e.g., for a service with custom handling):

```properties
ftgo.error-handling.enabled=false
```

## Standard Error Response Format

All error responses follow this JSON structure:

```json
{
  "code": "FTGO-01001",
  "message": "Validation failed",
  "details": "2 field(s) have validation errors",
  "timestamp": "2026-03-03T02:30:00Z",
  "traceId": "abc123def456",
  "path": "/orders",
  "status": 400,
  "fieldErrors": [
    {
      "field": "consumerId",
      "message": "must not be null",
      "rejectedValue": null
    }
  ]
}
```

### Field Descriptions

| Field         | Type     | Always Present | Description                                          |
|---------------|----------|----------------|------------------------------------------------------|
| `code`        | string   | Yes            | FTGO error code (e.g., `FTGO-01001`)                 |
| `message`     | string   | Yes            | Human-readable error summary                         |
| `details`     | string   | No             | Additional context about the error                   |
| `timestamp`   | string   | Yes            | ISO-8601 timestamp of when the error occurred        |
| `traceId`     | string   | Yes            | Distributed trace ID for log correlation             |
| `path`        | string   | Yes            | Request path that triggered the error                |
| `status`      | integer  | Yes            | HTTP status code                                     |
| `fieldErrors` | array    | No             | Field-level validation errors (only for 400 errors)  |

### Field Error Object

| Field           | Type   | Description                                |
|-----------------|--------|--------------------------------------------|
| `field`         | string | The field name that failed validation      |
| `message`       | string | The validation error message               |
| `rejectedValue` | any    | The value that was rejected (may be null)  |

## Error Code Catalog

Error codes follow the format `FTGO-XXYYY` where:
- `XX` = Category (00-07)
- `YYY` = Specific error within category

### General Errors (FTGO-00xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-00001 | 500         | Internal server error - unexpected failure |
| FTGO-00002 | 404         | Resource not found                         |
| FTGO-00003 | 405         | Method not allowed                         |
| FTGO-00004 | 415         | Unsupported media type                     |
| FTGO-00005 | 400         | Malformed or missing request body          |
| FTGO-00006 | 400         | Missing required request parameter         |
| FTGO-00007 | 400         | Type mismatch in request parameter         |

### Validation Errors (FTGO-01xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-01001 | 400         | Bean validation failed on request body     |
| FTGO-01002 | 400         | Required field is missing or null          |
| FTGO-01003 | 400         | Field value out of allowed range           |
| FTGO-01004 | 400         | Field value doesn't match expected pattern |
| FTGO-01005 | 400         | Field value exceeds maximum length         |

### Authentication & Authorization Errors (FTGO-02xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-02001 | 401         | Authentication required                    |
| FTGO-02002 | 403         | Access denied - insufficient permissions   |
| FTGO-02003 | 401         | Authentication token expired               |
| FTGO-02004 | 401         | Authentication token invalid or malformed  |

### Order Errors (FTGO-03xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-03001 | 404         | Order not found                            |
| FTGO-03002 | 409         | Invalid state transition for order         |
| FTGO-03003 | 422         | Order minimum amount not met               |
| FTGO-03004 | 409         | Order revision conflict                    |

### Consumer Errors (FTGO-04xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-04001 | 404         | Consumer not found                         |
| FTGO-04002 | 422         | Consumer validation failed for order       |

### Restaurant Errors (FTGO-05xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-05001 | 404         | Restaurant not found                       |
| FTGO-05002 | 404         | Menu item not found                        |
| FTGO-05003 | 503         | Restaurant not accepting orders            |

### Courier Errors (FTGO-06xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-06001 | 404         | Courier not found                          |
| FTGO-06002 | 503         | No available couriers                      |

### Inter-Service Communication Errors (FTGO-07xxx)

| Code       | HTTP Status | Description                                |
|------------|-------------|--------------------------------------------|
| FTGO-07001 | 503         | Downstream service unavailable             |
| FTGO-07002 | 503         | Downstream service request timed out       |
| FTGO-07003 | 502         | Downstream service returned unexpected error|
| FTGO-07004 | 503         | Circuit breaker open - calls rejected      |
| FTGO-07005 | 503         | Connection failure to downstream service   |

## Exception Mapping Reference

The `GlobalExceptionHandler` maps exceptions to HTTP status codes as follows:

| Exception Class                           | HTTP Status | Error Code       |
|-------------------------------------------|-------------|------------------|
| `MethodArgumentNotValidException`         | 400         | FTGO-01001       |
| `BindException`                           | 400         | FTGO-01001       |
| `HttpMessageNotReadableException`         | 400         | FTGO-00005       |
| `MissingServletRequestParameterException` | 400         | FTGO-00006       |
| `MethodArgumentTypeMismatchException`     | 400         | FTGO-00007       |
| `ResourceNotFoundException`               | 404         | FTGO-00002       |
| `NoHandlerFoundException`                 | 404         | FTGO-00002       |
| `HttpRequestMethodNotSupportedException`  | 405         | FTGO-00003       |
| `UnsupportedStateTransitionException`     | 409         | FTGO-03002       |
| `ConflictException`                       | 409         | (from exception) |
| `HttpMediaTypeNotSupportedException`      | 415         | FTGO-00004       |
| `OrderMinimumNotMetException`             | 422         | FTGO-03003       |
| `BusinessRuleException`                   | 422         | (from exception) |
| `ServiceCommunicationException`           | 502/503     | (from exception) |
| `ConnectException`                        | 503         | FTGO-07005       |
| `Exception` (fallback)                    | 500         | FTGO-00001       |

## Using Custom Exceptions

### ResourceNotFoundException

Use when an entity lookup fails:

```java
import com.ftgo.errorhandling.exception.ResourceNotFoundException;

Order order = orderRepository.findById(orderId)
    .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
```

### BusinessRuleException

Use for domain-level validation failures:

```java
import com.ftgo.errorhandling.exception.BusinessRuleException;
import com.ftgo.errorhandling.constants.ErrorCodes;

if (orderTotal.isLessThan(minimumAmount)) {
    throw new BusinessRuleException(
        ErrorCodes.ORDER_MINIMUM_NOT_MET,
        "Order total $" + orderTotal + " is below minimum $" + minimumAmount
    );
}
```

### ConflictException

Use for state conflicts or optimistic locking failures:

```java
import com.ftgo.errorhandling.exception.ConflictException;
import com.ftgo.errorhandling.constants.ErrorCodes;

if (!order.canTransitionTo(OrderState.ACCEPTED)) {
    throw new ConflictException(
        ErrorCodes.ORDER_INVALID_STATE_TRANSITION,
        "Cannot transition from " + order.getState() + " to ACCEPTED"
    );
}
```

### ServiceCommunicationException

Use for inter-service call failures:

```java
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import com.ftgo.errorhandling.constants.ErrorCodes;

try {
    restTemplate.getForObject(url, ConsumerDTO.class);
} catch (ResourceAccessException ex) {
    throw new ServiceCommunicationException(
        ErrorCodes.SERVICE_TIMEOUT,
        "consumer-service",
        "Timeout while validating consumer",
        ex
    );
}
```

## Bean Validation

The library enables Bean Validation (JSR 380) support. Annotate your request DTOs
with `javax.validation` constraints and use `@Valid` on controller parameters:

```java
// Request DTO
public class CreateOrderRequest {
    @NotNull(message = "Consumer ID is required")
    private Long consumerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<LineItemRequest> lineItems;
}

// Controller
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // If validation fails, GlobalExceptionHandler returns 400 with field errors
    return ResponseEntity.ok(orderService.createOrder(request));
}
```

## TraceId Integration

The error handler automatically includes the distributed trace ID in all error
responses. The trace ID is read from the SLF4J MDC context, which is populated
by the `ftgo-tracing` library's Brave/Micrometer integration.

This enables end-to-end request tracing: clients receive a `traceId` in error
responses that can be used to look up the full distributed trace in Zipkin/Jaeger.

## Architecture

```
shared/ftgo-error-handling/
├── build.gradle
└── src/
    ├── main/
    │   ├── java/com/ftgo/errorhandling/
    │   │   ├── config/
    │   │   │   └── FtgoErrorHandlingAutoConfiguration.java
    │   │   ├── constants/
    │   │   │   └── ErrorCodes.java
    │   │   ├── dto/
    │   │   │   └── ErrorResponse.java
    │   │   ├── exception/
    │   │   │   ├── BusinessRuleException.java
    │   │   │   ├── ConflictException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── ServiceCommunicationException.java
    │   │   └── handler/
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       └── META-INF/
    │           └── spring.factories
    └── test/
        └── java/com/ftgo/errorhandling/
            ├── config/
            │   └── FtgoErrorHandlingAutoConfigurationTest.java
            ├── dto/
            │   └── ErrorResponseTest.java
            └── handler/
                └── GlobalExceptionHandlerTest.java
```
