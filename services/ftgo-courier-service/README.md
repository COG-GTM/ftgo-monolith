# FTGO Courier Service

Manages courier registration, availability tracking, and delivery plan management for the FTGO platform.

## Bounded Context

The Courier bounded context owns:
- Courier registration and profile data
- Courier availability management
- Delivery plan and action tracking
- Courier location updates

## Package Structure

```
com.ftgo.courierservice
  .domain        - Courier entity, CourierService, CourierRepository, Plan, Action
  .application   - Use cases (RegisterCourier, UpdateAvailability)
  .web           - CourierController, DTOs
  .config        - Spring configuration
  .events        - CourierCreated, CourierAvailabilityUpdated events
  .messaging     - Event publishers/subscribers
```

## Building

```bash
./gradlew :services:ftgo-courier-service:build
./gradlew :services:ftgo-courier-service:test
```

## Running Locally

```bash
docker-compose -f services/ftgo-courier-service/docker/docker-compose.yml up
```

## API Endpoints

| Method | Path                              | Description                     |
|--------|-----------------------------------|---------------------------------|
| POST   | /couriers                         | Register a new courier          |
| GET    | /couriers/{id}                    | Get courier by ID               |
| POST   | /couriers/{id}/availability       | Update courier availability     |

## Port Assignment

| Environment | Port |
|-------------|------|
| Local       | 8084 |
| Docker      | 8084 |
| K8s Service | 80   |
