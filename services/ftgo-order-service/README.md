# FTGO Order Service

Manages the complete order lifecycle for the FTGO platform, including order creation, acceptance, preparation tracking, and delivery coordination.

## Bounded Context

The Order bounded context owns:
- Order creation and validation
- Order state machine (APPROVAL_PENDING -> APPROVED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP -> DELIVERED)
- Ticket management for kitchen orders
- Order revision handling

## Package Structure

```
com.ftgo.orderservice
  .domain        - Order aggregate, OrderService, OrderRepository, Ticket
  .application   - Use cases (CreateOrder, AcceptOrder, ReviseOrder)
  .web           - OrderController, TicketController, DTOs
  .config        - Spring configuration
  .events        - OrderCreated, OrderAccepted, OrderDelivered events
  .messaging     - Event publishers/subscribers
```

## Building

```bash
./gradlew :services:ftgo-order-service:build
./gradlew :services:ftgo-order-service:test
```

## Running Locally

```bash
docker-compose -f services/ftgo-order-service/docker/docker-compose.yml up
```

## API Endpoints

| Method | Path                     | Description                |
|--------|--------------------------|----------------------------|
| POST   | /orders                  | Create a new order         |
| GET    | /orders/{id}             | Get order by ID            |
| POST   | /orders/{id}/revise      | Revise an existing order   |
| POST   | /orders/{id}/accept      | Accept an order            |
| GET    | /tickets/{id}            | Get ticket by ID           |

## Port Assignment

| Environment | Port |
|-------------|------|
| Local       | 8082 |
| Docker      | 8082 |
| K8s Service | 80   |
