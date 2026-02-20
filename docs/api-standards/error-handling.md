# FTGO API Error Handling Standards

## Error Response Format

All error responses use the standard `ErrorResponse` DTO from `ftgo-api-standards`:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    { "field": "deliveryAddress", "message": "must not be null" },
    { "field": "lineItems", "message": "must not be empty" }
  ]
}
```

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
| `UNPROCESSABLE_ENTITY` | 422 | Business rule validation failed |

### Server Errors (5xx)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INTERNAL_ERROR` | 500 | Unexpected server error |
| `SERVICE_UNAVAILABLE` | 503 | Service temporarily unavailable |
| `UPSTREAM_ERROR` | 502 | Upstream dependency failure |

## Implementation with @ControllerAdvice

Services should implement a global exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                400, "VALIDATION_ERROR", "Validation failed", request.getRequestURI());
        ex.getBindingResult().getFieldErrors().forEach(
                error -> response.addFieldError(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                404, "NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                500, "INTERNAL_ERROR", "An unexpected error occurred",
                request.getRequestURI());
        return ResponseEntity.internalServerError().body(response);
    }
}
```

## Guidelines

1. **Never expose internal details** in error messages to clients (stack traces, SQL errors, etc.)
2. **Use specific error codes** so clients can programmatically handle errors
3. **Include field-level errors** for validation failures
4. **Log full details server-side** while returning sanitized messages to clients
5. **Use consistent error codes** across all microservices
