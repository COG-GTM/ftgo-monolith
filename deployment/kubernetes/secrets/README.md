# Kubernetes Secrets Configuration

Before deploying the FTGO microservices, you must create the following Kubernetes Secrets for database credentials.

## Required Secrets

### Consumer Service Database Secret
```bash
kubectl create secret generic consumer-mysql-secret \
  --from-literal=username=<your-username> \
  --from-literal=password=<your-password>
```

### Restaurant Service Database Secret
```bash
kubectl create secret generic restaurant-mysql-secret \
  --from-literal=username=<your-username> \
  --from-literal=password=<your-password>
```

### Courier Service Database Secret
```bash
kubectl create secret generic courier-mysql-secret \
  --from-literal=username=<your-username> \
  --from-literal=password=<your-password>
```

### Order Service Database Secret
```bash
kubectl create secret generic order-mysql-secret \
  --from-literal=username=<your-username> \
  --from-literal=password=<your-password>
```

## Environment Variables

Each microservice requires the following environment variables:
- `DB_USERNAME`: Database username (provided via Secret)
- `DB_PASSWORD`: Database password (provided via Secret)
- `DOCKER_HOST_IP`: MySQL host IP (defaults to localhost for local development)

For the Order Service, additional environment variables for service discovery:
- `CONSUMER_SERVICE_HOST`: Consumer service hostname (defaults to localhost)
- `RESTAURANT_SERVICE_HOST`: Restaurant service hostname (defaults to localhost)
- `COURIER_SERVICE_HOST`: Courier service hostname (defaults to localhost)

## Local Development

For local development, export these environment variables before starting the services:
```bash
export DB_USERNAME=mysqluser
export DB_PASSWORD=mysqlpw
```
