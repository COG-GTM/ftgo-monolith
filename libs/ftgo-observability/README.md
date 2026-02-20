# ftgo-observability

Shared observability library for FTGO microservices providing Micrometer/Prometheus metrics auto-configuration, custom business metrics, and actuator endpoint configuration.

## Overview

This module provides:

- **Prometheus Metrics Auto-Configuration**: Automatically configures Micrometer with Prometheus registry, common tags, and JVM/system metrics
- **Custom Business Metrics**: Domain-specific metrics for each bounded context (orders, consumers, couriers, restaurants)
- **Actuator Endpoint Configuration**: Pre-configured actuator endpoints for health, info, metrics, and Prometheus scraping

## Usage

Add this library as a dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-observability')
}
```

The auto-configuration will automatically register:
- Prometheus metrics endpoint at `/actuator/prometheus`
- Health endpoint at `/actuator/health`
- Info endpoint at `/actuator/info`
- Metrics endpoint at `/actuator/metrics`

## Configuration Properties

```yaml
ftgo:
  observability:
    application-name: my-service        # Override spring.application.name for metrics tags
    environment: production             # Environment tag (defaults to 'development')
    metrics:
      enabled: true                     # Enable/disable all metrics (default: true)
      business-metrics-enabled: true    # Enable/disable business metrics (default: true)
      order-metrics-enabled: true       # Enable/disable order metrics (default: true)
      consumer-metrics-enabled: true    # Enable/disable consumer metrics (default: true)
      courier-metrics-enabled: true     # Enable/disable courier metrics (default: true)
      restaurant-metrics-enabled: true  # Enable/disable restaurant metrics (default: true)
```

## Metrics Naming Conventions

All custom business metrics follow the naming pattern: `ftgo.<domain>.<metric_name>`

| Prefix              | Domain     | Description                    |
|---------------------|------------|--------------------------------|
| `ftgo.orders.*`     | order      | Order lifecycle metrics        |
| `ftgo.consumers.*`  | consumer   | Consumer registration/verification |
| `ftgo.deliveries.*` | courier    | Delivery tracking metrics      |
| `ftgo.couriers.*`   | courier    | Courier availability metrics   |
| `ftgo.restaurants.*` | restaurant | Restaurant management metrics |
| `ftgo.tickets.*`    | restaurant | Kitchen ticket metrics         |

### Metric Types

- **Counters**: Monotonically increasing values (e.g., `ftgo.orders.created`)
- **Gauges**: Current values that can go up/down (e.g., `ftgo.couriers.available`)
- **Timers**: Duration measurements (e.g., `ftgo.orders.fulfillment.duration`)
- **Distribution Summaries**: Value distributions (e.g., `ftgo.deliveries.distance`)

### Common Tags

All metrics are automatically tagged with:
- `application`: The service name (from `spring.application.name`)
- `env`: The deployment environment

## Business Metrics Reference

### Order Metrics (`OrderMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.orders.created` | Counter | Total orders created |
| `ftgo.orders.approved` | Counter | Total orders approved |
| `ftgo.orders.rejected` | Counter | Total orders rejected |
| `ftgo.orders.cancelled` | Counter | Total orders cancelled |
| `ftgo.orders.revised` | Counter | Total orders revised |
| `ftgo.orders.fulfillment.duration` | Timer | Order fulfillment time |
| `ftgo.orders.approval.duration` | Timer | Order approval time |

### Consumer Metrics (`ConsumerMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.consumers.registered` | Counter | Total consumers registered |
| `ftgo.consumers.verifications` | Counter | Total verifications performed |
| `ftgo.consumers.verification.failures` | Counter | Total verification failures |
| `ftgo.consumers.active` | Gauge | Current active consumers |

### Courier Metrics (`CourierMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.deliveries.completed` | Counter | Total deliveries completed |
| `ftgo.deliveries.failed` | Counter | Total deliveries failed |
| `ftgo.couriers.created` | Counter | Total couriers created |
| `ftgo.deliveries.duration` | Timer | Delivery duration (pickup to completion) |
| `ftgo.deliveries.pickup.duration` | Timer | Pickup duration (assignment to pickup) |
| `ftgo.deliveries.distance` | Summary | Delivery distance distribution (km) |
| `ftgo.couriers.available` | Gauge | Current available couriers |

### Restaurant Metrics (`RestaurantMetrics`)
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.restaurants.created` | Counter | Total restaurants created |
| `ftgo.restaurants.menu.revisions` | Counter | Total menu revisions |
| `ftgo.tickets.accepted` | Counter | Total tickets accepted |
| `ftgo.tickets.rejected` | Counter | Total tickets rejected |
| `ftgo.tickets.preparation.duration` | Timer | Ticket preparation time |
| `ftgo.restaurants.active` | Gauge | Current active restaurants |

## Grafana Dashboards

Pre-built Grafana dashboard templates are available in `infrastructure/monitoring/grafana/dashboards/`:

- `order-service-dashboard.json` - Order lifecycle, fulfillment times, HTTP metrics
- `consumer-service-dashboard.json` - Consumer registrations, verifications, HTTP metrics
- `courier-service-dashboard.json` - Delivery tracking, courier availability, HTTP metrics
- `restaurant-service-dashboard.json` - Restaurant management, ticket processing, HTTP metrics

Each dashboard includes:
1. **Business Metrics** - Domain-specific counters and rates
2. **HTTP Metrics** - Request rates and response time percentiles
3. **JVM Metrics** - Heap memory usage and thread counts
