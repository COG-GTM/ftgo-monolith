# JWT-Based Authentication — FTGO Platform

> **EM-40** · Implements stateless JWT authentication for the FTGO microservices platform.

---

## Overview

The FTGO platform uses **JSON Web Tokens (JWT)** for stateless authentication across
independently deployed microservices. Each service validates tokens locally using a
shared HMAC-SHA256 signing key — no centralised session store is required.

### Why JWT?

| Benefit        | Detail                                                         |
|----------------|----------------------------------------------------------------|
| **Stateless**  | No shared session store needed between services                |
| **Self-contained** | Token carries user claims (id, roles, permissions)         |
| **Scalable**   | Each service validates tokens independently                    |
| **Standard**   | Well-supported by Spring Security and ecosystem libraries      |

---

## Architecture

```
                         ┌───────────────┐
  Client                 │  Auth Service  │
  ──────► POST /auth ──► │  (or Gateway)  │
                         │  Issues JWT    │
                         └───────┬───────┘
                                 │ access_token + refresh_token
                                 ▼
              ┌──────────────────────────────────────┐
              │         API Gateway / Client          │
              │  Authorization: Bearer <access_token>  │
              └──────────────────────┬───────────────┘
                                     │
         ┌───────────────┬───────────┼───────────┬──────────────┐
         ▼               ▼           ▼           ▼              ▼
   ┌──────────┐   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
   │  Order   │   │ Consumer │ │Restaurant│ │ Courier  │ │  ...     │
   │ Service  │   │ Service  │ │ Service  │ │ Service  │ │          │
   └──────────┘   └──────────┘ └──────────┘ └──────────┘ └──────────┘
   Each service validates the JWT independently using the shared signing key.
```

---

## Token Structure

### Access Token (short-lived, default 15 min)

| Claim          | Type       | Description                              |
|----------------|------------|------------------------------------------|
| `sub`          | String     | Username / email                         |
| `iss`          | String     | Issuer (`ftgo-platform`)                 |
| `iat`          | Timestamp  | Issued-at time                           |
| `exp`          | Timestamp  | Expiration time                          |
| `userId`       | Long       | Numeric user identifier                  |
| `roles`        | String[]   | Granted roles (e.g. `ROLE_USER`)         |
| `permissions`  | String[]   | Fine-grained perms (e.g. `order:create`) |
| `type`         | String     | `access`                                 |

### Refresh Token (long-lived, default 7 days)

| Claim   | Type      | Description              |
|---------|-----------|--------------------------|
| `sub`   | String    | Username / email         |
| `iss`   | String    | Issuer                   |
| `iat`   | Timestamp | Issued-at time           |
| `exp`   | Timestamp | Expiration time          |
| `type`  | String    | `refresh`                |

---

## Token Lifecycle

```
1. AUTHENTICATE
   Client ──► POST /auth/login  { username, password }
   Server ◄── 200 OK  { access_token, refresh_token, token_type, expires_in }

2. ACCESS PROTECTED RESOURCES
   Client ──► GET /api/orders
              Authorization: Bearer <access_token>
   Server ◄── 200 OK  (if valid)
   Server ◄── 401 Unauthorized  (if invalid / expired)

3. REFRESH (before access token expires)
   Client ──► POST /auth/refresh  { refresh_token }
   Server ◄── 200 OK  { access_token, refresh_token, token_type, expires_in }
              (old refresh token is rotated — one-time use)

4. LOGOUT (client-side)
   Client discards both tokens.
   Optional: server-side token blacklist (future enhancement).
```

---

## Configuration

### Enabling JWT in a Microservice

Add the following to the service's `application.properties`:

```properties
# Enable JWT authentication
ftgo.security.jwt.enabled=true

# Signing secret — MUST be injected via environment variable
ftgo.security.jwt.secret=${JWT_SECRET}

# Token issuer (must match across all services)
ftgo.security.jwt.issuer=ftgo-platform

# Access token lifetime (ISO-8601 duration)
ftgo.security.jwt.access-token-expiration=PT15M

# Refresh token lifetime
ftgo.security.jwt.refresh-token-expiration=P7D
```

### Environment Variables

| Variable       | Required | Description                                |
|----------------|----------|--------------------------------------------|
| `JWT_SECRET`   | Yes      | Base64 or UTF-8 HMAC-SHA256 signing key (≥ 32 bytes) |

> **Security:** Never commit the signing secret to source control. Use a secrets
> manager (Vault, AWS Secrets Manager, K8s Secrets) to inject it at runtime.

### Fallback Behaviour

When `ftgo.security.jwt.enabled=false` (default), the security configuration
falls back to **HTTP Basic authentication** (from EM-39). This allows gradual
migration — services can adopt JWT independently.

---

## Accessing User Context

Once authenticated, user information is available via `SecurityUtils`:

```java
import com.ftgo.security.util.SecurityUtils;

// In any service-layer bean:
Optional<Long> userId    = SecurityUtils.getCurrentUserId();
Optional<String> username = SecurityUtils.getCurrentUsername();
List<String> roles       = SecurityUtils.getCurrentRoles();
List<String> permissions = SecurityUtils.getCurrentPermissions();

// Permission check
if (SecurityUtils.hasPermission("order:create")) { ... }

// Role check (Spring Security standard)
if (SecurityUtils.hasAuthority("ROLE_ADMIN")) { ... }
```

---

## Key Components

| Class                          | Package                    | Responsibility                             |
|--------------------------------|----------------------------|--------------------------------------------|
| `JwtProperties`                | `com.ftgo.security.jwt`   | Configuration properties (`ftgo.security.jwt.*`) |
| `JwtTokenProvider`             | `com.ftgo.security.jwt`   | Token generation, parsing, validation       |
| `JwtTokenService`              | `com.ftgo.security.jwt`   | High-level lifecycle (issue, refresh)       |
| `JwtAuthenticationFilter`      | `com.ftgo.security.jwt`   | Servlet filter — extracts Bearer token      |
| `JwtUserDetails`               | `com.ftgo.security.jwt`   | `UserDetails` impl backed by JWT claims     |
| `JwtTokenResponse`             | `com.ftgo.security.jwt`   | Token response DTO (OAuth 2.0 style)        |
| `JwtAutoConfiguration`         | `com.ftgo.security.jwt`   | Auto-config (conditional on `jwt.enabled`)  |
| `InvalidRefreshTokenException` | `com.ftgo.security.jwt`   | Thrown on invalid refresh attempts           |

---

## Security Considerations

1. **Key Management** — The HMAC-SHA256 secret must be at least 256 bits.
   Rotate keys periodically. Consider asymmetric signing (RS256) for
   production environments with an external IdP.

2. **Token Rotation** — Refresh tokens are rotated on each use (a new
   refresh token is issued alongside the new access token). This limits
   the window for token theft.

3. **Transport Security** — Always use HTTPS. Tokens in the `Authorization`
   header are visible in plaintext over HTTP.

4. **Token Revocation** — The current implementation is stateless and does
   not support immediate revocation. For critical scenarios, consider:
   - Short access token lifetimes (15 min default)
   - Server-side refresh token blacklist (future enhancement)
   - Token versioning per user

5. **CSRF** — CSRF protection is disabled because the API is stateless
   (no cookies). Tokens are transmitted via the `Authorization` header.

---

## Testing

### Unit Tests

```bash
./gradlew :shared:ftgo-security-lib:test
```

Tests cover:
- Access token generation and claim extraction
- Refresh token generation
- Token validation (expired, wrong signature, wrong issuer, malformed)
- Token refresh flow (happy path + error cases)
- Spring Security `Authentication` construction from JWT

### Integration Tests

`JwtAuthenticationIntegrationTest` boots a full Spring context with the JWT
filter chain enabled and verifies:
- Valid tokens grant access to protected endpoints
- Invalid / expired tokens return 401
- Refresh tokens are rejected for API access
- User context is correctly propagated via `SecurityContextHolder`
- Public endpoints (actuator health) remain accessible without tokens

---

## Future Enhancements

- **External IdP Integration** — Replace self-issued tokens with
  Keycloak or Auth0 (Spring Security OAuth2 Resource Server).
- **Asymmetric Signing** — Switch from HMAC-SHA256 to RS256 / ES256
  for zero-trust key distribution.
- **Token Blacklisting** — Redis-backed blacklist for immediate revocation.
- **Scope / Audience Validation** — Restrict tokens to specific services.
