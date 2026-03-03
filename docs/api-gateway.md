# FTGO API Gateway

## Overview

The FTGO API Gateway is a Spring Cloud Gateway service that serves as the single entry point for all FTGO microservices. It handles authentication, routing, rate limiting, circuit breaking, CORS, correlation ID propagation, and API versioning.

## Architecture

```
                    +-------------------+
                    |   API Gateway     |
   Clients ------->|  (Port 80/8080)   |
                    +---+-----------+---+
                        |           |
          +-------------+           +-------------+
          |             |           |             |
          v             v           v             v
   +-----------+  +-----------+  +-----------+  +-----------+
   |  Order    |  | Consumer  |  |Restaurant |  | Courier   |
   |  Service  |  |  Service  |  |  Service  |  |  Service  |
   +-----------+  +-----------+  +-----------+  +-----------+
```

## Route Configuration

| Path Pattern             | Target Service       | Internal Path    |
|--------------------------|----------------------|------------------|
| `/api/orders/**`         | Order Service        | `/orders/**`     |
| `/api/consumers/**`      | Consumer Service     | `/consumers/**`  |
| `/api/restaurants/**`    | Restaurant Service   | `/restaurants/**`|
| `/api/couriers/**`       | Courier Service      | `/couriers/**`   |
| `/v1/api/orders/**`      | Order Service (v1)   | `/orders/**`     |
| `/v1/api/consumers/**`   | Consumer Service (v1)| `/consumers/**`  |
| `/v1/api/restaurants/**` | Restaurant Service (v1)| `/restaurants/**`|
| `/v1/api/couriers/**`    | Courier Service (v1) | `/couriers/**`   |

### API Versioning

The gateway supports URL-path-based API versioning:

- **Non-versioned**: `/api/orders/123` routes to Order Service `/orders/123`
- **Versioned (v1)**: `/v1/api/orders/123` routes to Order Service `/orders/123`

Future API versions (v2, v3, etc.) can be added by creating new route definitions that target updated service instances.

## Security

### JWT Authentication

All API requests (except excluded paths) require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

The gateway:
1. Extracts the Bearer token from the `Authorization` header
2. Validates the token signature and expiration using HMAC-SHA256
3. Extracts claims (`sub`, `roles`) from the token
4. Forwards identity headers to downstream services:
   - `X-User-Id`: JWT subject claim
   - `X-User-Roles`: JWT roles claim

**Excluded paths** (no JWT required):
- `/actuator/**` - Health checks and metrics
- `/fallback/**` - Circuit breaker fallback endpoints

**Configuration**:
| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `JWT_SECRET`        | (required) | HMAC-SHA256 secret key (min 32 chars) |
| `JWT_ENABLED`       | `true`  | Enable/disable JWT validation |

### CORS

CORS is configured globally at the gateway level. Individual microservices do not need CORS configuration.

| Setting | Default |
|---------|---------|
| Allowed Origins | `*` |
| Allowed Methods | `GET, POST, PUT, DELETE, PATCH, OPTIONS` |
| Allowed Headers | `*` |
| Exposed Headers | `X-Correlation-Id, X-RateLimit-Remaining` |
| Allow Credentials | `true` |
| Max Age | `3600s` |

## Rate Limiting

Rate limiting uses Redis-backed token bucket algorithm via Spring Cloud Gateway's `RequestRateLimiter` filter.

### Key Resolution Priority

1. `X-API-Key` header (if present) - keyed as `apikey:<key>`
2. `X-User-Id` header (from JWT) - keyed as `user:<id>`
3. Client IP address (fallback) - keyed as `ip:<address>`

### Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `RATE_LIMIT_REPLENISH_RATE` | `10` | Tokens added per second |
| `RATE_LIMIT_BURST_CAPACITY` | `20` | Maximum tokens in bucket |
| `RATE_LIMIT_REQUESTED_TOKENS` | `1` | Tokens consumed per request |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |

### Response Headers

When rate limiting is active, the response includes:
- `X-RateLimit-Remaining`: Tokens remaining in the bucket
- `X-RateLimit-Burst-Capacity`: Maximum burst capacity
- `X-RateLimit-Replenish-Rate`: Token replenish rate

## Circuit Breaker

Each downstream service has its own Resilience4j circuit breaker instance. When a service becomes unavailable, the circuit breaker opens and requests are routed to fallback endpoints.

### Circuit Breaker States

```
CLOSED  --[failure rate >= threshold]--> OPEN
OPEN    --[wait duration elapsed]------> HALF_OPEN
HALF_OPEN --[success]-------------------> CLOSED
HALF_OPEN --[failure]-------------------> OPEN
```

### Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `CB_FAILURE_RATE_THRESHOLD` | `50` | Failure percentage to open circuit |
| `CB_WAIT_DURATION` | `30` | Seconds to wait before half-open |
| `CB_SLIDING_WINDOW_SIZE` | `10` | Number of calls for failure rate |
| `CB_PERMITTED_HALF_OPEN` | `3` | Calls allowed in half-open state |
| `CB_TIMEOUT_DURATION` | `5` | Request timeout in seconds |

### Circuit Breaker Instances

| Instance | Protects |
|----------|----------|
| `orderServiceCircuitBreaker` | Order Service |
| `consumerServiceCircuitBreaker` | Consumer Service |
| `restaurantServiceCircuitBreaker` | Restaurant Service |
| `courierServiceCircuitBreaker` | Courier Service |

### Fallback Responses

When a circuit breaker is open, a standardized JSON response is returned:

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Order Service is currently unavailable. Please try again later.",
  "service": "Order Service",
  "timestamp": "2026-03-03T00:00:00.000Z",
  "correlationId": "abc-123-def"
}
```

## Correlation ID

Every request passing through the gateway is assigned a correlation ID for distributed tracing:

- If `X-Correlation-Id` header is present in the request, it is preserved
- If absent, a new UUID is generated
- The correlation ID is forwarded to downstream services
- The correlation ID is included in the response headers
- The correlation ID is included in all gateway log entries

## Observability

### Health Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Overall health status |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |

### Metrics

Prometheus metrics are exposed at `/actuator/prometheus`:
- Gateway request metrics (count, latency, status codes)
- Circuit breaker state and call metrics
- Rate limiter metrics
- JVM and system metrics

### Gateway Management

| Endpoint | Description |
|----------|-------------|
| `/actuator/gateway/routes` | List all configured routes |
| `/actuator/circuitbreakers` | Circuit breaker states |
| `/actuator/circuitbreakerevents` | Circuit breaker event log |

## Deployment

### Building

The API Gateway is a standalone project built independently from the legacy monolith:

```bash
cd infrastructure/api-gateway
./gradlew build
```

### Docker

```bash
cd infrastructure/api-gateway
docker build -t ftgo/ftgo-api-gateway:latest .
```

### Kubernetes

The gateway is deployed using Kustomize alongside other FTGO services:

```bash
# Dev environment
kubectl apply -k infrastructure/kubernetes/overlays/dev/

# Staging
kubectl apply -k infrastructure/kubernetes/overlays/staging/

# Production
kubectl apply -k infrastructure/kubernetes/overlays/prod/
```

### Environment-Specific Configuration

| Environment | JWT Enabled | Replicas | Notes |
|-------------|------------|----------|-------|
| Dev | `false` | 1 | JWT disabled for easier testing |
| Staging | `true` | 2 | Production-like configuration |
| Production | `true` | 3+ (HPA) | Full security, auto-scaling |

## Local Development

### Prerequisites

- Java 17+
- Redis (for rate limiting)
- Docker (optional, for containerized development)

### Running Locally

```bash
cd infrastructure/api-gateway

# Start Redis (if not running)
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Run the gateway
./gradlew bootRun

# Or with JWT disabled
JWT_ENABLED=false ./gradlew bootRun
```

### Docker Compose

The gateway can also be started as part of the microservices Docker Compose setup:

```bash
docker compose -f docker-compose.microservices.yml up ftgo-api-gateway
```

## File Structure

```
infrastructure/api-gateway/
  build.gradle              # Standalone Gradle build
  settings.gradle           # Project settings
  gradle.properties         # Build properties
  Dockerfile                # Multi-stage Docker build
  src/
    main/
      java/net/chrisrichardson/ftgo/gateway/
        FtgoApiGatewayApplication.java     # Main application
        config/
          GatewayRoutesConfig.java         # Route definitions
          SecurityConfig.java              # Spring Security config
          RateLimiterConfig.java           # Redis rate limiter
          CircuitBreakerConfig.java        # Resilience4j config
          CorsConfig.java                  # CORS configuration
        filter/
          JwtAuthenticationFilter.java     # JWT validation filter
          CorrelationIdFilter.java         # Correlation ID filter
          RequestLoggingFilter.java        # Request/response logging
        fallback/
          FallbackController.java          # Circuit breaker fallbacks
      resources/
        application.yml                    # Application configuration
        logback-spring.xml                 # Logging configuration
    test/
      java/net/chrisrichardson/ftgo/gateway/
        FtgoApiGatewayApplicationTest.java # Context load test
      resources/
        application-test.yml               # Test configuration
```
