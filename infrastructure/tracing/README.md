# FTGO Distributed Tracing Infrastructure

This directory contains infrastructure configuration for deploying Jaeger as the distributed tracing backend for FTGO microservices.

## Components

- **Docker Compose** - Local development deployment of Jaeger all-in-one
- **Kubernetes** - Production-ready K8s manifests for Jaeger deployment

## Quick Start (Docker Compose)

```bash
cd infrastructure/tracing
docker-compose up -d
```

Jaeger UI will be available at: http://localhost:16686

## Kubernetes Deployment

```bash
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/jaeger-pvc.yaml
kubectl apply -f kubernetes/jaeger-deployment.yaml
kubectl apply -f kubernetes/jaeger-service.yaml
```

## Ports Reference

| Port  | Protocol | Purpose                    |
|-------|----------|----------------------------|
| 6831  | UDP      | Jaeger Agent (compact)     |
| 6832  | UDP      | Jaeger Agent (binary)      |
| 5778  | TCP      | Agent config               |
| 16686 | TCP      | Jaeger Query UI            |
| 4317  | TCP      | OTLP gRPC receiver         |
| 4318  | TCP      | OTLP HTTP receiver         |
| 14250 | TCP      | Model proto                |
| 14268 | TCP      | Collector HTTP             |
| 9411  | TCP      | Zipkin compatible endpoint |
