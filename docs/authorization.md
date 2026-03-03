# FTGO Role-Based Access Control (RBAC) Model

## Overview

The FTGO authorization framework provides role-based access control (RBAC) for all microservices. It enforces security at the method level using Spring Security's `@PreAuthorize` annotations, backed by a custom permission evaluator and role hierarchy.

**Library:** `shared/ftgo-authorization`  
**Package:** `net.chrisrichardson.ftgo.authorization`  
**Version:** 1.0.0

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   JWT Token (Claims)                     │
│  { "sub": "user-123", "roles": ["CUSTOMER"], ... }      │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Security Context                     │
│  Authentication with GrantedAuthority list               │
└──────────────────────────┬──────────────────────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
     ┌──────────────┐ ┌──────────┐ ┌──────────────────┐
     │ Role         │ │ Permission│ │ Resource         │
     │ Hierarchy    │ │ Evaluator │ │ Ownership        │
     │              │ │           │ │ Resolver         │
     └──────────────┘ └──────────┘ └──────────────────┘
```

## Roles

The system defines four roles with a hierarchical relationship:

| Role | Description | Hierarchy Level |
|------|-------------|----------------|
| `ADMIN` | System administrator with full access | Top (inherits all) |
| `RESTAURANT_OWNER` | Restaurant owner/manager | Mid (inherits CUSTOMER) |
| `COURIER` | Delivery courier | Mid (inherits CUSTOMER) |
| `CUSTOMER` | End consumer who places orders | Base |

### Role Hierarchy

```
ROLE_ADMIN > ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
ROLE_ADMIN > ROLE_COURIER > ROLE_CUSTOMER
```

- **ADMIN** inherits all permissions from RESTAURANT_OWNER, COURIER, and CUSTOMER
- **RESTAURANT_OWNER** inherits all CUSTOMER permissions
- **COURIER** inherits all CUSTOMER permissions
- **CUSTOMER** is the base role with no inheritance

## Permission Matrix

### Consumer Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| `consumer:create` | Yes | Yes (inherited) | Yes (inherited) | Yes |
| `consumer:read` | Own only | Yes (inherited) | Yes (inherited) | All |

### Order Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| `order:create` | Yes | Yes (inherited) | Yes (inherited) | Yes |
| `order:read` | Own only | Related orders | Assigned orders | All |
| `order:cancel` | Own only | No | No | Yes |

### Restaurant Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| `restaurant:create` | No | Yes | No | Yes |
| `restaurant:read` | Yes | Yes | No | Yes |
| `restaurant:update` | No | Own only | No | All |
| `restaurant:delete` | No | Own only | No | All |

### Courier Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| `courier:read` | No | No | Own only | All |
| `courier:update` | No | No | Own only | All |
| `delivery:read` | Own delivery | No | Assigned | All |
| `delivery:update` | No | No | Assigned | All |

## Usage

### 1. Add Dependency

```groovy
// In your service's build.gradle
compile project(':shared:ftgo-authorization')
```

The library auto-configures via Spring Boot's `spring.factories` mechanism.

### 2. Use @PreAuthorize Annotations

```java
// Role-based check
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyOperation() { ... }

// Permission-based check (simple)
@PreAuthorize("hasPermission(null, 'order:create')")
public Order createOrder(CreateOrderRequest request) { ... }

// Permission-based check with resource ownership
@PreAuthorize("hasPermission(#orderId, 'order', 'read')")
public Order getOrder(Long orderId) { ... }

// Multiple roles (OR logic via hierarchy)
@PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
public Restaurant updateRestaurant(Long id, UpdateRequest req) { ... }
```

### 3. Implement Resource Ownership Resolvers

Each service registers a `ResourceOwnershipResolver` to handle ownership checks:

```java
@Component
public class OrderOwnershipResolver implements ResourceOwnershipResolver {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean supports(String resourceType) {
        return "order".equals(resourceType);
    }

    @Override
    public boolean isOwner(String userId, Serializable resourceId) {
        Order order = orderRepository.findById((Long) resourceId).orElse(null);
        return order != null && order.getConsumerId().equals(Long.parseLong(userId));
    }
}
```

### 4. Programmatic Authorization Checks

Use `AuthorizationUtils` for programmatic checks in service code:

```java
import net.chrisrichardson.ftgo.authorization.util.AuthorizationUtils;

if (AuthorizationUtils.isAdmin()) {
    // Admin-specific logic
}

if (AuthorizationUtils.hasPermission(FtgoPermission.ORDER_CREATE)) {
    // Allow order creation
}
```

## Custom Annotations

The library provides convenience annotations for common patterns:

| Annotation | Description |
|-----------|-------------|
| `@RequireRole` | Restrict access to specific FTGO roles |
| `@RequirePermission` | Restrict access to specific FTGO permissions |
| `@RequireResourceOwner` | Restrict access to resource owners or admins |

## Security Defaults

- **Unauthorized access** returns HTTP 403 Forbidden (handled by `FtgoAccessDeniedHandler` in ftgo-security)
- **No resolver registered** for a resource type defaults to **deny access** (secure by default)
- **ADMIN role** bypasses all permission and ownership checks
- **Role hierarchy** is applied automatically via Spring Security's expression handler

## Components

| Component | Description |
|-----------|-------------|
| `FtgoRole` | Enum defining the four system roles |
| `FtgoPermission` | Enum defining all 13 service permissions |
| `RolePermissionMapping` | Maps roles to their granted permissions |
| `FtgoPermissionEvaluator` | Custom Spring Security PermissionEvaluator |
| `ResourceOwnershipResolver` | Strategy interface for ownership validation |
| `FtgoRoleHierarchyConfig` | Configures the Spring Security role hierarchy |
| `FtgoMethodSecurityConfig` | Enables @PreAuthorize with custom evaluator |
| `AuthorizationUtils` | Utility class for programmatic auth checks |
| `FtgoAuthorizationAutoConfiguration` | Spring Boot auto-configuration entry point |

## Dependencies

```
ftgo-authorization
  └── ftgo-jwt (for FtgoUserContext, JwtUserContextHolder)
      └── ftgo-security (for base security configuration)
```

## Testing

The library includes comprehensive tests covering:

- **FtgoRoleTest** - Role enum validation, parsing, authorities
- **FtgoPermissionTest** - Permission enum validation, parsing
- **RolePermissionMappingTest** - Complete permission matrix verification for all roles
- **FtgoPermissionEvaluatorTest** - Permission and ownership checks for all role/endpoint combinations
- **RoleHierarchyTest** - Role inheritance validation
- **AuthorizationIntegrationTest** - End-to-end RBAC flow with role hierarchy expansion

Run tests:
```bash
./gradlew :shared:ftgo-authorization:test
```
