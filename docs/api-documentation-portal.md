# FTGO API Documentation Portal

> **Version:** 1.0.0  
> **Last Updated:** 2026-03-17

## Overview

The FTGO API Documentation Portal provides centralized access to all microservice API documentation, generated OpenAPI specifications, and developer guides.

## Service API Documentation

Each FTGO microservice exposes interactive Swagger UI documentation and machine-readable OpenAPI 3.0 specifications.

### Service Endpoints

| Service | Swagger UI | OpenAPI Spec | Description |
|---------|-----------|--------------|-------------|
| **Order Service** | `/swagger-ui.html` | `/v3/api-docs` | Order lifecycle management |
| **Consumer Service** | `/swagger-ui.html` | `/v3/api-docs` | Consumer registration and management |
| **Restaurant Service** | `/swagger-ui.html` | `/v3/api-docs` | Restaurant and menu management |
| **Courier Service** | `/swagger-ui.html` | `/v3/api-docs` | Courier management and delivery tracking |
| **FTGO Application** (monolith) | `/swagger-ui.html` | `/v3/api-docs` | All services combined |

### Accessing Swagger UI

When running locally, access any service's Swagger UI at:

```
http://localhost:{port}/swagger-ui.html
```

Default ports:

| Service | Port |
|---------|------|
| FTGO Application (monolith) | 8080 |
| Order Service | 8081 |
| Consumer Service | 8082 |
| Restaurant Service | 8083 |
| Courier Service | 8084 |

### Accessing OpenAPI Specs

JSON specifications are available at:

```
http://localhost:{port}/v3/api-docs
```

YAML format:

```
http://localhost:{port}/v3/api-docs.yaml
```

## API Standards

All FTGO APIs follow the standards defined in [REST API Standards](rest-api-standards.md).

Key highlights:

- **URL Path Versioning**: All endpoints prefixed with `/api/v1/`
- **OpenAPI 3.0**: All endpoints documented with SpringDoc annotations
- **Standard Error Format**: Consistent `ApiErrorResponse` envelope
- **Pagination**: Standard `PagedResponse` wrapper for collections
- **ISO 8601**: All date/time values in UTC

## Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| API Documentation | SpringDoc OpenAPI | 2.3.0 |
| Interactive UI | Swagger UI | (bundled with SpringDoc) |
| Specification | OpenAPI 3.0 | 3.0.1 |
| Framework | Spring Boot | 3.2.2 |

### Shared Library: ftgo-openapi-lib

The `ftgo-openapi-lib` shared library (`shared-libraries/ftgo-openapi-lib/`) provides:

1. **FtgoOpenApiConfiguration** - Shared OpenAPI metadata (title, version, contact, license)
2. **ApiVersioningConfiguration** - URL path versioning (`/api/v1/` prefix)
3. **GlobalExceptionHandler** - Standardized error responses
4. **ApiErrorResponse** - Error response model
5. **PagedResponse** - Pagination response wrapper
6. **PaginationConstants** - Standard pagination defaults
7. **ApiContractTestBase** - Base class for contract testing

### Configuration

Services customize their API documentation via `application.properties`:

```properties
# Service-specific OpenAPI metadata
ftgo.openapi.title=Order Service API
ftgo.openapi.description=FTGO Order Management Microservice
ftgo.openapi.version=1.0.0
ftgo.openapi.contact.name=Order Team
ftgo.openapi.contact.email=order-team@ftgo.io
```

## Generated Artifacts

OpenAPI specifications are generated during the build process and published as artifacts:

```
build/
  generated/
    openapi/
      order-service-api.json
      consumer-service-api.json
      restaurant-service-api.json
      courier-service-api.json
```

These artifacts can be used for:

- **API Gateway Configuration**: Route definitions from spec
- **Client SDK Generation**: Auto-generate client libraries using OpenAPI Generator
- **Contract Testing**: Validate API compatibility between versions
- **External Documentation**: Publish to documentation platforms

## Client SDK Generation

Generate client SDKs from the OpenAPI spec:

```bash
# Generate a Java client for the Order Service
openapi-generator generate \
  -i http://localhost:8081/v3/api-docs \
  -g java \
  -o generated/order-service-client

# Generate a TypeScript client
openapi-generator generate \
  -i http://localhost:8081/v3/api-docs \
  -g typescript-axios \
  -o generated/order-service-ts-client
```

## Contract Testing

API contract tests verify that service APIs maintain backward compatibility. See `ApiContractTestBase` in `ftgo-openapi-lib` for the base test class.

### Writing Contract Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class OrderApiContractTest extends ApiContractTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOrderWhenExists() throws Exception {
        verifyGetEndpoint(mockMvc, "/api/v1/orders/1", 200);
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        verifyNotFoundResponse(mockMvc, "/api/v1/orders/999");
    }
}
```

## Migration from Springfox

This documentation portal replaces the previous Springfox-based Swagger 2.0 setup. Key changes:

| Aspect | Before (Springfox) | After (SpringDoc) |
|--------|--------------------|--------------------|
| Library | `springfox-swagger2` 2.8.0 | `springdoc-openapi-starter-webmvc-ui` 2.3.0 |
| Spec Format | Swagger 2.0 | OpenAPI 3.0.1 |
| Annotations | `@Api`, `@ApiOperation` | `@Tag`, `@Operation` |
| UI Path | `/swagger-ui.html` | `/swagger-ui.html` (same) |
| Spec Path | `/v2/api-docs` | `/v3/api-docs` |
| Status | Deprecated (last release 2020) | Actively maintained |
| Spring Boot | 2.x only | 3.x native support |
| Module | `common-swagger` | `ftgo-openapi-lib` |
