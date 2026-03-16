# API Gateway

The FTGO API Gateway is the single entry point for all client requests to the
microservices platform. It is built on **Spring Cloud Gateway** (reactive,
Netty-based) and runs on **Spring Boot 2.0.x** with **Java 8**.

## Architecture

```
                    ┌──────────────────┐
  Clients ────────► │   API Gateway    │
                    │   (port 8080)    │
                    └────────┬─────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │                │
   ┌────────▼───┐   ┌───────▼────┐   ┌───────▼──────┐  ┌─────▼──────┐
   │   Order    │   │  Consumer  │   │  Restaurant  │  │  Courier   │
   │  Service   │   │  Service   │   │   Service    │  │  Service   │
   │  :8081     │   │  :8082     │   │   :8083      │  │  :8084     │
   └────────────┘   └────────────┘   └──────────────┘  └────────────┘
```

## Route Configuration

| Public Path             | Downstream Service   | Internal URL                    |
|-------------------------|----------------------|---------------------------------|
| `/api/orders/**`        | Order Service        | `http://order-service:8081`     |
| `/api/consumers/**`     | Consumer Service     | `http://consumer-service:8082`  |
| `/api/restaurants/**`   | Restaurant Service   | `http://restaurant-service:8083`|
| `/api/couriers/**`      | Courier Service      | `http://courier-service:8084`   |

The `/api` prefix is stripped before forwarding (via `stripPrefix(1)`), so
`/api/orders/123` reaches the Order Service as `/orders/123`.

## Features

### JWT Validation

Every request must include a valid JWT in the `Authorization: Bearer <token>`
header. The gateway validates the signature and expiry, then forwards the
decoded user ID and roles as `X-User-Id` and `X-User-Roles` headers to
downstream services.

**Configuration:**

| Property               | Default                                                    | Description                  |
|------------------------|------------------------------------------------------------|------------------------------|
| `gateway.jwt.enabled`  | `true`                                                     | Enable/disable JWT checks    |
| `gateway.jwt.secret`   | `ftgo-secret-key-for-jwt-validation-minimum-256-bits-long` | HMAC signing key             |

Set `gateway.jwt.enabled=false` for local development without authentication.

### Rate Limiting

In-memory, per-client-IP rate limiting backed by Caffeine cache. When a client
exceeds the limit, the gateway returns `429 Too Many Requests`.

| Property                            | Default | Description                       |
|-------------------------------------|---------|-----------------------------------|
| `gateway.rate-limit.enabled`        | `true`  | Enable/disable rate limiting      |
| `gateway.rate-limit.max-requests`   | `100`   | Max requests per window           |
| `gateway.rate-limit.window-seconds` | `60`    | Sliding window duration (seconds) |

Response headers `X-RateLimit-Limit` and `X-RateLimit-Remaining` are returned
on every response.

### Circuit Breaker (Resilience4j)

Each downstream service has its own circuit breaker. When a service fails
beyond the configured threshold, the circuit opens and the gateway returns
`503 Service Unavailable` immediately.

| Parameter                           | Value        |
|-------------------------------------|--------------|
| Failure rate threshold              | 50%          |
| Slow call rate threshold            | 80%          |
| Slow call duration threshold        | 5 seconds    |
| Wait duration in open state         | 30 seconds   |
| Permitted calls in half-open state  | 5            |
| Sliding window size                 | 10 calls     |
| Minimum number of calls             | 5            |

### CORS

Cross-origin requests are handled at the gateway level so individual services
do not need their own CORS configuration. All origins are allowed by default
(`*`), with standard HTTP methods and headers permitted.

### Correlation ID

A `X-Correlation-Id` header is generated (UUID) for each request that does not
already carry one. The ID is forwarded to downstream services and included in
the response, enabling end-to-end distributed tracing.

### Request/Response Logging

All requests and responses are logged with:
- Correlation ID
- HTTP method and path
- Client IP
- Response status code
- Elapsed time (ms)

## Building

```bash
# Compile only
./gradlew :services:api-gateway:compileJava

# Full build (skip tests)
./gradlew :services:api-gateway:build -x test

# Build Docker image
docker build -f services/api-gateway/docker/Dockerfile -t ftgo/api-gateway .
```

## Running Locally

```bash
# Run with JWT disabled for development
java -jar services/api-gateway/build/libs/api-gateway.jar \
  --gateway.jwt.enabled=false \
  --gateway.services.order-service-url=http://localhost:8081 \
  --gateway.services.consumer-service-url=http://localhost:8082 \
  --gateway.services.restaurant-service-url=http://localhost:8083 \
  --gateway.services.courier-service-url=http://localhost:8084
```

## Kubernetes Deployment

Manifests are in `services/api-gateway/k8s/`:

| File              | Description                                   |
|-------------------|-----------------------------------------------|
| `deployment.yaml` | Deployment with 2 replicas, health probes     |
| `service.yaml`    | LoadBalancer service exposing port 80          |
| `configmap.yaml`  | Environment configuration                     |
| `secret.yaml`     | JWT secret (replace placeholder before deploy) |
| `hpa.yaml`        | Horizontal Pod Autoscaler (2-10 replicas)     |

```bash
kubectl apply -f services/api-gateway/k8s/
```

## Health Check

The gateway exposes Spring Boot Actuator endpoints:

- `GET /actuator/health` - Health status
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics
- `GET /actuator/prometheus` - Prometheus scrape endpoint
