# Entity-to-Service Ownership Mapping

## Overview

This document maps each domain entity in the FTGO system to its owning microservice.
During the monolith decomposition, each entity will be managed by exactly one service
(the "owner"), while other services access it through DTOs and API contracts.

## Entity Ownership

| Entity | Package | Owning Service | Database Table | Description |
|--------|---------|---------------|----------------|-------------|
| `Order` | `net.chrisrichardson.ftgo.domain` | `ftgo-order-service` | `orders` | Central order entity with state machine lifecycle |
| `OrderLineItems` | `net.chrisrichardson.ftgo.domain` | `ftgo-order-service` | `order_line_items` | Embedded collection of line items within an order |
| `OrderLineItem` | `net.chrisrichardson.ftgo.domain` | `ftgo-order-service` | `order_line_items` | Individual line item (menu item, quantity, price) |
| `Consumer` | `net.chrisrichardson.ftgo.domain` | `ftgo-consumer-service` | `consumers` | Customer entity with personal name |
| `Restaurant` | `net.chrisrichardson.ftgo.domain` | `ftgo-restaurant-service` | `restaurants` | Restaurant entity with address and menu |
| `RestaurantMenu` | `net.chrisrichardson.ftgo.domain` | `ftgo-restaurant-service` | `restaurant_menu_items` | Embedded menu containing menu items |
| `MenuItem` | `net.chrisrichardson.ftgo.domain` | `ftgo-restaurant-service` | `restaurant_menu_items` | Individual menu item (id, name, price) |
| `Courier` | `net.chrisrichardson.ftgo.domain` | `ftgo-courier-service` | `courier` | Delivery courier with availability and plan |
| `Plan` | `net.chrisrichardson.ftgo.domain` | `ftgo-courier-service` | (embedded) | Courier's delivery plan with actions |
| `Action` | `net.chrisrichardson.ftgo.domain` | `ftgo-courier-service` | (embedded) | Individual pickup/dropoff action in a plan |

## Value Objects (Shared)

These value objects are used across multiple services and reside in `shared/ftgo-common`:

| Value Object | Package | Used By | Description |
|-------------|---------|---------|-------------|
| `Money` | `net.chrisrichardson.ftgo.common` | All services | Monetary amount with arithmetic operations |
| `Address` | `net.chrisrichardson.ftgo.common` | Restaurant, Courier, Order services | Physical address (street, city, state, zip) |
| `PersonName` | `net.chrisrichardson.ftgo.common` | Consumer, Courier services | First/last name pair |

## Enums and Supporting Types

| Type | Owning Service | Description |
|------|---------------|-------------|
| `OrderState` | `ftgo-order-service` | Order lifecycle states (APPROVED, ACCEPTED, PREPARING, etc.) |
| `ActionType` | `ftgo-courier-service` | Courier action types (PICKUP, DROPOFF) |
| `OrderRevision` | `ftgo-order-service` | Order modification request |
| `DeliveryInformation` | `ftgo-order-service` | Delivery address and timing for an order |
| `PaymentInformation` | `ftgo-order-service` | Payment token for an order |
| `LineItemQuantityChange` | `ftgo-order-service` | Tracks quantity change impact on order total |
| `OrderMinimumNotMetException` | `ftgo-order-service` | Business rule exception |

## Repositories

| Repository | Owning Service | Entity | Description |
|-----------|---------------|--------|-------------|
| `OrderRepository` | `ftgo-order-service` | `Order` | CRUD + find by consumer ID |
| `ConsumerRepository` | `ftgo-consumer-service` | `Consumer` | CRUD operations |
| `RestaurantRepository` | `ftgo-restaurant-service` | `Restaurant` | CRUD operations |
| `CourierRepository` | `ftgo-courier-service` | `Courier` | CRUD + find available couriers |

## Cross-Service Dependencies

The following cross-entity references exist in the current monolith and must be
resolved during decomposition:

| From Entity | To Entity | Relationship | Migration Strategy |
|------------|-----------|-------------|-------------------|
| `Order` | `Restaurant` | `@ManyToOne` | Replace with `restaurantId` (Long) + API call |
| `Order` | `Courier` | `@ManyToOne` | Replace with `courierId` (Long) + API call |
| `Order` | `Consumer` | `consumerId` field | Already uses ID reference (no change needed) |
| `Action` | `Order` | `@ManyToOne` | Replace with `orderId` (Long) + API call |

## Cross-Service DTO Contracts

The `net.chrisrichardson.ftgo.domain.dto` package provides transport objects for
cross-service communication:

| DTO | Corresponding Entity | Used For |
|-----|---------------------|----------|
| `OrderDTO` | `Order` | Order details in inter-service calls |
| `OrderLineItemDTO` | `OrderLineItem` | Line item details without JPA dependencies |
| `ConsumerDTO` | `Consumer` | Consumer info shared with order/courier services |
| `RestaurantDTO` | `Restaurant` | Restaurant info shared with order service |
| `MenuItemDTO` | `MenuItem` | Menu item info without JPA annotations |
| `CourierDTO` | `Courier` | Courier info shared with order service |
| `DeliveryInformationDTO` | `DeliveryInformation` | Delivery details for courier service |
