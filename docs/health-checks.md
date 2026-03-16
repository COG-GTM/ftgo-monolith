# Health Check Configuration

## Overview

Health checks ensure that FTGO microservices are running correctly and can serve traffic. This document describes the health check strategy using Spring Boot Actuator endpoints and Kubernetes probes.

## Spring Boot Actuator Health Endpoints

Each FTGO microservice exposes health information via Spring Boot Actuator:

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Aggregated health status (UP/DOWN) |
| `/actuator/health/liveness` | Liveness check (is the process alive?) |
| `/actuator/health/readiness` | Readiness check (can it accept traffic?) |
| `/actuator/info` | Application metadata and build info |

### Configuration

Add the following to each service's `application.properties` or `configmap.yaml`:

```properties
# Enable health endpoints
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when-authorized

# Health group configuration (Spring Boot 2.x)
management.health.defaults.enabled=true

# Database health check
management.health.db.enabled=true

# Disk space health check
management.health.diskspace.enabled=true
management.health.diskspace.threshold=10485760
```

### Health Indicators

Each service includes the following automatic health indicators:

| Indicator | Description |
|-----------|-------------|
| `db` | Database connectivity (MySQL) |
| `diskSpace` | Minimum disk space threshold |
| `ping` | Basic liveness ping |

### Custom Health Indicators

Services can register custom health indicators for domain-specific checks:

```java
@Component
public class OrderServiceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public OrderServiceHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return Health.up()
                    .withDetail("database", "MySQL connection valid")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withException(e)
                .build();
        }
        return Health.down().build();
    }
}
```

## Kubernetes Probes

All services are configured with three types of Kubernetes probes. See [kubernetes-probes.md](kubernetes-probes.md) for detailed probe configuration.

### Probe Summary

| Probe | Purpose | Path | Initial Delay | Period |
|-------|---------|------|---------------|--------|
| Startup | Wait for app to finish starting | `/actuator/health` | 10s | 5s |
| Liveness | Detect deadlocks / hung processes | `/actuator/health` | 60s | 15s |
| Readiness | Control traffic routing | `/actuator/health` | 30s | 10s |

## Health Check Dependencies

### Per-Service Health Dependencies

| Service | Critical Dependencies |
|---------|----------------------|
| order-service | MySQL, consumer-service, restaurant-service |
| consumer-service | MySQL |
| restaurant-service | MySQL |
| courier-service | MySQL |
| api-gateway | All downstream services |

### Graceful Degradation

Services should remain operational even when non-critical dependencies are unavailable. The circuit breaker pattern (see [resilience-patterns.md](resilience-patterns.md)) ensures that downstream failures do not cascade.

## Monitoring Health Status

### Prometheus Metrics

Health check results are exposed as Prometheus metrics via the `/actuator/prometheus` endpoint:

```
# Health status (1 = UP, 0 = DOWN)
health_check_status{service="order-service"} 1
```

### Alerting Rules

Configure Prometheus alerts for health check failures:

```yaml
groups:
  - name: ftgo-health
    rules:
      - alert: ServiceUnhealthy
        expr: up{job=~"ftgo-.*"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "FTGO service {{ $labels.job }} is unhealthy"
```
