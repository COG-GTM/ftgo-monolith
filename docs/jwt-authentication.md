# JWT Authentication Flow

## Overview

The FTGO platform uses **JSON Web Tokens (JWT)** for stateless authentication across all microservices. Tokens are issued by an Identity Provider (IdP) — such as Keycloak — and validated by each service using the `ftgo-security-lib` shared library.

This design eliminates the need for session state or inter-service calls to verify identity, which is critical for a distributed microservices architecture.

## Architecture

```
┌─────────┐    (1) Login     ┌──────────────┐
│  Client  │ ───────────────>│  IdP/Keycloak │
│ (SPA/App)│ <───────────────│              │
└────┬─────┘  (2) JWT Tokens └──────────────┘
     │
     │ (3) API Request + Bearer Token
     ▼
┌─────────────────┐
│   API Gateway   │
│  (JWT Filter)   │──── Validates token, forwards to services
└────────┬────────┘
         │ (4) Forwarded JWT
         ▼
┌─────────────────────────────────────────────┐
│            Microservices                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐│
│  │  Order   │ │ Consumer │ │  Restaurant  ││
│  │ Service  │ │ Service  │ │   Service    ││
│  └──────────┘ └──────────┘ └──────────────┘│
│     Each service uses JwtAuthenticationFilter│
│     from ftgo-security-lib to validate JWT  │
└─────────────────────────────────────────────┘
```

## Token Structure

### Access Token

| Claim         | Type       | Description                                        |
|---------------|------------|----------------------------------------------------|
| `sub`         | String     | User ID (unique identifier)                        |
| `roles`       | String[]   | Granted roles (e.g., `ROLE_USER`, `ROLE_ADMIN`)    |
| `permissions` | String[]   | Fine-grained permissions (e.g., `order:read`)      |
| `type`        | String     | Token type: `access`                               |
| `iss`         | String     | Issuer (e.g., `ftgo-platform`)                     |
| `iat`         | Timestamp  | Issued-at time                                     |
| `exp`         | Timestamp  | Expiration time                                    |

### Refresh Token

| Claim | Type      | Description                  |
|-------|-----------|------------------------------|
| `sub` | String    | User ID                      |
| `type`| String    | Token type: `refresh`        |
| `iss` | String    | Issuer                       |
| `iat` | Timestamp | Issued-at time               |
| `exp` | Timestamp | Expiration time (longer TTL) |

## Authentication Flow

### 1. Token Issuance

```
Client → POST /auth/login (credentials)
       ← 200 OK { "accessToken": "eyJ...", "refreshToken": "eyJ..." }
```

The IdP (or a dedicated auth service) validates credentials and returns a token pair.

### 2. Authenticated API Requests

```
Client → GET /orders/123
         Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
       ← 200 OK { order details }
```

The `JwtAuthenticationFilter` in each service:
1. Extracts the token from the `Authorization` header
2. Strips the `Bearer ` prefix
3. Validates signature and expiration via `JwtTokenValidator`
4. Rejects refresh tokens (only access tokens are accepted for API calls)
5. Extracts user ID, roles, and permissions via `JwtClaimsExtractor`
6. Creates a `JwtAuthenticationToken` and sets it in the `SecurityContext`
7. Downstream code accesses the authenticated user via `SecurityContextHolder`

### 3. Token Refresh

```
Client → POST /auth/refresh
         { "refreshToken": "eyJ..." }
       ← 200 OK { "accessToken": "eyJ...", "refreshToken": "eyJ..." }
```

When an access token expires, the client uses the refresh token to obtain new tokens via the `JwtRefreshService`:
1. Validates the refresh token
2. Verifies it is of type `refresh` (not an access token)
3. Extracts the user ID
4. Issues a new access token and refresh token pair

### 4. Token Rejection

```
Client → GET /orders/123
         Authorization: Bearer <expired-or-invalid-token>
       ← 401 Unauthorized
         { "timestamp": "...", "status": 401, "error": "Unauthorized", "message": "...", "path": "/orders/123" }
```

## Library Components

### Configuration (`JwtProperties`)

All JWT settings are externalized via Spring Boot properties:

| Property                               | Type    | Default          | Description                     |
|----------------------------------------|---------|------------------|---------------------------------|
| `ftgo.security.jwt.enabled`            | boolean | `true`           | Enable/disable JWT auth         |
| `ftgo.security.jwt.secret`             | String  | (empty)          | Base64-encoded HMAC-SHA256 key  |
| `ftgo.security.jwt.expiration-ms`      | long    | `3600000` (1h)   | Access token TTL                |
| `ftgo.security.jwt.refresh-expiration-ms` | long | `86400000` (24h) | Refresh token TTL               |
| `ftgo.security.jwt.issuer`             | String  | `ftgo-platform`  | Token issuer claim              |
| `ftgo.security.jwt.header`             | String  | `Authorization`  | HTTP header for the token       |
| `ftgo.security.jwt.token-prefix`       | String  | `Bearer `        | Token prefix in the header      |

### Token Provider (`JwtTokenProvider`)

Creates signed JWT tokens:
- `createAccessToken(userId, roles, permissions)` — standard access token
- `createAccessToken(userId, roles, permissions, customClaims)` — with additional claims
- `createRefreshToken(userId)` — refresh token with longer TTL

### Token Validator (`JwtTokenValidator`)

Validates tokens and extracts claims:
- `validateAndExtractClaims(token)` — returns `Claims` or `null`
- `isValid(token)` — boolean check
- `isRefreshToken(claims)` / `isAccessToken(claims)` — token type checks

### Claims Extractor (`JwtClaimsExtractor`)

Static utility for typed claim extraction:
- `getUserId(claims)` — `sub` claim
- `getRoles(claims)` — `roles` claim as `List<String>`
- `getPermissions(claims)` — `permissions` claim as `List<String>`
- `getTokenType(claims)` — `type` claim
- `getIssuer(claims)` — `iss` claim
- `getExpiration(claims)` / `getIssuedAt(claims)` — date claims

### Authentication Filter (`JwtAuthenticationFilter`)

A `OncePerRequestFilter` that:
1. Extracts the Bearer token from the configured header
2. Validates and parses claims
3. Builds Spring Security `GrantedAuthority` list from roles + permissions
4. Sets a `JwtAuthenticationToken` in the `SecurityContext`

### Authentication Token (`JwtAuthenticationToken`)

Spring Security `Authentication` implementation providing:
- `getUserId()` — the authenticated user's ID
- `getRoles()` — role list
- `getPermissions()` — permission list
- Standard `getAuthorities()` — combined roles and permissions

### Refresh Service (`JwtRefreshService`)

Handles token refresh:
- `refresh(refreshToken, roles, permissions)` — returns a `TokenPair`
- `refresh(refreshToken)` — returns a `TokenPair` with empty roles/permissions

## Auto-Configuration

All JWT beans are auto-configured via `spring.factories` when the library is on the classpath. The auto-configuration:

- Is enabled by default (`ftgo.security.jwt.enabled=true`)
- Registers `JwtTokenProvider`, `JwtTokenValidator`, `JwtAuthenticationFilter`, and `JwtRefreshService`
- All beans are `@ConditionalOnMissingBean` — services can override any component
- The `JwtAuthenticationFilter` is automatically wired into the `FtgoWebSecurityConfiguration` filter chain

## Usage in Services

### 1. Add the dependency

```groovy
dependencies {
    compile project(":shared-libraries:ftgo-security-lib")
}
```

### 2. Configure the JWT secret

In the service's `application.properties`:

```properties
ftgo.security.jwt.secret=${FTGO_JWT_SECRET}
```

### 3. Access the authenticated user in code

```java
@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        // Option 1: Via SecurityContextHolder
        JwtAuthenticationToken auth = (JwtAuthenticationToken)
            SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getUserId();

        // Option 2: Via SecurityContextHelper
        String username = SecurityContextHelper.getCurrentUsername();

        // ...
    }
}
```

## Testing

The library includes `JwtTestSupport` for integration tests:

```java
import net.chrisrichardson.ftgo.security.test.JwtTestSupport;

public class OrderControllerTest {

    private final JwtTestSupport jwt = JwtTestSupport.withDefaults();

    @Test
    public void getOrder_authenticated_returnsOrder() throws Exception {
        String token = jwt.createAccessTokenWithRole("user-123", "ROLE_USER");

        mockMvc.perform(get("/orders/1")
                .header("Authorization", jwt.bearerHeader(token)))
            .andExpect(status().isOk());
    }

    @Test
    public void getOrder_noToken_returns401() throws Exception {
        mockMvc.perform(get("/orders/1"))
            .andExpect(status().isUnauthorized());
    }
}
```

## IdP Integration (Keycloak)

A Keycloak realm template is provided at:

```
shared-libraries/ftgo-security-lib/src/main/resources/idp/keycloak-realm-template.json
```

This template defines:
- **Realm**: `ftgo` with recommended security settings
- **Roles**: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_RESTAURANT_OWNER`, `ROLE_COURIER`
- **Clients**: `ftgo-api-gateway` (confidential) and `ftgo-web-app` (public SPA)
- **Client scopes**: Maps realm roles into the JWT `roles` claim
- **Test user**: `testuser` / `changeme` (temporary password)

An example Spring Boot properties file for Keycloak integration is at:

```
shared-libraries/ftgo-security-lib/src/main/resources/idp/application-keycloak.properties
```

## Security Considerations

1. **Secret management**: Never commit the JWT secret to source control. Use environment variables or a secrets manager (e.g., Kubernetes Secrets, HashiCorp Vault).

2. **Token expiration**: Access tokens have a short TTL (default 1 hour) to limit the window of compromise. Refresh tokens have a longer TTL (default 24 hours).

3. **Refresh token rotation**: Each refresh issues a new refresh token, invalidating the old one. This limits the impact of a stolen refresh token.

4. **Algorithm**: HMAC-SHA256 (symmetric) is used for service-to-service trust within the platform. For external IdP integration with asymmetric keys (RS256), use Spring Security OAuth2 Resource Server.

5. **Transport security**: All tokens must be transmitted over HTTPS in production.

6. **Token type validation**: The `JwtAuthenticationFilter` rejects refresh tokens used for API access, preventing token type confusion attacks.

## Design Decisions

1. **JJWT 0.9.1** was chosen for Java 8 compatibility. The newer JJWT 0.11+ requires Java 11+.

2. **Symmetric signing (HS256)** is used because all services are within the same trust boundary. When integrating with an external IdP, services should validate tokens using the IdP's public key (RS256/ES256).

3. **`OncePerRequestFilter`** ensures the JWT filter runs exactly once per request, even for forwarded/included requests.

4. **`@ConditionalOnMissingBean`** on all JWT beans allows services to override any component without disabling the entire auto-configuration.

5. **`@Autowired(required = false)`** for the JWT filter in `FtgoWebSecurityConfiguration` ensures the base security configuration works even if JWT is disabled.
