# FTGO Security Library

Shared Spring Security configuration for FTGO microservices.

## Features

- Base `SecurityFilterChain` configuration with sensible defaults
- Stateless session management for microservices
- CORS configuration for API gateway
- CSRF disabled for stateless REST APIs
- Actuator endpoint security (`/health` public, others secured)
- Security exception handlers (401/403 JSON responses)
- Security utility classes

## Usage

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared-libraries:ftgo-security")
}
```

The library auto-configures via `@AutoConfiguration`. Override defaults with application properties:

```yaml
ftgo:
  security:
    cors:
      allowed-origins: http://localhost:3000
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600
    public-paths:
      - /actuator/health
      - /actuator/info
      - /swagger-ui/**
      - /v3/api-docs/**
```
