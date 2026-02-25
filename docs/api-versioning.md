# FTGO API Versioning Strategy

> Version: 1.0  
> Status: Approved  
> Last Updated: 2024-03-01  
> Applies To: All FTGO microservices

## Strategy: URL Path Versioning

FTGO uses **URL path versioning** as the primary versioning mechanism:

```
/api/v{major}/{resource}
```

**Examples:**

```
/api/v1/orders
/api/v1/orders/123
/api/v2/orders
```

### Why URL Path Versioning?

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **URL Path** (`/api/v1/`) | Simple, visible, cacheable, easy routing | URL changes between versions | **Selected** |
| Header (`Accept: application/vnd.ftgo.v1+json`) | Clean URLs | Hard to test/debug, not cacheable | Rejected |
| Query Param (`?version=1`) | Easy to add | Not RESTful, hard to route | Rejected |

URL path versioning was chosen because it is:

- **Explicit**: Version is immediately visible in the URL
- **Simple**: Easy to implement with Spring MVC `@RequestMapping`
- **Cacheable**: Proxies and CDNs can cache per-version
- **Routable**: API gateways can route based on URL prefix
- **Testable**: Easy to test with curl, Postman, or browser

---

## Versioning Rules

### 1. Semantic Versioning for APIs

API versions follow [Semantic Versioning](https://semver.org/) principles:

| Change | Version Impact | URL Change | Backward Compatible |
|--------|---------------|------------|-------------------|
| New optional field added to response | Patch (v1.0.1) | No | Yes |
| New optional query parameter | Minor (v1.1.0) | No | Yes |
| New endpoint added | Minor (v1.1.0) | No | Yes |
| Required field removed from response | **Major (v2.0.0)** | **Yes** (`/api/v2/`) | **No** |
| Field renamed or type changed | **Major (v2.0.0)** | **Yes** (`/api/v2/`) | **No** |
| Endpoint removed | **Major (v2.0.0)** | **Yes** (`/api/v2/`) | **No** |
| Request field made required | **Major (v2.0.0)** | **Yes** (`/api/v2/`) | **No** |

### 2. Only Major Versions in URL

Only the **major** version number appears in the URL path. Minor and patch changes are backward-compatible and do not change the URL:

```
/api/v1/orders    (covers v1.0.0 through v1.x.y)
/api/v2/orders    (covers v2.0.0 through v2.x.y)
```

### 3. Maximum Two Concurrent Major Versions

At most **two major versions** of an API may be active at any time:

```
/api/v1/orders    (deprecated, sunset date announced)
/api/v2/orders    (current)
```

Once `v3` is released, `v1` must be removed.

---

## Deprecation Policy

### Timeline

| Phase | Duration | Action |
|-------|----------|--------|
| **Announcement** | Day 0 | New version released, old version marked deprecated |
| **Migration Period** | 6 months | Both versions active, deprecation headers returned |
| **Sunset** | After 6 months | Old version returns `410 Gone` |

### Deprecation Headers

When a version is deprecated, all responses include:

```http
Deprecation: true
Sunset: Sat, 01 Sep 2024 00:00:00 GMT
Link: </api/v2/orders>; rel="successor-version"
```

### Sunset Response

After the sunset date, the old version returns:

```http
HTTP/1.1 410 Gone
Content-Type: application/json

{
  "status": "error",
  "error": {
    "code": "API_VERSION_SUNSET",
    "message": "API v1 has been sunset. Please migrate to /api/v2/",
    "details": []
  },
  "timestamp": "2024-09-15T00:00:00Z",
  "path": "/api/v1/orders"
}
```

---

## Implementation Guide

### Controller Structure

Version-specific controllers use separate packages:

```
com.ftgo.order/
  web/
    v1/
      OrderControllerV1.java    # /api/v1/orders
    v2/
      OrderControllerV2.java    # /api/v2/orders
```

### Spring MVC Configuration

```java
// V1 Controller
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders v1", description = "Order management (v1)")
public class OrderControllerV1 {
    // ...
}

// V2 Controller (when needed)
@RestController
@RequestMapping("/api/v2/orders")
@Tag(name = "Orders v2", description = "Order management (v2)")
public class OrderControllerV2 {
    // ...
}
```

### Shared Service Layer

Controllers of different versions should share the same service layer. Version-specific mapping is handled in the controller:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 {

    private final OrderService orderService;  // Shared service
    private final OrderMapperV1 mapper;        // V1-specific DTO mapper

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDtoV1>> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toDto(order)));
    }
}
```

---

## OpenAPI Per-Version Documentation

Each API version has its own OpenAPI group:

```properties
# application.properties
springdoc.group-configs[0].group=v1
springdoc.group-configs[0].paths-to-match=/api/v1/**
springdoc.group-configs[0].display-name=API v1

springdoc.group-configs[1].group=v2
springdoc.group-configs[1].paths-to-match=/api/v2/**
springdoc.group-configs[1].display-name=API v2
```

---

## Version Changelog

| Version | Date | Changes |
|---------|------|---------|
| v1.0.0 | 2024-03-01 | Initial release - Order, Consumer, Restaurant, Courier APIs |

---

## References

- [FTGO REST API Standards](api-standards.md)
- [ADR-0002: SpringDoc OpenAPI Migration](adr/0002-springdoc-openapi-migration.md)
- [Semantic Versioning](https://semver.org/)
- [RFC 8594: Sunset Header](https://www.rfc-editor.org/rfc/rfc8594)
