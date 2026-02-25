# ftgo-security-lib

Shared Spring Security configuration library for FTGO platform microservices.

## Features

- **Base SecurityFilterChain**: Secure-by-default configuration with stateless session management
- **CORS Configuration**: Configurable CORS for API gateway integration
- **Exception Handlers**: JSON 401/403 error responses (no HTML login pages)
- **Correlation ID Filter**: Distributed tracing support via `X-Correlation-Id` header
- **Security Utilities**: Convenient static methods for accessing security context
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
}
```

### Configuration Properties

```properties
# CORS
ftgo.security.cors.allowed-origins=https://api-gateway.ftgo.com
ftgo.security.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS

# Public paths (no authentication required)
ftgo.security.public-paths=/actuator/health,/actuator/info

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
