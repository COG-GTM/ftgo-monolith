# Error Handling Patterns

This document describes the centralized error handling strategy provided by
`ftgo-error-handling-lib` for FTGO microservices.

## Overview

All REST APIs return a consistent **ErrorResponse** body (defined in
`ftgo-openapi-lib`) for every 4xx and 5xx response. A single
`@RestControllerAdvice` (`GlobalExceptionHandler`) catches exceptions globally
so individual controllers do not need their own try/catch blocks.

## Adding the Library

In your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-error-handling-lib')
}
```

The `GlobalExceptionHandler` is registered automatically via Spring Boot
auto-configuration. No additional `@Import` or `@ComponentScan` is required.

## Error Response Format

Every error response follows this JSON structure:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Order not found with id: 42",
  "timestamp": "2026-03-16T20:00:00Z",
  "path": "/api/v1/orders/42",
  "details": []
}
```

| Field       | Type            | Description                                         |
|-------------|-----------------|-----------------------------------------------------|
| `code`      | String          | Machine-readable error code (see Error Codes below) |
| `message`   | String          | Human-readable description                          |
| `timestamp` | String (ISO-8601) | When the error occurred                           |
| `path`      | String          | Request URI that caused the error                   |
| `details`   | Array           | Optional list of field-level errors                 |

Each entry in `details`:

| Field     | Type   | Description                        |
|-----------|--------|------------------------------------|
| `field`   | String | Field name (null for global errors)|
| `message` | String | Human-readable error message       |
| `code`    | String | Constraint/error code              |

## Exception Hierarchy

All application exceptions extend `BaseException`, which carries an `ErrorCode`.

```
BaseException (abstract)
 +-- BusinessException                 (422)
 |    +-- OrderMinimumNotMetException  (422)
 +-- ResourceNotFoundException         (404)
 +-- ResourceAlreadyExistsException    (409)
 +-- ValidationException               (400)
 +-- UnsupportedStateTransitionException (409)
 +-- AuthenticationException           (401)
 +-- AuthorizationException            (403)
 +-- OptimisticLockException           (409)
 +-- ServiceUnavailableException       (503)
```

## Error Codes

The `ErrorCode` enum defines all machine-readable codes:

| Code                          | HTTP Status | Description                          |
|-------------------------------|-------------|--------------------------------------|
| `INTERNAL_ERROR`              | 500         | Unexpected server error              |
| `BAD_REQUEST`                 | 400         | Malformed or invalid request         |
| `UNAUTHORIZED`                | 401         | Authentication required              |
| `FORBIDDEN`                   | 403         | Insufficient permissions             |
| `RESOURCE_NOT_FOUND`          | 404         | Resource does not exist              |
| `RESOURCE_ALREADY_EXISTS`     | 409         | Duplicate resource                   |
| `VALIDATION_FAILED`           | 400         | Bean/field validation failure        |
| `BUSINESS_RULE_VIOLATION`     | 422         | Domain rule violated                 |
| `UNSUPPORTED_STATE_TRANSITION`| 409         | Invalid state machine transition     |
| `ORDER_MINIMUM_NOT_MET`       | 422         | Order total below restaurant minimum |
| `OPTIMISTIC_LOCK_CONFLICT`    | 409         | Concurrent modification detected     |
| `SERVICE_UNAVAILABLE`         | 503         | Downstream service unreachable       |

## Usage Examples

### Throwing a standard exception

```java
import net.chrisrichardson.ftgo.errorhandling.exception.ResourceNotFoundException;

public Order findOrder(long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
}
```

### Throwing a validation exception with field errors

```java
import net.chrisrichardson.ftgo.errorhandling.exception.ValidationException;
import net.chrisrichardson.ftgo.errorhandling.exception.ValidationException.FieldError;

List<FieldError> errors = new ArrayList<>();
errors.add(new FieldError("email", "must not be blank", "NotBlank"));
errors.add(new FieldError("quantity", "must be greater than 0", "Min"));
throw new ValidationException("Request validation failed", errors);
```

### Creating a domain-specific business exception

```java
import net.chrisrichardson.ftgo.errorhandling.exception.BusinessException;
import net.chrisrichardson.ftgo.errorhandling.ErrorCode;

public class InsufficientInventoryException extends BusinessException {
    public InsufficientInventoryException(String menuItemId) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION,
              "Insufficient inventory for menu item: " + menuItemId);
    }
}
```

### Bean Validation with @Valid

Standard `@Valid` / JSR-380 annotations on request DTOs are handled
automatically. The `GlobalExceptionHandler` converts
`MethodArgumentNotValidException` into an `ErrorResponse` with field-level
details.

```java
@PostMapping("/orders")
public ResponseEntity<CreateOrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request) {
    // Validation failures are caught automatically
    // and returned as a 400 with VALIDATION_FAILED code
}
```

## Overriding the Default Handler

If a service needs custom error handling, define your own
`GlobalExceptionHandler` bean. The auto-configuration uses
`@ConditionalOnMissingBean` so your custom bean takes precedence:

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {
    // Override specific methods as needed
}
```

## Migration from Legacy Exceptions

The library provides centralized equivalents of the existing monolith
exceptions. Services migrating to microservices should adopt the new
hierarchy:

| Legacy Exception (monolith)                                              | New Exception (ftgo-error-handling-lib)                                     |
|--------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException`    | `net.chrisrichardson.ftgo.errorhandling.exception.UnsupportedStateTransitionException` |
| `net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException`            | `net.chrisrichardson.ftgo.errorhandling.exception.OrderMinimumNotMetException`         |
| `net.chrisrichardson.ftgo.common.NotYetImplementedException`             | Use standard `UnsupportedOperationException` or a `BusinessException`                  |

The legacy exceptions in `ftgo-common` and `ftgo-domain` remain unchanged
for backward compatibility with the monolith.
