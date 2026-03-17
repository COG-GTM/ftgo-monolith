# FTGO REST API Standards

> **Version:** 1.0.0  
> **Last Updated:** 2026-03-17  
> **Status:** Published  
> **Applies To:** All FTGO Microservices

## Table of Contents

1. [URL Naming Conventions](#1-url-naming-conventions)
2. [HTTP Method Usage](#2-http-method-usage)
3. [Status Code Standards](#3-status-code-standards)
4. [Request/Response Format](#4-requestresponse-format)
5. [Pagination](#5-pagination)
6. [Filtering and Sorting](#6-filtering-and-sorting)
7. [Date/Time Format](#7-datetime-format)
8. [API Versioning](#8-api-versioning)
9. [Error Handling](#9-error-handling)
10. [OpenAPI Documentation](#10-openapi-documentation)
11. [Security Headers](#11-security-headers)

---

## 1. URL Naming Conventions

### Rules

- Use **plural nouns** for resource collections: `/orders`, `/consumers`, `/restaurants`
- Use **lowercase** with **hyphens** for multi-word resources: `/order-items`, `/delivery-addresses`
- Use **path parameters** for resource identification: `/orders/{orderId}`
- Use **query parameters** for filtering and pagination: `/orders?consumerId=123&page=0&size=20`
- Avoid verbs in URLs; use HTTP methods to convey the action
- Nest sub-resources to express relationships: `/orders/{orderId}/line-items`

### URL Structure

```
/api/v{version}/{resource}
/api/v{version}/{resource}/{id}
/api/v{version}/{resource}/{id}/{sub-resource}
```

### Examples

| Pattern | Example | Description |
|---------|---------|-------------|
| Collection | `GET /api/v1/orders` | List all orders |
| Single resource | `GET /api/v1/orders/42` | Get order 42 |
| Sub-resource | `GET /api/v1/orders/42/line-items` | Get line items for order 42 |
| Action | `POST /api/v1/orders/42/cancel` | Cancel order 42 |

### Anti-Patterns (Avoid)

```
GET /api/v1/getOrders          # Don't use verbs
GET /api/v1/order              # Don't use singular
GET /api/v1/Order_Items        # Don't use uppercase or underscores
```

---

## 2. HTTP Method Usage

| Method | Purpose | Idempotent | Safe | Request Body |
|--------|---------|------------|------|--------------|
| `GET` | Retrieve resource(s) | Yes | Yes | No |
| `POST` | Create a new resource | No | No | Yes |
| `PUT` | Full replacement of a resource | Yes | No | Yes |
| `PATCH` | Partial update of a resource | No | No | Yes |
| `DELETE` | Remove a resource | Yes | No | No |

### Guidelines

- **GET**: Never modifies data. Use query parameters for filtering.
- **POST**: Used for resource creation and non-idempotent operations (e.g., cancel, accept).
- **PUT**: Replaces the entire resource. Requires all fields.
- **PATCH**: Updates specific fields only. Uses JSON Merge Patch (`application/merge-patch+json`).
- **DELETE**: Returns `204 No Content` on success.

---

## 3. Status Code Standards

### Success Codes

| Code | Name | When to Use |
|------|------|-------------|
| `200` | OK | Successful GET, PUT, PATCH, or action POST |
| `201` | Created | Successful resource creation via POST |
| `204` | No Content | Successful DELETE or action with no response body |

### Client Error Codes

| Code | Name | When to Use |
|------|------|-------------|
| `400` | Bad Request | Malformed request, validation failure |
| `401` | Unauthorized | Missing or invalid authentication |
| `403` | Forbidden | Authenticated but insufficient permissions |
| `404` | Not Found | Resource does not exist |
| `409` | Conflict | State conflict (e.g., duplicate creation, invalid state transition) |
| `422` | Unprocessable Entity | Valid syntax but semantically incorrect |

### Server Error Codes

| Code | Name | When to Use |
|------|------|-------------|
| `500` | Internal Server Error | Unexpected server-side failure |
| `502` | Bad Gateway | Upstream service failure |
| `503` | Service Unavailable | Service temporarily unavailable |

---

## 4. Request/Response Format

### Content Type

All APIs use `application/json` for both requests and responses.

### Standard Response Envelope

Single resource responses return the resource directly:

```json
{
  "orderId": 42,
  "state": "APPROVED",
  "orderTotal": { "amount": "25.99" },
  "restaurantName": "Ajanta"
}
```

### Standard Error Response Envelope

All error responses follow the `ApiErrorResponse` format:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Order not found",
  "path": "/api/v1/orders/999",
  "timestamp": "2026-03-17T12:00:00.000Z",
  "fieldErrors": null
}
```

Validation errors include field details:

```json
{
  "status": 400,
  "error": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/v1/orders",
  "timestamp": "2026-03-17T12:00:00.000Z",
  "fieldErrors": [
    {
      "field": "consumerId",
      "rejectedValue": null,
      "message": "must not be null"
    }
  ]
}
```

---

## 5. Pagination

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Zero-based page number |
| `size` | int | `20` | Number of items per page (max: 100) |
| `sort` | string | varies | Sort field and direction (e.g., `createdAt,desc`) |

### Response Format

All paginated endpoints return a `PagedResponse` envelope:

```json
{
  "content": [
    { "orderId": 1, "state": "APPROVED" },
    { "orderId": 2, "state": "PREPARING" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Usage Example

```
GET /api/v1/orders?consumerId=1&page=0&size=10&sort=createdAt,desc
```

---

## 6. Filtering and Sorting

### Filtering

Use query parameters for filtering:

```
GET /api/v1/orders?state=APPROVED&consumerId=42
GET /api/v1/restaurants?name=Ajanta
```

### Sorting

Use the `sort` parameter with the format `field,direction`:

```
GET /api/v1/orders?sort=createdAt,desc
GET /api/v1/orders?sort=orderTotal,asc
```

Multiple sort fields are supported:

```
GET /api/v1/orders?sort=state,asc&sort=createdAt,desc
```

---

## 7. Date/Time Format

All date/time values use **ISO 8601** format in **UTC**:

| Format | Example | Use Case |
|--------|---------|----------|
| DateTime | `2026-03-17T12:00:00.000Z` | Timestamps, events |
| Date | `2026-03-17` | Date-only fields |
| Time | `12:00:00` | Time-only fields |

### Jackson Configuration

```properties
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
spring.jackson.time-zone=UTC
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 8. API Versioning

### Strategy: URL Path Versioning

All endpoints are prefixed with `/api/v{major}`:

```
/api/v1/orders
/api/v1/consumers
/api/v1/restaurants
/api/v1/couriers
```

### Why URL Path Versioning?

| Approach | Pros | Cons |
|----------|------|------|
| **URL Path** (chosen) | Visible, easy to debug, cacheable, simple routing | URL bloat |
| Header | Clean URLs | Hidden, harder to test/debug |
| Query Parameter | Easy to add | Not RESTful, caching issues |

### Version Deprecation Policy

1. **Announcement**: Deprecated versions are announced at least **6 months** before removal
2. **Concurrent Versions**: Maximum of **2** concurrent API versions supported
3. **Sunset Header**: Deprecated versions return a `Sunset` header with the planned removal date
4. **Deprecation Header**: Deprecated versions include a `Deprecation` header with the deprecation date
5. **Documentation**: Deprecated endpoints are marked in OpenAPI specs with `@Deprecated`

### Version Lifecycle

```
v1 (current, stable) → v2 (new) → v1 (deprecated, +6 months) → v1 (removed)
```

---

## 9. Error Handling

### Error Codes

Each error type has a unique error code string:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_FAILED` | 400 | Request body validation failed |
| `TYPE_MISMATCH` | 400 | Path/query parameter type mismatch |
| `BAD_REQUEST` | 400 | General bad request |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | State conflict |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### Global Exception Handling

All services use the shared `GlobalExceptionHandler` from `ftgo-openapi-lib` which maps exceptions to the standard `ApiErrorResponse` format. Services can extend this with domain-specific exception handlers.

---

## 10. OpenAPI Documentation

### Configuration

Each service includes the `ftgo-openapi-lib` dependency which provides:

- **SpringDoc OpenAPI 3.0** auto-configuration
- **Swagger UI** at `/swagger-ui.html`
- **OpenAPI JSON spec** at `/v3/api-docs`

### Customization

Services configure their OpenAPI metadata in `application.properties`:

```properties
ftgo.openapi.title=Order Service API
ftgo.openapi.description=FTGO Order Management Service
ftgo.openapi.version=1.0.0
```

### Annotation Requirements

All controllers must include:

1. `@Tag` - Class-level tag for grouping endpoints
2. `@Operation` - Method-level summary and description
3. `@ApiResponse` - Documented response codes
4. `@Parameter` - Documented path/query parameters
5. `@Schema` - Documented request/response models

### Generated Spec Publishing

OpenAPI specs are generated at build time and published as artifacts for:

- API gateway configuration
- Client SDK generation
- Contract testing
- External documentation portals

---

## 11. Security Headers

All API responses should include standard security headers:

| Header | Value | Purpose |
|--------|-------|---------|
| `X-Content-Type-Options` | `nosniff` | Prevent MIME-type sniffing |
| `X-Frame-Options` | `DENY` | Prevent clickjacking |
| `Cache-Control` | `no-store` | Prevent caching of sensitive data |
| `Strict-Transport-Security` | `max-age=31536000` | Enforce HTTPS |

---

## Appendix: Quick Reference

### New Endpoint Checklist

- [ ] URL follows naming conventions (plural, lowercase, hyphens)
- [ ] Appropriate HTTP method used
- [ ] Correct status codes returned
- [ ] Error responses follow standard format
- [ ] OpenAPI annotations added (`@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`)
- [ ] Pagination used for collection endpoints returning variable-size results
- [ ] Date/time fields use ISO 8601
- [ ] API version prefix applied (`/api/v1`)
- [ ] Unit and contract tests written
