# JWT Authentication - Architecture and Token Lifecycle

## Overview

The FTGO microservices platform uses JWT (JSON Web Token) based authentication to provide
stateless, scalable authentication across independently deployed services. Each microservice
validates tokens independently without relying on shared server-side sessions.

## Architecture

```
                                    ┌─────────────────┐
                                    │    Keycloak      │
                                    │  (Token Issuer)  │
                                    │                  │
                                    │  /realms/ftgo/   │
                                    │  protocol/       │
                                    │  openid-connect/ │
                                    └────────┬─────────┘
                                             │
                          ┌──────────────────┼──────────────────┐
                          │  1. Login         │  2. Token        │
                          │  (credentials)    │  (JWT pair)      │
                          ▼                   ▼                  │
                    ┌───────────┐     ┌───────────────┐          │
                    │  Frontend │────▶│  API Gateway   │          │
                    │   (SPA)   │     │                │          │
                    └───────────┘     │  JWT Filter    │          │
                                      │  (validates &  │          │
                                      │   forwards)    │          │
                                      └───────┬────────┘          │
                                              │                   │
                          ┌───────────────────┼──────────────────┐│
                          │                   │                  ││
                          ▼                   ▼                  ▼│
                   ┌──────────┐       ┌──────────┐       ┌──────────┐
                   │  Order   │       │ Consumer │       │Restaurant│
                   │ Service  │       │ Service  │       │ Service  │
                   │          │       │          │       │          │
                   │ JWT Auth │       │ JWT Auth │       │ JWT Auth │
                   │ Filter   │       │ Filter   │       │ Filter   │
                   └──────────┘       └──────────┘       └──────────┘
```

## Components

### 1. Token Issuer - Keycloak

Keycloak serves as the OAuth2/OpenID Connect identity provider:

- **Realm**: `ftgo` - Dedicated realm for the FTGO platform
- **Token endpoint**: `/realms/ftgo/protocol/openid-connect/token`
- **JWKS endpoint**: `/realms/ftgo/protocol/openid-connect/certs`
- **Discovery**: `/realms/ftgo/.well-known/openid-configuration`

**Clients configured in Keycloak:**

| Client | Type | Purpose |
|--------|------|---------|
| `ftgo-api-gateway` | Confidential | API Gateway handles token exchange |
| `ftgo-services` | Bearer-only | Backend services validate tokens only |
| `ftgo-frontend` | Public (PKCE) | Frontend SPA authentication |

**Roles:**

| Role | Description |
|------|-------------|
| `ADMIN` | Full system administration |
| `USER` | Standard customer access |
| `RESTAURANT_OWNER` | Restaurant management |
| `COURIER` | Delivery courier access |

### 2. Shared JWT Library (`shared/ftgo-jwt`)

The `ftgo-jwt` shared library provides JWT authentication for all microservices:

- **`JwtTokenService`** - Core service for token encoding, decoding, validation, and refresh
- **`JwtTokenAuthenticationFilter`** - Servlet filter that validates tokens and populates SecurityContext
- **`FtgoUserContext`** - Immutable model representing the authenticated user
- **`JwtUserContextHolder`** - Thread-local holder for accessing user context in service layer
- **`FtgoJwtProperties`** - Externalized configuration under `ftgo.jwt.*`
- **`FtgoJwtAutoConfiguration`** - Spring Boot auto-configuration

### 3. API Gateway JWT Filter

The API Gateway (`infrastructure/api-gateway`) has its own reactive JWT filter that:
1. Validates incoming tokens
2. Extracts user identity (subject, roles)
3. Forwards identity as headers (`X-User-Id`, `X-User-Roles`) to downstream services

## Token Lifecycle

### 1. Authentication (Login)

```
Client                    API Gateway              Keycloak
  │                           │                       │
  │  POST /auth/login         │                       │
  │  {username, password}     │                       │
  │ ─────────────────────────▶│                       │
  │                           │  POST /token          │
  │                           │  grant_type=password  │
  │                           │ ─────────────────────▶│
  │                           │                       │
  │                           │  {access_token,       │
  │                           │   refresh_token}      │
  │                           │ ◀─────────────────────│
  │  {access_token,           │                       │
  │   refresh_token,          │                       │
  │   expires_at}             │                       │
  │ ◀─────────────────────────│                       │
```

### 2. Authenticated Request

```
Client                    API Gateway              Service
  │                           │                       │
  │  GET /api/orders          │                       │
  │  Authorization: Bearer    │                       │
  │  <access_token>           │                       │
  │ ─────────────────────────▶│                       │
  │                           │  Validate JWT         │
  │                           │  Extract claims       │
  │                           │                       │
  │                           │  GET /api/orders      │
  │                           │  X-User-Id: user-123  │
  │                           │  X-User-Roles: USER   │
  │                           │ ─────────────────────▶│
  │                           │                       │
  │                           │  Validate JWT (again) │
  │                           │  Populate SecurityCtx │
  │                           │                       │
  │  200 OK                   │  200 OK               │
  │  {orders: [...]}          │  {orders: [...]}      │
  │ ◀─────────────────────────│◀──────────────────────│
```

### 3. Token Refresh

```
Client                    API Gateway              Keycloak
  │                           │                       │
  │  POST /auth/refresh       │                       │
  │  {refresh_token}          │                       │
  │ ─────────────────────────▶│                       │
  │                           │  Validate refresh     │
  │                           │  token locally        │
  │                           │                       │
  │                           │  POST /token          │
  │                           │  grant_type=          │
  │                           │  refresh_token        │
  │                           │ ─────────────────────▶│
  │                           │                       │
  │                           │  {new_access_token,   │
  │                           │   new_refresh_token}  │
  │                           │ ◀─────────────────────│
  │  {access_token,           │                       │
  │   refresh_token,          │                       │
  │   expires_at}             │                       │
  │ ◀─────────────────────────│                       │
```

### 4. Token Expiration

```
Client                    Service
  │                          │
  │  GET /api/orders         │
  │  Authorization: Bearer   │
  │  <expired_token>         │
  │ ────────────────────────▶│
  │                          │
  │                          │  Validate JWT
  │                          │  → ExpiredJwtException
  │                          │
  │  401 Unauthorized        │
  │  {error: "Token has      │
  │   expired"}              │
  │ ◀────────────────────────│
```

## JWT Token Structure

### Access Token Claims

```json
{
  "sub": "user-123",
  "iss": "https://keycloak.ftgo.com/realms/ftgo",
  "iat": 1704067200,
  "exp": 1704070800,
  "jti": "unique-token-id",
  "username": "john.doe",
  "roles": ["USER", "ADMIN"],
  "permissions": ["order:read", "order:write"],
  "token_type": "access"
}
```

### Refresh Token Claims

```json
{
  "sub": "user-123",
  "iss": "https://keycloak.ftgo.com/realms/ftgo",
  "iat": 1704067200,
  "exp": 1704153600,
  "jti": "unique-refresh-token-id",
  "username": "john.doe",
  "roles": ["USER", "ADMIN"],
  "permissions": ["order:read", "order:write"],
  "token_type": "refresh"
}
```

## Configuration

### Application Properties

```yaml
ftgo:
  jwt:
    # Token issuer (Keycloak realm URL)
    issuer: https://keycloak.ftgo.com/realms/ftgo

    # HMAC secret for token signing (via environment variable)
    secret: ${JWT_SECRET}

    # Token expiration (seconds)
    expiration-seconds: 3600          # 1 hour for access tokens
    refresh-expiration-seconds: 86400 # 24 hours for refresh tokens

    # Clock skew tolerance (seconds)
    clock-skew-seconds: 30

    # JWT is enabled by default
    enabled: true

    # Paths excluded from JWT validation
    excluded-paths:
      - /actuator/health
      - /actuator/health/**
      - /actuator/info

    # Custom claim names (if Keycloak uses different names)
    roles-claim: roles
    permissions-claim: permissions
    user-id-claim: sub
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | HMAC signing key (min 32 bytes) | Yes (prod) |
| `KEYCLOAK_URL` | Keycloak base URL | Yes |
| `KEYCLOAK_REALM` | Keycloak realm name | Yes |

## Security Considerations

1. **Never hardcode signing keys** - Always use environment variables or Kubernetes Secrets
2. **Use RSA/EC in production** - HMAC is suitable for development; production should use asymmetric keys via JWKS
3. **Short-lived access tokens** - Default 1 hour; adjust based on security requirements
4. **Secure refresh tokens** - Store securely on client side; use HTTP-only cookies when possible
5. **Token revocation** - For immediate revocation, implement a token blacklist or use short expiration with refresh
6. **HTTPS only** - Always transmit tokens over TLS in production
7. **Validate all claims** - Check issuer, expiration, and signature on every request

## Service Layer Usage

### Accessing User Context

```java
// Option 1: Via JwtUserContextHolder (FTGO-specific)
FtgoUserContext user = JwtUserContextHolder.requireCurrentUser();
String userId = user.getUserId();
boolean isAdmin = user.hasRole("ADMIN");

// Option 2: Via Spring SecurityContextHolder
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userId = auth.getName(); // Returns the JWT subject (user ID)
boolean isAdmin = auth.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

// Option 3: Via SecurityUtils (from ftgo-security library)
Optional<String> username = SecurityUtils.getCurrentUsername();
boolean hasRole = SecurityUtils.hasRole("ADMIN");
```

## Kubernetes Deployment

### Keycloak

```bash
# Deploy Keycloak and PostgreSQL
kubectl apply -k infrastructure/kubernetes/base/keycloak/

# Verify deployment
kubectl get pods -n ftgo -l app=keycloak

# Access admin console (port-forward for local access)
kubectl port-forward -n ftgo svc/keycloak 8080:8080

# Open: http://localhost:8080/admin
# Default credentials: admin / CHANGE_ME_IN_PRODUCTION
```

### Configuring Services

Each microservice that includes the `ftgo-jwt` dependency will automatically:
1. Register the JWT authentication filter
2. Validate tokens on incoming requests
3. Populate the Spring Security context
4. Make user context available via `JwtUserContextHolder`

No additional configuration is needed beyond setting the `JWT_SECRET` environment variable.

## Testing

### Running JWT Tests

```bash
# Run all JWT module tests
./gradlew :shared:ftgo-jwt:test

# Run specific test class
./gradlew :shared:ftgo-jwt:test --tests "net.chrisrichardson.ftgo.jwt.JwtTokenServiceTest"

# Run integration tests
./gradlew :shared:ftgo-jwt:test --tests "net.chrisrichardson.ftgo.jwt.JwtAuthenticationIntegrationTest"
```

### Test Coverage

The JWT module includes tests for:
- Token generation (access and refresh tokens)
- Token validation (valid, expired, malformed, tampered)
- Claims extraction (userId, roles, permissions)
- Token refresh flow
- User context propagation
- Properties configuration
- Integration with Spring Security filter chain
