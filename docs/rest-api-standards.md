# FTGO REST API Standards

## API Versioning

All FTGO microservice APIs use **URL path versioning**:

```
/api/v1/orders
/api/v1/consumers
/api/v1/restaurants
/api/v1/couriers
```

- Major version increments (`v1` → `v2`) for breaking changes
- Minor/patch changes are backward-compatible and do not bump the version
- Deprecated versions are supported for a minimum of 6 months after successor release

For the full versioning lifecycle, breaking change definitions, and multi-version
implementation guidelines, see [API Versioning Strategy](api-versioning-strategy.md).

## URL Conventions

| Rule | Example |
|------|---------|
| Use plural nouns | `/api/v1/orders` (not `/order`) |
| Lowercase with hyphens | `/api/v1/order-items` (not `/orderItems`) |
| No trailing slashes | `/api/v1/orders` (not `/orders/`) |
| Nested resources for ownership | `/api/v1/restaurants/{id}/menu-items` |
| Query params for filtering | `/api/v1/orders?status=APPROVED&consumerId=123` |

## HTTP Methods

| Method | Usage | Idempotent | Response Code |
|--------|-------|------------|---------------|
| GET | Retrieve resource(s) | Yes | 200 |
| POST | Create resource | No | 201 |
| PUT | Full update | Yes | 200 |
| PATCH | Partial update | No | 200 |
| DELETE | Remove resource | Yes | 204 |

## Status Codes

| Code | Usage |
|------|-------|
| 200 | Successful GET/PUT/PATCH |
| 201 | Successful POST (resource created) |
| 204 | Successful DELETE (no body) |
| 400 | Validation error / malformed request |
| 401 | Unauthenticated |
| 403 | Unauthorized (insufficient permissions) |
| 404 | Resource not found |
| 409 | Conflict (e.g., duplicate, state transition error) |
| 422 | Business rule violation |
| 500 | Internal server error |

## Request/Response Format

All requests and responses use `application/json` with UTF-8 encoding.

### Success Response

```json
{
  "orderId": 123,
  "status": "APPROVED",
  "consumerId": 456,
  "totalAmount": { "amount": "25.99" }
}
```

### Error Response (Standard)

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid order request",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/orders",
  "fieldErrors": [
    { "field": "consumerId", "message": "must not be null" }
  ]
}
```

Use `ApiErrorResponse` from `ftgo-openapi-lib` for all error responses.

## Pagination

Use query parameters for pagination on list endpoints:

```
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

Response wraps items in a `PageResponse` envelope:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

Use `PageResponse<T>` from `ftgo-openapi-lib`.

## Date/Time Format

- All timestamps use **ISO 8601** format: `2024-01-15T10:30:00Z`
- Store and transmit in UTC
- Use `Instant` in Java, serialize via Jackson ISO module

## OpenAPI Documentation

Each service auto-configures Swagger UI via `ftgo-openapi-lib`:

- Swagger UI: `http://<service>:<port>/swagger-ui.html`
- OpenAPI JSON: `http://<service>:<port>/v3/api-docs`
- OpenAPI YAML: `http://<service>:<port>/v3/api-docs.yaml`

Customize per service in `application.properties`:

```properties
ftgo.openapi.title=FTGO Order Service API
ftgo.openapi.description=Order lifecycle management
ftgo.openapi.version=v1
ftgo.openapi.server-url=http://localhost:8082
```

## Service API Endpoints

| Service | Base Path | Key Resources |
|---------|-----------|---------------|
| Order | `/api/v1/orders` | Orders, order line items |
| Consumer | `/api/v1/consumers` | Consumers, validation |
| Restaurant | `/api/v1/restaurants` | Restaurants, menus, menu items |
| Courier | `/api/v1/couriers` | Couriers, deliveries, availability |
