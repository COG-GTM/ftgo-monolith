# Metrics and Monitoring

## Overview

The FTGO metrics infrastructure provides comprehensive observability across all microservices using:

- **Micrometer 1.12.5** — Application metrics instrumentation
- **Prometheus** — Metrics collection and alerting
- **Grafana** — Dashboards and visualization

## Architecture

```
┌─────────────┐  ┌──────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Order Svc   │  │ Consumer Svc     │  │ Restaurant Svc  │  │ Courier Svc     │
│ :8081       │  │ :8082            │  │ :8083           │  │ :8084           │
│ /actuator/  │  │ /actuator/       │  │ /actuator/      │  │ /actuator/      │
│  prometheus │  │  prometheus      │  │  prometheus     │  │  prometheus     │
└──────┬──────┘  └───────┬──────────┘  └───────┬─────────┘  └───────┬─────────┘
       │                 │                     │                     │
       └────────────┬────┴─────────────┬───────┴──────────┬──────────┘
                    │   Prometheus      │                  │
                    │   :9090          │                  │
                    │   (scrapes every │ 10s)             │
                    └──────────┬───────┘                  │
                               │                          │
                    ┌──────────▼──────────┐               │
                    │   Grafana           │               │
                    │   :3000             │               │
                    │   (dashboards)      │               │
                    └─────────────────────┘
```

## Shared Metrics Library

The `shared/ftgo-metrics-lib` module provides:

### Auto-Configuration
- Prometheus MeterRegistry setup
- Common metric tags (application name, environment)
- JVM metrics (heap, GC, threads, classloaders)
- System metrics (CPU, uptime)

### Custom Business Metrics

#### Order Service (`OrderMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `orders.created` | Counter | Total orders created |
| `orders.cancelled` | Counter | Total orders cancelled |
| `orders.total_value` | Counter | Cumulative order value in cents |
| `orders.by_state` | Gauge | Current count of orders per state |

#### Consumer Service (`ConsumerMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `consumers.registered` | Counter | Total consumers registered |
| `consumers.validated` | Counter | Total consumer validations |

#### Restaurant Service (`RestaurantMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `restaurants.created` | Counter | Total restaurants created |
| `menus.updated` | Counter | Total menu updates |

#### Courier Service (`CourierMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `deliveries.assigned` | Counter | Total deliveries assigned |
| `deliveries.completed` | Counter | Total deliveries completed |
| `deliveries.average_time` | Timer | Delivery duration (with p50/p95/p99) |

### Usage

Add the dependency to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-ftgo-metrics-lib')
}
```

Inject the metrics component in your service class:

```java
@Service
public class OrderService {
    private final OrderMetrics orderMetrics;

    public OrderService(OrderMetrics orderMetrics) {
        this.orderMetrics = orderMetrics;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = // ... create order
        orderMetrics.orderCreated();
        orderMetrics.recordOrderValue(order.getTotalInCents());
        return order;
    }
}
```

## Grafana Dashboards

Three pre-built dashboards are provided:

### 1. Service Health Overview (RED Metrics)
- **Rate**: Request throughput per service and endpoint
- **Errors**: 5xx error rate and count per service
- **Duration**: p50, p95, p99 latency per service

### 2. JVM Metrics
- Heap and non-heap memory usage
- Garbage collection pause times and frequency
- Live thread counts and states
- Class loader statistics

### 3. Business Metrics
- Order creation/cancellation rates and value
- Consumer registration and validation rates
- Restaurant creation and menu update rates
- Delivery assignment, completion, and duration

## Alerting Rules

Alerts are configured in `deploy/prometheus/alert-rules.yml`:

### Service Health
| Alert | Condition | Severity |
|-------|-----------|----------|
| ServiceDown | Service unreachable for 1m | Critical |
| HighErrorRate | 5xx rate > 5% for 2m | Critical |
| HighResponseLatency | p95 > 2s for 3m | Warning |
| HighRequestRate | > 1000 req/s for 5m | Warning |

### JVM Health
| Alert | Condition | Severity |
|-------|-----------|----------|
| HighHeapUsage | Heap > 85% for 5m | Warning |
| CriticalHeapUsage | Heap > 95% for 2m | Critical |
| HighGcPauseTime | Avg GC pause > 500ms for 5m | Warning |
| HighThreadCount | > 500 threads for 5m | Warning |

### Business Metrics
| Alert | Condition | Severity |
|-------|-----------|----------|
| HighOrderCancellationRate | > 30% cancellation for 10m | Warning |
| NoOrdersCreated | Zero orders for 30m | Warning |
| DeliveryTimeExceeded | p95 > 1 hour for 10m | Warning |

## Local Development

### Starting the Metrics Stack

```bash
# Start Prometheus and Grafana
docker-compose -f docker-compose-metrics.yml up -d

# Or start alongside the main application stack
docker-compose -f docker-compose.yml -f docker-compose-metrics.yml up -d
```

### Accessing Services
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (credentials: `admin`/`admin`)

### Verifying Metrics

1. Start a service with the metrics library
2. Visit `http://localhost:<port>/actuator/prometheus` to see raw metrics
3. Check Prometheus targets at http://localhost:9090/targets
4. View dashboards in Grafana

### Stopping the Metrics Stack

```bash
docker-compose -f docker-compose-metrics.yml down

# To also remove volumes (metrics data):
docker-compose -f docker-compose-metrics.yml down -v
```

## Configuration

### Actuator Endpoints

Add to your service's `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99
```

### Prometheus Scrape Configuration

Scrape targets are defined in `deploy/prometheus/prometheus.yml`. To add a new service:

```yaml
- job_name: "ftgo-new-service"
  metrics_path: "/actuator/prometheus"
  scrape_interval: 10s
  static_configs:
    - targets: ["ftgo-new-service:8085"]
      labels:
        service: "new-service"
```
