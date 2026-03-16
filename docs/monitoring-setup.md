# FTGO Monitoring Setup

This document describes the metrics and monitoring infrastructure for FTGO microservices using Prometheus and Grafana.

## Architecture Overview

```
+-------------------+     +-------------------+     +-------------------+
|  Order Service    |     | Consumer Service  |     | Restaurant Service|
|  /actuator/prom   |     |  /actuator/prom   |     |  /actuator/prom   |
+--------+----------+     +--------+----------+     +--------+----------+
         |                         |                          |
         +------------+------------+-----------+--------------+
                      |                        |
                      v                        v
              +-------+--------+    +----------+---------+
              |  Prometheus    |    |  Courier Service   |
              |  (scraping)    |    |  /actuator/prom    |
              +-------+--------+    +--------------------+
                      |
                      v
              +-------+--------+
              |    Grafana     |
              |  (dashboards)  |
              +----------------+
```

## Components

### 1. ftgo-metrics-lib (Shared Library)

**Location:** `shared-libraries/ftgo-metrics-lib/`

A shared Spring Boot auto-configuration library that provides:

- **Micrometer configuration** with Prometheus registry
- **JVM metrics bindings** (memory, GC, threads, class loading, CPU)
- **HTTP request metrics filter** recording RED metrics (Rate, Errors, Duration)
- **BusinessMetricsHelper** for domain-specific event tracking
- **Common tags** for service identification (`application`, `environment`)

#### Adding to a Service

Add the dependency to a service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-metrics-lib')
}
```

The library auto-configures via Spring Boot's `spring.factories` mechanism. No additional configuration is needed for basic metrics.

#### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `ftgo-application` | Used as the `application` tag on all metrics |
| `ftgo.metrics.environment` | `local` | Environment tag for metrics |
| `management.endpoints.web.exposure.include` | `health,info,prometheus,metrics` | Actuator endpoints exposed |

#### Using BusinessMetricsHelper

Inject `BusinessMetricsHelper` into service classes for domain metrics:

```java
@Service
public class OrderService {

    private final BusinessMetricsHelper metrics;

    public OrderService(BusinessMetricsHelper metrics) {
        this.metrics = metrics;
    }

    public Order createOrder(CreateOrderRequest request) {
        // ... business logic ...
        metrics.orderCreated(String.valueOf(request.getRestaurantId()));
        return order;
    }
}
```

Available convenience methods:
- `orderCreated(restaurantId)` - Track order creation
- `orderStateChanged(fromState, toState)` - Track state transitions
- `deliveryAssigned(courierId)` - Track delivery assignments
- `deliveryCompleted(courierId, durationMs)` - Track delivery completions
- `consumerRegistered()` - Track new consumers
- `restaurantRegistered()` - Track new restaurants
- `menuRevised(restaurantId)` - Track menu changes
- `paymentProcessed(status)` - Track payment events

Generic methods for custom metrics:
- `incrementCounter(name, tags...)` - Increment any counter
- `recordTimer(name, duration, unit, tags...)` - Record timing
- `recordGauge(name, value, tags...)` - Record gauge value

### 2. Prometheus

**Configuration:** `deployment/monitoring/prometheus/prometheus.yml`

Scrapes all FTGO services at their `/actuator/prometheus` endpoints every 10 seconds. Pre-configured jobs:

| Job | Target | Port |
|-----|--------|------|
| `ftgo-order-service` | `order-service:8080` | 8080 |
| `ftgo-consumer-service` | `consumer-service:8080` | 8080 |
| `ftgo-restaurant-service` | `restaurant-service:8080` | 8080 |
| `ftgo-courier-service` | `courier-service:8080` | 8080 |
| `ftgo-api-gateway` | `api-gateway:8080` | 8080 |
| `ftgo-monolith` | `ftgo-application:8081` | 8081 |

**Alert Rules:** `deployment/monitoring/prometheus/alert-rules.yml`

Pre-configured alerts:
- **ServiceDown** - Service unreachable for 1 minute (critical)
- **HighErrorRate** - Error rate above 5% for 2 minutes (warning)
- **HighLatency** - p95 latency above 1 second for 5 minutes (warning)
- **HighHeapUsage** - JVM heap above 85% for 5 minutes (warning)
- **HighGcPause** - Average GC pause above 500ms (warning)
- **NoOrdersCreated** - Zero order rate for 15 minutes (warning)

### 3. Grafana Dashboards

**Location:** `deployment/monitoring/grafana/dashboards/`

Three pre-built dashboards:

#### Service Health Overview (RED Metrics)
- **UID:** `ftgo-service-health`
- Service availability status panels (UP/DOWN)
- Request rate by service and HTTP method
- Error rate (5xx) by service
- Request duration percentiles (p50, p95, p99)
- Average request duration

#### JVM Metrics
- **UID:** `ftgo-jvm-metrics`
- Heap and non-heap memory usage
- Heap memory utilization percentage
- GC pause duration and count rates
- Thread counts (live, daemon, peak) and states
- Class loading metrics
- CPU usage (system and process)
- Service selector variable for filtering

#### Business Metrics
- **UID:** `ftgo-business-metrics`
- Order creation stats and rate by restaurant
- Order state transitions
- Delivery assignment and completion rates
- Delivery duration percentiles
- Payment processing rate by status
- Menu revision rate by restaurant

## Running Locally

### Start the Monitoring Stack

```bash
docker-compose -f deployment/monitoring/docker-compose-monitoring.yml up -d
```

### Access Endpoints

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | N/A |
| Grafana | http://localhost:3000 | admin / admin |

### Stop the Monitoring Stack

```bash
docker-compose -f deployment/monitoring/docker-compose-monitoring.yml down
```

To also remove volumes:

```bash
docker-compose -f deployment/monitoring/docker-compose-monitoring.yml down -v
```

## Kubernetes Deployment

### Deploy Monitoring Stack

```bash
# Apply all monitoring resources
kubectl apply -k deployment/monitoring/kubernetes/

# Verify pods are running
kubectl get pods -n ftgo-monitoring

# Access Grafana (port-forward)
kubectl port-forward -n ftgo-monitoring svc/grafana 3000:3000

# Access Prometheus (port-forward)
kubectl port-forward -n ftgo-monitoring svc/prometheus 9090:9090
```

### Service Annotations for Auto-Discovery

For Kubernetes service discovery, add these annotations to FTGO service manifests:

```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
```

### Resources Created

| Resource | Namespace | Description |
|----------|-----------|-------------|
| Namespace `ftgo-monitoring` | - | Dedicated monitoring namespace |
| ServiceAccount `prometheus` | ftgo-monitoring | RBAC identity for Prometheus |
| ClusterRole `prometheus` | - | Permissions to scrape endpoints |
| ConfigMap `prometheus-config` | ftgo-monitoring | Prometheus configuration and alert rules |
| Deployment `prometheus` | ftgo-monitoring | Prometheus server |
| Service `prometheus` | ftgo-monitoring | ClusterIP service on port 9090 |
| PVC `prometheus-data` | ftgo-monitoring | 10Gi persistent storage |
| Secret `grafana-credentials` | ftgo-monitoring | Admin credentials |
| ConfigMap `grafana-datasources` | ftgo-monitoring | Prometheus datasource config |
| ConfigMap `grafana-dashboard-provider` | ftgo-monitoring | Dashboard provisioning |
| Deployment `grafana` | ftgo-monitoring | Grafana server |
| Service `grafana` | ftgo-monitoring | ClusterIP service on port 3000 |
| PVC `grafana-data` | ftgo-monitoring | 5Gi persistent storage |

## Metrics Reference

### HTTP Metrics (Automatic)

| Metric | Type | Labels | Description |
|--------|------|--------|-------------|
| `http_server_requests_total` | Counter | method, uri, status, outcome | Total HTTP requests |
| `http_server_requests_seconds` | Timer | method, uri, status, outcome | HTTP request duration |

### JVM Metrics (Automatic)

| Metric | Type | Description |
|--------|------|-------------|
| `jvm_memory_used_bytes` | Gauge | JVM memory usage |
| `jvm_memory_max_bytes` | Gauge | JVM memory max |
| `jvm_gc_pause_seconds` | Timer | GC pause durations |
| `jvm_threads_live_threads` | Gauge | Live thread count |
| `jvm_classes_loaded_classes` | Gauge | Loaded class count |
| `system_cpu_usage` | Gauge | System CPU usage |
| `process_cpu_usage` | Gauge | Process CPU usage |

### Business Metrics (via BusinessMetricsHelper)

| Metric | Type | Labels | Description |
|--------|------|--------|-------------|
| `ftgo_orders_created_total` | Counter | restaurant_id | Orders created |
| `ftgo_order_state_transitions_total` | Counter | from_state, to_state | Order state changes |
| `ftgo_deliveries_assigned_total` | Counter | courier_id | Delivery assignments |
| `ftgo_deliveries_completed_total` | Counter | courier_id | Completed deliveries |
| `ftgo_delivery_duration_milliseconds` | Timer | courier_id | Delivery durations |
| `ftgo_consumers_registered_total` | Counter | - | Consumer registrations |
| `ftgo_restaurants_registered_total` | Counter | - | Restaurant registrations |
| `ftgo_menu_revisions_total` | Counter | restaurant_id | Menu revisions |
| `ftgo_payments_processed_total` | Counter | status | Payment events |

## Version Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| Micrometer | 1.0.4 | Compatible with Spring Boot 2.0.x |
| Spring Boot | 2.0.3.RELEASE | Includes Actuator auto-configuration |
| Prometheus | 2.37.0 | LTS release |
| Grafana | 9.5.0 | Stable release with dashboard provisioning |
