# FTGO API Gateway

## Overview

The FTGO API Gateway (`ftgo-api-gateway`) is the single entry point for all FTGO microservices. Built on **Spring Cloud Gateway** (reactive/WebFlux), it handles routing, authentication, rate limiting, circuit breaking, and cross-cutting concerns.

## Architecture

```
Client → API Gateway (port 8080) → Downstream Microservices
                                    ├── Order Service      (port 8081)
                                    ├── Consumer Service   (port 8082)
                                    ├── Restaurant Service (port 8083)
                                    └── Courier Service    (port 8084)
```

## Features

### 1. Request Routing

Routes are configured for each microservice:

| Path Pattern              | Destination Service  | Port |
|---------------------------|---------------------|------|
| `/api/orders/**`          | Order Service       | 8081 |
| `/api/consumers/**`       | Consumer Service    | 8082 |
| `/api/restaurants/**`     | Restaurant Service  | 8083 |
| `/api/couriers/**`        | Courier Service     | 8084 |

Versioned routes are also available:
- `/v1/api/orders/**` → rewrites to `/api/orders/**` with `X-API-Version: v1` header

### 2. JWT Authentication

JWT validation happens at the gateway level. Valid tokens are parsed, and claims are forwarded to downstream services as HTTP headers:

| Header               | Description                     |
|----------------------|---------------------------------|
| `X-Auth-UserId`      | Numeric user ID                 |
| `X-Auth-Username`    | Username (JWT subject)          |
| `X-Auth-Roles`       | Comma-separated roles           |
| `X-Auth-Permissions` | Comma-separated permissions     |

The original `Authorization` header is also forwarded.

**Excluded paths** (no JWT required):
- `/actuator/**`
- `/fallback/**`

Configuration:
```yaml
ftgo:
  gateway:
    jwt:
      enabled: true
      secret: ${JWT_SECRET}
      issuer: ftgo-platform
      excluded-paths:
        - /actuator/**
        - /fallback/**
```

### 3. Rate Limiting

Two implementations are available:

- **In-memory** (default): Token bucket per client IP, suitable for single-instance/dev
- **Redis-backed**: Distributed rate limiting for multi-instance production deployments

Configuration:
```yaml
ftgo:
  gateway:
    rate-limit:
      enabled: true
      redis-enabled: false  # Set to true for Redis-backed
      default-replenish-rate: 10   # tokens per second
      default-burst-capacity: 20   # max burst
      endpoints:
        order-service:
          replenish-rate: 20
          burst-capacity: 40
```

Rate limit headers returned in responses:
- `X-RateLimit-Remaining`
- `X-RateLimit-Burst-Capacity`
- `X-RateLimit-Replenish-Rate`

### 4. Circuit Breaker (Resilience4j)

Each downstream service has a named circuit breaker with fallback endpoints:

| Circuit Breaker       | Fallback Endpoint        |
|----------------------|--------------------------|
| `orderServiceCB`     | `/fallback/orders`       |
| `consumerServiceCB`  | `/fallback/consumers`    |
| `restaurantServiceCB`| `/fallback/restaurants`  |
| `courierServiceCB`   | `/fallback/couriers`     |

Default settings:
- Failure rate threshold: 50%
- Wait in open state: 10 seconds
- Sliding window: 10 requests (count-based)
- Time limiter: 4 seconds

### 5. CORS Configuration

CORS is handled centrally at the gateway:

```yaml
ftgo:
  gateway:
    cors:
      allowed-origins: ["*"]
      allowed-methods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
      allowed-headers: ["*"]
      allow-credentials: false
      max-age: 3600s
```

### 6. SSL/TLS Termination

SSL can be enabled via:
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: ftgo-gateway
```

### 7. Correlation ID Propagation

Every request gets a correlation ID (`X-Correlation-Id` header):
- If present in the incoming request, it is propagated
- If absent, a new UUID is generated
- The ID is forwarded to downstream services and included in the response

### 8. Request/Response Logging

All requests and responses are logged with:
- HTTP method and URI
- Client IP
- Correlation ID
- Response status code and duration

### 9. API Versioning

Two strategies are supported:

1. **Header-based** (default): `X-API-Version` header
   - Default version injected if header is missing
2. **URL path-based**: `/v1/api/orders/**`, `/v2/api/orders/**`

### 10. Health Check Aggregation

The gateway aggregates health from all downstream services at `/actuator/health`:
- Reports individual service status
- Reports `DEGRADED` if any service is down (gateway remains operational)

## Kubernetes Deployment

The gateway has its own K8s deployment with:
- **Deployment**: 2 replicas (default)
- **Service**: LoadBalancer type (ports 80/443 → 8080)
- **HPA**: Auto-scales 2-8 replicas based on CPU (60%) and memory (75%)
- **ConfigMap**: Environment-specific configuration
- **Secret**: JWT secret (replace with SealedSecret/ExternalSecret in production)

## Docker Compose

The gateway is available at port `8080` in Docker Compose. In the monolith setup, all routes point to `ftgo-application:8080`.

## Environment Variables

| Variable                   | Default                    | Description                       |
|---------------------------|----------------------------|-----------------------------------|
| `JWT_ENABLED`             | `true`                     | Enable/disable JWT validation     |
| `JWT_SECRET`              | (dev default)              | HMAC-SHA256 signing secret        |
| `JWT_ISSUER`              | `ftgo-platform`            | Token issuer for validation       |
| `RATE_LIMIT_REDIS_ENABLED`| `false`                    | Use Redis for rate limiting       |
| `REDIS_HOST`              | `localhost`                | Redis host                        |
| `REDIS_PORT`              | `6379`                     | Redis port                        |
| `ORDER_SERVICE_URL`       | `http://ftgo-order-service:8081`    | Order service base URL   |
| `CONSUMER_SERVICE_URL`    | `http://ftgo-consumer-service:8082` | Consumer service base URL|
| `RESTAURANT_SERVICE_URL`  | `http://ftgo-restaurant-service:8083`| Restaurant service URL  |
| `COURIER_SERVICE_URL`     | `http://ftgo-courier-service:8084`  | Courier service base URL |

## Actuator Endpoints

| Endpoint                  | Description                         |
|--------------------------|-------------------------------------|
| `/actuator/health`       | Aggregated health (includes downstream) |
| `/actuator/info`         | Application info                    |
| `/actuator/metrics`      | Micrometer metrics                  |
| `/actuator/prometheus`   | Prometheus scrape endpoint          |
| `/actuator/gateway`      | Gateway route information           |
