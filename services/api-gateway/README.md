# API Gateway Service

Spring Cloud Gateway service providing a unified entry point to the FTGO microservices platform.

## Features

- **Dynamic Routing**: Routes requests to Order, Consumer, Restaurant, and Courier services
- **JWT Authentication**: Validates JWT tokens on incoming requests using `libs/ftgo-jwt`
- **Role-Based Access Control**: Enforces per-route role requirements
- **Rate Limiting**: Token-bucket rate limiting per client IP and route
- **CORS**: Configurable cross-origin resource sharing policy
- **Correlation ID Propagation**: Generates/propagates correlation IDs for distributed tracing
- **Request/Response Logging**: Logs all requests with timing and correlation data
- **Health Checks**: Custom health indicator with route status details
- **Actuator Endpoints**: Prometheus metrics, health, info, gateway route inspection

## Running Locally

```bash
# From repository root
./gradlew :services:api-gateway:bootRun
```

The gateway starts on port `8080` by default.

## Configuration

All configuration is via `application.yml` or environment variables:

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | (dev default) | JWT signing secret (min 32 chars) |
| `ORDER_SERVICE_URL` | `http://localhost:8081` | Order service base URL |
| `CONSUMER_SERVICE_URL` | `http://localhost:8082` | Consumer service base URL |
| `RESTAURANT_SERVICE_URL` | `http://localhost:8083` | Restaurant service base URL |
| `COURIER_SERVICE_URL` | `http://localhost:8084` | Courier service base URL |

## Route Configuration

Routes are configured under `ftgo.gateway.routes` in `application.yml`:

```yaml
ftgo:
  gateway:
    routes:
      - id: order-service
        path: /api/orders/**
        uri: http://localhost:8081
        strip-prefix: 1
        auth-required: true
        rate-limit: 100
        burst-capacity: 150
```

## Testing

```bash
./gradlew :services:api-gateway:test
```

## Docker

```bash
./gradlew :services:api-gateway:bootJar
docker build -t ftgo/api-gateway -f docker/Dockerfile .
docker run -p 8080:8080 ftgo/api-gateway
```
