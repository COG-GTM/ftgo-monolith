# FTGO REST API Standards

> Version: 1.0  
> Status: Approved  
> Last Updated: 2026-03-03

This document defines the REST API standards for all FTGO microservices. All new and migrated endpoints MUST conform to these standards to ensure consistency, maintainability, and a good developer experience.

---

## Table of Contents

1. [URL Naming Conventions](#1-url-naming-conventions)
2. [HTTP Method Usage](#2-http-method-usage)
3. [Status Codes](#3-status-codes)
4. [Request/Response Envelope](#4-requestresponse-envelope)
5. [Pagination](#5-pagination)
6. [Filtering and Sorting](#6-filtering-and-sorting)
7. [Date/Time Format](#7-datetime-format)
8. [API Versioning](#8-api-versioning)
9. [Error Handling](#9-error-handling)
10. [OpenAPI Documentation](#10-openapi-documentation)
11. [Security](#11-security)
12. [Migration from Springfox to SpringDoc](#12-migration-from-springfox-to-springdoc)

---

## 1. URL Naming Conventions

### General Rules

- Use **lowercase** with **hyphens** as word separators (kebab-case).
- Use **plural nouns** for resource collections.
- Use **path parameters** for resource identifiers.
- Do NOT use trailing slashes.
- Do NOT include file extensions (e.g., `.json`).
- Do NOT use verbs in URLs (use HTTP methods instead).

### URL Structure

```
/{api-version}/{resource-collection}/{resource-id}/{sub-resource-collection}/{sub-resource-id}
```

### Examples

| Pattern | Example | Description |
|---------|---------|-------------|
| Collection | `GET /v1/orders` | List all orders |
| Single resource | `GET /v1/orders/{orderId}` | Get order by ID |
| Sub-resource | `GET /v1/orders/{orderId}/line-items` | List line items for an order |
| Action (exceptional) | `POST /v1/orders/{orderId}/cancel` | Cancel an order (state transition) |

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Path segments | kebab-case, plural nouns | `/order-line-items` |
| Path parameters | camelCase | `{orderId}` |
| Query parameters | camelCase | `?pageSize=20&sortBy=createdAt` |

---

## 2. HTTP Method Usage

| Method | Usage | Idempotent | Safe | Request Body |
|--------|-------|------------|------|-------------|
| `GET` | Retrieve resource(s) | Yes | Yes | No |
| `POST` | Create a new resource | No | No | Yes |
| `PUT` | Full replacement of a resource | Yes | No | Yes |
| `PATCH` | Partial update of a resource | No | No | Yes |
| `DELETE` | Remove a resource | Yes | No | No |

### Guidelines

- **GET** MUST NOT have side effects. Use for read-only operations.
- **POST** is used for resource creation and non-idempotent operations (e.g., state transitions like `/orders/{id}/cancel`).
- **PUT** replaces the entire resource. All required fields must be present.
- **PATCH** updates only the provided fields. Use JSON Merge Patch (RFC 7396).
- **DELETE** should return `204 No Content` on success. Deleting a non-existent resource returns `404`.

---

## 3. Status Codes

### Success Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `200 OK` | Success | GET, PUT, PATCH returning data |
| `201 Created` | Resource created | POST when a new resource is created |
| `204 No Content` | Success, no body | DELETE, PUT/PATCH with no response body |

### Client Error Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `400 Bad Request` | Invalid request | Malformed JSON, validation failures |
| `401 Unauthorized` | Authentication required | Missing or invalid credentials |
| `403 Forbidden` | Insufficient permissions | Valid credentials, but not authorized |
| `404 Not Found` | Resource not found | Resource does not exist |
| `405 Method Not Allowed` | HTTP method not supported | Wrong method on endpoint |
| `409 Conflict` | State conflict | Duplicate creation, concurrent modification |
| `422 Unprocessable Entity` | Semantic validation failure | Business rule violation |
| `429 Too Many Requests` | Rate limit exceeded | Client is being throttled |

### Server Error Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `500 Internal Server Error` | Unexpected error | Unhandled exceptions |
| `502 Bad Gateway` | Upstream failure | Downstream service unavailable |
| `503 Service Unavailable` | Temporary outage | Maintenance, circuit breaker open |
| `504 Gateway Timeout` | Upstream timeout | Downstream service too slow |

---

## 4. Request/Response Envelope

All API responses MUST use the standard envelope defined in `com.ftgo.openapi.model.ApiResponse`.

### Success Response

```json
{
  "status": "success",
  "data": {
    "orderId": 12345,
    "state": "APPROVED",
    "totalAmount": "29.99"
  },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

### Error Response

```json
{
  "status": "error",
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order with ID 99999 was not found",
    "details": "Verify the order ID and try again"
  },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

### Validation Error Response

```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "fieldErrors": [
      {
        "field": "deliveryAddress.zip",
        "message": "must not be blank",
        "rejectedValue": ""
      },
      {
        "field": "lineItems",
        "message": "must not be empty",
        "rejectedValue": null
      }
    ]
  },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

### Error Code Conventions

Error codes MUST be:
- **UPPER_SNAKE_CASE** (e.g., `ORDER_NOT_FOUND`)
- **Prefixed with the domain** for domain-specific errors (e.g., `ORDER_ALREADY_CANCELLED`)
- **Generic for cross-cutting errors** (e.g., `VALIDATION_FAILED`, `INTERNAL_ERROR`)

### Standard Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_FAILED` | 400 | Request body validation failure |
| `UNAUTHORIZED` | 401 | Missing or invalid authentication |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource state conflict |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 5. Pagination

All list endpoints returning collections MUST support pagination using the standard `PagedResponse` model from `com.ftgo.openapi.model.PagedResponse`.

### Query Parameters

| Parameter | Type | Default | Max | Description |
|-----------|------|---------|-----|-------------|
| `page` | integer | `0` | - | Page number (0-based) |
| `size` | integer | `20` | `100` | Number of items per page |
| `sort` | string | - | - | Sort field and direction (e.g., `createdAt,desc`) |

### Paginated Response

```json
{
  "status": "success",
  "data": [
    { "orderId": 1, "state": "APPROVED" },
    { "orderId": 2, "state": "PREPARING" }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

### Guidelines

- Default page size is 20 items.
- Maximum page size is 100 items. Requests exceeding this MUST be capped at 100.
- Page numbering is 0-based (first page is `page=0`).
- Empty pages return an empty `data` array with `totalElements: 0`.

---

## 6. Filtering and Sorting

### Filtering

Use query parameters for filtering. Each filterable field gets its own query parameter.

```
GET /v1/orders?state=APPROVED&consumerId=123
GET /v1/restaurants?cuisine=ITALIAN&city=Oakland
```

### Date Range Filtering

Use `From` and `To` suffixes for date range filters:

```
GET /v1/orders?createdFrom=2026-01-01T00:00:00Z&createdTo=2026-01-31T23:59:59Z
```

### Sorting

Use the `sort` query parameter with field name and direction separated by a comma:

```
GET /v1/orders?sort=createdAt,desc
GET /v1/orders?sort=totalAmount,asc
```

Multiple sort criteria can be specified by repeating the `sort` parameter:

```
GET /v1/orders?sort=state,asc&sort=createdAt,desc
```

---

## 7. Date/Time Format

All date/time values MUST use **ISO 8601** format in **UTC** (with `Z` suffix).

| Type | Format | Example |
|------|--------|---------|
| Date-time | `yyyy-MM-dd'T'HH:mm:ss'Z'` | `2026-01-15T10:30:00Z` |
| Date only | `yyyy-MM-dd` | `2026-01-15` |
| Time only | `HH:mm:ss` | `10:30:00` |

### Guidelines

- Always store and transmit in UTC.
- Use `java.time.Instant` for timestamps in Java code.
- Use `java.time.LocalDate` for date-only values.
- Configure Jackson with `jackson-datatype-jsr310` for proper serialization (already included in `ftgo-openapi` library).
- Never use epoch timestamps in API responses.

---

## 8. API Versioning

See [API Versioning Strategy](api-versioning-strategy.md) for the full versioning approach.

### Summary

- Use **URL path versioning**: `/v1/orders`, `/v2/orders`
- Major version in the URL path (`v1`, `v2`)
- Minor/patch versions are backward-compatible and do not require a new URL version
- All services start at `v1`

---

## 9. Error Handling

### Controller Advice

Each service SHOULD implement a `@ControllerAdvice` that maps exceptions to the standard `ApiResponse` error format:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        ApiResponse<Void> response = ApiResponse.error("VALIDATION_FAILED", "Request validation failed");
        // Map field errors from BindingResult to ApiError.FieldError
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

### Guidelines

- Never expose stack traces or internal details in production error responses.
- Log the full exception server-side with a correlation ID.
- Return meaningful, actionable error messages to clients.

---

## 10. OpenAPI Documentation

### Library

All FTGO services use the `shared:ftgo-openapi` library, which provides:

- **SpringDoc OpenAPI 3.0** auto-configuration (replaces legacy Springfox/Swagger 2.x)
- **Swagger UI** at `/swagger-ui/index.html`
- **OpenAPI JSON spec** at `/v3/api-docs`
- **OpenAPI YAML spec** at `/v3/api-docs.yaml`
- Standard response models (`ApiResponse`, `ApiError`, `PagedResponse`)

### Annotation Guidelines

Use OpenAPI 3.0 annotations from `io.swagger.v3.oas.annotations`:

| Annotation | Usage |
|------------|-------|
| `@Tag(name = "Orders")` | Group endpoints by resource on controller class |
| `@Operation(summary = "...")` | Describe endpoint purpose on each method |
| `@Parameter(description = "...")` | Document path/query parameters |
| `@Schema(description = "...")` | Document request/response model fields |
| `@ApiResponses` / `@io.swagger.v3.oas.annotations.responses.ApiResponse` | Document possible response codes |

### Example

```java
@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its unique identifier")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    public ApiResponse<GetOrderResponse> getOrder(
            @Parameter(description = "Unique order identifier") @PathVariable long orderId) {
        // ...
    }
}
```

### Service Configuration

Each service configures its API metadata in `application.yml`:

```yaml
ftgo:
  openapi:
    title: FTGO Order Service API
    description: REST API for managing orders in the FTGO platform
    version: v1
    contact-name: FTGO Engineering
    base-package: net.chrisrichardson.ftgo.orderservice

springdoc:
  swagger-ui:
    tags-sorter: alpha
    operations-sorter: alpha
    display-request-duration: true
  show-actuator: false
```

---

## 11. Security

### Headers

All API responses SHOULD include:

| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Type` | `application/json` | Response content type |
| `X-Content-Type-Options` | `nosniff` | Prevent MIME-type sniffing |
| `X-Request-Id` | UUID | Correlation ID for tracing |

### Request Validation

- Validate all input using Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- Sanitize string inputs to prevent injection attacks
- Enforce maximum request body size

---

## 12. Migration from Springfox to SpringDoc

### Annotation Mapping

| Springfox (Old) | SpringDoc (New) |
|-----------------|-----------------|
| `@EnableSwagger2` | Not needed (auto-configured) |
| `@Api(tags = "...")` | `@Tag(name = "...")` |
| `@ApiOperation(value = "...")` | `@Operation(summary = "...")` |
| `@ApiParam(value = "...")` | `@Parameter(description = "...")` |
| `@ApiModel(value = "...")` | `@Schema(description = "...")` |
| `@ApiModelProperty(value = "...")` | `@Schema(description = "...")` |
| `@ApiResponse(code = 200, ...)` | `@ApiResponse(responseCode = "200", ...)` |
| `@ApiIgnore` | `@Hidden` |

### URL Mapping

| Old URL | New URL |
|---------|---------|
| `/swagger-ui.html` | `/swagger-ui/index.html` (redirect from `/swagger-ui.html`) |
| `/v2/api-docs` | `/v3/api-docs` |
| `/swagger-resources` | Not needed |

### Dependency Migration

| Old (Springfox) | New (SpringDoc) |
|-----------------|-----------------|
| `io.springfox:springfox-swagger2:2.8.0` | `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0` |
| `io.springfox:springfox-swagger-ui:2.8.0` | (included in above) |
| `common-swagger` module | `shared:ftgo-openapi` module |

---

## References

- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [RFC 7231 - HTTP Semantics](https://tools.ietf.org/html/rfc7231)
- [RFC 7396 - JSON Merge Patch](https://tools.ietf.org/html/rfc7396)
- [ISO 8601 Date/Time Format](https://www.iso.org/iso-8601-date-and-time-format.html)
- [API Versioning Strategy](api-versioning-strategy.md)
