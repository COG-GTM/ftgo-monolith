# FTGO Security Configuration

## Overview

The `ftgo-security-lib` shared library provides a base Spring Security configuration for all FTGO microservices. It is designed for **stateless REST APIs** using Spring Boot 2.0.x and Spring Security 5.x.

The monolith currently has zero security. This library establishes a security foundation that each microservice can adopt by adding the library as a dependency.

## Architecture

```
shared-libraries/ftgo-security-lib/
├── build.gradle
└── src/main/java/net/chrisrichardson/ftgo/security/
    ├── config/
    │   ├── FtgoSecurityAutoConfiguration.java   # Auto-config entry point
    │   ├── FtgoWebSecurityConfiguration.java     # Base security filter chain
    │   ├── FtgoCorsConfiguration.java            # CORS policy
    │   └── FtgoActuatorSecurityConfiguration.java # Actuator endpoint security
    ├── handler/
    │   ├── FtgoAuthenticationEntryPoint.java      # JSON 401 responses
    │   ├── FtgoAccessDeniedHandler.java           # JSON 403 responses
    │   └── SecurityExceptionAdvice.java           # @RestControllerAdvice for security exceptions
    └── util/
        ├── SecurityContextHelper.java             # Convenience methods for SecurityContext access
        └── SecurityConstants.java                 # Shared security constants
```

## Features

### Stateless Security (CSRF Disabled)

All microservices are stateless REST APIs that use token-based authentication (e.g., JWT forwarded from the API Gateway). CSRF protection is disabled since there is no server-side session or browser-based form submission.

```java
http.csrf().disable()
    .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
```

### Actuator Endpoint Security

Actuator endpoints are secured with a dedicated `WebSecurityConfigurerAdapter` at `@Order(99)`:

| Endpoint            | Access   | Reason                            |
|---------------------|----------|-----------------------------------|
| `/actuator/health`  | Public   | Kubernetes liveness/readiness probes |
| `/actuator/info`    | Public   | Service metadata                  |
| All other actuator  | Secured  | Sensitive operational data        |

### CORS Configuration

CORS is configurable via application properties. Defaults are permissive for development:

| Property                                  | Default                                              |
|-------------------------------------------|------------------------------------------------------|
| `ftgo.security.cors.allowed-origins`      | `*`                                                  |
| `ftgo.security.cors.allowed-methods`      | `GET,POST,PUT,DELETE,PATCH,OPTIONS`                  |
| `ftgo.security.cors.allowed-headers`      | `Authorization,Content-Type,X-Requested-With,Accept,Origin` |
| `ftgo.security.cors.allow-credentials`    | `false`                                              |
| `ftgo.security.cors.max-age`              | `3600`                                               |

### Security Exception Handlers

Custom handlers return structured JSON error responses instead of Spring's default HTML error pages:

**401 Unauthorized** (via `FtgoAuthenticationEntryPoint`):
```json
{
  "timestamp": "2026-03-16T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource",
  "path": "/orders/123"
}
```

**403 Forbidden** (via `FtgoAccessDeniedHandler`):
```json
{
  "timestamp": "2026-03-16T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access to this resource is denied",
  "path": "/admin/config"
}
```

### Security Utility Classes

**`SecurityContextHelper`** — Static methods for accessing the current security context:
- `getCurrentUsername()` — Returns the authenticated user's name
- `isAuthenticated()` — Checks if a non-anonymous user is present
- `hasAuthority(String)` — Checks if the user has a specific authority/role

**`SecurityConstants`** — Shared constants (header names, role prefixes, default public paths)

## Usage

### Adding to a Microservice

Add the dependency in the service's `build.gradle`:

```groovy
dependencies {
    compile project(":shared-libraries:ftgo-security-lib")
}
```

The auto-configuration (`spring.factories`) will apply the security configuration automatically when Spring Boot starts.

### Customizing Public Paths

Override the default public paths in the service's `application.properties`:

```properties
ftgo.security.public-paths=/actuator/health,/actuator/info,/api/public/**
```

### Overriding Security Configuration

A service can provide its own `WebSecurityConfigurerAdapter` with a custom `@Order` to override or extend the defaults:

```java
@Configuration
@Order(98) // Higher priority than the base config (@Order 100)
public class OrderServiceSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/orders/**")
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/orders/**").permitAll()
                .anyRequest().authenticated();
    }
}
```

### Restricting CORS for Production

In `application-prod.properties`:

```properties
ftgo.security.cors.allowed-origins=https://app.ftgo.com,https://admin.ftgo.com
ftgo.security.cors.allow-credentials=true
```

## Configuration Reference

| Property                                  | Type     | Default                              | Description                       |
|-------------------------------------------|----------|--------------------------------------|-----------------------------------|
| `ftgo.security.public-paths`              | String[] | `/actuator/health,/actuator/info`    | Paths that bypass authentication  |
| `ftgo.security.cors.allowed-origins`      | String[] | `*`                                  | Allowed CORS origins              |
| `ftgo.security.cors.allowed-methods`      | String[] | `GET,POST,PUT,DELETE,PATCH,OPTIONS`  | Allowed HTTP methods              |
| `ftgo.security.cors.allowed-headers`      | String[] | `Authorization,Content-Type,...`     | Allowed request headers           |
| `ftgo.security.cors.allow-credentials`    | boolean  | `false`                              | Whether credentials are allowed   |
| `ftgo.security.cors.max-age`              | long     | `3600`                               | Pre-flight cache duration (secs)  |

## Design Decisions

1. **WebSecurityConfigurerAdapter** is used (not `SecurityFilterChain` bean) because this project uses Spring Boot 2.0.x / Spring Security 5.0.x, which predates the lambda DSL and component-based configuration introduced in Spring Security 5.4+.

2. **Separate actuator adapter** at a higher priority (`@Order(99)`) ensures actuator rules are evaluated before the general API security rules (`@Order(100)`).

3. **Auto-configuration via `spring.factories`** allows services to get security "for free" by adding the dependency, with no manual `@Import` required.

4. **JSON error responses** align with the REST API contract — clients always receive structured JSON, even for authentication/authorization failures.
