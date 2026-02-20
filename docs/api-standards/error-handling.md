# FTGO API Error Handling Standards

## Error Response Format

All error responses use the standard `ErrorResponse` DTO from `ftgo-api-standards`:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    { "field": "deliveryAddress", "message": "must not be null" },
    { "field": "lineItems", "message": "must not be empty" }
  ]
}
```

## Correlation ID

Every error response includes a `correlationId` field for distributed tracing. The correlation ID is:

- Extracted from the incoming `X-Correlation-ID` request header if present
- Auto-generated (UUID) if no header is provided
- Stored in the SLF4J MDC for structured logging
- Returned in the `X-Correlation-ID` response header
- Included in all `ErrorResponse` bodies

The `CorrelationIdFilter` is auto-configured with highest precedence to ensure correlation IDs are available throughout the request lifecycle.

## Standard Error Codes

### Client Errors (4xx)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request body or parameter validation failed |
| `BAD_REQUEST` | 400 | Malformed request syntax |
| `UNAUTHORIZED` | 401 | Missing or invalid authentication |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Requested resource does not exist |
| `METHOD_NOT_ALLOWED` | 405 | HTTP method not supported for this endpoint |
| `CONFLICT` | 409 | Resource state conflict |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content type not supported |
| `UNPROCESSABLE_ENTITY` | 422 | Business rule validation failed |

### Server Errors (5xx)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INTERNAL_ERROR` | 500 | Unexpected server error |
| `UPSTREAM_ERROR` | 502 | Upstream dependency failure |
| `SERVICE_UNAVAILABLE` | 503 | Service temporarily unavailable |

## Exception Hierarchy

The `ftgo-api-standards` library provides a centralized exception hierarchy rooted in `BaseException`:

```
BaseException (abstract)
├── BusinessException          → 422 UNPROCESSABLE_ENTITY
├── ResourceNotFoundException  → 404 NOT_FOUND
├── ValidationException        → 400 VALIDATION_ERROR (with field errors)
├── ConflictException          → 409 CONFLICT
├── UnauthorizedException      → 401 UNAUTHORIZED
├── ForbiddenException         → 403 FORBIDDEN
├── ServiceUnavailableException → 503 SERVICE_UNAVAILABLE
└── UpstreamServiceException   → 502 UPSTREAM_ERROR
```

All exceptions extend `RuntimeException` and carry an `errorCode` and `httpStatus`.

## Auto-Configured Global Exception Handler

The `GlobalExceptionHandler` is registered via Spring Boot auto-configuration. It handles:

| Exception Type | HTTP Status | Error Code |
|---|---|---|
| `ValidationException` | 400 | `VALIDATION_ERROR` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `MissingServletRequestParameterException` | 400 | `BAD_REQUEST` |
| `MethodArgumentTypeMismatchException` | 400 | `BAD_REQUEST` |
| `HttpMessageNotReadableException` | 400 | `BAD_REQUEST` |
| `HttpRequestMethodNotSupportedException` | 405 | `METHOD_NOT_ALLOWED` |
| `HttpMediaTypeNotSupportedException` | 415 | `UNSUPPORTED_MEDIA_TYPE` |
| `NoResourceFoundException` | 404 | `NOT_FOUND` |
| All `BaseException` subclasses | Per exception | Per exception |
| All other `Exception` | 500 | `INTERNAL_ERROR` |

## Usage

### 1. Add Dependency

```groovy
dependencies {
    implementation project(':libs:ftgo-api-standards')
}
```

### 2. Throw Exceptions in Service Layer

```java
@Service
public class OrderService {

    public Order findOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public void cancelOrder(Long id) {
        Order order = findOrder(id);
        if (!order.isCancellable()) {
            throw new BusinessException("Order cannot be cancelled in state: " + order.getState());
        }
    }

    public void createOrder(CreateOrderRequest request) {
        if (orderRepository.existsByReferenceId(request.getReferenceId())) {
            throw new ConflictException("Order with reference " + request.getReferenceId() + " already exists");
        }
    }
}
```

### 3. Throw Validation Exceptions with Field Errors

```java
Map<String, String> fieldErrors = Map.of(
    "email", "must be a valid email",
    "name", "must not be blank"
);
throw new ValidationException("Validation failed", fieldErrors);
```

### 4. Override the Global Handler (Optional)

Services can provide a custom `GlobalExceptionHandler` bean to override the auto-configured one:

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {
    // Override or add handlers as needed
}
```

## Guidelines

1. **Never expose internal details** in error messages to clients (stack traces, SQL errors, etc.)
2. **Use specific error codes** so clients can programmatically handle errors
3. **Include field-level errors** for validation failures
4. **Log full details server-side** while returning sanitized messages to clients
5. **Use consistent error codes** across all microservices
6. **Always include correlation IDs** - the auto-configured filter handles this automatically
7. **Use the exception hierarchy** - throw `BaseException` subclasses instead of generic exceptions
8. **Custom error codes** - use `BusinessException(message, errorCode)` for domain-specific codes
