# FTGO REST API Standards

> Version: 1.0 | Last Updated: 2024-01-15 | Status: Active

This document defines the REST API standards for all FTGO microservices. All new and existing endpoints must conform to these standards.

## Table of Contents

1. [URL Naming Conventions](#1-url-naming-conventions)
2. [HTTP Methods](#2-http-methods)
3. [Status Codes](#3-status-codes)
4. [Request/Response Format](#4-requestresponse-format)
5. [Pagination](#5-pagination)
6. [Filtering and Sorting](#6-filtering-and-sorting)
7. [Date/Time Format](#7-datetime-format)
8. [API Versioning](#8-api-versioning)
9. [Error Handling](#9-error-handling)
10. [OpenAPI Documentation](#10-openapi-documentation)
11. [Security](#11-security)

---

## 1. URL Naming Conventions

### Rules

| Rule | Example | Anti-pattern |
|------|---------|--------------|
| Use **plural nouns** for collections | `/orders`, `/restaurants` | `/order`, `/getOrders` |
| Use **lowercase** with **hyphens** | `/order-items` | `/orderItems`, `/order_items` |
| Use **path parameters** for identity | `/orders/{orderId}` | `/orders?id=123` |
| Use **query parameters** for filtering | `/orders?status=APPROVED` | `/orders/approved` |
| No trailing slashes | `/orders` | `/orders/` |
| No file extensions | `/orders/123` | `/orders/123.json` |
| No verbs in URLs (use HTTP methods) | `POST /orders` | `/createOrder` |

### Resource Hierarchy

```
/api/v1/{resource}                    # Collection
/api/v1/{resource}/{id}               # Single resource
/api/v1/{resource}/{id}/{sub-resource} # Sub-resource collection
/api/v1/{resource}/{id}/{action}       # Action on resource (when CRUD doesn't fit)
```

### Examples

```
GET    /api/v1/orders                         # List orders
POST   /api/v1/orders                         # Create order
GET    /api/v1/orders/123                      # Get order 123
PUT    /api/v1/orders/123                      # Update order 123
DELETE /api/v1/orders/123                      # Delete order 123
POST   /api/v1/orders/123/cancel               # Cancel order 123 (action)
GET    /api/v1/orders/123/line-items            # Get line items for order 123
GET    /api/v1/restaurants/456/menu-items       # Get menu items for restaurant 456
```

---

## 2. HTTP Methods

| Method | Purpose | Idempotent | Request Body | Response Body |
|--------|---------|------------|--------------|---------------|
| `GET` | Retrieve resource(s) | Yes | No | Yes |
| `POST` | Create a resource or trigger an action | No | Yes | Yes |
| `PUT` | Full update of a resource | Yes | Yes | Yes |
| `PATCH` | Partial update of a resource | No | Yes | Yes |
| `DELETE` | Remove a resource | Yes | No | No (or confirmation) |

### Guidelines

- **GET** must never modify server state (safe method)
- **PUT** replaces the entire resource; **PATCH** updates specific fields
- **POST** is used for creation and for actions that don't map to CRUD (e.g., `/orders/{id}/cancel`)
- **DELETE** should be idempotent - deleting a non-existent resource returns 204 (not 404)

---

## 3. Status Codes

### Success Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `200 OK` | Success | GET, PUT, PATCH with response body |
| `201 Created` | Resource created | POST that creates a resource |
| `204 No Content` | Success, no body | DELETE, or PUT/POST with no response body |

### Client Error Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `400 Bad Request` | Invalid request | Validation errors, malformed JSON |
| `401 Unauthorized` | Not authenticated | Missing or invalid authentication token |
| `403 Forbidden` | Not authorized | Authenticated but lacks permission |
| `404 Not Found` | Resource not found | Resource with given ID doesn't exist |
| `409 Conflict` | State conflict | Duplicate resource, invalid state transition |
| `422 Unprocessable Entity` | Semantic error | Request is syntactically valid but semantically incorrect |

### Server Error Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `500 Internal Server Error` | Server error | Unexpected server-side errors |
| `502 Bad Gateway` | Upstream error | Error from a downstream service |
| `503 Service Unavailable` | Temporarily unavailable | Service is overloaded or in maintenance |

---

## 4. Request/Response Format

### Content Type

All API endpoints use `application/json` as the content type:

```
Content-Type: application/json
Accept: application/json
```

### Standard Response Envelope

All successful responses use the `ApiResponse<T>` envelope:

```json
{
  "status": "success",
  "data": {
    "id": 123,
    "state": "APPROVED",
    "orderTotal": "12.50"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Standard Error Response

All error responses use the `ApiError` format:

```json
{
  "status": "error",
  "code": 400,
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "errors": [
    {
      "field": "consumerId",
      "message": "must not be null"
    },
    {
      "field": "restaurantId",
      "message": "must be greater than 0"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 5. Pagination

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Zero-based page number |
| `size` | int | `20` | Number of items per page (max: 100) |
| `sort` | string | varies | Sort criteria: `field,direction` (e.g., `createdAt,desc`) |

### Example Request

```
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

### Paginated Response Format

All list endpoints that may return large datasets use `PagedResponse<T>`:

```json
{
  "status": "success",
  "data": [
    { "id": 1, "state": "APPROVED" },
    { "id": 2, "state": "DELIVERED" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "sort": "createdAt,desc",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Guidelines

- Default page size is 20, maximum is 100
- Page numbering is zero-based
- Always return `totalElements` and `totalPages` in paginated responses
- Multiple sort criteria can be specified: `sort=createdAt,desc&sort=name,asc`

---

## 6. Filtering and Sorting

### Filtering

Use query parameters for filtering:

```
GET /api/v1/orders?status=APPROVED&consumerId=123
GET /api/v1/restaurants?name=Pizza
```

### Filter Operators

For advanced filtering, use these conventions:

| Operator | Example | Meaning |
|----------|---------|---------|
| Equality | `?status=APPROVED` | Exact match |
| Greater than | `?minTotal=10.00` | Greater than or equal |
| Less than | `?maxTotal=50.00` | Less than or equal |
| Date range | `?createdAfter=2024-01-01&createdBefore=2024-01-31` | Date range |
| Search | `?q=pizza` | Full-text search |

### Sorting

Sorting uses the `sort` query parameter:

```
GET /api/v1/orders?sort=createdAt,desc
GET /api/v1/orders?sort=orderTotal,asc&sort=createdAt,desc
```

- Format: `sort=field,direction`
- Direction: `asc` (ascending) or `desc` (descending)
- Default direction: `asc`
- Multiple sort criteria: repeat the `sort` parameter

---

## 7. Date/Time Format

All dates and times use **ISO 8601** format:

| Type | Format | Example |
|------|--------|---------|
| Date-time (UTC) | `yyyy-MM-dd'T'HH:mm:ss'Z'` | `2024-01-15T10:30:00Z` |
| Date-time (offset) | `yyyy-MM-dd'T'HH:mm:ssXXX` | `2024-01-15T10:30:00+05:30` |
| Date only | `yyyy-MM-dd` | `2024-01-15` |
| Time only | `HH:mm:ss` | `10:30:00` |

### Guidelines

- Store all timestamps in **UTC** internally
- Return all timestamps in **UTC** (suffix `Z`)
- Accept timestamps with timezone offsets and convert to UTC
- Use `Instant` or `OffsetDateTime` in Java (never `Date` or `LocalDateTime` for API fields)
- Configure Jackson with `jackson-datatype-jsr310` for proper serialization

---

## 8. API Versioning

### Strategy: URL Path Versioning

FTGO uses **URL path versioning** with the format `/api/v{N}/...`:

```
/api/v1/orders
/api/v1/restaurants
/api/v1/consumers
/api/v1/couriers
```

### Version Constants

Use the `ApiVersioning` class from `ftgo-openapi-lib`:

```java
import net.chrisrichardson.ftgo.openapi.config.ApiVersioning;

@RestController
@RequestMapping(path = ApiVersioning.API_V1 + "/orders")
public class OrderController {
    // ...
}
```

### When to Create a New Version

**Breaking changes (require new version):**
- Removing an endpoint
- Removing or renaming a response field
- Changing a field's data type
- Changing a field from optional to required
- Changing URL structure

**Non-breaking changes (no new version needed):**
- Adding new endpoints
- Adding optional fields to responses
- Adding optional query parameters
- Adding new error codes

### Deprecation Policy

1. When a new version is released, the previous version is marked as **deprecated**
2. Deprecated versions include a `Sunset` header in responses:
   ```
   Sunset: Sat, 01 Jul 2025 00:00:00 GMT
   Deprecation: true
   ```
3. Deprecated versions are supported for a minimum of **6 months**
4. Communicate deprecation via API docs, changelogs, and response headers
5. After the sunset date, deprecated endpoints return `410 Gone`

---

## 9. Error Handling

### Standard Error Format

All errors use the `ApiError` class from `ftgo-openapi-lib`:

```java
import net.chrisrichardson.ftgo.openapi.model.ApiError;

@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ApiError> handleNotFound(OrderNotFoundException ex, HttpServletRequest request) {
    ApiError error = new ApiError(404, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
}
```

### Validation Errors

Return field-level errors for validation failures:

```json
{
  "status": "error",
  "code": 400,
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "errors": [
    { "field": "consumerId", "message": "must not be null" },
    { "field": "lineItems", "message": "must not be empty" }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Guidelines

- Always return a meaningful `message`
- Include `path` to help identify which request caused the error
- For validation errors, include field-level `errors` array
- Never expose stack traces, internal class names, or SQL queries in error responses
- Log detailed error information server-side for debugging

---

## 10. OpenAPI Documentation

### Library

All services use **SpringDoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui`) via the `ftgo-openapi-lib` shared library.

### Endpoints

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Swagger UI interactive documentation |
| `/v3/api-docs` | OpenAPI 3.0 spec (JSON) |
| `/v3/api-docs.yaml` | OpenAPI 3.0 spec (YAML) |

### Required Annotations

Every controller and endpoint must include:

```java
@Tag(name = "Orders", description = "Order management operations")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Operation(summary = "Create a new order", description = "Creates an order for a consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
        // ...
    }
}
```

### Annotation Checklist

- [ ] `@Tag` on every controller class
- [ ] `@Operation` with `summary` and `description` on every endpoint
- [ ] `@ApiResponses` with all possible status codes on every endpoint
- [ ] `@Parameter` on all path variables and query parameters
- [ ] `@Schema` on all request/response DTOs

---

## 11. Security

### Authentication Headers

```
Authorization: Bearer <jwt-token>
```

### CORS

Configure CORS per-service for allowed origins, methods, and headers.

### Rate Limiting

Response headers for rate-limited endpoints:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1705312200
```

---

## Appendix: Quick Reference

### Shared Libraries

| Library | Purpose |
|---------|---------|
| `ftgo-openapi-lib` | OpenAPI 3.0 configuration, response models, versioning |
| `ftgo-api-contract-testing` | Contract test base classes and verification utilities |

### Response Model Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `ApiResponse<T>` | `net.chrisrichardson.ftgo.openapi.model` | Standard success envelope |
| `PagedResponse<T>` | `net.chrisrichardson.ftgo.openapi.model` | Paginated list response |
| `ApiError` | `net.chrisrichardson.ftgo.openapi.model` | Standard error response |
| `ApiError.FieldError` | `net.chrisrichardson.ftgo.openapi.model` | Field validation error |

### Versioning Constants

| Constant | Value | Class |
|----------|-------|-------|
| `API_V1` | `/api/v1` | `ApiVersioning` |
| `CURRENT_VERSION` | `1` | `ApiVersioning` |
