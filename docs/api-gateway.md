# FTGO API Gateway

## Overview

The FTGO API Gateway is a Spring Cloud Gateway service that serves as the single entry point for all FTGO microservices. It provides centralized security, routing, rate limiting, circuit breaking, and observability.

## Architecture

```
                    ┌──────────────────────────────────┐
                    │         API Gateway (:8080)       │
                    │                                    │
  Client ──────────▶│  ┌─────────┐  ┌──────────────┐   │
                    │  │  CORS   │  │ Correlation  │   │
                    │  │ Filter  │  │  ID Filter   │   │
                    │  └────┬────┘  └──────┬───────┘   │
                    │       │              │            │
                    │  ┌────┴────┐  ┌──────┴───────┐   │
                    │  │  API    │  │   Request    │   │
                    │  │Version  │  │  Logging     │   │
                    │  │ Filter  │  │  Filter      │   │
                    │  └────┬────┘  └──────┬───────┘   │
                    │       │              │            │
                    │  ┌────┴──────────────┴───────┐   │
                    │  │   JWT Validation Filter    │   │
                    │  └────────────┬───────────────┘   │
                    │               │                    │
                    │  ┌────────────┴───────────────┐   │
                    │  │  Rate Limiter (Redis)      │   │
                    │  └────────────┬───────────────┘   │
                    │               │                    │
                    │  ┌────────────┴───────────────┐   │
                    │  │  Circuit Breaker           │   │
                    │  │  (Resilience4j)            │   │
                    │  └────────────┬───────────────┘   │
                    │               │                    │
                    └───────────────┼────────────────────┘
                                    │
                    ┌───────────────┼───────────────────┐
                    │               │                    │
              ┌─────┴─────┐  ┌─────┴──────┐  ┌────────┴────────┐
              │   Order   │  │  Consumer  │  │  Restaurant    │
              │  Service  │  │  Service   │  │   Service      │
              │  (:8081)  │  │  (:8082)   │  │   (:8083)      │
              └───────────┘  └────────────┘  └────────────────┘
                                                    │
                                              ┌─────┴──────┐
                                              │  Courier   │
                                              │  Service   │
                                              │  (:8084)   │
                                              └────────────┘
```

## Route Configuration

| Path Pattern | Downstream Service | Port |
|---|---|---|
| `/api/orders/**` | Order Service | 8081 |
| `/api/consumers/**` | Consumer Service | 8082 |
| `/api/restaurants/**` | Restaurant Service | 8083 |
| `/api/couriers/**` | Courier Service | 8084 |

### Path Rewriting

The gateway strips the `/api` prefix when forwarding requests to downstream services:
- `GET /api/orders/123` → `GET /orders/123` on Order Service

## Security

### JWT Validation

The gateway validates JWT tokens at the entry point and forwards validated claims as headers to downstream services:

| Header | Description |
|---|---|
| `X-User-ID` | User ID extracted from JWT `sub` claim |
| `X-Username` | Username from JWT `username` claim |
| `X-User-Roles` | Comma-separated roles from JWT `roles` claim |

**Public paths** (no JWT required):
- `/actuator/**` — Health checks and metrics
- `/health/**` — Health endpoints
- `/info` — Info endpoint

### Configuration

```yaml
ftgo:
  security:
    jwt:
      secret: ${FTGO_SECURITY_JWT_SECRET}  # Must be ≥ 32 bytes
      issuer: ftgo-platform
      enabled: true
```

## Rate Limiting

Rate limiting is backed by Redis using the Token Bucket algorithm.

### Client Identification (Key Resolution)

Priority order:
1. `X-API-Key` header — for API key-authenticated clients
2. `X-User-ID` header — for JWT-authenticated users
3. Client IP address — fallback

### Default Configuration

| Parameter | Default | Description |
|---|---|---|
| `replenishRate` | 10 | Tokens added per second |
| `burstCapacity` | 20 | Maximum token bucket size |
| `requestedTokens` | 1 | Tokens consumed per request |

### Per-Route Overrides

```yaml
ftgo:
  gateway:
    rate-limit:
      per-route:
        orders:
          replenish-rate: 50
          burst-capacity: 100
```

## Circuit Breaker

Uses Resilience4j with the following default configuration:

| Parameter | Default |
|---|---|
| Sliding window size | 10 requests |
| Failure rate threshold | 50% |
| Wait in open state | 10 seconds |
| Permitted calls in half-open | 3 |
| Minimum number of calls | 5 |
| Time limiter | 4 seconds |

When a circuit breaker opens, requests receive a 503 Service Unavailable response with a descriptive message.

## API Versioning

Two versioning strategies are supported:

### 1. URL Path Versioning (Recommended)

```
GET /api/v1/orders/123
GET /api/v2/orders/123
```

The version prefix is stripped and forwarded as an `X-API-Version` header.

### 2. Header Versioning

```
GET /api/orders/123
X-API-Version: v2
```

If no version is specified, defaults to `v1`.

## CORS

CORS is configured at the gateway level to avoid per-service configuration.

| Property | Default |
|---|---|
| Allowed Origins | `http://localhost:3000,http://localhost:8080` |
| Allowed Methods | `GET,POST,PUT,DELETE,PATCH,OPTIONS` |
| Allowed Headers | `*` |
| Exposed Headers | `Authorization,Content-Type,X-Correlation-ID,X-API-Version` |
| Max Age | 3600 seconds |
| Allow Credentials | true |

Configure via environment variables:
```
FTGO_GATEWAY_CORS_ORIGINS=https://app.ftgo.com,https://admin.ftgo.com
```

## SSL/TLS Termination

SSL/TLS termination is configurable at the gateway level:

```yaml
server:
  ssl:
    enabled: ${FTGO_GATEWAY_SSL_ENABLED:false}
    key-store: ${FTGO_GATEWAY_SSL_KEY_STORE:classpath:certs/gateway-keystore.p12}
    key-store-password: ${FTGO_GATEWAY_SSL_KEY_STORE_PASSWORD:changeit}
    key-store-type: PKCS12
    key-alias: ${FTGO_GATEWAY_SSL_KEY_ALIAS:ftgo-gateway}
```

For production, provide a proper certificate via environment variables or Kubernetes secrets.

## Observability

### Correlation ID Propagation

Every request receives a correlation ID (`X-Correlation-ID`):
- If the client provides one, it is reused
- Otherwise, a new UUID is generated
- The ID is propagated to downstream services and included in response headers
- The ID is placed in SLF4J MDC for log correlation

### Request/Response Logging

All requests and responses are logged with:
- HTTP method and path
- Client IP address
- Response status code
- Request duration (ms)
- Correlation ID

### Log Format

```
2024-03-15 10:30:45.123 [reactor-http-nio-1] [abc-123-def] INFO  c.f.g.filter.RequestLoggingGatewayFilter - Gateway Request: GET /api/orders from 10.0.0.1 [correlationId=abc-123-def]
```

### Metrics

The gateway exposes Prometheus metrics at `/actuator/prometheus`:
- Gateway route metrics
- Circuit breaker state metrics
- Rate limiter metrics
- JVM and system metrics

## Kubernetes Deployment

### Resources

| Resource | Request | Limit |
|---|---|---|
| CPU | 200m | 1 |
| Memory | 384Mi | 768Mi |

### Autoscaling (HPA)

| Parameter | Value |
|---|---|
| Min replicas | 2 |
| Max replicas | 10 |
| CPU target | 60% |
| Memory target | 75% |

### Service Type

The gateway K8s Service is of type `LoadBalancer`, exposing ports 80 (HTTP) and 443 (HTTPS).

## Local Development

### Running with Docker Compose

```bash
docker-compose -f docker-compose-services.yml up -d
```

The gateway will be available at `http://localhost:8080`.

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `FTGO_SECURITY_JWT_SECRET` | (dev default) | JWT signing secret |
| `FTGO_ORDER_SERVICE_URL` | `http://localhost:8081` | Order Service URL |
| `FTGO_CONSUMER_SERVICE_URL` | `http://localhost:8082` | Consumer Service URL |
| `FTGO_RESTAURANT_SERVICE_URL` | `http://localhost:8083` | Restaurant Service URL |
| `FTGO_COURIER_SERVICE_URL` | `http://localhost:8084` | Courier Service URL |
| `SPRING_REDIS_HOST` | `localhost` | Redis host for rate limiting |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `FTGO_GATEWAY_SSL_ENABLED` | `false` | Enable SSL/TLS |
| `FTGO_GATEWAY_CORS_ORIGINS` | `http://localhost:3000,http://localhost:8080` | CORS origins |

## Building

```bash
# Build only the gateway
./gradlew :services-ftgo-api-gateway:build

# Run tests
./gradlew :services-ftgo-api-gateway:test
```
