# FTGO API Versioning Strategy

> Version: 1.0  
> Status: Approved  
> Last Updated: 2026-03-03

---

## Overview

This document defines the API versioning strategy for all FTGO microservices. The goal is to provide a clear, predictable approach to evolving APIs while maintaining backward compatibility and minimizing disruption to API consumers.

---

## Versioning Approach: URL Path Versioning

FTGO uses **URL path versioning** as the primary versioning mechanism.

```
https://api.ftgo.com/v1/orders
https://api.ftgo.com/v2/orders
```

### Why URL Path Versioning?

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **URL path** (`/v1/orders`) | Simple, visible, cacheable, easy to route | URL changes between versions | **Selected** |
| Header (`Accept-Version: v1`) | Clean URLs | Hidden, harder to test, cache issues | Rejected |
| Query parameter (`?version=1`) | Simple | Messy, cache key complexity | Rejected |
| Content negotiation (`Accept: application/vnd.ftgo.v1+json`) | RESTful purist approach | Complex, poor tooling support | Rejected |

URL path versioning was chosen because:
1. **Visibility**: The version is immediately obvious in URLs, logs, and documentation.
2. **Routing**: API gateways and load balancers can route based on URL path.
3. **Caching**: Each version has distinct URLs, making CDN/cache behavior predictable.
4. **Tooling**: Swagger UI and OpenAPI specs handle path-based versions naturally.
5. **Simplicity**: Developers and consumers can easily understand and use it.

---

## Version Format

### URL Version

- Format: `v{MAJOR}` (e.g., `v1`, `v2`)
- Only the **major version** appears in the URL
- Minor and patch changes are backward-compatible and do NOT create a new URL version

### Semantic Versioning (Internal)

Internally, each API uses [Semantic Versioning](https://semver.org/) for tracking changes:

```
MAJOR.MINOR.PATCH
```

| Component | When to Increment | URL Impact |
|-----------|-------------------|------------|
| **MAJOR** | Breaking changes | New URL version (`v1` -> `v2`) |
| **MINOR** | New features (backward-compatible) | No URL change |
| **PATCH** | Bug fixes (backward-compatible) | No URL change |

---

## What Constitutes a Breaking Change?

A change is **breaking** (requires a new major version) if it:

- Removes an existing endpoint
- Removes or renames a field in a response body
- Changes the type of an existing field
- Changes the meaning/behavior of an existing field
- Adds a new required field to a request body
- Changes an HTTP status code for the same scenario
- Changes authentication/authorization requirements
- Changes the URL structure of an existing endpoint

A change is **non-breaking** (backward-compatible) if it:

- Adds a new optional field to a request body
- Adds a new field to a response body
- Adds a new endpoint
- Adds a new query parameter (optional)
- Adds a new HTTP header (optional)
- Fixes a bug without changing the API contract
- Improves performance
- Updates documentation

---

## Version Lifecycle

Each API version progresses through three lifecycle stages:

```
CURRENT  -->  DEPRECATED  -->  RETIRED
```

| Stage | Description | Duration |
|-------|-------------|----------|
| **Current** | Actively developed and supported | Indefinite |
| **Deprecated** | Still functional but no new features; consumers should migrate | Minimum 6 months |
| **Retired** | Shut down; requests return `410 Gone` | - |

### Deprecation Policy

1. When a new major version is released, the previous version enters **Deprecated** status.
2. Deprecated versions are maintained for a minimum of **6 months**.
3. Deprecated responses include the `Sunset` and `Deprecation` HTTP headers:

```http
Deprecation: true
Sunset: Sat, 01 Jan 2027 00:00:00 GMT
Link: </v2/orders>; rel="successor-version"
```

4. After the sunset date, the old version is **Retired** and returns:

```json
{
  "status": "error",
  "error": {
    "code": "API_VERSION_RETIRED",
    "message": "API v1 has been retired. Please migrate to v2.",
    "details": "See https://docs.ftgo.com/migration/v1-to-v2 for migration guide"
  }
}
```

---

## Implementation

### Controller Structure

Each controller maps to a versioned base path:

```java
@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Orders", description = "Order management (v1)")
public class OrderControllerV1 {

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<GetOrderResponse> getOrder(@PathVariable long orderId) {
        // v1 implementation
    }
}
```

When a new version is needed:

```java
@RestController
@RequestMapping("/v2/orders")
@Tag(name = "Orders v2", description = "Order management (v2)")
public class OrderControllerV2 {

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<GetOrderResponseV2> getOrder(@PathVariable long orderId) {
        // v2 implementation - may delegate to shared service layer
    }
}
```

### Service Layer Sharing

The business logic (service layer) should be version-agnostic. Version-specific controllers map requests to the shared service:

```
OrderControllerV1  -->  OrderService  <--  OrderControllerV2
       |                     |                    |
   V1 DTOs           Domain Model             V2 DTOs
```

### OpenAPI Grouping

Use SpringDoc grouped APIs to generate separate specs per version:

```java
@Bean
public GroupedOpenApi v1Api() {
    return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/v1/**")
            .build();
}

@Bean
public GroupedOpenApi v2Api() {
    return GroupedOpenApi.builder()
            .group("v2")
            .pathsToMatch("/v2/**")
            .build();
}
```

Swagger UI will show a dropdown to switch between API versions.

### Application Properties

```yaml
ftgo:
  openapi:
    title: FTGO Order Service API
    version: v1
    base-package: net.chrisrichardson.ftgo.orderservice
```

---

## Migration Guidelines

When creating a new API version:

1. **Create new controller** with the new version prefix (`/v2/...`).
2. **Keep old controller** functional and unchanged.
3. **Share service layer** between versions.
4. **Create version-specific DTOs** if the response shape changes.
5. **Update OpenAPI grouping** to include the new version.
6. **Add deprecation headers** to the old version responses.
7. **Update documentation** with migration guide.
8. **Notify consumers** with timeline and migration instructions.

---

## Current API Versions

| Service | Current Version | Base Path | Status |
|---------|----------------|-----------|--------|
| Order Service | v1 | `/v1/orders` | Current |
| Consumer Service | v1 | `/v1/consumers` | Current |
| Restaurant Service | v1 | `/v1/restaurants` | Current |
| Courier Service | v1 | `/v1/couriers` | Current |

---

## References

- [Semantic Versioning 2.0.0](https://semver.org/)
- [RFC 8594 - The Sunset HTTP Header Field](https://tools.ietf.org/html/rfc8594)
- [REST API Versioning Best Practices](https://restfulapi.net/versioning/)
- [FTGO REST API Standards](api-standards.md)
