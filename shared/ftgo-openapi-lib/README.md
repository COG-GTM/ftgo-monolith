# FTGO OpenAPI Library (`ftgo-openapi-lib`)

Shared OpenAPI 3.0 configuration library for all FTGO microservices. Replaces the legacy `common-swagger` module (Springfox 2.x) with SpringDoc OpenAPI 3.0.

## Features

- **Auto-configured OpenAPI 3.0** metadata (title, version, description, contact)
- **Standard response models**: `ApiResponse<T>`, `ApiErrorResponse`, `PagedResponse<T>`
- **Reusable annotations**: `@ApiStandardResponses`, `@ApiPageable`
- **Swagger UI** accessible at `/swagger-ui.html` for each service
- **Customizable** via `application.properties` per service

## Quick Start

### 1. Add Dependency

```groovy
dependencies {
    implementation project(":shared:ftgo-openapi-lib")
}
```

### 2. Configure (Optional)

```properties
# application.properties
ftgo.openapi.title=Order Service API
ftgo.openapi.description=Manages order lifecycle in the FTGO platform
ftgo.openapi.version=v1
ftgo.openapi.server.url=http://localhost:8082
```

### 3. Annotate Controllers

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiStandardResponses
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.findById(id)));
    }

    @GetMapping
    @Operation(summary = "List orders with pagination")
    @ApiPageable
    public ResponseEntity<PagedResponse<OrderDTO>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // ...
    }
}
```

### 4. Access Swagger UI

Start your service and navigate to: `http://localhost:{port}/swagger-ui.html`

## Standard Response Models

### Success Response (`ApiResponse<T>`)

```json
{
  "status": "success",
  "data": { "orderId": 123, "status": "PENDING" },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/orders/123"
}
```

### Error Response (`ApiErrorResponse`)

```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "quantity", "message": "must be greater than 0" }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/orders"
}
```

### Paginated Response (`PagedResponse<T>`)

```json
{
  "status": "success",
  "data": [ ... ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/orders?page=0&size=20"
}
```

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `ftgo.openapi.title` | `FTGO Service API` | API title in OpenAPI spec |
| `ftgo.openapi.description` | `FTGO Platform Microservice API` | API description |
| `ftgo.openapi.version` | `v1` | API version |
| `ftgo.openapi.contact.name` | `FTGO Platform Team` | Contact name |
| `ftgo.openapi.contact.email` | `platform@ftgo.com` | Contact email |
| `ftgo.openapi.contact.url` | GitHub repo URL | Contact URL |
| `ftgo.openapi.server.url` | `http://localhost:8080` | Server URL |
| `ftgo.openapi.server.description` | `Local Development Server` | Server description |

## Migration from Springfox

See [ADR-0002](../../docs/adr/0002-springdoc-openapi-migration.md) and the [REST API Standards](../../docs/api-standards.md) for full migration guidance.

| Springfox (Old) | SpringDoc (New) |
|---|---|
| `@EnableSwagger2` | Auto-configured (no annotation needed) |
| `@Api(tags = ...)` | `@Tag(name = ...)` |
| `@ApiOperation(...)` | `@Operation(summary = ...)` |
| `@ApiParam(...)` | `@Parameter(...)` |
| `@ApiModel(...)` | `@Schema(...)` |
| `@ApiModelProperty(...)` | `@Schema(...)` |
| `Docket` bean | `OpenAPI` bean (auto-configured) |
| `/swagger-ui.html` (Springfox) | `/swagger-ui.html` (SpringDoc redirect) |
