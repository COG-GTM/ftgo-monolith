# FTGO Microservices Kubernetes Deployment

This directory contains Kubernetes deployment configurations for the FTGO microservices architecture.

## Prerequisites

Before deploying the services, you must create the MySQL secrets that are referenced by the database deployments.

### Creating MySQL Secrets

Run the following command to create the required secrets:

```bash
kubectl create secret generic mysql-secrets \
  --from-literal=root-password=YOUR_ROOT_PASSWORD \
  --from-literal=mysql-user=YOUR_MYSQL_USER \
  --from-literal=mysql-password=YOUR_MYSQL_PASSWORD
```

Replace the placeholder values with your actual database credentials.

For local development/testing, you can use:

```bash
kubectl create secret generic mysql-secrets \
  --from-literal=root-password=rootpassword \
  --from-literal=mysql-user=mysqluser \
  --from-literal=mysql-password=mysqlpw
```

## Deployment Order

1. Create the MySQL secrets (see above)
2. Deploy MySQL databases:
   - `kubectl apply -f consumer-mysql-deployment.yml`
   - `kubectl apply -f restaurant-mysql-deployment.yml`
   - `kubectl apply -f courier-mysql-deployment.yml`
   - `kubectl apply -f order-mysql-deployment.yml`
3. Wait for databases to be ready
4. Deploy the services:
   - `kubectl apply -f consumer-service-deployment.yml`
   - `kubectl apply -f restaurant-service-deployment.yml`
   - `kubectl apply -f courier-service-deployment.yml`
   - `kubectl apply -f order-service-deployment.yml`

## Services

- **Consumer Service**: Port 8081, Database: ftgo_consumer
- **Restaurant Service**: Port 8082, Database: ftgo_restaurant
- **Courier Service**: Port 8083, Database: ftgo_courier
- **Order Service**: Port 8084, Database: ftgo_order

## Architecture

Each service has its own dedicated MySQL database and communicates with other services via HTTP REST APIs. The Order Service acts as a consumer of the Consumer, Restaurant, and Courier services.
