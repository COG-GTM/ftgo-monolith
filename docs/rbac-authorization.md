# Role-Based Access Control (RBAC) Authorization Model

## Overview

The FTGO platform implements Role-Based Access Control (RBAC) to restrict API endpoint access based on user roles and permissions. Roles are stored in JWT claims and enforced at the method level using Spring Security's `@PreAuthorize` annotations.

## Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | End-user who places food orders |
| `RESTAURANT_OWNER` | Owner of one or more restaurants on the platform |
| `COURIER` | Delivery courier who fulfills orders |
| `ADMIN` | Platform administrator with unrestricted access |

## Role Hierarchy

The role hierarchy defines inheritance relationships between roles:

```
ROLE_ADMIN > ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
ROLE_ADMIN > ROLE_COURIER
```

This means:
- **ADMIN** inherits all permissions of RESTAURANT_OWNER, CUSTOMER, and COURIER
- **RESTAURANT_OWNER** inherits all permissions of CUSTOMER
- **COURIER** has its own permissions (no inheritance from/to CUSTOMER or RESTAURANT_OWNER)

### Hierarchy Diagram

```
         ADMIN
        /     \
RESTAURANT_    COURIER
   OWNER
     |
  CUSTOMER
```

## Permission Matrix

### Consumer Service

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/consumers` | POST | Yes | Yes* | No | Yes |
| `/consumers/{id}` | GET | Own | Own* | No | Yes |
| `/consumers` | GET | No | No | No | Yes |

### Order Service

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/orders` | POST | Yes | Yes* | No | Yes |
| `/orders/{id}` | GET | Own | Yes | Yes | Yes |
| `/orders/{id}/cancel` | POST | Own | No | No | Yes |

### Restaurant Service

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/restaurants` | GET | Yes | Yes | No | Yes |
| `/restaurants/{id}` | GET | Yes | Yes | No | Yes |
| `/restaurants` | POST | No | Yes | No | Yes |
| `/restaurants/{id}` | PUT | No | Own | No | Yes |
| `/restaurants/{id}` | DELETE | No | Own | No | Yes |

### Courier / Delivery Service

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/deliveries/{id}/track` | GET | Own | No | Yes | Yes |
| `/deliveries/{id}/status` | PUT | No | No | Assigned | Yes |
| `/couriers/{id}/orders` | GET | No | No | Own | Yes |

**Legend:**
- **Yes** — Full access
- **Own** — Access to own resources only (ownership validation)
- **Assigned** — Access to assigned resources only
- **No** — No access
- **\*** — Inherited via role hierarchy

## Implementation Details

### JWT Claims

Roles are stored in the JWT token under the `roles` claim:

```json
{
  "sub": "user-id-123",
  "username": "john.doe",
  "roles": ["ROLE_CUSTOMER"],
  "iss": "ftgo-platform",
  "iat": 1709568000,
  "exp": 1709571600,
  "type": "access"
}
```

### Security Annotations

Method-level security is enforced using `@PreAuthorize` annotations on controller methods:

```java
// Role-based access
@PreAuthorize("hasRole('CUSTOMER')")
@PostMapping("/orders")
public Order createOrder(...) { ... }

// Role-based access with ownership validation
@PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'VIEW'))")
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable String id) { ... }

// Multiple roles allowed
@PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') or hasRole('RESTAURANT_OWNER') "
    + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'VIEW'))")
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable String id) { ... }
```

### Resource Ownership Validation

The `FtgoPermissionEvaluator` validates resource ownership by comparing the authenticated user's ID (from JWT `sub` claim) with the resource owner's ID.

Domain objects implement the `ResourceOwner` interface:

```java
public class Order implements ResourceOwner {
    private String customerId;

    @Override
    public String getOwnerId() {
        return customerId;
    }
}
```

Two evaluation modes are supported:

1. **Object-based**: `hasPermission(#order, 'VIEW')` — validates access to a loaded domain object
2. **ID-based**: `hasPermission(#id, 'Order', 'VIEW')` — validates access by resource ID (compares with user's JWT `sub` claim)

ADMIN users bypass all ownership checks.

### Shared Library Components

All RBAC components are in `shared/ftgo-security-lib/`:

| Component | Package | Description |
|-----------|---------|-------------|
| `FtgoRole` | `authorization` | Role enumeration (CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN) |
| `FtgoPermission` | `authorization` | Permission enumeration for all operations |
| `FtgoRoleHierarchyConfig` | `authorization` | Spring Security role hierarchy bean |
| `FtgoPermissionEvaluator` | `authorization` | Custom PermissionEvaluator for ownership validation |
| `ResourceOwner` | `authorization` | Interface for domain objects with an owner |

### Per-Service Security Configuration

Each service has its own security configuration class in `src/main/java/.../security/`:

| Service | Configuration Class |
|---------|-------------------|
| Consumer Service | `ConsumerServiceSecurityConfig` |
| Order Service | `OrderServiceSecurityConfig` |
| Restaurant Service | `RestaurantServiceSecurityConfig` |
| Courier Service | `CourierServiceSecurityConfig` |

These classes enable `@EnableMethodSecurity` and document the expected `@PreAuthorize` expressions for each endpoint.

### Error Responses

| Status Code | Scenario |
|-------------|----------|
| 401 Unauthorized | Missing or invalid JWT token |
| 403 Forbidden | Valid JWT but insufficient role/permission |

Error response format:
```json
{
  "timestamp": "2024-03-04T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/orders/123"
}
```

## Configuration

The RBAC framework is auto-configured via `FtgoSecurityAutoConfiguration`. Services only need to:

1. Add `shared-ftgo-security-lib` as a dependency
2. Add `@EnableMethodSecurity` to their security configuration
3. Apply `@PreAuthorize` annotations to controller methods
4. Implement `ResourceOwner` on domain entities that need ownership validation

No additional configuration properties are required — the role hierarchy and permission evaluator are registered automatically.
