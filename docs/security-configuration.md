# FTGO Security Configuration

## Overview

The `ftgo-security-lib` shared library provides a centralized Spring Security 6.x configuration for all FTGO microservices. It establishes a consistent security posture across services while allowing per-service customization.

## Architecture

```
shared/ftgo-security-lib/
├── src/main/java/com/ftgo/security/
│   ├── config/
│   │   ├── FtgoSecurityAutoConfiguration.java   # Auto-configuration entry point
│   │   ├── FtgoBaseSecurityConfig.java           # Main SecurityFilterChain
│   │   ├── FtgoCorsConfig.java                   # CORS configuration
│   │   ├── FtgoActuatorSecurityConfig.java       # Actuator endpoint security
│   │   └── FtgoSecurityProperties.java           # Externalized properties
│   ├── handler/
│   │   ├── FtgoAuthenticationEntryPoint.java     # 401 JSON responses
│   │   └── FtgoAccessDeniedHandler.java          # 403 JSON responses
│   └── util/
│       └── SecurityUtils.java                    # Security helper utilities
├── src/main/resources/
│   ├── META-INF/spring/...AutoConfiguration.imports  # Spring Boot auto-config
│   └── application-security.properties               # Default security properties
└── src/test/
    ├── java/com/ftgo/security/
    │   ├── config/
    │   │   ├── FtgoBaseSecurityConfigTest.java   # SecurityFilterChain tests
    │   │   └── FtgoCorsConfigTest.java           # CORS tests
    │   └── util/
    │       └── SecurityUtilsTest.java            # Utility tests
    └── resources/
        └── application.properties                # Test configuration
```

## Security Posture

### Session Management
- **Stateless** — No HTTP sessions are created or used (`SessionCreationPolicy.STATELESS`)
- Suitable for token-based authentication (JWT, OAuth2)

### CSRF Protection
- **Disabled** for all endpoints
- Appropriate for stateless REST APIs that use token-based authentication
- CSRF protection is unnecessary when no session cookies are used

### Endpoint Security

| Endpoint Pattern | Access | Notes |
|---|---|---|
| `/actuator/health` | Public | Kubernetes liveness/readiness probes |
| `/actuator/health/**` | Public | Health detail endpoints |
| `/actuator/info` | Public | Application info |
| `/actuator/metrics` | Authenticated | Metrics data |
| `/actuator/env` | Authenticated | Environment properties |
| `/actuator/**` (others) | Authenticated | All other actuator endpoints |
| `/v3/api-docs/**` | Public | OpenAPI documentation |
| `/swagger-ui/**` | Public | Swagger UI |
| `/swagger-ui.html` | Public | Swagger UI entry point |
| All other endpoints | Authenticated | API endpoints |

### CORS Configuration
Configured via application properties:

| Property | Default | Description |
|---|---|---|
| `ftgo.security.cors.allowed-origins` | `http://localhost:3000,http://localhost:8080` | Allowed CORS origins |
| `ftgo.security.cors.allowed-methods` | `GET,POST,PUT,DELETE,PATCH,OPTIONS` | Allowed HTTP methods |
| `ftgo.security.cors.allowed-headers` | `*` | Allowed request headers |
| `ftgo.security.cors.max-age` | `3600` | Pre-flight cache duration (seconds) |

### Error Responses
Security failures return structured JSON responses:

**401 Unauthorized:**
```json
{
  "timestamp": "2026-03-04T20:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource",
  "path": "/api/orders"
}
```

**403 Forbidden:**
```json
{
  "timestamp": "2026-03-04T20:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/admin/users"
}
```

## Usage

### Adding to a Microservice

1. Add the dependency to the service's `build.gradle`:
   ```groovy
   compile project(':shared-ftgo-security-lib')
   ```

2. Add security properties to `application.properties`:
   ```properties
   spring.profiles.include=security
   ftgo.security.cors.allowed-origins=http://localhost:3000,http://localhost:8080
   ```

3. (Optional) Add test dependency:
   ```groovy
   testCompile "org.springframework.security:spring-security-test:6.2.4"
   ```

### Customizing Security per Service

Services can override the default security configuration by defining their own `SecurityFilterChain` bean:

```java
@Configuration
public class OrderServiceSecurityConfig {

    @Bean
    @Order(90) // Higher priority than default (100)
    public SecurityFilterChain orderServiceFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/orders/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/orders/public/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### Adding Public Paths

Use the `ftgo.security.public-paths` property to add service-specific public paths:
```properties
ftgo.security.public-paths=/api/public/**,/webhooks/**
```

### Security Utilities

Use `SecurityUtils` for common security operations:

```java
import com.ftgo.security.util.SecurityUtils;

// Get current username
Optional<String> username = SecurityUtils.getCurrentUsername();

// Check authentication
if (SecurityUtils.isAuthenticated()) {
    // ...
}

// Check specific authority
if (SecurityUtils.hasAuthority("ROLE_ADMIN")) {
    // ...
}
```

## Version Catalog

Security dependencies are managed in `gradle/libs.versions.toml`:

```toml
[versions]
spring-security = "6.2.4"

[libraries]
spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security", version.ref = "spring-boot" }
spring-security-test = { module = "org.springframework.security:spring-security-test", version.ref = "spring-security" }
```

And mirrored in `FtgoVersions.groovy`:
```groovy
static final String SPRING_SECURITY = '6.2.4'
```

## Future Enhancements

- **JWT Authentication**: Add JWT token validation filter
- **OAuth2 Resource Server**: Integrate with OAuth2 authorization server
- **Method-Level Security**: Enable `@PreAuthorize` / `@PostAuthorize` annotations
- **Rate Limiting**: Add request rate limiting filter
- **Security Auditing**: Add security event logging and auditing
