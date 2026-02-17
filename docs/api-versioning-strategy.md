# FTGO API Versioning Strategy

## Overview

This document defines the API versioning strategy for all FTGO microservices. Consistent versioning ensures backward compatibility, clear communication of breaking changes, and a smooth migration path for API consumers.

## Versioning Scheme

FTGO uses **URL path versioning** with a major version prefix:

```
/api/v{major}/{resource}
```

Examples:
```
/api/v1/orders
/api/v1/consumers
/api/v2/orders
```

### Why URL Path Versioning

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| URL path (`/api/v1/`) | Explicit, easy to route, cache-friendly | URL changes per version | **Selected** |
| Header (`Accept-Version`) | Clean URLs | Hard to test in browser, easy to forget | Rejected |
| Query param (`?version=1`) | Simple to add | Pollutes query string, caching issues | Rejected |
| Content negotiation (`Accept: application/vnd.ftgo.v1+json`) | RESTful purist approach | Complex, poor tooling support | Rejected |

## Version Lifecycle

### States

| State | Description | Duration |
|-------|-------------|----------|
| **Current** | Active version receiving new features and fixes | Indefinite |
| **Deprecated** | Supported but no new features; consumers should migrate | Minimum 6 months |
| **Retired** | No longer available; requests return `410 Gone` | After deprecation period |

### Lifecycle Flow

```
Current (v1) → Deprecated (v1) → Retired (v1)
                 ↑
            Current (v2) introduced
```

## What Constitutes a Breaking Change

A new major version (`v1` → `v2`) is required when:

- Removing or renaming a field in a response body
- Removing or renaming a query parameter
- Changing the type of a field (e.g., `string` → `integer`)
- Removing an endpoint
- Changing the URL path of an existing endpoint
- Changing required fields in a request body
- Changing authentication/authorization requirements
- Changing error response structure

## What Is NOT a Breaking Change

These changes are backward-compatible and do **not** require a version bump:

- Adding a new optional field to a request body
- Adding a new field to a response body
- Adding a new endpoint
- Adding a new query parameter (optional)
- Adding a new HTTP method to an existing resource
- Changing the order of fields in a response
- Fixing bugs in existing behavior (to match documented spec)

## Implementation Guidelines

### Controller Mapping

Each controller version maps to a separate base path. Controllers should use a constant for the version prefix:

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {
    // ...
}
```

### Running Multiple Versions

When a new major version is introduced:

1. The new controller class is created alongside the existing one
2. Both versions share the same service layer where possible
3. Version-specific DTOs handle request/response shape differences
4. The old version is marked deprecated via OpenAPI annotations

```java
@Deprecated
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders (v1 - Deprecated)", description = "Use /api/v2/orders instead")
public class OrderControllerV1 { ... }

@RestController
@RequestMapping("/api/v2/orders")
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderControllerV2 { ... }
```

### Deprecation Headers

Deprecated endpoints should include the `Deprecation` and `Sunset` HTTP headers in responses:

```
Deprecation: true
Sunset: Sat, 01 Jan 2026 00:00:00 GMT
Link: </api/v2/orders>; rel="successor-version"
```

### OpenAPI Documentation

Each API version is documented separately via SpringDoc OpenAPI groups:

```yaml
springdoc:
  group-configs:
    - group: v1
      paths-to-match: /api/v1/**
    - group: v2
      paths-to-match: /api/v2/**
```

## Service Version Matrix

| Service | Current Version | Base Path | Port |
|---------|----------------|-----------|------|
| Consumer Service | v1 | `/api/v1/consumers` | 8081 |
| Order Service | v1 | `/api/v1/orders` | 8082 |
| Restaurant Service | v1 | `/api/v1/restaurants` | 8083 |
| Courier Service | v1 | `/api/v1/couriers` | 8084 |

## Inter-Service Communication

Internal service-to-service calls follow the same versioning scheme. Each service API client targets a specific version:

```
Consumer Service → Order Service: /api/v1/orders
Order Service → Restaurant Service: /api/v1/restaurants
```

When upgrading an internal API version:
1. Deploy the new version alongside the old one
2. Update consumers one at a time
3. Monitor for errors during the transition
4. Retire the old version after all consumers have migrated

## Related Documents

- [REST API Standards](rest-api-standards.md)
- [OpenAPI Library](../libs/ftgo-openapi-lib/)
