# FTGO REST API Standards

> Version: 1.0.0  
> Last Updated: 2024-01-15  
> Status: Approved

This document defines the REST API standards for all FTGO microservices. All new and migrated endpoints **must** conform to these standards.

---

## Table of Contents

1. [URL Naming Conventions](#1-url-naming-conventions)
2. [HTTP Method Usage](#2-http-method-usage)
3. [Status Code Standards](#3-status-code-standards)
4. [API Versioning Strategy](#4-api-versioning-strategy)
5. [Request/Response Envelope Format](#5-requestresponse-envelope-format)
6. [Pagination Format](#6-pagination-format)
7. [Date/Time Format](#7-datetime-format)
8. [Error Handling](#8-error-handling)
9. [OpenAPI Documentation](#9-openapi-documentation)

---

## 1. URL Naming Conventions

### Rules

| Rule | Example | Anti-Pattern |
|------|---------|-------------|
| Use **plural nouns** for resource collections | `/api/v1/orders` | `/api/v1/order` |
| Use **lowercase** letters only | `/api/v1/menu-items` | `/api/v1/MenuItems` |
| Use **hyphens** (`-`) to separate words | `/api/v1/delivery-addresses` | `/api/v1/delivery_addresses` |
| Use **path parameters** for resource identifiers | `/api/v1/orders/{orderId}` | `/api/v1/orders?id=123` |
| Use **query parameters** for filtering/sorting | `/api/v1/orders?status=APPROVED` | `/api/v1/orders/approved` |
| Nest sub-resources under parent resources | `/api/v1/orders/{orderId}/line-items` | `/api/v1/line-items?orderId=123` |
| Maximum nesting depth: **2 levels** | `/api/v1/orders/{id}/line-items` | `/api/v1/orders/{id}/line-items/{lid}/options` |
| No trailing slashes | `/api/v1/orders` | `/api/v1/orders/` |
| No file extensions in URLs | `/api/v1/orders` | `/api/v1/orders.json` |
| No verbs in resource URLs | `/api/v1/orders` | `/api/v1/getOrders` |

### Actions That Don't Map to CRUD

For actions that don't map cleanly to CRUD operations, use a sub-resource verb:

```
POST /api/v1/orders/{orderId}/cancel
POST /api/v1/orders/{orderId}/revise
POST /api/v1/restaurants/{restaurantId}/activate
```

---

## 2. HTTP Method Usage

| Method | Usage | Idempotent | Request Body | Example |
|--------|-------|-----------|-------------|---------|
| `GET` | Retrieve a resource or collection | Yes | No | `GET /api/v1/orders/123` |
| `POST` | Create a new resource | No | Yes | `POST /api/v1/orders` |
| `PUT` | Full update of a resource | Yes | Yes | `PUT /api/v1/orders/123` |
| `PATCH` | Partial update of a resource | No | Yes | `PATCH /api/v1/orders/123` |
| `DELETE` | Remove a resource | Yes | No | `DELETE /api/v1/orders/123` |

### Guidelines

- **GET** requests must never modify server state
- **POST** is used for creating resources and triggering actions
- **PUT** replaces the entire resource; all fields are required
- **PATCH** updates only the specified fields
- **DELETE** should be idempotent; deleting a non-existent resource returns `204`

---

## 3. Status Code Standards

### Success Codes

| Code | Meaning | When to Use |
|------|---------|------------|
| `200 OK` | Request succeeded | GET, PUT, PATCH responses with body |
| `201 Created` | Resource created | POST that creates a resource |
| `204 No Content` | Request succeeded, no body | DELETE, PUT/PATCH with no response body |

### Client Error Codes

| Code | Meaning | When to Use |
|------|---------|------------|
| `400 Bad Request` | Invalid request syntax or parameters | Validation errors, malformed JSON |
| `401 Unauthorized` | Authentication required | Missing or invalid authentication token |
| `403 Forbidden` | Insufficient permissions | Valid auth but no permission for the resource |
| `404 Not Found` | Resource does not exist | GET/PUT/PATCH/DELETE on non-existent resource |
| `409 Conflict` | Conflicting state | Invalid state transition (e.g., cancelling a delivered order) |
| `422 Unprocessable Entity` | Semantically invalid request | Valid JSON but business rule violation |

### Server Error Codes

| Code | Meaning | When to Use |
|------|---------|------------|
| `500 Internal Server Error` | Unexpected server error | Unhandled exceptions |
| `503 Service Unavailable` | Service temporarily down | Maintenance, dependency outage |

---

## 4. API Versioning Strategy

### Strategy: URL Path Versioning

All FTGO APIs use **URL path versioning** with the prefix `/api/v{major}/`.

```
/api/v1/orders
/api/v1/consumers
/api/v1/restaurants
/api/v1/couriers
```

### Rules

1. **Version in the URL path**: `/api/v1/resource`
2. **Major version only**: Increment only for breaking changes
3. **Breaking changes** include:
   - Removing an endpoint
   - Removing or renaming a required field
   - Changing the type of an existing field
   - Changing the response status code for an existing operation
4. **Non-breaking changes** (no version bump):
   - Adding a new optional field
   - Adding a new endpoint
   - Adding a new optional query parameter
5. **Deprecation process**:
   - Announce deprecation in the OpenAPI spec using `@Deprecated`
   - Maintain the old version for a minimum of **6 months**
   - Include `Sunset` header in responses: `Sunset: Sat, 01 Jul 2025 00:00:00 GMT`
   - Include `Deprecation` header: `Deprecation: true`

### Controller Mapping

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {
    // ...
}
```

---

## 5. Request/Response Envelope Format

### Success Response

All successful responses use the standard envelope:

```json
{
  "status": "success",
  "data": { ... },
  "message": "Optional human-readable message",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | string | Yes | Always `"success"` |
| `data` | object/array | Yes | Response payload |
| `message` | string | No | Optional message |
| `timestamp` | string | Yes | ISO 8601 timestamp |

### Java Implementation

```java
import net.chrisrichardson.ftgo.openapi.model.ApiResponse;

@GetMapping("/{orderId}")
public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long orderId) {
    OrderDTO order = orderService.findById(orderId);
    return ResponseEntity.ok(ApiResponse.success(order));
}
```

### Error Response

```json
{
  "status": "error",
  "code": "ORDER_NOT_FOUND",
  "message": "Order with id 12345 not found",
  "details": [
    {
      "field": "orderId",
      "rejectedValue": "12345",
      "message": "No order exists with this ID"
    }
  ],
  "path": "/api/v1/orders/12345",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | string | Yes | Always `"error"` |
| `code` | string | Yes | Application error code (e.g., `ORDER_NOT_FOUND`) |
| `message` | string | Yes | Human-readable error message |
| `details` | array | No | Field-level validation errors |
| `path` | string | Yes | Request path |
| `timestamp` | string | Yes | ISO 8601 timestamp |

### Java Implementation

```java
import net.chrisrichardson.ftgo.openapi.model.ErrorResponse;

@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex,
                                                    HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("ORDER_NOT_FOUND", ex.getMessage(), request.getRequestURI()));
}
```

---

## 6. Pagination Format

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `20` | Items per page (max: 100) |
| `sort` | string | varies | Sort criteria: `field,direction` (e.g., `createdAt,desc`) |

### Paginated Response

```json
{
  "status": "success",
  "data": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "sort": "createdAt,desc",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `data` | array | Items in the current page |
| `page` | int | Current page number (0-based) |
| `size` | int | Items per page |
| `totalElements` | long | Total items across all pages |
| `totalPages` | int | Total number of pages |
| `sort` | string | Applied sort criteria |

### Java Implementation

```java
import net.chrisrichardson.ftgo.openapi.model.PagedResponse;

@GetMapping
public ResponseEntity<PagedResponse<OrderDTO>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort) {
    Page<OrderDTO> result = orderService.findAll(PageRequest.of(page, size));
    return ResponseEntity.ok(
            PagedResponse.of(result.getContent(), page, size, result.getTotalElements(), sort));
}
```

---

## 7. Date/Time Format

### Standard: ISO 8601

All date/time values **must** use [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format.

| Type | Format | Example |
|------|--------|---------|
| Date-time (UTC) | `yyyy-MM-dd'T'HH:mm:ss'Z'` | `2024-01-15T10:30:00Z` |
| Date-time (offset) | `yyyy-MM-dd'T'HH:mm:ssXXX` | `2024-01-15T10:30:00-08:00` |
| Date only | `yyyy-MM-dd` | `2024-01-15` |
| Time only | `HH:mm:ss` | `10:30:00` |

### Rules

1. **Always use UTC** for API responses; include the `Z` suffix
2. **Accept timezone offsets** in requests but normalize to UTC for storage
3. **Configure Jackson** to serialize as ISO 8601 strings (not timestamps):
   ```properties
   spring.jackson.serialization.write-dates-as-timestamps=false
   ```
4. Use `java.time.Instant` for timestamps and `java.time.LocalDate` for dates

---

## 8. Error Handling

### Error Code Convention

Error codes follow the pattern: `{DOMAIN}_{ERROR_TYPE}`

| Code | HTTP Status | Description |
|------|------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `AUTHENTICATION_REQUIRED` | 401 | Missing or invalid auth token |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `ORDER_NOT_FOUND` | 404 | Order does not exist |
| `CONSUMER_NOT_FOUND` | 404 | Consumer does not exist |
| `RESTAURANT_NOT_FOUND` | 404 | Restaurant does not exist |
| `ORDER_INVALID_STATE` | 409 | Invalid order state transition |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### Global Exception Handler Pattern

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<ErrorResponse.FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorResponse.FieldError(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", details, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", request.getRequestURI()));
    }
}
```

---

## 9. OpenAPI Documentation

### Library

All FTGO microservices use [SpringDoc OpenAPI 3](https://springdoc.org/) (replacing the deprecated Springfox Swagger 2.x).

### Shared Library

The `shared/ftgo-openapi-lib` module provides:
- Auto-configured `OpenAPI` bean with FTGO metadata
- Swagger UI redirect at `/swagger-ui.html`
- Standard response envelope models (`ApiResponse`, `PagedResponse`, `ErrorResponse`)
- Example annotations in `OrderApiExample`

### Swagger UI Access

Each service exposes Swagger UI at:
```
http://{host}:{port}/swagger-ui.html
```

### Annotation Standards

Use OpenAPI 3 annotations from `io.swagger.v3.oas.annotations`:

```java
@Tag(name = "Orders", description = "Order management operations")
@Operation(summary = "Get order by ID", description = "Retrieves a single order")
@Parameter(description = "Order ID", required = true, example = "12345")
@ApiResponse(responseCode = "200", description = "Order found")
@Schema(description = "Order data transfer object")
```

### Migration from Springfox

| Springfox (Old) | SpringDoc (New) |
|-----------------|-----------------|
| `@Api` | `@Tag` |
| `@ApiOperation` | `@Operation` |
| `@ApiParam` | `@Parameter` |
| `@ApiResponse` (Springfox) | `@ApiResponse` (io.swagger.v3) |
| `@ApiModel` | `@Schema` |
| `@ApiModelProperty` | `@Schema` |
| `@ApiIgnore` | `@Parameter(hidden = true)` |
| `@EnableSwagger2` | Not needed (auto-configured) |
| `Docket` bean | `OpenAPI` + `GroupedOpenApi` beans |

See `shared/ftgo-openapi-lib/src/main/java/net/chrisrichardson/ftgo/openapi/example/OrderApiExample.java` for complete annotation examples.
