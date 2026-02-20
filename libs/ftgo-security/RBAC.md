# Role-Based Authorization Framework (RBAC)

## Overview

The FTGO Security library (`libs/ftgo-security/`) provides a comprehensive Role-Based Access Control (RBAC) framework built on Spring Security. It integrates with the JWT authentication library (`libs/ftgo-jwt/`) to enforce authorization at both the endpoint and method level.

## Role Hierarchy

```
ADMIN > MANAGER > USER
SERVICE (independent, for inter-service communication)
```

| Role      | Authority       | Description                          |
|-----------|----------------|--------------------------------------|
| ADMIN     | ROLE_ADMIN     | Full system access, inherits MANAGER and USER |
| MANAGER   | ROLE_MANAGER   | Elevated access, inherits USER       |
| USER      | ROLE_USER      | Standard authenticated user          |
| SERVICE   | ROLE_SERVICE   | Inter-service communication (independent) |

## Configuration

### Auto-Configuration

RBAC is automatically configured when Spring Security is on the classpath. The `FtgoAuthorizationAutoConfiguration` registers:

- `RoleHierarchy` bean with the hierarchy definition
- `MethodSecurityExpressionHandler` with role hierarchy support
- `RoleAuthorizationService` for programmatic authorization checks
- Method-level security via `@EnableMethodSecurity`

### Enabling Method-Level Security

Method-level security is enabled automatically with support for:

- `@PreAuthorize` / `@PostAuthorize` (SpEL expressions)
- `@Secured` (role-based)
- `@RolesAllowed` (JSR-250)

## Usage

### Using @PreAuthorize with RoleConstants

```java
import com.ftgo.security.authorization.RoleConstants;

@RestController
public class OrderController {

    @GetMapping("/api/orders")
    @PreAuthorize(RoleConstants.HAS_ROLE_USER)
    public List<Order> getOrders() { ... }

    @DeleteMapping("/api/orders/{id}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public void deleteOrder(@PathVariable Long id) { ... }

    @PostMapping("/api/orders/sync")
    @PreAuthorize(RoleConstants.HAS_ANY_ROLE_ADMIN_SERVICE)
    public void syncOrders() { ... }
}
```

### Using @Secured

```java
@Secured("ROLE_ADMIN")
public void adminOnlyMethod() { ... }
```

### Using Custom Annotations

```java
import com.ftgo.security.authorization.RequireRole;
import com.ftgo.security.authorization.RequirePermission;
import com.ftgo.security.authorization.FtgoRole;
import com.ftgo.security.authorization.FtgoPermission;

@RequireRole(FtgoRole.ADMIN)
public void adminAction() { ... }

@RequireRole(value = {FtgoRole.ADMIN, FtgoRole.MANAGER})
public void managerOrAdminAction() { ... }

@RequirePermission(FtgoPermission.ORDER_CREATE)
public void createOrder() { ... }
```

### Programmatic Authorization with RoleAuthorizationService

```java
@Service
public class OrderService {

    private final RoleAuthorizationService authService;

    public OrderService(RoleAuthorizationService authService) {
        this.authService = authService;
    }

    public void processOrder() {
        if (authService.isAdmin()) {
            // admin-specific logic
        }

        if (authService.hasPermission(FtgoPermission.ORDER_UPDATE)) {
            // update logic
        }

        String username = authService.getCurrentUsername();
        Set<FtgoRole> roles = authService.getCurrentRoles();
    }
}
```

## Permissions

Each role has a set of granular permissions. ADMIN has all permissions.

| Permission         | USER | MANAGER | ADMIN | SERVICE |
|-------------------|------|---------|-------|---------|
| order:read        | Y    | Y       | Y     | Y       |
| order:create      | Y    | Y       | Y     | Y       |
| order:update      |      | Y       | Y     | Y       |
| order:delete      |      |         | Y     |         |
| order:cancel      |      | Y       | Y     | Y       |
| restaurant:read   | Y    | Y       | Y     | Y       |
| restaurant:create |      | Y       | Y     |         |
| restaurant:update |      | Y       | Y     |         |
| restaurant:delete |      |         | Y     |         |
| menu:read         | Y    | Y       | Y     | Y       |
| menu:update       |      | Y       | Y     |         |
| user:read         |      | Y       | Y     |         |
| user:create       |      | Y       | Y     |         |
| user:update       |      | Y       | Y     |         |
| user:delete       |      |         | Y     |         |
| delivery:read     | Y    | Y       | Y     | Y       |
| delivery:update   |      | Y       | Y     | Y       |
| admin:access      |      |         | Y     |         |
| system:config     |      |         | Y     |         |

## JWT Integration

Roles are stored in JWT tokens as the `roles` claim. The `JwtAuthenticationFilter` from `libs/ftgo-jwt/` extracts roles and sets them as Spring Security `GrantedAuthority` entries. The RBAC framework then uses these authorities for authorization decisions.

### Token Generation with Roles

```java
Map<String, Object> claims = Map.of("roles", List.of("ROLE_USER"));
String token = jwtTokenProvider.generateToken("username", claims);
```

## Available SpEL Constants

| Constant                        | Expression                           |
|--------------------------------|--------------------------------------|
| `RoleConstants.HAS_ROLE_ADMIN`  | `hasRole('ADMIN')`                  |
| `RoleConstants.HAS_ROLE_MANAGER`| `hasRole('MANAGER')`                |
| `RoleConstants.HAS_ROLE_USER`   | `hasRole('USER')`                   |
| `RoleConstants.HAS_ROLE_SERVICE`| `hasRole('SERVICE')`                |
| `RoleConstants.HAS_ANY_ROLE_ADMIN_MANAGER` | `hasAnyRole('ADMIN', 'MANAGER')` |
| `RoleConstants.HAS_ANY_ROLE_ADMIN_SERVICE` | `hasAnyRole('ADMIN', 'SERVICE')` |
| `RoleConstants.IS_AUTHENTICATED`| `isAuthenticated()`                 |
| `RoleConstants.PERMIT_ALL`      | `permitAll()`                       |
