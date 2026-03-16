# Role-Based Access Control (RBAC) Authorization

> **Library**: `ftgo-security-lib`
> **Package**: `net.chrisrichardson.ftgo.security.rbac`

## Overview

The FTGO RBAC framework extends the existing JWT authentication in `ftgo-security-lib` with
role-based and permission-based access control.  It provides:

- **Role and Permission enums** with a static role-to-permission mapping
- **Method-level security** via `@PreAuthorize` / `@PostAuthorize` annotations
- **Custom SpEL expressions** for ownership checks and FTGO-specific authorization logic

All services that include `ftgo-security-lib` on their classpath get RBAC support
automatically through Spring Boot auto-configuration.

---

## Roles

Defined in `net.chrisrichardson.ftgo.security.rbac.Role`:

| Role               | Authority String        | Description                          |
|--------------------|-------------------------|--------------------------------------|
| `CUSTOMER`         | `ROLE_CUSTOMER`         | Consumer who places food orders      |
| `RESTAURANT_OWNER` | `ROLE_RESTAURANT_OWNER` | Manages restaurants and menus        |
| `COURIER`          | `ROLE_COURIER`          | Delivers orders                      |
| `ADMIN`            | `ROLE_ADMIN`            | Platform administrator (full access) |

---

## Permissions

Defined in `net.chrisrichardson.ftgo.security.rbac.Permission`, organized by bounded context:

### Order Context

| Permission      | Authority String | Description                    |
|-----------------|------------------|--------------------------------|
| `ORDER_CREATE`  | `order:create`   | Create a new order             |
| `ORDER_READ`    | `order:read`     | View order details             |
| `ORDER_UPDATE`  | `order:update`   | Update an order (accept, etc.) |
| `ORDER_CANCEL`  | `order:cancel`   | Cancel an order                |
| `ORDER_MANAGE`  | `order:manage`   | Full order administration      |

### Consumer Context

| Permission       | Authority String  | Description                |
|------------------|-------------------|----------------------------|
| `CONSUMER_READ`  | `consumer:read`   | View consumer profiles     |
| `CONSUMER_UPDATE`| `consumer:update` | Update own profile         |
| `CONSUMER_MANAGE`| `consumer:manage` | Full consumer admin        |

### Restaurant Context

| Permission           | Authority String      | Description                  |
|----------------------|-----------------------|------------------------------|
| `RESTAURANT_READ`    | `restaurant:read`     | View restaurant info         |
| `RESTAURANT_UPDATE`  | `restaurant:update`   | Update restaurant details    |
| `RESTAURANT_MANAGE`  | `restaurant:manage`   | Full restaurant admin        |
| `MENU_UPDATE`        | `menu:update`         | Update menu items            |

### Courier / Delivery Context

| Permission        | Authority String   | Description                      |
|-------------------|--------------------|----------------------------------|
| `COURIER_READ`    | `courier:read`     | View courier info                |
| `COURIER_UPDATE`  | `courier:update`   | Update courier profile/status    |
| `COURIER_MANAGE`  | `courier:manage`   | Full courier admin               |
| `DELIVERY_UPDATE` | `delivery:update`  | Update delivery status           |
| `DELIVERY_MANAGE` | `delivery:manage`  | Full delivery admin              |

---

## Role-to-Permission Mapping

Defined in `RolePermissionMapping`. This is the single source of truth for which permissions
each role carries.

| Role               | Permissions                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------|
| `CUSTOMER`         | `order:create`, `order:read`, `order:cancel`, `consumer:read`, `consumer:update`, `restaurant:read` |
| `RESTAURANT_OWNER` | `order:read`, `order:update`, `restaurant:read`, `restaurant:update`, `menu:update`          |
| `COURIER`          | `order:read`, `courier:read`, `courier:update`, `delivery:update`                            |
| `ADMIN`            | **All permissions**                                                                          |

---

## Setup

### 1. Add the Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-security-lib')
}
```

### 2. Auto-Configuration (Zero Config)

`ftgo-security-lib` uses Spring Boot auto-configuration. Adding the dependency
automatically enables:

- JWT authentication filter
- Method-level security with `@PreAuthorize` / `@PostAuthorize`
- Custom FTGO SpEL expressions
- CORS and actuator security

No additional annotations or configuration classes are needed in your service.

### 3. Configure JWT Properties

In your service's `application.properties`:

```properties
ftgo.security.jwt.secret=<your-base64-encoded-secret>
ftgo.security.jwt.issuer=ftgo-platform
ftgo.security.jwt.expiration-ms=3600000
ftgo.security.jwt.refresh-expiration-ms=86400000
```

---

## Usage

### Protecting Methods with @PreAuthorize

```java
import org.springframework.security.access.prepost.PreAuthorize;

// Require a specific permission
@PreAuthorize("hasAuthority('order:create')")
public Order createOrder(CreateOrderRequest request) { ... }

// Require a specific role
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public void deleteUser(String userId) { ... }

// Use the custom FTGO permission expression
@PreAuthorize("hasFtgoPermission('restaurant:update')")
public void updateRestaurant(long restaurantId, UpdateRequest request) { ... }
```

### Ownership Checks

```java
// Only the resource owner can access
@PreAuthorize("isResourceOwner(#consumerId)")
public Consumer getConsumer(String consumerId) { ... }

// Owner or admin
@PreAuthorize("isResourceOwnerOrAdmin(#consumerId)")
public Consumer updateConsumer(String consumerId, UpdateRequest req) { ... }

// Combining ownership with permissions
@PreAuthorize("isResourceOwner(#orderId) or hasAuthority('order:manage')")
public void cancelOrder(String orderId) { ... }
```

### Admin Shorthand

```java
@PreAuthorize("isAdmin()")
public List<User> listAllUsers() { ... }
```

### Using the Role Enum in Code

```java
import net.chrisrichardson.ftgo.security.rbac.Role;
import net.chrisrichardson.ftgo.security.rbac.Permission;

// Check role programmatically
if (SecurityContextHelper.hasAuthority(Role.ADMIN.getAuthority())) {
    // admin-only logic
}

// Check permission programmatically
if (SecurityContextHelper.hasAuthority(Permission.ORDER_CREATE.getAuthority())) {
    // user can create orders
}
```

### Resolving Permissions from Roles

Use `RolePermissionResolver` at token-creation time to expand roles into permissions:

```java
import net.chrisrichardson.ftgo.security.rbac.RolePermissionResolver;

List<String> roles = Arrays.asList("ROLE_CUSTOMER");
Set<String> permissions = RolePermissionResolver.resolvePermissions(roles);
// permissions = ["order:create", "order:read", "order:cancel",
//                "consumer:read", "consumer:update", "restaurant:read"]

String token = jwtTokenProvider.createAccessToken(userId, roles, new ArrayList<>(permissions));
```

---

## Custom SpEL Expression Reference

These expressions are available in `@PreAuthorize` and `@PostAuthorize` annotations:

| Expression                              | Description                                          |
|-----------------------------------------|------------------------------------------------------|
| `isResourceOwner(#id)`                  | Current user's ID matches the given resource owner   |
| `isAdmin()`                             | Current user has the `ROLE_ADMIN` authority           |
| `isResourceOwnerOrAdmin(#id)`           | User is owner **or** admin                           |
| `hasFtgoPermission('permission:string')`| User has the given FTGO permission                   |
| `hasFtgoRole(T(Role).CUSTOMER)`         | User has the given FTGO role                         |

Standard Spring Security expressions (`hasRole`, `hasAuthority`, `isAuthenticated`, etc.)
remain available as usual.

---

## Architecture

```
ftgo-security-lib/
  src/main/java/net/chrisrichardson/ftgo/security/
    rbac/
      Role.java                             # Role enum
      Permission.java                       # Permission enum (all contexts)
      RolePermissionMapping.java            # Static role -> permissions map
      RolePermissionResolver.java           # Resolves roles to permission strings
      FtgoSecurityExpressionRoot.java       # Custom SpEL expression root
      FtgoMethodSecurityExpressionHandler.java  # Registers custom expressions
      FtgoMethodSecurityConfiguration.java  # @EnableGlobalMethodSecurity config
    config/
      FtgoSecurityAutoConfiguration.java    # Imports RBAC config automatically
    jwt/
      JwtAuthenticationToken.java           # Carries roles & permissions
      JwtAuthenticationFilter.java          # Populates SecurityContext
```

---

## Testing

Use `JwtTestSupport` to create tokens with specific roles and permissions in tests:

```java
JwtTestSupport jwt = JwtTestSupport.withDefaults();

// Token with CUSTOMER role and resolved permissions
String token = jwt.createAccessToken(
    "user-123",
    Arrays.asList("ROLE_CUSTOMER"),
    new ArrayList<>(RolePermissionResolver.resolvePermissions(
        Collections.singletonList("ROLE_CUSTOMER")))
);

mockMvc.perform(get("/orders")
        .header("Authorization", "Bearer " + token))
    .andExpect(status().isOk());
```

---

## Related Documentation

- [JWT Authentication](jwt-authentication.md)
- [Security Configuration](security-configuration.md)
- [Testing Strategy](testing-strategy.md)
