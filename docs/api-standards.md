# FTGO REST API Standards

> Version: 1.0  
> Status: Approved  
> Last Updated: 2024-03-01  
> Applies To: All FTGO microservices

## Table of Contents

1. [URL Naming Conventions](#1-url-naming-conventions)
2. [HTTP Method Usage](#2-http-method-usage)
3. [HTTP Status Codes](#3-http-status-codes)
4. [Request/Response Format](#4-requestresponse-format)
5. [Pagination](#5-pagination)
6. [Filtering and Sorting](#6-filtering-and-sorting)
7. [Date and Time Format](#7-date-and-time-format)
8. [Error Handling](#8-error-handling)
9. [API Versioning](#9-api-versioning)
10. [Security Headers](#10-security-headers)
11. [OpenAPI Documentation](#11-openapi-documentation)

---

## 1. URL Naming Conventions

### Rules

| Rule | Example | Anti-Pattern |
|------|---------|-------------|
| Use **plural nouns** for resource collections | `/api/v1/orders` | `/api/v1/order` |
| Use **lowercase** with **hyphens** for multi-word resources | `/api/v1/menu-items` | `/api/v1/menuItems`, `/api/v1/menu_items` |
| Use **path parameters** for resource identification | `/api/v1/orders/{orderId}` | `/api/v1/orders?id=123` |
| Use **query parameters** for filtering/sorting | `/api/v1/orders?status=PENDING` | `/api/v1/orders/status/PENDING` |
| Use **nested resources** for sub-collections | `/api/v1/orders/{orderId}/line-items` | `/api/v1/order-line-items?orderId=123` |
| Use **verbs** only for non-CRUD actions | `/api/v1/orders/{orderId}/cancel` | `/api/v1/cancelOrder/{orderId}` |

### URL Structure

```
/api/v{version}/{resource-collection}/{resource-id}/{sub-resource}
```

**Examples:**

```
GET    /api/v1/orders                          # List orders
GET    /api/v1/orders/123                       # Get order by ID
POST   /api/v1/orders                           # Create order
PUT    /api/v1/orders/123                       # Replace order
PATCH  /api/v1/orders/123                       # Partially update order
DELETE /api/v1/orders/123                       # Delete order
GET    /api/v1/orders/123/line-items            # List line items for order
POST   /api/v1/orders/123/cancel                # Cancel order (action)
```

---

## 2. HTTP Method Usage

| Method | Purpose | Idempotent | Request Body | Success Code |
|--------|---------|------------|-------------|--------------|
| `GET` | Retrieve resource(s) | Yes | No | `200 OK` |
| `POST` | Create a new resource | No | Yes | `201 Created` |
| `PUT` | Replace a resource entirely | Yes | Yes | `200 OK` |
| `PATCH` | Partially update a resource | No | Yes (partial) | `200 OK` |
| `DELETE` | Remove a resource | Yes | No | `204 No Content` |

### Method Guidelines

- **GET**: Must be safe (no side effects). Never use GET to modify state.
- **POST**: Returns the created resource with a `Location` header pointing to the new resource URI.
- **PUT**: Requires the complete resource representation. Missing fields are set to defaults/null.
- **PATCH**: Only include fields that need to change. Use [JSON Merge Patch](https://tools.ietf.org/html/rfc7386) format.
- **DELETE**: Returns `204 No Content` on success. Deleting a non-existent resource returns `404`.

---

## 3. HTTP Status Codes

### Success Codes

| Code | Meaning | Usage |
|------|---------|-------|
| `200 OK` | Request succeeded | GET, PUT, PATCH responses |
| `201 Created` | Resource created | POST responses |
| `204 No Content` | Request succeeded, no body | DELETE responses |

### Client Error Codes

| Code | Meaning | Usage |
|------|---------|-------|
| `400 Bad Request` | Invalid input or validation failure | Malformed JSON, missing required fields, constraint violations |
| `401 Unauthorized` | Authentication required | Missing or invalid authentication token |
| `403 Forbidden` | Insufficient permissions | Authenticated but not authorized for this resource |
| `404 Not Found` | Resource does not exist | Invalid resource ID or path |
| `409 Conflict` | Business rule violation or state conflict | Duplicate creation, invalid state transition |
| `422 Unprocessable Entity` | Semantically invalid request | Valid JSON but business logic rejection |

### Server Error Codes

| Code | Meaning | Usage |
|------|---------|-------|
| `500 Internal Server Error` | Unexpected server failure | Unhandled exceptions, infrastructure issues |
| `503 Service Unavailable` | Service temporarily unavailable | Maintenance mode, circuit breaker open |

---

## 4. Request/Response Format

### Content Type

All endpoints accept and return `application/json` unless otherwise specified.

```
Content-Type: application/json
Accept: application/json
```

### Standard Success Response Envelope

All successful responses **must** use the `ApiResponse<T>` envelope:

```json
{
  "status": "success",
  "data": {
    "orderId": 123,
    "status": "PENDING",
    "totalAmount": {
      "amount": "29.99",
      "currency": "USD"
    },
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-15T10:30:00.123Z",
  "path": "/api/v1/orders/123"
}
```

### Standard Error Response Envelope

All error responses **must** use the `ApiErrorResponse` envelope:

```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "deliveryAddress.zipCode",
        "message": "must be a valid 5-digit ZIP code"
      },
      {
        "field": "lineItems",
        "message": "must not be empty"
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00.456Z",
  "path": "/api/v1/orders"
}
```

### Error Codes

Use **SCREAMING_SNAKE_CASE** for machine-readable error codes:

| Error Code | HTTP Status | Description |
|-----------|------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `INVALID_FORMAT` | 400 | Malformed request body |
| `AUTHENTICATION_REQUIRED` | 401 | Missing authentication |
| `INVALID_CREDENTIALS` | 401 | Invalid authentication token |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource not found |
| `RESOURCE_CONFLICT` | 409 | State conflict or duplicate |
| `INVALID_STATE_TRANSITION` | 409 | Invalid status change |
| `INTERNAL_ERROR` | 500 | Unexpected server error |
| `SERVICE_UNAVAILABLE` | 503 | Dependent service unavailable |

---

## 5. Pagination

All list endpoints **must** support pagination using the following standard parameters:

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | `0` | Zero-based page number |
| `size` | integer | `20` | Number of items per page (max: 100) |
| `sort` | string | `createdAt` | Field name to sort by |
| `direction` | string | `DESC` | Sort direction: `ASC` or `DESC` |

### Paginated Response Format

```json
{
  "status": "success",
  "data": [
    { "orderId": 1, "status": "DELIVERED" },
    { "orderId": 2, "status": "PENDING" }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/orders?page=0&size=20&sort=createdAt&direction=DESC"
}
```

### Implementation

Use `PagedResponse<T>` from `ftgo-openapi-lib`:

```java
@GetMapping
@ApiPageable
public ResponseEntity<PagedResponse<OrderDTO>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "DESC") String direction) {

    Page<Order> result = orderRepository.findAll(
        PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(direction), sort))
    );

    return ResponseEntity.ok(PagedResponse.of(
        result.getContent().stream().map(this::toDto).toList(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages()
    ));
}
```

---

## 6. Filtering and Sorting

### Filtering

Use query parameters for filtering. Multiple filters are combined with AND logic:

```
GET /api/v1/orders?status=PENDING&customerId=42
GET /api/v1/restaurants?cuisine=ITALIAN&city=san-francisco
```

**Conventions:**

- Use the field name as the query parameter name
- Use comma-separated values for multi-value filters: `?status=PENDING,APPROVED`
- Use range filters with `From`/`To` suffixes: `?createdAtFrom=2024-01-01&createdAtTo=2024-01-31`
- Use `q` for full-text search: `?q=pizza`

### Sorting

Use `sort` and `direction` parameters:

```
GET /api/v1/orders?sort=createdAt&direction=DESC
GET /api/v1/restaurants?sort=name&direction=ASC
```

---

## 7. Date and Time Format

All date and time values **must** use [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format in UTC:

| Type | Format | Example |
|------|--------|---------|
| Date-time | `yyyy-MM-dd'T'HH:mm:ss'Z'` | `2024-01-15T10:30:00Z` |
| Date-time with millis | `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` | `2024-01-15T10:30:00.123Z` |
| Date only | `yyyy-MM-dd` | `2024-01-15` |
| Time only | `HH:mm:ss` | `10:30:00` |

### Jackson Configuration

Configure Jackson in `application.properties`:

```properties
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
spring.jackson.time-zone=UTC
spring.jackson.deserialization.adjust-dates-to-context-time-zone=false
```

---

## 8. Error Handling

### Global Exception Handler

Each microservice should define a `@RestControllerAdvice` that maps exceptions to the standard `ApiErrorResponse` format:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
            "VALIDATION_ERROR", "Validation failed", request.getRequestURI());
        ex.getBindingResult().getFieldErrors().forEach(error ->
            response.addDetail(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.of("RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", request.getRequestURI()));
    }
}
```

---

## 9. API Versioning

See [API Versioning Strategy](api-versioning.md) for the full versioning policy.

**Summary:**

- Use **URL path versioning**: `/api/v1/orders`
- Major version in URL, minor/patch versions are backward-compatible
- Support at most **2 major versions** concurrently
- Deprecation notices via `Deprecation` and `Sunset` HTTP headers

---

## 10. Security Headers

All API responses should include:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Cache-Control: no-store
```

Authenticated endpoints should return:

```
WWW-Authenticate: Bearer realm="ftgo"    (on 401)
```

---

## 11. OpenAPI Documentation

### Requirements

- Every microservice **must** expose OpenAPI 3.0 documentation
- Swagger UI accessible at `/swagger-ui.html`
- OpenAPI JSON spec at `/v3/api-docs`
- OpenAPI YAML spec at `/v3/api-docs.yaml`
- All endpoints annotated with `@Operation`, `@Tag`, and `@Schema`
- Use `@ApiStandardResponses` for standard error documentation
- Use `@ApiPageable` for paginated endpoints

### Controller Annotation Example

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves a specific order by its unique identifier"
    )
    @ApiStandardResponses
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Order found"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Order not found"
    )
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @Parameter(description = "Order ID", example = "123")
            @PathVariable Long orderId) {
        // ...
    }
}
```

### DTO Annotation Example

```java
@Schema(description = "Order details")
public class OrderDTO {

    @Schema(description = "Unique order identifier", example = "123")
    private Long orderId;

    @Schema(description = "Current order status", example = "PENDING",
            allowableValues = {"PENDING", "APPROVED", "PREPARING", "READY", "DELIVERED", "CANCELLED"})
    private String status;

    @Schema(description = "Order total amount")
    private MoneyDTO totalAmount;

    @Schema(description = "Order creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;
}
```

---

## References

- [FTGO OpenAPI Library](../shared/ftgo-openapi-lib/README.md) - Shared library providing standard models and configuration
- [API Versioning Strategy](api-versioning.md) - Detailed versioning policy
- [ADR-0002: SpringDoc OpenAPI Migration](adr/0002-springdoc-openapi-migration.md) - Architecture decision record
- [API Contract Testing](api-contract-testing.md) - Cross-service contract testing strategy
- [SpringDoc Documentation](https://springdoc.org/) - SpringDoc OpenAPI reference
- [OpenAPI 3.0 Specification](https://swagger.io/specification/) - OpenAPI standard
