# FTGO REST API Standards

> Canonical reference for designing, implementing, and consuming REST APIs
> across all FTGO microservices.

---

## 1. URL Structure

```
https://{host}/api/{version}/{resource}
```

| Component   | Convention                                     | Example                          |
|-------------|------------------------------------------------|----------------------------------|
| Version     | URL-path prefix (`v1`, `v2`)                   | `/api/v1/orders`                 |
| Resource    | Plural nouns, lowercase, kebab-case             | `/api/v1/order-line-items`       |
| Identifier  | Path parameter                                 | `/api/v1/orders/{orderId}`       |
| Sub-resource| Nested under parent                            | `/api/v1/orders/{orderId}/items` |
| Action      | Avoid verbs; use HTTP methods instead           | `POST /api/v1/orders` (not `/api/v1/createOrder`) |

### Resource Naming Rules

- Use **plural nouns** for collection endpoints: `/orders`, `/restaurants`, `/consumers`
- Use **kebab-case** for multi-word resources: `/order-line-items`, `/delivery-addresses`
- Nest sub-resources only one level deep: `/orders/{id}/items` (avoid `/orders/{id}/items/{itemId}/options`)
- Keep URLs concise — move filtering to query parameters

---

## 2. HTTP Methods

| Method  | Usage                        | Idempotent | Safe | Request Body | Example                       |
|---------|------------------------------|------------|------|--------------|-------------------------------|
| GET     | Retrieve resource(s)         | Yes        | Yes  | No           | `GET /api/v1/orders/42`       |
| POST    | Create a new resource        | No         | No   | Yes          | `POST /api/v1/orders`         |
| PUT     | Full replacement of resource | Yes        | No   | Yes          | `PUT /api/v1/orders/42`       |
| PATCH   | Partial update               | No*        | No   | Yes          | `PATCH /api/v1/orders/42`     |
| DELETE  | Remove a resource            | Yes        | No   | No           | `DELETE /api/v1/orders/42`    |

> *PATCH is idempotent when the same patch body is applied repeatedly to the same resource state.

---

## 3. HTTP Status Codes

### Success Codes

| Code | Meaning    | When to Use                                                |
|------|------------|------------------------------------------------------------|
| 200  | OK         | Successful GET, PUT, PATCH, or DELETE                      |
| 201  | Created    | Successful POST that created a new resource                |
| 204  | No Content | Successful DELETE or action with no response body          |

### Client Error Codes

| Code | Meaning              | When to Use                                             |
|------|----------------------|---------------------------------------------------------|
| 400  | Bad Request          | Malformed JSON, missing required fields, validation errors |
| 401  | Unauthorized         | Missing or invalid authentication token                 |
| 403  | Forbidden            | Authenticated but insufficient permissions              |
| 404  | Not Found            | Resource does not exist                                 |
| 409  | Conflict             | Resource state conflict (e.g., duplicate creation)      |
| 422  | Unprocessable Entity | Semantically invalid request (valid JSON but bad data)  |
| 429  | Too Many Requests    | Rate limit exceeded                                     |

### Server Error Codes

| Code | Meaning              | When to Use                                    |
|------|----------------------|------------------------------------------------|
| 500  | Internal Server Error| Unexpected server-side failure                 |
| 502  | Bad Gateway          | Upstream service returned invalid response     |
| 503  | Service Unavailable  | Service temporarily overloaded or in maintenance |
| 504  | Gateway Timeout      | Upstream service did not respond in time        |

---

## 4. Content Types

- Request and response bodies **MUST** use `application/json`
- All endpoints **MUST** set `Content-Type: application/json` in responses
- APIs **SHOULD** set `Accept: application/json` validation on inbound requests

---

## 5. Response Envelope

All successful responses are wrapped in a standard envelope provided by `ftgo-openapi-lib`.

### Single Resource Response

```json
{
  "status": "success",
  "data": {
    "orderId": 42,
    "state": "APPROVED",
    "consumerId": 7,
    "restaurantId": 3,
    "orderTotal": "12.50"
  }
}
```

### Collection Response (Paginated)

```json
{
  "status": "success",
  "data": [
    { "orderId": 42, "state": "APPROVED" },
    { "orderId": 43, "state": "PREPARING" }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8
  }
}
```

### Java Usage

```java
// Single resource
return ResponseEntity.ok(ApiResponseEnvelope.success(orderDto));

// Paginated collection
return ResponseEntity.ok(PagedResponse.of(orderDtos, page, size, totalCount));

// Created
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponseEnvelope.success(createdOrder));
```

---

## 6. Error Response Format

All error responses follow a standard structure.

```json
{
  "status": "error",
  "data": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "timestamp": "2026-03-16T20:00:00Z",
    "path": "/api/v1/orders",
    "details": [
      {
        "field": "consumerId",
        "message": "must not be null",
        "code": "NotNull"
      },
      {
        "field": "restaurantId",
        "message": "must be greater than 0",
        "code": "Min"
      }
    ]
  }
}
```

### Standard Error Codes

| Code                    | HTTP Status | Description                          |
|-------------------------|-------------|--------------------------------------|
| `VALIDATION_FAILED`     | 400 / 422   | Request body validation errors       |
| `UNAUTHORIZED`          | 401         | Missing or invalid auth token        |
| `FORBIDDEN`             | 403         | Insufficient permissions             |
| `RESOURCE_NOT_FOUND`    | 404         | Requested resource does not exist    |
| `CONFLICT`              | 409         | State conflict or duplicate resource |
| `RATE_LIMITED`           | 429         | Too many requests                    |
| `INTERNAL_ERROR`        | 500         | Unexpected server error              |
| `SERVICE_UNAVAILABLE`   | 503         | Downstream dependency failure        |

### Java Usage

```java
ErrorResponse error = ErrorResponse.builder()
    .code("VALIDATION_FAILED")
    .message("Request validation failed")
    .path("/api/v1/orders")
    .detail("consumerId", "must not be null", "NotNull")
    .build();

return ResponseEntity.badRequest()
    .body(ApiResponseEnvelope.error(error));
```

---

## 7. Pagination

Collection endpoints **MUST** support pagination using query parameters.

### Query Parameters

| Parameter | Type    | Default | Description                              |
|-----------|---------|---------|------------------------------------------|
| `page`    | integer | `0`     | Zero-based page index                    |
| `size`    | integer | `20`    | Number of items per page (max: 100)      |
| `sort`    | string  | —       | Sort field and direction: `name,asc`     |

### Example Request

```
GET /api/v1/orders?page=2&size=10&sort=createdAt,desc
```

### Response

The response uses the `PagedResponse` model from `ftgo-openapi-lib` (see Section 5).

### Implementation Guidelines

- Default page size: **20**
- Maximum page size: **100** (reject requests above this with 400)
- Always return `totalElements` and `totalPages` in pagination metadata
- Return an empty `data` array (not `null`) when no results are found

---

## 8. Filtering and Searching

### Query Parameter Conventions

| Pattern                   | Example                                     | Usage                       |
|---------------------------|---------------------------------------------|-----------------------------|
| Exact match               | `?state=APPROVED`                           | Filter by exact value       |
| Multiple values           | `?state=APPROVED,PREPARING`                 | OR filter (comma-separated) |
| Range (min/max)           | `?minTotal=10.00&maxTotal=50.00`            | Numeric range               |
| Date range                | `?createdAfter=2026-01-01&createdBefore=2026-03-01` | Date range           |
| Full-text search          | `?q=pizza`                                  | Search query                |

---

## 9. Field Naming Conventions

- Use **camelCase** for JSON field names: `orderId`, `createdAt`, `orderTotal`
- Use **ISO 8601** for date/time fields: `"2026-03-16T20:00:00Z"`
- Represent monetary values as **strings** with explicit currency when needed: `"12.50"`
- Use **null** for absent optional fields (or omit via `@JsonInclude(NON_NULL)`)

---

## 10. Request Headers

| Header          | Required | Description                                 |
|-----------------|----------|---------------------------------------------|
| `Authorization` | Yes*     | `Bearer {jwt-token}` (* except public endpoints) |
| `Content-Type`  | Yes      | `application/json`                          |
| `Accept`        | Recommended | `application/json`                       |
| `X-Request-Id`  | Recommended | Correlation ID for distributed tracing   |

---

## 11. HATEOAS (Optional)

For services that choose to include hypermedia links:

```json
{
  "status": "success",
  "data": {
    "orderId": 42,
    "state": "APPROVED"
  },
  "meta": {
    "_links": {
      "self": { "href": "/api/v1/orders/42" },
      "cancel": { "href": "/api/v1/orders/42/cancel", "method": "POST" },
      "consumer": { "href": "/api/v1/consumers/7" }
    }
  }
}
```

> HATEOAS is **optional** for FTGO services. Prioritise simplicity unless the
> service has complex state transitions that benefit from discoverability.

---

## 12. OpenAPI Documentation

All services **MUST** expose an OpenAPI 3.0 specification:

| Endpoint               | Description                    |
|------------------------|--------------------------------|
| `/v3/api-docs`         | JSON OpenAPI specification     |
| `/swagger-ui.html`     | Interactive Swagger UI         |

### Integration

Add `ftgo-openapi-lib` as a dependency:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-openapi-lib')
}
```

Customise via `application.properties`:

```properties
ftgo.openapi.title=Order Service API
ftgo.openapi.version=v1
ftgo.openapi.description=Manages order lifecycle and fulfillment
ftgo.openapi.base-package=net.chrisrichardson.ftgo.orderservice
```

---

## 13. Idempotency

- `PUT` and `DELETE` operations **MUST** be idempotent
- For non-idempotent `POST` operations, consider accepting an `Idempotency-Key` header
- Services **SHOULD** document idempotency guarantees in the OpenAPI spec

---

## 14. Rate Limiting

- Services behind the API Gateway inherit gateway-level rate limits
- Individual services **MAY** enforce additional rate limits
- Rate-limited responses **MUST** include:
  - `Retry-After` header (seconds until next allowed request)
  - HTTP 429 status code
  - Standard error response body with code `RATE_LIMITED`
