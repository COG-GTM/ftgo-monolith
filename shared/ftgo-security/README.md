# FTGO Security Library

Shared Spring Security configuration library for FTGO microservices.

## Overview

This library provides a base security configuration that can be included in any FTGO microservice to enable consistent security defaults across the platform.

## Features

- **Base SecurityFilterChain**: All REST endpoints require authentication by default
- **CORS Configuration**: Configurable cross-origin resource sharing for API gateway
- **CSRF Protection**: Disabled for stateless REST APIs
- **Actuator Security**: Health and info endpoints public, all others secured
- **Exception Handlers**: Structured JSON error responses for 401/403 errors
- **Utility Classes**: Convenient security context access methods
- **Profile Support**: Development, production, and base security profiles

## Usage

Add as a dependency in your microservice's `build.gradle`:

```groovy
implementation project(':shared:ftgo-security')
```

The security configuration will be auto-configured via Spring Boot's `spring.factories` mechanism.

## Configuration

Configure via `application.yml`:

```yaml
ftgo:
  security:
    public-paths:
      - /actuator/health
      - /actuator/info
      - /api/public/**
    cors:
      allowed-origins:
        - http://localhost:3000
        - https://api-gateway.ftgo.com
    actuator:
      public-endpoints:
        - health
        - info
```

## Security Profiles

- `security` - Base security profile with sensible defaults
- `security-dev` - Relaxed settings for local development
- `security-prod` - Strict settings for production deployment

## Components

| Class | Description |
|-------|-------------|
| `FtgoSecurityAutoConfiguration` | Auto-configuration entry point |
| `FtgoSecurityFilterChainConfig` | Base security filter chain |
| `FtgoCorsConfig` | CORS configuration |
| `FtgoSecurityProperties` | Configurable security properties |
| `SecurityExceptionHandler` | Global security exception handler |
| `SecurityUtils` | Security context utility methods |
