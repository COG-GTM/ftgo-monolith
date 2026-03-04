# FTGO Error Code Catalog

This document describes all standardized error codes used across FTGO microservices.
Every error response follows the `FtgoErrorResponse` envelope format defined in
`shared/ftgo-error-handling-lib`.

## Error Response Format

All error responses use the following JSON envelope:

```json
{
  "status": "error",
  "code": "ORD_NOT_FOUND",
  "message": "Order with id 123 not found",
  "details": null,
  "path": "/api/v1/orders/123",
  "timestamp": "2024-01-15T10:30:00Z",
  "traceId": "6a3e94b234f9a1c2"
}
```

### Fields

| Field       | Type             | Required | Description                                              |
|-------------|------------------|----------|----------------------------------------------------------|
| `status`    | `string`         | Yes      | Always `"error"`                                         |
| `code`      | `string`         | Yes      | Application-specific error code (see tables below)       |
| `message`   | `string`         | Yes      | Human-readable error description                         |
| `details`   | `FieldError[]`   | No       | Field-level validation errors (only for validation failures) |
| `path`      | `string`         | Yes      | The request URI that triggered the error                 |
| `timestamp` | `string`         | Yes      | ISO 8601 timestamp of the error occurrence               |
| `traceId`   | `string`         | No       | Distributed tracing ID for log correlation (from Micrometer Tracing) |

### FieldError Object

Included in `details` when validation fails:

| Field           | Type     | Description                           |
|-----------------|----------|---------------------------------------|
| `field`         | `string` | Name of the field that failed         |
| `rejectedValue` | `any`    | The value that was rejected           |
| `message`       | `string` | Validation constraint error message   |

---

## Error Codes

### General Errors (`GEN_*`)

| Code                   | HTTP Status | Description                                          |
|------------------------|-------------|------------------------------------------------------|
| `GEN_INTERNAL_ERROR`   | 500         | Generic internal server error. No internal details are leaked to the client. |
| `GEN_RESOURCE_NOT_FOUND` | 404       | The requested resource or endpoint was not found.    |
| `GEN_METHOD_NOT_ALLOWED` | 405       | The HTTP method is not supported for this endpoint.  |
| `GEN_BAD_REQUEST`      | 400         | The request body could not be parsed (malformed JSON, missing body, etc.). |
| `GEN_NOT_IMPLEMENTED`  | 501         | The requested functionality is not yet implemented.  |

### Validation Errors (`VAL_*`)

| Code                     | HTTP Status | Description                                         |
|--------------------------|-------------|-----------------------------------------------------|
| `VAL_VALIDATION_FAILED`  | 400         | One or more request fields failed Bean Validation constraints. The `details` array contains per-field errors. |
| `VAL_CONSTRAINT_VIOLATION` | 400       | One or more method parameters or path variables failed constraint validation. The `details` array contains per-constraint errors. |

### Order Errors (`ORD_*`)

| Code                          | HTTP Status | Description                                    |
|-------------------------------|-------------|------------------------------------------------|
| `ORD_NOT_FOUND`               | 404         | The requested order was not found.             |
| `ORD_MINIMUM_NOT_MET`         | 422         | The order total does not meet the restaurant's minimum order amount. |
| `ORD_INVALID_STATE_TRANSITION`| 409         | The requested state transition is not valid for the current order state. |

### Consumer Errors (`CON_*`)

| Code             | HTTP Status | Description                          |
|------------------|-------------|--------------------------------------|
| `CON_NOT_FOUND`  | 404         | The requested consumer was not found.|

### Restaurant Errors (`RST_*`)

| Code             | HTTP Status | Description                            |
|------------------|-------------|----------------------------------------|
| `RST_NOT_FOUND`  | 404         | The requested restaurant was not found.|

### Courier Errors (`CUR_*`)

| Code             | HTTP Status | Description                          |
|------------------|-------------|--------------------------------------|
| `CUR_NOT_FOUND`  | 404         | The requested courier was not found. |

### Security Errors (`SEC_*`)

| Code              | HTTP Status | Description                                              |
|-------------------|-------------|----------------------------------------------------------|
| `SEC_UNAUTHORIZED`| 401         | Authentication is required but was not provided or is invalid. |
| `SEC_ACCESS_DENIED`| 403        | The authenticated user does not have permission for this operation. |

### State Transition Errors (`STATE_*`)

| Code                          | HTTP Status | Description                                        |
|-------------------------------|-------------|----------------------------------------------------|
| `STATE_UNSUPPORTED_TRANSITION`| 409         | The requested state transition is not supported for the current entity state. |

### Inter-Service Communication Errors (`SVC_*`)

| Code                       | HTTP Status | Description                                       |
|----------------------------|-------------|---------------------------------------------------|
| `SVC_COMMUNICATION_ERROR`  | 502         | A downstream service returned an error response or the connection failed. |
| `SVC_TIMEOUT`              | 504         | A downstream service did not respond within the configured timeout. |
| `SVC_UNAVAILABLE`          | 503         | A downstream service is currently unavailable (connection refused, DNS failure, etc.). |

---

## Exception-to-HTTP-Status Mapping

The `GlobalExceptionHandler` (`@ControllerAdvice`) maps Java exceptions to HTTP statuses
as follows:

| Exception Class                              | HTTP Status | Error Code                      |
|----------------------------------------------|-------------|---------------------------------|
| `UnsupportedStateTransitionException`        | 409 Conflict | `STATE_UNSUPPORTED_TRANSITION` |
| `OrderMinimumNotMetException`                | 422 Unprocessable Entity | `ORD_MINIMUM_NOT_MET` |
| `NotYetImplementedException`                 | 501 Not Implemented | `GEN_NOT_IMPLEMENTED`     |
| `ResourceNotFoundException`                  | 404 Not Found | `GEN_RESOURCE_NOT_FOUND`     |
| `MethodArgumentNotValidException`            | 400 Bad Request | `VAL_VALIDATION_FAILED`     |
| `ConstraintViolationException`               | 400 Bad Request | `VAL_CONSTRAINT_VIOLATION`  |
| `HttpMessageNotReadableException`            | 400 Bad Request | `GEN_BAD_REQUEST`           |
| `NoHandlerFoundException`                    | 404 Not Found | `GEN_RESOURCE_NOT_FOUND`     |
| `NoResourceFoundException`                   | 404 Not Found | `GEN_RESOURCE_NOT_FOUND`     |
| `HttpRequestMethodNotSupportedException`     | 405 Method Not Allowed | `GEN_METHOD_NOT_ALLOWED` |
| `AccessDeniedException`                      | 403 Forbidden | `SEC_ACCESS_DENIED`          |
| `AuthenticationException`                    | 401 Unauthorized | `SEC_UNAUTHORIZED`          |
| `ServiceCommunicationException`              | 502 Bad Gateway | `SVC_COMMUNICATION_ERROR`   |
| `ServiceCommunicationException` (timeout)    | 504 Gateway Timeout | `SVC_TIMEOUT`            |
| `ResourceAccessException`                    | 503 Service Unavailable | `SVC_UNAVAILABLE`     |
| `RestClientException`                        | 502 Bad Gateway | `SVC_COMMUNICATION_ERROR`   |
| `Exception` (catch-all)                      | 500 Internal Server Error | `GEN_INTERNAL_ERROR` |

---

## Integration Guide

### Adding Error Handling to a Service

1. Add the dependency in your service's `build.gradle`:

   ```groovy
   compile project(':shared-ftgo-error-handling-lib')
   ```

2. The `GlobalExceptionHandler` is auto-configured via Spring Boot's
   auto-configuration mechanism. No additional configuration is needed.

3. To disable the error handler (e.g., in tests):

   ```yaml
   ftgo:
     error-handling:
       enabled: false
   ```

### Using Bean Validation

Add Jakarta Bean Validation annotations to your request DTOs:

```java
public class CreateOrderRequest {
    @NotNull(message = "Consumer ID is required")
    private Long consumerId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<LineItem> lineItems;
}
```

Then use `@Valid` on your controller method parameter:

```java
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // validation happens automatically before this method is called
}
```

### Using Custom Exceptions

For resource-not-found scenarios, use `ResourceNotFoundException`:

```java
Order order = orderRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
```

For inter-service failures, use `ServiceCommunicationException`:

```java
try {
    return restTemplate.getForObject(url, ConsumerDto.class);
} catch (ResourceAccessException ex) {
    throw new ServiceCommunicationException("consumer-service",
        "Failed to fetch consumer", ex);
}
```

---

## Related Documentation

- [REST API Standards](rest-api-standards.md)
- [Distributed Tracing](distributed-tracing.md)
- [Security Configuration](security-configuration.md)
- [Shared Gradle Configuration](shared-gradle-configuration.md)
