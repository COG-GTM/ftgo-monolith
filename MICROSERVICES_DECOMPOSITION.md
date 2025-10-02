# FTGO Microservices Decomposition

This document describes the decomposition of the FTGO monolith into four independent microservices: Consumer Service, Restaurant Service, Courier Service, and Order Service.

## Overview

The original FTGO monolith application has been refactored into four independently deployable microservices with the following characteristics:

- **Separate codebases**: Each service has its own main application class and can be run independently
- **HTTP-based communication**: Services communicate via REST APIs instead of direct method calls
- **Isolated databases**: Each service has its own database schema
- **Independent deployment**: Each service can be built, tested, and deployed separately

## Services

### 1. Consumer Service
- **Port**: 8080
- **Database**: `consumer_db`
- **Main Class**: `net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceMain`
- **Responsibilities**: Consumer/customer management and validation

#### Key Endpoints:
- `POST /consumers` - Create a new consumer
- `GET /consumers/{consumerId}` - Get consumer details
- `POST /consumers/{consumerId}/validate` - Validate order for consumer (used by Order Service)

### 2. Restaurant Service
- **Port**: 8081
- **Database**: `restaurant_db`
- **Main Class**: `net.chrisrichardson.ftgo.restaurantservice.main.RestaurantServiceMain`
- **Responsibilities**: Restaurant and menu management

#### Key Endpoints:
- `POST /restaurants` - Create a new restaurant
- `GET /restaurants/{restaurantId}` - Get restaurant details including menu items

### 3. Courier Service
- **Port**: 8082
- **Database**: `courier_db`
- **Main Class**: `net.chrisrichardson.ftgo.courierservice.main.CourierServiceMain`
- **Responsibilities**: Courier availability and delivery scheduling

#### Key Endpoints:
- `POST /couriers` - Create a new courier
- `GET /couriers/{courierId}` - Get courier details
- `POST /couriers/{courierId}/availability` - Update courier availability
- `GET /couriers/available` - Get list of available couriers (used by Order Service)

### 4. Order Service
- **Port**: 8083
- **Database**: `order_db`
- **Main Class**: `net.chrisrichardson.ftgo.orderservice.main.OrderServiceMain`
- **Responsibilities**: Order creation and lifecycle management

#### Key Endpoints:
- `POST /orders` - Create a new order
- `GET /orders/{orderId}` - Get order details
- `POST /orders/{orderId}/cancel` - Cancel an order
- `POST /orders/{orderId}/revise` - Revise an order

## Architecture Changes

### HTTP Client Communication

The Order Service now uses HTTP clients to communicate with other services:

- **ConsumerServiceClient**: Validates orders for consumers via HTTP POST to Consumer Service
- **RestaurantServiceClient**: Retrieves restaurant and menu information via HTTP GET
- **CourierServiceClient**: Retrieves available couriers for delivery scheduling via HTTP GET

### Database Isolation

Each service has its own database with separate Flyway migrations:

- `ftgo-consumer-service/src/main/resources/db/migration/V1__create_consumer_db.sql`
- `ftgo-restaurant-service/src/main/resources/db/migration/V1__create_restaurant_db.sql`
- `ftgo-courier-service/src/main/resources/db/migration/V1__create_courier_db.sql`
- `ftgo-order-service/src/main/resources/db/migration/V1__create_order_db.sql`

Foreign key constraints between services have been removed from the Order Service database, as references to consumers, restaurants, and couriers are now stored as IDs only.

### Configuration

Each service has its own `application.properties` file with:
- Separate server ports
- Independent database connections
- Service URLs for inter-service communication (Order Service only)

Example from Order Service:
```properties
consumer.service.url=http://${CONSUMER_SERVICE_HOST:localhost}:${CONSUMER_SERVICE_PORT:8080}
restaurant.service.url=http://${RESTAURANT_SERVICE_HOST:localhost}:${RESTAURANT_SERVICE_PORT:8081}
courier.service.url=http://${COURIER_SERVICE_HOST:localhost}:${COURIER_SERVICE_PORT:8082}
```

## Building and Running

### Building Individual Services

Each service can now be built independently:

```bash
# Consumer Service
./gradlew :ftgo-consumer-service:build

# Restaurant Service
./gradlew :ftgo-restaurant-service:build

# Courier Service
./gradlew :ftgo-courier-service:build

# Order Service
./gradlew :ftgo-order-service:build
```

### Running Services Locally

1. **Start MySQL databases** (you'll need 4 separate databases):
```bash
docker run -d -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_USER=mysqluser \
  -e MYSQL_PASSWORD=mysqlpw \
  -e MYSQL_DATABASE=consumer_db \
  mysql:5.7
# Repeat for restaurant_db, courier_db, and order_db on different ports or create all databases in one instance
```

2. **Run each service**:
```bash
# Consumer Service
java -jar ftgo-consumer-service/build/libs/ftgo-consumer-service.jar

# Restaurant Service
java -jar ftgo-restaurant-service/build/libs/ftgo-restaurant-service.jar

# Courier Service
java -jar ftgo-courier-service/build/libs/ftgo-courier-service.jar

# Order Service (with service URLs configured)
java -jar ftgo-order-service/build/libs/ftgo-order-service.jar
```

## Docker Deployment

Each service has a Dockerfile for containerized deployment:

```bash
# Build Docker images
docker build -t ftgo-consumer-service:latest ./ftgo-consumer-service
docker build -t ftgo-restaurant-service:latest ./ftgo-restaurant-service
docker build -t ftgo-courier-service:latest ./ftgo-courier-service
docker build -t ftgo-order-service:latest ./ftgo-order-service
```

## Kubernetes Deployment

Kubernetes deployment manifests for each service and their databases can be found in:
- `deployment/kubernetes/microservices/`

Deploy all services:
```bash
kubectl apply -f deployment/kubernetes/microservices/
```

## Key Implementation Details

### Order Domain Model Changes

The `Order` entity now supports storing `restaurantId` as a Long instead of requiring a `Restaurant` object reference. This allows the Order Service to operate independently without needing direct database access to the Restaurant table.

### Transaction Boundaries

- Each service maintains transactional consistency within its own database
- Cross-service operations (like order creation) now follow an eventual consistency model
- The Order Service validates data via HTTP calls before persisting orders

### Error Handling

HTTP clients implement appropriate error handling for service unavailability:
- Consumer validation failures return HTTP 400
- Restaurant not found throws `RestaurantNotFoundException`
- No available couriers throws `RuntimeException`

## Testing

Tests for each service can be run independently:

```bash
./gradlew :ftgo-consumer-service:test
./gradlew :ftgo-restaurant-service:test
./gradlew :ftgo-courier-service:test
./gradlew :ftgo-order-service:test
```

## Migration Path

To migrate from the monolith to microservices:

1. Deploy all four databases with separate schemas
2. Run Flyway migrations for each service
3. Deploy services in order: Consumer, Restaurant, Courier, then Order (since Order depends on the others)
4. Configure service URLs in Order Service
5. Test inter-service communication
6. Update client applications to use individual service endpoints or implement an API Gateway

## Future Improvements

1. **Service Discovery**: Implement Eureka or Consul for dynamic service discovery
2. **API Gateway**: Add Spring Cloud Gateway or Kong for unified entry point
3. **Circuit Breakers**: Implement Hystrix or Resilience4j for fault tolerance
4. **Distributed Tracing**: Add Zipkin or Jaeger for request tracing across services
5. **Event-Driven Communication**: Consider using message queues (RabbitMQ, Kafka) for asynchronous communication
6. **Service Mesh**: Implement Istio or Linkerd for advanced networking features
7. **Centralized Configuration**: Use Spring Cloud Config or Consul for configuration management
8. **Database per Service Pattern**: Currently using shared domain models; consider creating service-specific data models

## Related Documentation

- Original monolith README: [README.adoc](README.adoc)
- Microservices Patterns book: https://microservices.io/book
