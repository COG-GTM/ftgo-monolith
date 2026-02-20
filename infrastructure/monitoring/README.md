# FTGO Monitoring Infrastructure

Prometheus and Grafana monitoring configuration for FTGO microservices.

## Directory Structure

```
infrastructure/monitoring/
  prometheus/
    prometheus.yml          # Prometheus scrape configuration
  grafana/
    provisioning/
      datasources.yml       # Grafana datasource provisioning
      dashboards.yml        # Grafana dashboard provisioning
    dashboards/
      order-service-dashboard.json
      consumer-service-dashboard.json
      courier-service-dashboard.json
      restaurant-service-dashboard.json
```

## Prometheus Configuration

The `prometheus.yml` file configures scrape targets for all FTGO services:

| Service | Target | Scrape Path | Interval |
|---------|--------|-------------|----------|
| order-service | order-service:8080 | /actuator/prometheus | 10s |
| consumer-service | consumer-service:8080 | /actuator/prometheus | 10s |
| courier-service | courier-service:8080 | /actuator/prometheus | 10s |
| restaurant-service | restaurant-service:8080 | /actuator/prometheus | 10s |

### Customizing Targets

For non-Docker environments, update the `targets` field with actual host:port values:

```yaml
static_configs:
  - targets: ['localhost:8081']
```

## Grafana Dashboards

### Dashboard Overview

Each service dashboard contains three sections:

1. **Business Metrics**: Domain-specific counters, rates, and gauges
2. **HTTP Metrics**: Request rates and p95 response times by endpoint
3. **JVM Metrics**: Heap memory usage and thread counts

### Importing Dashboards

Dashboards are automatically provisioned when using the provisioning configuration. For manual import:

1. Open Grafana UI
2. Navigate to Dashboards > Import
3. Upload the JSON file or paste its contents
4. Select the Prometheus datasource

### Dashboard UIDs

| Dashboard | UID |
|-----------|-----|
| Order Service | ftgo-order-service |
| Consumer Service | ftgo-consumer-service |
| Courier Service | ftgo-courier-service |
| Restaurant Service | ftgo-restaurant-service |

## Quick Start with Docker Compose

```yaml
services:
  prometheus:
    image: prom/prometheus:v2.50.1
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:10.3.3
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```
