# API Gateway Documentation

## Overview

The API Gateway is the single entry point for all client requests to the FTGO microservices platform.
It is built with Spring Cloud Gateway (reactive) and provides cross-cutting concerns such as
authentication, authorization, rate limiting, CORS, correlation ID propagation, and request logging.

## Architecture

```
Client → API Gateway (8080) → Backend Services
                │
                ├── /api/orders/**     → Order Service (8081)
                ├── /api/consumers/**  → Consumer Service (8082)
                ├── /api/restaurants/** → Restaurant Service (8083)
                └── /api/couriers/**   → Courier Service (8084)
```

## Components

### Filters

| Filter | Type | Order | Purpose |
|---|---|---|---|
| `CorrelationIdGlobalFilter` | Global | HIGHEST_PRECEDENCE | Generates/propagates X-Correlation-Id |
| `RequestLoggingGlobalFilter` | Global | HIGHEST_PRECEDENCE+1 | Logs requests and response timing |
| `JwtAuthenticationGatewayFilterFactory` | Per-Route | N/A | Validates JWT tokens and enforces RBAC |
| `RateLimitGatewayFilterFactory` | Per-Route | N/A | Token-bucket rate limiting |

### JWT Authentication Flow

1. Client sends request with `Authorization: Bearer <token>` header
2. `JwtAuthenticationGatewayFilterFactory` extracts and validates the token using `JwtTokenProvider` from `libs/ftgo-jwt`
3. If valid, the user's subject and roles are extracted from the token claims
4. If the route requires specific roles, those are checked against the token's roles
5. On success, `X-User-Id` and `X-User-Roles` headers are added to the downstream request
6. On failure, a 401 (Unauthorized) or 403 (Forbidden) response is returned

### Rate Limiting

The gateway uses an in-memory token-bucket algorithm:

- Each unique client IP + route combination gets its own bucket
- Configurable `requestsPerSecond` and `burstCapacity` per route
- When exceeded, returns HTTP 429 (Too Many Requests)
- Rate limit headers (`X-RateLimit-Remaining`, `X-RateLimit-Limit`) are added to responses

For production with multiple gateway instances, switch to Redis-based rate limiting by
enabling `spring-boot-starter-data-redis-reactive` and configuring the Redis connection.

### CORS Configuration

CORS is configured globally via `ftgo.gateway.cors` properties:

```yaml
ftgo:
  gateway:
    cors:
      allowed-origins:
        - http://localhost:3000
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - PATCH
        - OPTIONS
      allowed-headers:
        - "*"
      exposed-headers:
        - X-Correlation-Id
        - X-RateLimit-Remaining
      allow-credentials: true
      max-age: 3600
```

### Health Check

The gateway exposes health information at `/actuator/health` with custom details:

```json
{
  "status": "UP",
  "components": {
    "gateway": {
      "status": "UP",
      "details": {
        "service": "api-gateway",
        "routeCount": 4,
        "route.order-service": "http://localhost:8081",
        "route.consumer-service": "http://localhost:8082",
        "route.restaurant-service": "http://localhost:8083",
        "route.courier-service": "http://localhost:8084"
      }
    }
  }
}
```

### Actuator Endpoints

| Endpoint | Description |
|---|---|
| `/actuator/health` | Gateway and component health |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Micrometer metrics |
| `/actuator/prometheus` | Prometheus scrape endpoint |
| `/actuator/gateway/routes` | List all configured routes |

## Configuration Reference

### Route Properties (`ftgo.gateway.routes[]`)

| Property | Type | Default | Description |
|---|---|---|---|
| `id` | String | required | Unique route identifier |
| `path` | String | required | URL path pattern to match |
| `uri` | String | required | Backend service URI |
| `strip-prefix` | int | 1 | Number of path segments to strip |
| `auth-required` | boolean | true | Whether JWT auth is required |
| `required-roles` | List | [] | Roles required to access route |
| `rate-limit` | int | 100 | Requests per second |
| `burst-capacity` | int | 150 | Maximum burst token count |

### Global Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `ftgo.jwt.secret` | String | required | JWT signing secret |
| `ftgo.jwt.expiration` | long | 3600000 | Token expiration (ms) |
| `ftgo.jwt.issuer` | String | ftgo-platform | Token issuer |

## Deployment

### Environment Variables

Set the following environment variables for production deployment:

```bash
JWT_SECRET=<production-secret-min-32-chars>
ORDER_SERVICE_URL=http://order-service:8080
CONSUMER_SERVICE_URL=http://consumer-service:8080
RESTAURANT_SERVICE_URL=http://restaurant-service:8080
COURIER_SERVICE_URL=http://courier-service:8080
```

### Kubernetes

The gateway is deployed as a Kubernetes service. See `infrastructure/k8s/` for manifests.
