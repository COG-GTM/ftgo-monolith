# FTGO API Versioning Strategy

## Approach: URL Path Versioning

FTGO microservices use **URL path versioning** as the primary API versioning strategy.

### Format

```
/{service-base-path}/v{major}/{resource}
```

### Examples

```
GET /api/v1/orders
GET /api/v1/consumers/123
GET /api/v2/orders
```

## Versioning Rules

### When to Increment the Version

A **new major version** (`v1` -> `v2`) is required when:

- Removing or renaming an existing field in a response
- Removing or renaming an endpoint
- Changing the type of an existing field
- Changing required/optional status of a request field
- Changing the fundamental behavior or semantics of an endpoint

### Backward-Compatible Changes (No Version Bump)

The following changes do **not** require a new version:

- Adding a new optional field to a request
- Adding a new field to a response
- Adding a new endpoint
- Adding a new optional query parameter
- Fixing bugs without changing contracts

## Version Lifecycle

### Active Versions

Each service should support **at most two major versions** concurrently:

| State | Description | Duration |
|-------|-------------|----------|
| **Current** | Latest stable version, receives new features | Indefinite |
| **Deprecated** | Previous version, receives only critical fixes | 6 months after new version GA |
| **Retired** | No longer available | After deprecation period |

### Deprecation Process

1. **Announce** deprecation in release notes and API documentation
2. **Add** `Deprecation` and `Sunset` headers to deprecated version responses:
   ```
   Deprecation: true
   Sunset: Sat, 01 Jul 2025 00:00:00 GMT
   ```
3. **Monitor** usage of deprecated version
4. **Retire** version after sunset date

## Implementation

### Controller Structure

Each API version is handled by a separate controller class:

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders V1", description = "Order management endpoints")
public class OrderControllerV1 {
    // v1 endpoints
}
```

### SpringDoc Configuration

Version-specific API groups can be configured per service:

```yaml
springdoc:
  group-configs:
    - group: v1
      paths-to-match: /api/v1/**
      display-name: API v1
    - group: v2
      paths-to-match: /api/v2/**
      display-name: API v2
```

## Service-Specific Base Paths

| Service | Base Path | Current Version |
|---------|-----------|-----------------|
| Consumer Service | `/api` | `v1` |
| Courier Service | `/api` | `v1` |
| Order Service | `/api` | `v1` |
| Restaurant Service | `/api` | `v1` |

## API Documentation Endpoints

Each service exposes the following documentation endpoints:

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Swagger UI interactive documentation |
| `/v3/api-docs` | OpenAPI 3 JSON specification |
| `/v3/api-docs.yaml` | OpenAPI 3 YAML specification |
