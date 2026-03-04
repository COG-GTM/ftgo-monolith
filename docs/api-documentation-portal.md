# FTGO API Documentation Portal

> This page serves as the central hub for all FTGO microservice API documentation.

---

## Quick Links

| Service | Swagger UI | API Docs (JSON) | Description |
|---------|-----------|-----------------|-------------|
| Order Service | `/swagger-ui.html` | `/v3/api-docs` | Order lifecycle management |
| Consumer Service | `/swagger-ui.html` | `/v3/api-docs` | Consumer registration and management |
| Restaurant Service | `/swagger-ui.html` | `/v3/api-docs` | Restaurant and menu management |
| Courier Service | `/swagger-ui.html` | `/v3/api-docs` | Courier availability and delivery tracking |

> **Note:** Each service runs on its own port. Replace the base URL with the appropriate service host and port.

---

## Service API Overview

### Order Service (`/api/v1/orders`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/orders` | List orders (paginated) |
| `POST` | `/api/v1/orders` | Create a new order |
| `GET` | `/api/v1/orders/{orderId}` | Get order by ID |
| `PUT` | `/api/v1/orders/{orderId}` | Update an order |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Cancel an order |
| `POST` | `/api/v1/orders/{orderId}/revise` | Revise an order |

### Consumer Service (`/api/v1/consumers`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/consumers` | List consumers (paginated) |
| `POST` | `/api/v1/consumers` | Register a new consumer |
| `GET` | `/api/v1/consumers/{consumerId}` | Get consumer by ID |
| `PUT` | `/api/v1/consumers/{consumerId}` | Update consumer details |

### Restaurant Service (`/api/v1/restaurants`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/restaurants` | List restaurants (paginated) |
| `POST` | `/api/v1/restaurants` | Register a new restaurant |
| `GET` | `/api/v1/restaurants/{restaurantId}` | Get restaurant by ID |
| `PUT` | `/api/v1/restaurants/{restaurantId}` | Update restaurant details |
| `GET` | `/api/v1/restaurants/{restaurantId}/menu` | Get restaurant menu |
| `PUT` | `/api/v1/restaurants/{restaurantId}/menu` | Update restaurant menu |

### Courier Service (`/api/v1/couriers`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/couriers` | List couriers (paginated) |
| `POST` | `/api/v1/couriers` | Register a new courier |
| `GET` | `/api/v1/couriers/{courierId}` | Get courier by ID |
| `PUT` | `/api/v1/couriers/{courierId}` | Update courier details |
| `PATCH` | `/api/v1/couriers/{courierId}/availability` | Update courier availability |
| `GET` | `/api/v1/couriers/{courierId}/deliveries` | Get courier delivery history |

---

## API Standards

All APIs follow the [FTGO REST API Standards](rest-api-standards.md). Key points:

- **Versioning**: URL path versioning (`/api/v1/`)
- **Response Format**: Standard envelope with `status`, `data`, `message`, `timestamp`
- **Pagination**: `page`, `size`, `sort` query parameters with `totalElements`/`totalPages` metadata
- **Dates**: ISO 8601 format in UTC
- **Errors**: Structured error responses with error codes and field-level details

---

## OpenAPI Specification

Each service generates an OpenAPI 3.0 specification automatically via SpringDoc. The specification can be accessed at:

- **JSON**: `GET /v3/api-docs`
- **YAML**: `GET /v3/api-docs.yaml`
- **Swagger UI**: `GET /swagger-ui.html`

### Downloading Specifications

To download the OpenAPI spec for a running service:

```bash
# JSON format
curl http://localhost:8080/v3/api-docs > order-service-api.json

# YAML format
curl http://localhost:8080/v3/api-docs.yaml > order-service-api.yaml
```

---

## Shared OpenAPI Library

The `shared/ftgo-openapi-lib` module provides:

1. **Auto-configuration** — Default OpenAPI metadata (title, version, contact)
2. **Swagger UI** — Redirect from `/swagger-ui.html` to SpringDoc UI
3. **Response models** — `ApiResponse<T>`, `PagedResponse<T>`, `ErrorResponse`
4. **Annotation examples** — Reference implementation in `OrderApiExample`

### Adding to a Service

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-ftgo-openapi-lib')
}
```

### Customizing OpenAPI Metadata

Override defaults in your service's `application.properties`:

```properties
ftgo.openapi.title=Order Service API
ftgo.openapi.version=1.0.0
ftgo.openapi.description=FTGO Order Service - manages order lifecycle
ftgo.openapi.base-package=net.chrisrichardson.ftgo.orderservice
```

Or define your own `OpenAPI` bean to fully override the auto-configuration:

```java
@Configuration
public class OrderServiceOpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0.0")
                        .description("Manages the order lifecycle"));
    }
}
```

---

## Migration from Springfox

The legacy `common-swagger` module uses Springfox 2.8.0 (Swagger 2.0). New and migrated services should use `shared/ftgo-openapi-lib` (SpringDoc OpenAPI 3.0) instead.

See [REST API Standards - OpenAPI Documentation](rest-api-standards.md#9-openapi-documentation) for the complete migration guide and annotation mapping.

> **Important:** The `common-swagger` module is preserved for backward compatibility with legacy monolith modules. Do not modify it.
