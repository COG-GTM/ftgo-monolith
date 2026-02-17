# FTGO Consumer Service

Manages consumer registration, profile management, and consumer verification for the FTGO platform.

## Bounded Context

The Consumer bounded context owns:
- Consumer registration and profile data
- Consumer address management
- Consumer payment method validation
- Consumer verification for order placement

## Package Structure

```
com.ftgo.consumerservice
  .domain        - Consumer entity, ConsumerService, ConsumerRepository
  .application   - Use cases (RegisterConsumer, VerifyConsumer)
  .web           - ConsumerController, DTOs
  .config        - Spring configuration
  .events        - ConsumerCreated, ConsumerVerified events
  .messaging     - Event publishers/subscribers
```

## Building

```bash
./gradlew :services:ftgo-consumer-service:build
./gradlew :services:ftgo-consumer-service:test
```

## Running Locally

```bash
docker-compose -f services/ftgo-consumer-service/docker/docker-compose.yml up
```

## API Endpoints

| Method | Path                | Description              |
|--------|---------------------|--------------------------|
| POST   | /consumers          | Register a new consumer  |
| GET    | /consumers/{id}     | Get consumer by ID       |

## Port Assignment

| Environment | Port |
|-------------|------|
| Local       | 8081 |
| Docker      | 8081 |
| K8s Service | 80   |
