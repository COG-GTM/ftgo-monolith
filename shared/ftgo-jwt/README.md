# FTGO JWT Authentication Library

Shared JWT-based authentication library for FTGO microservices. Provides stateless token validation, claims extraction, token refresh, and user context propagation via Spring Security's `SecurityContextHolder`.

## Features

- **JWT Token Service** - Encode, decode, validate, and refresh JWT tokens
- **Authentication Filter** - Servlet filter that validates Bearer tokens and populates security context
- **User Context** - Thread-local holder for accessing authenticated user information in service layer
- **Auto-Configuration** - Spring Boot auto-configuration for seamless integration
- **Externalized Config** - All settings configurable via `ftgo.jwt.*` properties

## Quick Start

1. Add dependency to your service's `build.gradle`:

```groovy
compile project(':shared:ftgo-jwt')
```

2. Configure the JWT secret (via environment variable):

```yaml
ftgo:
  jwt:
    secret: ${JWT_SECRET}
    issuer: https://keycloak.ftgo.com/realms/ftgo
```

3. Access user context in your service:

```java
FtgoUserContext user = JwtUserContextHolder.requireCurrentUser();
String userId = user.getUserId();
boolean isAdmin = user.hasRole("ADMIN");
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.jwt.enabled` | `true` | Enable/disable JWT authentication |
| `ftgo.jwt.secret` | - | HMAC signing key (min 32 bytes) |
| `ftgo.jwt.issuer` | `https://keycloak.ftgo.com/realms/ftgo` | Token issuer URI |
| `ftgo.jwt.expiration-seconds` | `3600` | Access token lifetime |
| `ftgo.jwt.refresh-expiration-seconds` | `86400` | Refresh token lifetime |
| `ftgo.jwt.clock-skew-seconds` | `30` | Clock skew tolerance |
| `ftgo.jwt.excluded-paths` | `/actuator/health,/actuator/info` | Paths excluded from auth |
| `ftgo.jwt.roles-claim` | `roles` | JWT claim name for roles |
| `ftgo.jwt.permissions-claim` | `permissions` | JWT claim name for permissions |

## Testing

```bash
./gradlew :shared:ftgo-jwt:test
```

See [docs/authentication.md](../../docs/authentication.md) for full architecture documentation.
