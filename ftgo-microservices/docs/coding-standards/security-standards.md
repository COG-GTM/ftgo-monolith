# FTGO Microservices Security Standards

## 1. Authentication Architecture

### JWT Token Structure

All inter-service and client-to-service authentication uses JWT (JSON Web Tokens).

#### Access Token Claims
```json
{
  "sub": "user-id-or-service-id",
  "iss": "ftgo-auth-service",
  "iat": 1700000000,
  "exp": 1700003600,
  "type": "access",
  "roles": ["CONSUMER", "ADMIN"],
  "tenantId": "optional-tenant-id"
}
```

#### Refresh Token Claims
```json
{
  "sub": "user-id-or-service-id",
  "iss": "ftgo-auth-service",
  "iat": 1700000000,
  "exp": 1700604800,
  "type": "refresh"
}
```

#### Token Lifetimes
| Token Type    | Lifetime    | Storage             |
|---------------|-------------|---------------------|
| Access Token  | 1 hour      | Memory / Header     |
| Refresh Token | 7 days      | HttpOnly cookie     |

### Signing Algorithm
- Algorithm: HMAC-SHA256 (HS256) for internal services
- Key size: Minimum 256 bits
- Key rotation: Every 90 days with grace period

---

## 2. Authentication Flow

### Client Authentication Flow
1. Client sends credentials to API Gateway `/auth/login`
2. API Gateway forwards to Auth Service
3. Auth Service validates credentials and issues JWT access + refresh tokens
4. Client includes access token in `Authorization: Bearer <token>` header
5. API Gateway validates token and forwards request with claims to downstream service
6. On token expiry, client uses refresh token to obtain new access token

### Service-to-Service Authentication
1. Each service has a service account with pre-configured credentials
2. Services request access tokens using client credentials grant
3. Tokens include `service` role and the originating service name
4. All inter-service HTTP calls include the service token

---

## 3. Authorization Rules Per Service

### Consumer Service
| Endpoint                    | Method | Required Role        |
|-----------------------------|--------|----------------------|
| `POST /consumers`           | POST   | PUBLIC (registration)|
| `GET /consumers/{id}`       | GET    | CONSUMER (own) or ADMIN |
| `PUT /consumers/{id}`       | PUT    | CONSUMER (own) or ADMIN |

### Restaurant Service
| Endpoint                        | Method | Required Role           |
|---------------------------------|--------|-------------------------|
| `POST /restaurants`             | POST   | RESTAURANT_OWNER, ADMIN |
| `GET /restaurants/{id}`         | GET    | PUBLIC                  |
| `PUT /restaurants/{id}`         | PUT    | RESTAURANT_OWNER (own), ADMIN |
| `GET /restaurants`              | GET    | PUBLIC                  |

### Order Service
| Endpoint                           | Method | Required Role              |
|------------------------------------|--------|----------------------------|
| `POST /orders`                     | POST   | CONSUMER                   |
| `GET /orders/{id}`                 | GET    | CONSUMER (own), RESTAURANT_OWNER, ADMIN |
| `GET /orders?consumerId=`          | GET    | CONSUMER (own), ADMIN      |
| `POST /orders/{id}/cancel`         | POST   | CONSUMER (own), ADMIN      |
| `POST /orders/{id}/accept`         | POST   | RESTAURANT_OWNER, SERVICE  |
| `POST /orders/{id}/preparing`      | POST   | RESTAURANT_OWNER, SERVICE  |
| `POST /orders/{id}/ready`          | POST   | RESTAURANT_OWNER, SERVICE  |
| `POST /orders/{id}/pickedup`       | POST   | COURIER, SERVICE           |
| `POST /orders/{id}/delivered`      | POST   | COURIER, SERVICE           |

### Courier Service
| Endpoint                       | Method | Required Role       |
|--------------------------------|--------|---------------------|
| `POST /couriers`               | POST   | ADMIN               |
| `GET /couriers/{id}`           | GET    | COURIER (own), ADMIN|
| `GET /couriers/available`      | GET    | SERVICE, ADMIN      |
| `PUT /couriers/{id}/available` | PUT    | COURIER (own)       |

---

## 4. API Endpoint Security Requirements

### All Endpoints
- HTTPS required in production (TLS 1.2+)
- Rate limiting at API Gateway: 100 requests/minute per client
- Request size limit: 1MB maximum
- Response headers must include security headers (CSP, X-Frame-Options, etc.)

### Public Endpoints (No Authentication)
- `GET /actuator/health` - Health checks
- `POST /auth/login` - Authentication
- `POST /auth/register` - Registration
- `GET /restaurants` - Restaurant listing
- `GET /restaurants/{id}` - Restaurant details

### Authenticated Endpoints
- All other endpoints require a valid JWT token
- Resource ownership is enforced (users can only access their own data)
- Admin role bypasses ownership checks

---

## 5. Secrets Management

### Development
- Secrets stored in environment variables via `.env` files (not committed to git)
- Docker Compose uses `${VARIABLE:-default}` syntax with safe defaults

### Production
- **Kubernetes Secrets** for database credentials and service tokens
- **External secret stores** (HashiCorp Vault or AWS Secrets Manager) for JWT signing keys
- Secrets rotated on a defined schedule (90 days for keys, 30 days for passwords)

### Secret Categories
| Secret                    | Storage               | Rotation  |
|---------------------------|-----------------------|-----------|
| JWT signing key           | Vault / K8s Secret    | 90 days   |
| Database passwords        | K8s Secret            | 30 days   |
| Service account credentials| K8s Secret           | 90 days   |
| API keys (external)       | Vault                 | As needed |

### Prohibited Practices
- Never hardcode secrets in source code or configuration files
- Never log secrets or tokens
- Never include secrets in Docker images
- Never commit `.env` files or secret files to version control

---

## 6. Security Configuration Template

The `ftgo-security-lib` provides `BaseSecurityConfiguration` which each service extends:

```java
@Configuration
@EnableMethodSecurity
public class OrderServiceSecurityConfig extends BaseSecurityConfiguration {

    public OrderServiceSecurityConfig(JwtTokenProvider jwtTokenProvider) {
        super(jwtTokenProvider);
    }

    @Override
    protected void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers(HttpMethod.POST, "/orders").hasRole("CONSUMER")
            .requestMatchers(HttpMethod.GET, "/orders/**").authenticated()
            .anyRequest().authenticated()
        );
    }
}
```

### CORS Configuration
- Allowed origins configured per environment
- Credentials allowed for cookie-based refresh tokens
- Pre-flight cache: 3600 seconds

### Security Headers
- `Content-Security-Policy: default-src 'self'`
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains` (production only)

---

## 7. Input Validation

- All request bodies validated using Jakarta Bean Validation (`@Valid`)
- Path variables and query parameters validated with `@Validated`
- SQL injection prevented by using JPA parameterized queries (never string concatenation)
- XSS prevented by response encoding and CSP headers

---

## 8. Audit Logging

All security-relevant events must be logged:
- Authentication attempts (success and failure)
- Authorization failures
- Token refresh events
- Admin actions
- Data access patterns for sensitive resources

Log format:
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "service": "order-service",
  "event": "AUTH_FAILURE",
  "principal": "user-123",
  "resource": "/orders/456",
  "action": "GET",
  "outcome": "DENIED",
  "reason": "INSUFFICIENT_ROLE",
  "sourceIp": "10.0.0.1"
}
```
