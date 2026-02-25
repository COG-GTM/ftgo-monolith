# FTGO API Documentation Portal

> Version: 1.0  
> Status: Approved  
> Last Updated: 2024-03-01

## Overview

The FTGO API Documentation Portal provides a centralized view of all microservice APIs, their documentation, and related resources.

## Service API Directory

| Service | Base URL | Swagger UI | OpenAPI Spec | Description |
|---------|----------|-----------|--------------|-------------|
| Order Service | `/api/v1/orders` | `/swagger-ui.html` | `/v3/api-docs` | Order lifecycle management |
| Consumer Service | `/api/v1/consumers` | `/swagger-ui.html` | `/v3/api-docs` | Consumer profile management |
| Restaurant Service | `/api/v1/restaurants` | `/swagger-ui.html` | `/v3/api-docs` | Restaurant and menu management |
| Courier Service | `/api/v1/couriers` | `/swagger-ui.html` | `/v3/api-docs` | Courier availability and delivery tracking |

## Accessing Documentation

### Local Development

Each microservice exposes its own Swagger UI when running locally:

```
Order Service:       http://localhost:8082/swagger-ui.html
Consumer Service:    http://localhost:8083/swagger-ui.html
Restaurant Service:  http://localhost:8084/swagger-ui.html
Courier Service:     http://localhost:8085/swagger-ui.html
```

### Kubernetes / Staging / Production

In deployed environments, API documentation is accessible through the API Gateway:

```
https://api.ftgo.example.com/order-service/swagger-ui.html
https://api.ftgo.example.com/consumer-service/swagger-ui.html
https://api.ftgo.example.com/restaurant-service/swagger-ui.html
https://api.ftgo.example.com/courier-service/swagger-ui.html
```

### OpenAPI Spec Endpoints

Each service also exposes its raw OpenAPI specification:

| Format | Endpoint |
|--------|----------|
| JSON | `/v3/api-docs` |
| YAML | `/v3/api-docs.yaml` |
| Grouped (v1) | `/v3/api-docs/v1` |
| Grouped (v2) | `/v3/api-docs/v2` |

---

## Generating Static OpenAPI Specs

To generate OpenAPI specifications as build artifacts:

```bash
# Start the service
./gradlew :services:ftgo-order-service:bootRun &

# Download the spec
curl http://localhost:8082/v3/api-docs -o docs/specs/order-service-openapi.json
curl http://localhost:8082/v3/api-docs.yaml -o docs/specs/order-service-openapi.yaml
```

### CI/CD Artifact Generation

In CI, specs can be generated and published as artifacts:

```yaml
# GitHub Actions example
- name: Generate OpenAPI specs
  run: |
    ./gradlew :services:ftgo-order-service:bootRun &
    sleep 10
    curl -f http://localhost:8082/v3/api-docs -o order-service-openapi.json
    kill %1

- name: Upload OpenAPI artifact
  uses: actions/upload-artifact@v4
  with:
    name: openapi-specs
    path: "*-openapi.json"
```

---

## API Standards Quick Reference

### Response Format

All APIs follow the standard envelope format defined in the [REST API Standards](api-standards.md):

```json
{
  "status": "success | error",
  "data": { },
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/..."
}
```

### Versioning

All APIs use URL path versioning: `/api/v1/resource`

See [API Versioning Strategy](api-versioning.md) for details.

### Authentication

All APIs require Bearer token authentication (except health check endpoints):

```
Authorization: Bearer <jwt-token>
```

### Pagination

All list endpoints support standard pagination:

```
GET /api/v1/orders?page=0&size=20&sort=createdAt&direction=DESC
```

See [REST API Standards - Pagination](api-standards.md#5-pagination) for details.

---

## Documentation Index

| Document | Description |
|----------|-------------|
| [REST API Standards](api-standards.md) | URL conventions, HTTP methods, status codes, pagination, filtering |
| [API Versioning Strategy](api-versioning.md) | Version scheme, deprecation policy, implementation guide |
| [API Contract Testing](api-contract-testing.md) | Consumer-driven contract testing with Spring Cloud Contract |
| [ADR-0002: SpringDoc Migration](adr/0002-springdoc-openapi-migration.md) | Architecture decision: Springfox to SpringDoc migration |
| [OpenAPI Library README](../shared/ftgo-openapi-lib/README.md) | Shared library usage guide |

---

## Shared Library: `ftgo-openapi-lib`

All microservices use the shared `ftgo-openapi-lib` library for:

- **Auto-configured OpenAPI 3.0** metadata
- **Standard models**: `ApiResponse<T>`, `ApiErrorResponse`, `PagedResponse<T>`
- **Annotations**: `@ApiStandardResponses`, `@ApiPageable`

### Adding to a New Service

```groovy
// build.gradle
dependencies {
    implementation project(":shared:ftgo-openapi-lib")
}
```

See the [OpenAPI Library README](../shared/ftgo-openapi-lib/README.md) for full documentation.

---

## Configuring Swagger UI Per Service

Each service customizes its OpenAPI documentation via `application.properties`:

```properties
# Order Service example
ftgo.openapi.title=FTGO Order Service API
ftgo.openapi.description=Manages order lifecycle including creation, approval, preparation, and delivery
ftgo.openapi.version=v1
ftgo.openapi.server.url=http://localhost:8082
ftgo.openapi.server.description=Order Service - Local Development

# SpringDoc configuration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.filter=true
springdoc.default-produces-media-type=application/json
springdoc.default-consumes-media-type=application/json
```
