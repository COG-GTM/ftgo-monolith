# FTGO API Versioning Strategy

> Defines how FTGO microservices version their REST APIs, manage backward
> compatibility, and handle the lifecycle of API versions.

---

## 1. Versioning Approach

FTGO uses **URL path versioning** as the primary versioning mechanism.

```
/api/v1/orders
/api/v2/orders
```

### Why URL Path Versioning?

| Approach           | Pros                                    | Cons                                        |
|--------------------|-----------------------------------------|---------------------------------------------|
| **URL path** (chosen) | Simple, visible, easy to route        | URL changes between versions                |
| Header-based       | Clean URLs                              | Hidden, harder to test in browser           |
| Query parameter    | Easy to add                             | Mixes versioning with query logic           |
| Content negotiation| REST-pure approach                      | Complex, poor tooling support               |

URL path versioning was chosen for FTGO because:
- It is **explicit and discoverable** — the version is visible in every request
- API Gateway routing rules are straightforward (`/api/v1/**` → service v1)
- Works naturally with OpenAPI/Swagger documentation grouping
- Most widely adopted pattern in enterprise REST APIs

---

## 2. URL Format

```
/api/{version}/{resource}
```

### Examples

| Endpoint                         | Description                       |
|----------------------------------|-----------------------------------|
| `GET /api/v1/orders`             | List orders (version 1)           |
| `GET /api/v1/orders/42`          | Get order 42 (version 1)          |
| `POST /api/v2/orders`            | Create order (version 2)          |
| `GET /api/v1/restaurants/5/menu` | Get restaurant menu (version 1)   |

### Controller Configuration

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    // ...
}
```

---

## 3. Version Lifecycle

Each API version moves through a defined lifecycle:

```
ACTIVE  →  DEPRECATED  →  SUNSET  →  REMOVED
```

### Lifecycle Stages

| Stage        | Duration     | Description                                          |
|--------------|--------------|------------------------------------------------------|
| **Active**   | Indefinite   | Current recommended version; receives new features    |
| **Deprecated** | 6 months min | Still functional; no new features; clients should migrate |
| **Sunset**   | 3 months     | Read-only or limited; final migration window          |
| **Removed**  | —            | Endpoint returns 410 Gone                             |

### Deprecation Signalling

When an API version is deprecated, services **MUST**:

1. Add the `Deprecation` response header:
   ```
   Deprecation: true
   Sunset: Sat, 01 Nov 2026 00:00:00 GMT
   ```

2. Add the `Link` header pointing to the successor version:
   ```
   Link: </api/v2/orders>; rel="successor-version"
   ```

3. Log a warning for each request to the deprecated version (sampled to avoid log flooding)

4. Update the OpenAPI spec with `@Deprecated` annotations

5. Communicate the timeline to API consumers via changelog and release notes

---

## 4. What Constitutes a Breaking Change

### Breaking Changes (require new major version)

| Change                                | Example                                    |
|---------------------------------------|--------------------------------------------|
| Removing an endpoint                  | `DELETE /api/v1/orders/cancel` removed      |
| Removing a response field             | Dropping `orderTotal` from response         |
| Renaming a response field             | `orderTotal` → `totalAmount`                |
| Changing a field's type               | `orderId` from `number` to `string`         |
| Making an optional field required     | `deliveryAddress` now required              |
| Changing an endpoint's HTTP method    | `PUT /orders/42/cancel` → `POST`            |
| Changing error codes or status codes  | 404 → 410 for same scenario                |
| Changing pagination structure         | Offset-based → cursor-based                |

### Non-Breaking Changes (safe within current version)

| Change                                | Example                                    |
|---------------------------------------|--------------------------------------------|
| Adding a new endpoint                 | `GET /api/v1/orders/{id}/history`           |
| Adding an optional response field     | New `estimatedDelivery` field               |
| Adding an optional request field      | New `specialInstructions` parameter         |
| Adding a new enum value               | `OrderState.ON_HOLD` added                  |
| Relaxing a validation constraint      | `name` max length 50 → 100                  |
| Adding a new query parameter          | `?includeItems=true`                        |
| Improving error messages              | More descriptive text in `message` field    |

---

## 5. When to Create a New Version

Follow this decision tree:

```
Is the change breaking? (see Section 4)
├── NO  → Add to current version
└── YES → Can it be made non-breaking?
    ├── YES → Refactor to non-breaking, add to current version
    └── NO  → Create new version (v1 → v2)
```

### Guidelines

1. **Prefer non-breaking changes** — most new features can be added without a new version
2. **Batch breaking changes** — if v2 is needed, combine multiple breaking changes into a single release
3. **One active + one deprecated** — aim to have at most two versions of any API in production at a time
4. **Version per service** — each service versions independently (Order Service v2 does not require Consumer Service v2)

---

## 6. Multi-Version Implementation

### Option A: Separate Controller Classes (Recommended)

```java
// Version 1
@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 {
    @GetMapping("/{id}")
    public ApiResponseEnvelope<OrderResponseV1> getOrder(@PathVariable long id) { ... }
}

// Version 2
@RestController
@RequestMapping("/api/v2/orders")
public class OrderControllerV2 {
    @GetMapping("/{id}")
    public ApiResponseEnvelope<OrderResponseV2> getOrder(@PathVariable long id) { ... }
}
```

**Pros**: Clear separation, easy to remove old versions, independent testing.

### Option B: Shared Controller with Delegation

```java
@RestController
public class OrderController {

    @GetMapping("/api/v1/orders/{id}")
    public ApiResponseEnvelope<OrderResponseV1> getOrderV1(@PathVariable long id) {
        return ApiResponseEnvelope.success(orderService.getOrderV1(id));
    }

    @GetMapping("/api/v2/orders/{id}")
    public ApiResponseEnvelope<OrderResponseV2> getOrderV2(@PathVariable long id) {
        return ApiResponseEnvelope.success(orderService.getOrderV2(id));
    }
}
```

**Pros**: Shared service layer logic, less duplication for minor differences.

### Recommendation

Use **Option A** for major version differences and **Option B** when versions
differ only in DTO shape.

---

## 7. API Gateway Routing

The API Gateway routes versioned requests to the appropriate service instance:

```yaml
# API Gateway route configuration
spring:
  cloud:
    gateway:
      routes:
        - id: order-service-v1
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**

        - id: order-service-v2
          uri: lb://order-service
          predicates:
            - Path=/api/v2/orders/**
```

- Both v1 and v2 run within the **same service instance** (version handling is in the controller layer)
- The gateway does not need separate deployments per version
- When a version is removed, the gateway route returns **410 Gone**

---

## 8. OpenAPI Documentation per Version

Each API version has its own OpenAPI group, configured via `ftgo-openapi-lib`:

```properties
# Service supports v1 and v2
ftgo.openapi.version=v1
```

```java
@Bean
public GroupedOpenApi v1Api() {
    return GroupedOpenApi.builder()
        .group("v1")
        .pathsToMatch("/api/v1/**")
        .build();
}

@Bean
public GroupedOpenApi v2Api() {
    return GroupedOpenApi.builder()
        .group("v2")
        .pathsToMatch("/api/v2/**")
        .build();
}
```

The Swagger UI dropdown will let consumers switch between version groups.

---

## 9. Client Migration Guide

When a new version is released:

1. **Review the changelog** — understand what changed and why
2. **Update DTOs** — adapt request/response models to the new schema
3. **Update base URL** — change `/api/v1/` to `/api/v2/` in client configuration
4. **Run integration tests** — verify all API interactions work with the new version
5. **Deploy gradually** — use feature flags or canary releases to shift traffic
6. **Decommission old version calls** — once fully migrated, remove v1 references

### Migration Timeline

| Event                        | Timeframe             |
|------------------------------|-----------------------|
| v2 released, v1 deprecated   | Day 0                |
| Migration support available   | Day 0 – Month 6     |
| v1 enters sunset phase        | Month 6              |
| v1 removed (410 Gone)         | Month 9              |

---

## 10. Version History Template

Each service should maintain a version history in its documentation:

```markdown
## Order Service API Versions

### v2 (2026-06-01) — Active
- Changed `orderTotal` from `String` to `Money` object
- Added `estimatedDelivery` field to order response
- Added `GET /api/v2/orders/{id}/timeline` endpoint

### v1 (2026-01-01) — Deprecated (sunset: 2026-12-01)
- Initial release
- Order CRUD operations
- Order state management
```
