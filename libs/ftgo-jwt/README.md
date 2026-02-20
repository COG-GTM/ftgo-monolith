# ftgo-jwt

JWT (JSON Web Token) utilities for the FTGO platform, providing token generation, validation, parsing, refresh, and a Spring Security filter integration.

## Features

- **Token Generation** - Create JWTs with configurable claims, subject, roles, and expiration
- **Token Validation** - Validate tokens handling expired, malformed, and invalid signatures
- **Token Parsing** - Extract claims, subject, roles, and expiration from tokens
- **Token Refresh** - Refresh tokens before expiry with configurable refresh window
- **Security Filter** - `JwtAuthenticationFilter` integrates with Spring Security filter chain
- **Auto-Configuration** - Spring Boot auto-configuration with `ftgo.jwt.*` properties

## Configuration

```yaml
ftgo:
  jwt:
    secret: "your-base64-encoded-secret-key-minimum-256-bits"
    expiration: 3600000        # Token expiration in ms (default: 1 hour)
    refresh-threshold: 300000  # Refresh window in ms (default: 5 minutes)
    issuer: "ftgo-platform"    # Token issuer
    header: "Authorization"    # HTTP header name
    prefix: "Bearer "          # Token prefix in header
```

## Usage

### Token Generation

```java
@Autowired
private JwtTokenProvider tokenProvider;

String token = tokenProvider.generateToken("user@example.com", Map.of("roles", List.of("ROLE_USER")));
```

### Token Validation

```java
boolean valid = tokenProvider.validateToken(token);
```

### Token Parsing

```java
Claims claims = tokenProvider.getClaims(token);
String subject = tokenProvider.getSubject(token);
```

### Token Refresh

```java
@Autowired
private JwtTokenRefreshService refreshService;

Optional<String> refreshed = refreshService.refreshToken(token);
```

## Dependencies

- Spring Boot Starter Security
- Spring Boot Starter Web
- JJWT (io.jsonwebtoken) 0.12.5
