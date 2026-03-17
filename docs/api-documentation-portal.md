# FTGO API Documentation Portal

> Central hub for all FTGO microservice API documentation.

## Service API Documentation

Each FTGO microservice exposes interactive API documentation via Swagger UI and machine-readable OpenAPI 3.0 specifications.

### Service Endpoints

| Service | Swagger UI | OpenAPI Spec (JSON) | OpenAPI Spec (YAML) |
|---------|------------|---------------------|---------------------|
| **Order Service** | [/swagger-ui.html](http://localhost:8082/swagger-ui.html) | [/v3/api-docs](http://localhost:8082/v3/api-docs) | [/v3/api-docs.yaml](http://localhost:8082/v3/api-docs.yaml) |
| **Consumer Service** | [/swagger-ui.html](http://localhost:8081/swagger-ui.html) | [/v3/api-docs](http://localhost:8081/v3/api-docs) | [/v3/api-docs.yaml](http://localhost:8081/v3/api-docs.yaml) |
| **Restaurant Service** | [/swagger-ui.html](http://localhost:8083/swagger-ui.html) | [/v3/api-docs](http://localhost:8083/v3/api-docs) | [/v3/api-docs.yaml](http://localhost:8083/v3/api-docs.yaml) |
| **Courier Service** | [/swagger-ui.html](http://localhost:8084/swagger-ui.html) | [/v3/api-docs](http://localhost:8084/v3/api-docs) | [/v3/api-docs.yaml](http://localhost:8084/v3/api-docs.yaml) |
| **FTGO Application** (monolith) | [/swagger-ui.html](http://localhost:8080/swagger-ui.html) | [/v3/api-docs](http://localhost:8080/v3/api-docs) | [/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

> **Note:** Port numbers above are defaults for local development. In Kubernetes deployments, access services via their ingress URLs.

## API Standards

All FTGO services follow the [REST API Standards](api-standards.md) document, which covers:

- URL naming conventions
- HTTP method usage
- Status code standards
- Request/response envelope format
- Pagination format
- Filtering and sorting conventions
- Date/time format (ISO 8601)
- API versioning strategy
- Error handling standards

## API Versioning

FTGO uses **URL path versioning** (`/api/v1/...`). See the [API Standards - Versioning](api-standards.md#8-api-versioning) section for details.

### Current API Versions

| Version | Status | Sunset Date |
|---------|--------|-------------|
| v1 | **Active** | N/A |

## Architecture Overview

```
                    ┌─────────────────────────────────┐
                    │        API Gateway / LB          │
                    └──────────┬──────────────────────┘
                               │
            ┌──────────────────┼──────────────────────┐
            │                  │                       │
   ┌────────▼─────┐  ┌────────▼─────┐  ┌─────────────▼──┐
   │ Order Service │  │  Consumer    │  │  Restaurant    │
   │              │  │  Service     │  │  Service       │
   │ /orders      │  │ /consumers   │  │ /restaurants   │
   │ /swagger-ui  │  │ /swagger-ui  │  │ /swagger-ui    │
   └──────────────┘  └──────────────┘  └────────────────┘
                               │
                    ┌──────────▼─────────┐
                    │  Courier Service   │
                    │ /couriers          │
                    │ /swagger-ui        │
                    └────────────────────┘
```

## Shared Libraries

### ftgo-openapi-lib

Provides shared OpenAPI 3.0 configuration for all services:

- **`FtgoOpenApiAutoConfiguration`** - Auto-configures OpenAPI metadata (title, version, description, contact)
- **`ApiVersioning`** - API version constants and URL path helpers
- **`ApiResponse<T>`** - Standard success response envelope
- **`PagedResponse<T>`** - Standardized paginated response format
- **`ApiError`** - Standard error response format

#### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.openapi.title` | `FTGO API` | API title shown in Swagger UI |
| `ftgo.openapi.description` | `Food To Go Microservices Platform API` | API description |
| `ftgo.openapi.version` | `1.0.0` | API version |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI URL path |
| `springdoc.api-docs.path` | `/v3/api-docs` | OpenAPI spec URL path |

### ftgo-api-contract-testing

Provides base classes for consumer-driven contract testing between services:

- **`ContractTestBase`** - Base class for REST-Assured MockMvc contract tests
- **`ContractVerifier`** - Utility for loading and verifying JSON contracts

## Generated Artifacts

OpenAPI specs are generated as build artifacts during the Gradle build:

```bash
# Generate OpenAPI specs for all services
./gradlew generateOpenApiSpec

# Specs are output to:
# build/openapi/openapi-spec.json
# build/openapi/openapi-spec.yaml
```

## Getting Started

### Adding OpenAPI to a New Service

1. Add the `ftgo-openapi-lib` dependency to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared-libraries:ftgo-openapi-lib")
}
```

2. Configure service-specific metadata in `application.properties`:

```properties
ftgo.openapi.title=Order Service API
ftgo.openapi.description=FTGO Order Management Service
ftgo.openapi.version=1.0.0
```

3. Annotate your controllers:

```java
@Tag(name = "Orders", description = "Order management operations")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Operation(summary = "Create order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
        // ...
    }
}
```

4. Access Swagger UI at `http://localhost:{port}/swagger-ui.html`

### Adding Contract Tests

1. Add the contract testing dependency:

```groovy
dependencies {
    testImplementation project(":shared-libraries:ftgo-api-contract-testing")
}
```

2. Create a contract test:

```java
@SpringBootTest
class OrderContractTest extends ContractTestBase {

    @MockBean
    private OrderService orderService;

    @Override
    protected void setup() {
        super.setup();
        when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
    }

    @Test
    void shouldReturnOrderById() {
        given()
            .when()
            .get("/orders/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }
}
```
