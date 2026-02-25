# Role-Based Access Control (RBAC) Authorization Framework

> **EM-37** | Depends on: EM-39 (Spring Security Foundation), EM-40 (JWT Authentication)

## Overview

The FTGO platform implements a Role-Based Access Control (RBAC) system that restricts access to API endpoints based on user roles and permissions. The framework is built on top of the Spring Security foundation (EM-39) and JWT authentication (EM-40) provided by `shared/ftgo-security-lib`.

## Roles

The platform defines four roles, each mapped to bounded context business rules:

| Role | Authority String | Description |
|------|-----------------|-------------|
| **CUSTOMER** | `ROLE_CUSTOMER` | End users who place orders, view restaurants, and track deliveries |
| **RESTAURANT_OWNER** | `ROLE_RESTAURANT_OWNER` | Restaurant managers who manage their restaurant and view related orders |
| **COURIER** | `ROLE_COURIER` | Delivery personnel who manage deliveries and view assigned orders |
| **ADMIN** | `ROLE_ADMIN` | Platform administrators with full access to all operations |

### Role Hierarchy

The role hierarchy defines inheritance relationships:

```
ROLE_ADMIN > ROLE_RESTAURANT_OWNER
ROLE_ADMIN > ROLE_COURIER
ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
```

This means:
- **ADMIN** inherits all permissions from RESTAURANT_OWNER, COURIER, and CUSTOMER
- **RESTAURANT_OWNER** inherits all permissions from CUSTOMER
- **COURIER** is independent (does not inherit from CUSTOMER or RESTAURANT_OWNER)
- **CUSTOMER** is the base role

## Permission Matrix

### Consumer Service

| Operation | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| Create Consumer | - | - | - | Yes |
| View Consumer | Own only | Inherited | - | Yes |
| Update Consumer | Own only | Inherited | - | Yes |
| Delete Consumer | - | - | - | Yes |

### Order Service

| Operation | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| Create Order | Yes (own) | Inherited | - | Yes |
| View Order | Own only | Related orders | Assigned orders | Yes |
| Cancel Order | Own only | Inherited | - | Yes |
| Update Order | - | - | - | Yes |
| Delete Order | - | - | - | Yes |

### Restaurant Service

| Operation | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| Create Restaurant | - | Yes | - | Yes |
| View Restaurant | Yes | Yes | - | Yes |
| Update Restaurant | - | Own only | - | Yes |
| Delete Restaurant | - | - | - | Yes |

### Delivery / Courier Service

| Operation | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|-----------|----------|-------------------|---------|-------|
| View Delivery | Own only | - | Assigned | Yes |
| Update Delivery | - | - | Yes | Yes |
| Plan Delivery | - | - | Yes | Yes |
| Delete Delivery | - | - | - | Yes |

## Fine-Grained Permissions

Permissions follow the `<context>:<operation>` naming convention and are carried in JWT `permissions` claims:

| Permission | Description |
|-----------|-------------|
| `consumer:create` | Create a new consumer |
| `consumer:view` | View consumer details |
| `consumer:update` | Update consumer information |
| `consumer:delete` | Delete a consumer |
| `order:create` | Create a new order |
| `order:view` | View order details |
| `order:cancel` | Cancel an order |
| `order:update` | Update order status |
| `order:delete` | Delete an order |
| `restaurant:create` | Create a new restaurant |
| `restaurant:view` | View restaurant details and menus |
| `restaurant:update` | Update restaurant information or menu |
| `restaurant:delete` | Delete a restaurant |
| `delivery:view` | View delivery details |
| `delivery:update` | Update delivery status |
| `delivery:plan` | Plan a delivery route |
| `delivery:delete` | Delete a delivery record |

## Architecture

### Components

```
shared/ftgo-security-lib/src/main/java/com/ftgo/security/authorization/
  FtgoRole.java                          # Role enumeration
  FtgoPermission.java                    # Permission enumeration
  FtgoRoleHierarchyConfiguration.java    # Role hierarchy bean
  FtgoMethodSecurityConfiguration.java   # @EnableMethodSecurity + expression handler
  FtgoPermissionEvaluator.java           # Custom PermissionEvaluator
  ResourceOwnershipStrategy.java         # Strategy interface for ownership checks
  FtgoAuthorizationAutoConfiguration.java # Auto-configuration entry point
  RequireRole.java                       # Documentation annotation
```

### Auto-Configuration

The RBAC framework is automatically enabled when importing `FtgoSecurityAutoConfiguration`:

```java
@Configuration
@Import(FtgoSecurityAutoConfiguration.class)
public class ServiceSecurityConfiguration {
    // RBAC is auto-configured
}
```

## Usage Guide

### 1. Role-Based Access with @PreAuthorize

```java
// Admin only
@PreAuthorize("hasRole('ADMIN')")
public void adminOperation() { }

// Customer or higher (includes RESTAURANT_OWNER and ADMIN via hierarchy)
@PreAuthorize("hasRole('CUSTOMER')")
public Order viewOrder(Long orderId) { }

// Multiple specific roles
@PreAuthorize("hasAnyRole('COURIER', 'ADMIN')")
public void courierOrAdminOperation() { }
```

### 2. Resource Ownership Validation

```java
// User can only access their own resources
@PreAuthorize("hasRole('CUSTOMER') and #consumerId == authentication.principal.userId")
public Order createOrder(Long consumerId, Long restaurantId, List<LineItem> items) { }
```

### 3. Custom Permission Evaluator

```java
// Permission-based with ownership check
@PreAuthorize("hasPermission(#orderId, 'Order', 'VIEW')")
public Order getOrder(Long orderId) { }

// Simple permission check
@PreAuthorize("hasPermission(null, 'order:create')")
public Order createOrder(...) { }
```

### 4. Implementing Resource Ownership Strategy

Services can register ownership strategies for resource-specific authorization:

```java
@Component
public class OrderOwnershipStrategy implements ResourceOwnershipStrategy {

    private final OrderRepository orderRepository;

    public OrderOwnershipStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean isOwner(Long userId, Serializable resourceId) {
        return orderRepository.findById((Long) resourceId)
                .map(order -> order.getConsumerId().equals(userId))
                .orElse(false);
    }

    @Override
    public String getResourceType() {
        return "Order";
    }
}
```

Ownership strategies are automatically discovered and registered at startup.

### 5. Documentation Annotation

Use `@RequireRole` to document role requirements (complementary to `@PreAuthorize`):

```java
@RequireRole(FtgoRole.CUSTOMER)
@PreAuthorize("hasRole('CUSTOMER')")
public Order createOrder(Long consumerId, ...) { }
```

## Security Context Access

The existing `SecurityUtils` class provides convenient access to the current user's context:

```java
// Get current user ID
Optional<Long> userId = SecurityUtils.getCurrentUserId();

// Get current roles
List<String> roles = SecurityUtils.getCurrentRoles();

// Check a specific permission
boolean canCreate = SecurityUtils.hasPermission("order:create");

// Get full JWT user details
Optional<JwtUserDetails> userDetails = SecurityUtils.getCurrentJwtUserDetails();
```

## Error Handling

- **401 Unauthorized**: Returned when no valid authentication is provided (handled by `FtgoAuthenticationEntryPoint`)
- **403 Forbidden**: Returned when the authenticated user lacks the required role or permission (handled by `FtgoAccessDeniedHandler`)

Both responses are in JSON format with structured error details.

## Testing

### Test Coverage

The authorization framework includes comprehensive tests:

- **FtgoRoleTest**: Validates role definitions, authority strings, and resolution
- **FtgoPermissionTest**: Validates permission definitions and format
- **FtgoRoleHierarchyConfigurationTest**: Validates role hierarchy inheritance
- **FtgoPermissionEvaluatorTest**: Validates permission evaluation and ownership checks
- **MethodSecurityIntegrationTest**: Integration tests for all role/endpoint combinations

### Writing Authorization Tests

```java
// Set up authentication context in tests
JwtUserDetails userDetails = new JwtUserDetails(
    42L, "customer",
    List.of("ROLE_CUSTOMER"),
    List.of("order:create", "order:view")
);
Authentication auth = new UsernamePasswordAuthenticationToken(
    userDetails, "token",
    List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
);
SecurityContextHolder.getContext().setAuthentication(auth);

// Test that the service method is accessible
assertThat(orderService.createOrder(42L)).isNotNull();

// Clean up
SecurityContextHolder.clearContext();
```

## Migration Notes

### From Monolith to Microservices

The monolith currently has no authorization. When migrating to microservices:

1. Each service imports `FtgoSecurityAutoConfiguration` to get RBAC
2. Add `@PreAuthorize` annotations to service methods
3. Implement `ResourceOwnershipStrategy` for domain-specific ownership
4. JWT tokens carry roles and permissions in claims
5. The role hierarchy is shared across all services via the security library
