# ftgo-security

Shared Spring Security configuration library providing consistent authentication and security defaults across all FTGO microservices.

## Features

- Base `SecurityFilterChain` with REST API defaults (stateless sessions, permit actuator/swagger)
- Configurable CORS policy for cross-service communication
- CSRF protection disabled by default for stateless REST APIs
- Spring Boot auto-configuration for zero-config setup

## Usage

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-security')
}
```

The security configuration is auto-configured via Spring Boot's `AutoConfiguration` mechanism. No additional setup is required for default behavior.

## Configuration Properties

### Security Properties (`ftgo.security.*`)

| Property | Type | Default | Description |
|---|---|---|---|
| `ftgo.security.csrf-enabled` | `boolean` | `false` | Enable CSRF protection (disable for stateless REST APIs) |
| `ftgo.security.public-paths` | `List<String>` | `[/actuator/**, /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html]` | Paths accessible without authentication |

### CORS Properties (`ftgo.security.cors.*`)

| Property | Type | Default | Description |
|---|---|---|---|
| `ftgo.security.cors.allowed-origins` | `List<String>` | `[*]` | Allowed CORS origins |
| `ftgo.security.cors.allowed-methods` | `List<String>` | `[GET, POST, PUT, DELETE, PATCH, OPTIONS]` | Allowed HTTP methods |
| `ftgo.security.cors.allowed-headers` | `List<String>` | `[*]` | Allowed request headers |
| `ftgo.security.cors.exposed-headers` | `List<String>` | `[]` | Headers exposed to the client |
| `ftgo.security.cors.allow-credentials` | `boolean` | `false` | Allow credentials in CORS requests |
| `ftgo.security.cors.max-age` | `long` | `3600` | Max age (seconds) for preflight cache |

## Example: Custom Configuration

```yaml
ftgo:
  security:
    csrf-enabled: false
    public-paths:
      - /actuator/**
      - /v3/api-docs/**
      - /swagger-ui/**
      - /swagger-ui.html
      - /api/public/**
    cors:
      allowed-origins:
        - http://localhost:3000
        - https://ftgo.example.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allow-credentials: true
      max-age: 7200
```

## Overriding Security Configuration

If a service needs custom security, define its own `SecurityFilterChain` bean. The auto-configuration is conditional and will back off when a custom bean is present (`@ConditionalOnMissingBean`).

```java
@Configuration
@EnableWebSecurity
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain customFilterChain(HttpSecurity http) throws Exception {
        // custom security configuration
        return http.build();
    }
}
```

## Architecture

- `FtgoSecurityAutoConfiguration` - Spring Boot auto-configuration entry point
- `FtgoSecurityConfiguration` - Core security configuration with `SecurityFilterChain` and CORS
- `SecurityProperties` - Configurable security properties (CSRF, public paths)
- `CorsProperties` - Configurable CORS properties
