# FTGO Health Checks, Service Discovery, and Resilience

## Health Check Endpoints

Each FTGO microservice exposes Spring Boot Actuator health endpoints:

| Endpoint | Purpose | Access |
|----------|---------|--------|
| `/actuator/health` | Overall health | Public |
| `/actuator/health/liveness` | Kubernetes liveness probe | Public |
| `/actuator/health/readiness` | Kubernetes readiness probe | Public |
| `/actuator/info` | Service info | Public |
| `/actuator/prometheus` | Prometheus metrics | Public |

### Configuration

```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

## Service Discovery

Services register with Kubernetes DNS for service discovery:

- **Internal**: `http://<service-name>.<namespace>.svc.cluster.local:<port>`
- **Short form**: `http://<service-name>:<port>` (within same namespace)

The API Gateway (`ftgo-api-gateway`) uses Spring Cloud Gateway with `lb://` URIs for load-balanced routing.

## Resilience Patterns

### Circuit Breaker (Resilience4j)

```properties
resilience4j.circuitbreaker.instances.default.slidingWindowSize=10
resilience4j.circuitbreaker.instances.default.failureRateThreshold=50
resilience4j.circuitbreaker.instances.default.waitDurationInOpenState=10s
resilience4j.circuitbreaker.instances.default.permittedNumberOfCallsInHalfOpenState=3
```

### Retry

```properties
resilience4j.retry.instances.default.maxAttempts=3
resilience4j.retry.instances.default.waitDuration=500ms
resilience4j.retry.instances.default.retryExceptions=java.io.IOException,java.net.SocketTimeoutException
```

### Rate Limiting

API Gateway applies rate limiting per client:
- Default: 50 requests/second, burst capacity 100
- Configurable per route in `infrastructure/api-gateway/application.yml`

### Timeouts

```properties
spring.cloud.gateway.httpclient.connect-timeout=5000
spring.cloud.gateway.httpclient.response-timeout=10s
```

## Kubernetes Probes

Configured in `deployment/kubernetes/base/deployment.yml`:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```
