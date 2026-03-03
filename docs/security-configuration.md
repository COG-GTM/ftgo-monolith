# FTGO Security Configuration

## Overview

The FTGO security library (`shared/ftgo-security`) provides a foundational Spring Security configuration for all FTGO microservices. It establishes consistent security defaults while allowing per-service customization through configuration properties.

## Architecture

```
shared/ftgo-security/
├── src/main/java/net/chrisrichardson/ftgo/security/
│   ├── FtgoSecurityAutoConfiguration.java    # Auto-configuration entry point
│   ├── FtgoSecurityProperties.java           # Configurable properties
│   ├── config/
│   │   ├── FtgoSecurityFilterChainConfig.java # Base security filter chain
│   │   ├── FtgoCorsConfig.java               # CORS configuration
│   │   ├── FtgoAuthenticationEntryPoint.java  # Custom 401 handler
│   │   └── FtgoAccessDeniedHandler.java       # Custom 403 handler
│   ├── exception/
│   │   └── SecurityExceptionHandler.java      # Global exception handler
│   └── util/
│       └── SecurityUtils.java                 # Security context utilities
├── src/main/resources/
│   ├── META-INF/spring.factories             # Auto-configuration registration
│   ├── application-security.yml              # Base security profile
│   ├── application-security-dev.yml          # Development profile
│   └── application-security-prod.yml         # Production profile
└── src/test/
    ├── java/.../security/
    │   ├── FtgoSecurityPropertiesTest.java   # Properties unit tests
    │   ├── SecurityIntegrationTest.java      # Integration tests
    │   └── util/SecurityUtilsTest.java       # Utility unit tests
    └── resources/application.yml             # Test configuration
```

## Security Defaults

### Authentication
- All REST endpoints require authentication by default
- HTTP Basic authentication enabled as a fallback mechanism
- Services can add their own authentication providers (JWT, OAuth2, etc.)

### Authorization
- Default deny-all policy: all endpoints require authentication unless explicitly whitelisted
- Public paths are configurable via `ftgo.security.public-paths`

### Session Management
- Stateless session management (`SessionCreationPolicy.STATELESS`)
- No HTTP sessions are created or used
- Suitable for token-based authentication patterns

### CSRF Protection
- CSRF is disabled for all endpoints
- Appropriate for stateless REST APIs that use token-based authentication
- Tokens (JWT, API keys) provide inherent CSRF protection

### Actuator Endpoints
| Endpoint | Access |
|----------|--------|
| `/actuator/health` | Public |
| `/actuator/health/**` | Public |
| `/actuator/info` | Public |
| All other actuator endpoints | Authenticated |

### CORS Configuration
Default CORS settings allow:
- **Origins**: `http://localhost:3000`, `http://localhost:8080`
- **Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Headers**: Authorization, Content-Type, Accept, Origin, X-Requested-With, X-XSRF-TOKEN
- **Exposed Headers**: Authorization, X-Total-Count
- **Credentials**: Allowed
- **Max Age**: 3600 seconds (1 hour)

## Configuration Properties

All properties are under the `ftgo.security` prefix:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `ftgo.security.public-paths` | `List<String>` | `/actuator/health`, `/actuator/health/**`, `/actuator/info` | URL patterns accessible without authentication |
| `ftgo.security.cors.enabled` | `boolean` | `true` | Whether CORS is enabled |
| `ftgo.security.cors.allowed-origins` | `List<String>` | `localhost:3000`, `localhost:8080` | Allowed CORS origins |
| `ftgo.security.cors.allowed-methods` | `List<String>` | GET, POST, PUT, DELETE, PATCH, OPTIONS | Allowed HTTP methods |
| `ftgo.security.cors.allowed-headers` | `List<String>` | Authorization, Content-Type, etc. | Allowed request headers |
| `ftgo.security.cors.exposed-headers` | `List<String>` | Authorization, X-Total-Count | Response headers exposed to client |
| `ftgo.security.cors.allow-credentials` | `boolean` | `true` | Whether credentials are allowed |
| `ftgo.security.cors.max-age` | `long` | `3600` | Preflight cache duration (seconds) |
| `ftgo.security.cors.pattern` | `String` | `/**` | URL pattern for CORS |
| `ftgo.security.actuator.public-endpoints` | `List<String>` | `health`, `info` | Publicly accessible actuator endpoints |

## Security Profiles

### Base Profile (`security`)
Activate with: `spring.profiles.active=security`

Provides sensible defaults suitable for most environments. Includes the base CORS configuration, actuator endpoint exposure, and standard public paths.

### Development Profile (`security-dev`)
Activate with: `spring.profiles.active=security,security-dev`

Extends the base profile with relaxed settings:
- Additional localhost origins (port 8081, 127.0.0.1)
- All actuator endpoints exposed
- Health details shown always

### Production Profile (`security-prod`)
Activate with: `spring.profiles.active=security,security-prod`

Stricter settings for production:
- Only production domains in CORS allowed origins
- Limited actuator endpoint exposure
- Health details only shown when authorized

## Error Responses

Security errors return structured JSON responses:

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource",
  "path": "/api/orders"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: insufficient permissions",
  "path": "/api/admin/settings"
}
```

## Utility Classes

### SecurityUtils

Static utility class for common security operations:

```java
// Get current authenticated username
Optional<String> username = SecurityUtils.getCurrentUsername();

// Check authentication status
boolean isAuth = SecurityUtils.isAuthenticated();

// Check roles
boolean isAdmin = SecurityUtils.hasRole("ADMIN");

// Get all roles
Collection<String> roles = SecurityUtils.getRoles();
```

## Per-Service Customization

Services can override the base security configuration by creating their own `WebSecurityConfigurerAdapter` with a higher `@Order`:

```java
@Configuration
@Order(90) // Higher priority than the library default
public class OrderServiceSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Custom security for order service
        http.authorizeRequests()
            .antMatchers("/api/orders/public/**").permitAll()
            .anyRequest().authenticated();
    }
}
```

## Future Enhancements

When the microservices migration progresses to Spring Boot 3.x:
- Migrate from `WebSecurityConfigurerAdapter` to `SecurityFilterChain` bean approach
- Add JWT/OAuth2 resource server support
- Integrate with a centralized identity provider
- Add method-level security with `@PreAuthorize`
