# ftgo-security-lib

Shared Spring Security configuration library for FTGO platform microservices.

## Features

- **Base SecurityFilterChain**: Secure-by-default configuration with stateless session management
- **CORS Configuration**: Configurable CORS for API gateway integration
- **Exception Handlers**: JSON 401/403 error responses (no HTML login pages)
- **Correlation ID Filter**: Distributed tracing support via `X-Correlation-Id` header
- **Security Utilities**: Convenient static methods for accessing security context
- **JWT Authentication** (EM-40): Token-based authentication with access/refresh tokens
- **Role-Based Authorization** (EM-37): RBAC with role hierarchy and method-level security
- **Custom Permission Evaluator** (EM-37): Resource ownership validation
- **Externalized Configuration**: All settings via `ftgo.security.*` properties

## Usage

### Add Dependency

```groovy
compile project(":shared:ftgo-security-lib")
```

### Import Configuration

```java
@Configuration
@Import(FtgoSecurityAutoConfiguration.class)
public class ServiceSecurityConfiguration {
    // Service-specific customizations here
    // RBAC, method security, and role hierarchy are auto-configured
}
```

### Configuration Properties

```properties
# CORS
ftgo.security.cors.allowed-origins=https://api-gateway.ftgo.com
ftgo.security.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS

# Public paths (no authentication required)
ftgo.security.public-paths=/actuator/health,/actuator/info

# JWT Authentication
ftgo.security.jwt.enabled=true
ftgo.security.jwt.secret=${JWT_SECRET}
ftgo.security.jwt.issuer=ftgo-platform

# Activate security profile
spring.profiles.active=security
```

## Security Defaults

| Feature | Default |
|---------|---------|
| Session Management | Stateless |
| CSRF | Disabled |
| Authentication | HTTP Basic (all endpoints) |
| `/actuator/health` | Public |
| `/actuator/info` | Public |
| Other actuator endpoints | Authenticated |
| All API endpoints | Authenticated |

## Role-Based Authorization (EM-37)

### Roles

| Role | Authority | Description |
|------|-----------|-------------|
| CUSTOMER | `ROLE_CUSTOMER` | End users (orders, restaurants, deliveries) |
| RESTAURANT_OWNER | `ROLE_RESTAURANT_OWNER` | Restaurant managers (inherits CUSTOMER) |
| COURIER | `ROLE_COURIER` | Delivery personnel |
| ADMIN | `ROLE_ADMIN` | Full access (inherits all roles) |

### Method-Level Security

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnly() { }

@PreAuthorize("hasRole('CUSTOMER') and #consumerId == authentication.principal.userId")
public Order createOrder(Long consumerId, ...) { }

@PreAuthorize("hasPermission(#orderId, 'Order', 'VIEW')")
public Order getOrder(Long orderId) { }
```

### Resource Ownership

Implement `ResourceOwnershipStrategy` for domain-specific ownership validation.
Strategies are auto-discovered and registered with the `FtgoPermissionEvaluator`.

See [docs/rbac-authorization.md](../../docs/rbac-authorization.md) for full documentation.
