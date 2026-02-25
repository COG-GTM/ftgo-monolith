# FTGO Error Handling Guide

## Overview

All FTGO microservices use the shared `ftgo-error-handling-lib` for centralized error handling. This library provides:

- **`@ControllerAdvice`** base class (`GlobalExceptionHandler`) for consistent exception mapping
- **Standardized error response format** following RFC 7807 Problem Details
- **Error code enums** per domain for programmatic error identification
- **Custom exception hierarchy** mapping to appropriate HTTP status codes
- **Bean Validation** (`jakarta.validation`) support with field-level error details
- **Inter-service communication** error handling
- **Trace ID integration** via Micrometer Tracing (when available)

## Error Response Format

All error responses follow this standardized JSON structure:

```json
{
  "error": {
    "code": "ORDER_MINIMUM_NOT_MET",
    "message": "Order total must be at least $10.00",
    "status": 422,
    "details": [
      {
        "field": "orderTotal",
        "message": "Must be at least 10.00",
        "rejectedValue": "5.00"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "traceId": "abc123def456",
    "instance": "/api/orders"
  }
}
```

### Fields

| Field       | Type     | Required | Description                                      |
|-------------|----------|----------|--------------------------------------------------|
| `code`      | string   | Yes      | Machine-readable error code (see catalog below)  |
| `message`   | string   | Yes      | Human-readable error description                 |
| `status`    | integer  | Yes      | HTTP status code                                 |
| `details`   | array    | No       | Field-level validation errors                    |
| `timestamp` | string   | Yes      | ISO 8601 timestamp of the error                  |
| `traceId`   | string   | No       | Distributed tracing ID (when tracing is enabled) |
| `instance`  | string   | No       | The request URI that triggered the error          |

## Error Code Catalog

### Common Error Codes

These error codes are shared across all microservices.

| Code                      | HTTP Status | Description                                       |
|---------------------------|-------------|---------------------------------------------------|
| `VALIDATION_ERROR`        | 400         | Request validation failed (Bean Validation)       |
| `INVALID_REQUEST`         | 400         | Malformed or invalid request data                 |
| `MISSING_REQUIRED_FIELD`  | 400         | A required field is missing                       |
| `UNAUTHORIZED`            | 401         | Authentication required                           |
| `INVALID_CREDENTIALS`     | 401         | Invalid credentials provided                      |
| `TOKEN_EXPIRED`           | 401         | Authentication token has expired                  |
| `ACCESS_DENIED`           | 403         | Insufficient permissions                          |
| `INSUFFICIENT_PERMISSIONS`| 403         | Operation requires additional permissions         |
| `RESOURCE_NOT_FOUND`      | 404         | Requested resource was not found                  |
| `STATE_TRANSITION_ERROR`  | 409         | Invalid state transition attempted                |
| `RESOURCE_CONFLICT`       | 409         | Request conflicts with current resource state     |
| `OPTIMISTIC_LOCK_ERROR`   | 409         | Resource modified by another request              |
| `BUSINESS_RULE_VIOLATION` | 422         | A business rule was violated                      |
| `RATE_LIMIT_EXCEEDED`     | 429         | Too many requests                                 |
| `INTERNAL_ERROR`          | 500         | Unexpected internal error                         |
| `NOT_YET_IMPLEMENTED`     | 501         | Feature not yet implemented                       |
| `UPSTREAM_SERVICE_ERROR`  | 502         | Upstream service returned an error                |
| `SERVICE_UNAVAILABLE`     | 503         | Service temporarily unavailable                   |
| `SERVICE_TIMEOUT`         | 504         | Upstream service request timed out                |

### Order Domain Error Codes

| Code                       | HTTP Status | Description                                 |
|----------------------------|-------------|---------------------------------------------|
| `ORDER_NOT_FOUND`          | 404         | Order not found                             |
| `ORDER_MINIMUM_NOT_MET`    | 422         | Order total below minimum requirement       |
| `ORDER_ALREADY_CANCELLED`  | 409         | Order has already been cancelled            |
| `ORDER_STATE_INVALID`      | 409         | Order not in valid state for operation      |
| `ORDER_REVISION_REJECTED`  | 422         | Order revision was rejected                 |
| `ORDER_LINE_ITEM_NOT_FOUND`| 404         | Order line item not found                   |

### Consumer Domain Error Codes

| Code                          | HTTP Status | Description                              |
|-------------------------------|-------------|------------------------------------------|
| `CONSUMER_NOT_FOUND`          | 404         | Consumer not found                       |
| `CONSUMER_ALREADY_EXISTS`     | 409         | Consumer with identifier already exists  |
| `CONSUMER_VALIDATION_FAILED`  | 422         | Consumer data validation failed          |

### Restaurant Domain Error Codes

| Code                          | HTTP Status | Description                              |
|-------------------------------|-------------|------------------------------------------|
| `RESTAURANT_NOT_FOUND`        | 404         | Restaurant not found                     |
| `RESTAURANT_CLOSED`           | 422         | Restaurant is currently closed           |
| `MENU_ITEM_NOT_FOUND`         | 404         | Menu item not found                      |
| `RESTAURANT_ALREADY_EXISTS`   | 409         | Restaurant already exists                |

### Courier Domain Error Codes

| Code                          | HTTP Status | Description                              |
|-------------------------------|-------------|------------------------------------------|
| `COURIER_NOT_FOUND`           | 404         | Courier not found                        |
| `COURIER_UNAVAILABLE`         | 422         | Courier is currently unavailable         |
| `DELIVERY_NOT_FOUND`          | 404         | Delivery not found                       |
| `DELIVERY_ALREADY_ASSIGNED`   | 409         | Delivery already assigned to a courier   |

## Exception Hierarchy

```
RuntimeException
  └── FtgoException (base)
        ├── ResourceNotFoundException     → 404 Not Found
        ├── BusinessRuleException         → 422 Unprocessable Entity
        ├── StateTransitionException      → 409 Conflict
        ├── ServiceCommunicationException → 502 Bad Gateway
        └── ServiceTimeoutException       → 504 Gateway Timeout
```

### Legacy Exception Mapping

| Legacy Exception                      | New Exception                | HTTP Status |
|---------------------------------------|------------------------------|-------------|
| `UnsupportedStateTransitionException` | `StateTransitionException`   | 409         |
| `OrderMinimumNotMetException`         | `BusinessRuleException`      | 422         |
| `NotYetImplementedException`          | Handled by catch-all         | 501         |

## Usage Guide

### 1. Add Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared:ftgo-error-handling-lib")
}
```

The `GlobalExceptionHandler` is auto-configured via Spring Boot auto-configuration. No additional setup is needed.

### 2. Throw Typed Exceptions

```java
// Resource not found
throw new ResourceNotFoundException("Order", orderId);

// Business rule violation
throw new BusinessRuleException(
    OrderErrorCode.ORDER_MINIMUM_NOT_MET,
    "Order total must be at least $10.00");

// Invalid state transition
throw new StateTransitionException(order.getState());

// Inter-service communication failure
try {
    restTemplate.getForObject(url, Response.class);
} catch (RestClientException e) {
    throw new ServiceCommunicationException("restaurant-service", e);
}
```

### 3. Use Bean Validation on DTOs

```java
public class CreateOrderRequest {

    @NotNull(message = "Consumer ID is required")
    private Long consumerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<OrderLineItemRequest> items;
}
```

Then in your controller:

```java
@PostMapping("/orders")
public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // Validation errors are automatically handled by GlobalExceptionHandler
}
```

### 4. Custom Validators

Use the provided `@MoneyAmount` annotation:

```java
public class OrderLineItemRequest {

    @NotNull
    private Long menuItemId;

    @MoneyAmount(min = 0.01, message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
```

### 5. Extend for Domain-Specific Handling

Services can add their own exception handlers by creating a `@ControllerAdvice`:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OrderServiceExceptionHandler {

    @ExceptionHandler(OrderMinimumNotMetException.class)
    public ResponseEntity<ErrorResponseWrapper> handleOrderMinimumNotMet(
            OrderMinimumNotMetException ex) {
        // Custom handling for legacy exceptions
    }
}
```

## Configuration

| Property                              | Default | Description                          |
|---------------------------------------|---------|--------------------------------------|
| `ftgo.error-handling.enabled`         | `true`  | Enable/disable error handling        |
| `ftgo.error-handling.include-stacktrace` | `false` | Include stack traces (dev only)   |

## Dependencies

- **ftgo-tracing-lib** (optional): When present, trace IDs are automatically included in error responses
- **Spring Boot Starter Validation**: Provides Bean Validation (jakarta.validation) support
- **Spring Boot Starter Web**: Required for `@ControllerAdvice` support
