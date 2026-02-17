# FTGO Restaurant Service

Manages restaurant registration, menu management, and restaurant information for the FTGO platform.

## Bounded Context

The Restaurant bounded context owns:
- Restaurant registration and profile data
- Menu management (items, prices, availability)
- Restaurant search and discovery

## Package Structure

```
com.ftgo.restaurantservice
  .domain        - Restaurant entity, RestaurantService, RestaurantRepository
  .application   - Use cases (CreateRestaurant, UpdateMenu)
  .web           - RestaurantController, DTOs
  .config        - Spring configuration
  .events        - RestaurantCreated, MenuUpdated events
  .messaging     - Event publishers/subscribers
```

## Building

```bash
./gradlew :services:ftgo-restaurant-service:build
./gradlew :services:ftgo-restaurant-service:test
```

## Running Locally

```bash
docker-compose -f services/ftgo-restaurant-service/docker/docker-compose.yml up
```

## API Endpoints

| Method | Path                          | Description                    |
|--------|-------------------------------|--------------------------------|
| POST   | /restaurants                  | Register a new restaurant      |
| GET    | /restaurants/{id}             | Get restaurant by ID           |
| PUT    | /restaurants/{id}/menu        | Update restaurant menu         |

## Port Assignment

| Environment | Port |
|-------------|------|
| Local       | 8083 |
| Docker      | 8083 |
| K8s Service | 80   |
