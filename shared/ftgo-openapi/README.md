# FTGO OpenAPI Library

SpringDoc OpenAPI 3.0 auto-configuration library for FTGO microservices. Replaces the legacy `common-swagger` module (Springfox 2.8.0).

## Features

- **SpringDoc OpenAPI 3.0** auto-configuration with customizable properties
- **Swagger UI** available at `/swagger-ui/index.html`
- **Standard API response models**: `ApiResponse`, `ApiError`, `PagedResponse`
- **Spring Boot auto-configuration** via `spring.factories` / `AutoConfiguration.imports`

## Usage

### 1. Add Dependency

```groovy
// Legacy modules (Spring Boot 2.x at repo root)
compile project(':shared:ftgo-openapi')

// Future microservices (Spring Boot 3.x under services/)
implementation project(':shared:ftgo-openapi')
```

### 2. Configure in application.yml

```yaml
ftgo:
  openapi:
    title: FTGO Order Service API
    description: REST API for managing orders in the FTGO platform
    version: v1
    contact-name: FTGO Engineering
    base-package: net.chrisrichardson.ftgo.orderservice
```

### 3. Use Standard Response Models

```java
import com.ftgo.openapi.model.ApiResponse;
import com.ftgo.openapi.model.PagedResponse;

@GetMapping("/{orderId}")
public ApiResponse<GetOrderResponse> getOrder(@PathVariable long orderId) {
    return ApiResponse.success(orderService.findById(orderId));
}

@GetMapping
public PagedResponse<OrderSummary> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Page<OrderSummary> result = orderService.findAll(page, size);
    return PagedResponse.of(result.getContent(), page, size, result.getTotalElements());
}
```

### 4. Add OpenAPI Annotations

```java
@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    public ApiResponse<GetOrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable long orderId) {
        // ...
    }
}
```

## Swagger UI Endpoints

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui/index.html` | Swagger UI interface |
| `/swagger-ui.html` | Redirect (backward-compatible with Springfox) |
| `/v3/api-docs` | OpenAPI 3.0 JSON spec |
| `/v3/api-docs.yaml` | OpenAPI 3.0 YAML spec |

## Migration from Springfox

See [API Standards - Migration Guide](../../docs/api-standards.md#12-migration-from-springfox-to-springdoc) for the complete annotation mapping and migration steps.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.openapi.title` | `FTGO API` | API title in Swagger UI |
| `ftgo.openapi.description` | `FTGO Microservices REST API` | API description |
| `ftgo.openapi.version` | `v1` | API version |
| `ftgo.openapi.contact-name` | `FTGO Engineering` | Contact name |
| `ftgo.openapi.contact-email` | (empty) | Contact email |
| `ftgo.openapi.contact-url` | GitHub URL | Contact URL |
| `ftgo.openapi.license-name` | `Apache 2.0` | License name |
| `ftgo.openapi.license-url` | Apache URL | License URL |
| `ftgo.openapi.base-package` | `net.chrisrichardson.ftgo` | Package to scan for controllers |
| `ftgo.openapi.external-docs-url` | (empty) | External docs URL |
| `ftgo.openapi.external-docs-description` | (empty) | External docs description |
