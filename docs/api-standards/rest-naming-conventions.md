# FTGO REST API Naming Conventions

## URI Conventions

### General Rules

- Use **lowercase** letters in URIs
- Use **hyphens** (`-`) to separate words in URI segments (kebab-case)
- Use **plural nouns** for resource collections
- Use **forward slashes** (`/`) to indicate hierarchy
- Do **not** use trailing slashes
- Do **not** use file extensions in URIs

### Resource URI Patterns

| Pattern | Example | Description |
|---------|---------|-------------|
| `/{version}/{resource}` | `/v1/orders` | Collection resource |
| `/{version}/{resource}/{id}` | `/v1/orders/123` | Single resource |
| `/{version}/{resource}/{id}/{sub-resource}` | `/v1/orders/123/line-items` | Sub-resource collection |
| `/{version}/{resource}/{id}/{sub-resource}/{id}` | `/v1/orders/123/line-items/456` | Single sub-resource |

### Naming Examples

```
GET    /v1/orders                    # List orders
POST   /v1/orders                    # Create an order
GET    /v1/orders/{orderId}          # Get order by ID
PUT    /v1/orders/{orderId}          # Update order
DELETE /v1/orders/{orderId}          # Delete order
GET    /v1/orders/{orderId}/line-items   # List order line items
POST   /v1/orders/{orderId}/revisions    # Trigger order revision
```

### Actions and Non-CRUD Operations

For operations that do not map cleanly to CRUD, use a **verb sub-resource**:

```
POST   /v1/orders/{orderId}/cancel
POST   /v1/orders/{orderId}/accept
POST   /v1/couriers/{courierId}/availability
```

## HTTP Methods

| Method | Usage | Idempotent | Safe |
|--------|-------|------------|------|
| `GET` | Retrieve resource(s) | Yes | Yes |
| `POST` | Create resource or trigger action | No | No |
| `PUT` | Full replacement of a resource | Yes | No |
| `PATCH` | Partial update of a resource | No | No |
| `DELETE` | Remove a resource | Yes | No |

## HTTP Status Codes

### Success Codes

| Code | Usage |
|------|-------|
| `200 OK` | Successful GET, PUT, PATCH, or DELETE |
| `201 Created` | Successful POST that creates a resource |
| `204 No Content` | Successful DELETE with no response body |

### Client Error Codes

| Code | Usage |
|------|-------|
| `400 Bad Request` | Malformed request or validation failure |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but insufficient permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | State conflict (e.g., duplicate creation) |
| `422 Unprocessable Entity` | Semantic validation error |

### Server Error Codes

| Code | Usage |
|------|-------|
| `500 Internal Server Error` | Unexpected server failure |
| `502 Bad Gateway` | Upstream service failure |
| `503 Service Unavailable` | Service temporarily unavailable |

## Standard Headers

### Request Headers

| Header | Description |
|--------|-------------|
| `Content-Type: application/json` | Required for request bodies |
| `Accept: application/json` | Preferred response format |
| `Authorization: Bearer {token}` | JWT authentication |
| `X-Request-Id` | Client-generated correlation ID |

### Response Headers

| Header | Description |
|--------|-------------|
| `Content-Type: application/json` | Response format |
| `X-Request-Id` | Echoed correlation ID |
| `X-Total-Count` | Total items for paginated responses |

## Query Parameters

### Pagination

Use `page` and `size` query parameters:

```
GET /v1/orders?page=0&size=20
```

### Sorting

Use `sort` parameter with `field,direction` format:

```
GET /v1/orders?sort=createdAt,desc
GET /v1/orders?sort=status,asc&sort=createdAt,desc
```

### Filtering

Use field names as query parameters:

```
GET /v1/orders?status=PENDING&customerId=123
```

## Standard Response Envelope

All API responses use the standard envelope format provided by `ftgo-api-standards`:

### Success Response

```json
{
  "success": true,
  "data": { ... },
  "message": "Order created successfully",
  "path": "/v1/orders",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/v1/orders",
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    { "field": "deliveryAddress", "message": "must not be null" }
  ]
}
```

### Paginated Response

```json
{
  "content": [ ... ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```
