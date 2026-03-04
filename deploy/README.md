# FTGO Deployment

## Scripts

| Script | Description |
|--------|-------------|
| `scripts/build-service.sh` | Build a specific microservice via Gradle |
| `scripts/build-docker-image.sh` | Build Docker image for a microservice |

## Terraform

Placeholder for infrastructure-as-code definitions. To be populated as the platform deployment is defined.

## Usage

```bash
# Build a service
./deploy/scripts/build-service.sh ftgo-order-service

# Build a Docker image
./deploy/scripts/build-docker-image.sh ftgo-order-service v1.0.0
```
