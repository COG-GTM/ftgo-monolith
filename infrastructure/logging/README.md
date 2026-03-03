# FTGO Logging Infrastructure (EFK Stack)

This directory contains configuration for the FTGO centralized logging stack based on **Elasticsearch + Fluentd + Kibana (EFK)**.

## Structure

```
infrastructure/logging/
  elasticsearch/          # Elasticsearch configs, index templates, ILM policies
  fluentd/               # Fluentd configs and custom Docker image
  kibana/                # Kibana configs and pre-built dashboards
    dashboards/          # Exported Kibana saved objects (NDJSON)
  alerts/                # Log-based alert rule definitions
```

## Components

| Component       | Purpose                            | Port  |
|----------------|------------------------------------|-------|
| Elasticsearch  | Log storage and indexing           | 9200  |
| Fluentd        | Log collection and forwarding      | 24224 |
| Kibana         | Log visualization and dashboards   | 5601  |

## Quick Start (Docker Compose)

```bash
docker compose -f docker-compose.logging.yml up -d
```

## Kubernetes Deployment

See `infrastructure/kubernetes/base/logging/` for K8s manifests.

## Documentation

See `docs/logging-guide.md` for the full logging architecture guide.
