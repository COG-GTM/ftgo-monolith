# Entity-to-Service Ownership Mapping

This document maps domain entities from `shared-libraries/ftgo-domain` to their owning microservices. Each entity will ultimately be owned by a single service as the monolith is decomposed.

## Ownership Matrix

| Entity / Class              | Package                                      | Type         | Owning Service       | Notes                                              |
|-----------------------------|----------------------------------------------|--------------|----------------------|----------------------------------------------------|
| `Order`                     | `net.chrisrichardson.ftgo.domain`            | Entity       | order-service        | Central order entity with state machine             |
| `OrderState`                | `net.chrisrichardson.ftgo.domain`            | Enum         | order-service        | Order lifecycle states (APPROVED, ACCEPTED, etc.)   |
| `OrderLineItem`             | `net.chrisrichardson.ftgo.domain`            | Embeddable   | order-service        | Line item within an order                           |
| `OrderLineItems`            | `net.chrisrichardson.ftgo.domain`            | Embeddable   | order-service        | Collection wrapper for order line items             |
| `OrderRevision`             | `net.chrisrichardson.ftgo.domain`            | POJO         | order-service        | Revision data for modifying orders                  |
| `OrderMinimumNotMetException`| `net.chrisrichardson.ftgo.domain`           | Exception    | order-service        | Business rule exception for order minimums          |
| `OrderRepository`           | `net.chrisrichardson.ftgo.domain`            | Repository   | order-service        | Spring Data repository for Order                    |
| `LineItemQuantityChange`    | `net.chrisrichardson.ftgo.domain`            | POJO         | order-service        | Tracks price deltas when quantities change          |
| `Consumer`                  | `net.chrisrichardson.ftgo.domain`            | Entity       | consumer-service     | Customer entity with order validation               |
| `ConsumerRepository`        | `net.chrisrichardson.ftgo.domain`            | Repository   | consumer-service     | Spring Data repository for Consumer                 |
| `Restaurant`                | `net.chrisrichardson.ftgo.domain`            | Entity       | restaurant-service   | Restaurant entity with menu items                   |
| `RestaurantMenu`            | `net.chrisrichardson.ftgo.domain`            | Embeddable   | restaurant-service   | Embedded menu within a restaurant                   |
| `MenuItem`                  | `net.chrisrichardson.ftgo.domain`            | Embeddable   | restaurant-service   | Individual menu item with pricing                   |
| `RestaurantRepository`      | `net.chrisrichardson.ftgo.domain`            | Repository   | restaurant-service   | Spring Data repository for Restaurant               |
| `Courier`                   | `net.chrisrichardson.ftgo.domain`            | Entity       | courier-service      | Courier entity with availability and delivery plan  |
| `Plan`                      | `net.chrisrichardson.ftgo.domain`            | Embeddable   | courier-service      | Courier delivery plan with ordered actions           |
| `Action`                    | `net.chrisrichardson.ftgo.domain`            | Embeddable   | courier-service      | Single pickup/dropoff action in a delivery plan     |
| `ActionType`                | `net.chrisrichardson.ftgo.domain`            | Enum         | courier-service      | PICKUP or DROPOFF action types                      |
| `CourierRepository`         | `net.chrisrichardson.ftgo.domain`            | Repository   | courier-service      | Spring Data repository for Courier                  |
| `DeliveryInformation`       | `net.chrisrichardson.ftgo.domain`            | Embeddable   | order-service        | Delivery address and time embedded in Order         |
| `PaymentInformation`        | `net.chrisrichardson.ftgo.domain`            | Embeddable   | order-service        | Payment token embedded in Order                     |
| `DomainConfiguration`       | `net.chrisrichardson.ftgo.domain`            | Configuration| shared (all services)| Spring Boot auto-configuration for JPA and scanning |

## Service Summary

| Service              | Owned Entities                                                                 |
|----------------------|--------------------------------------------------------------------------------|
| **order-service**    | Order, OrderState, OrderLineItem, OrderLineItems, OrderRevision, OrderMinimumNotMetException, OrderRepository, LineItemQuantityChange, DeliveryInformation, PaymentInformation |
| **consumer-service** | Consumer, ConsumerRepository                                                   |
| **restaurant-service** | Restaurant, RestaurantMenu, MenuItem, RestaurantRepository                   |
| **courier-service**  | Courier, Plan, Action, ActionType, CourierRepository                           |
| **shared**           | DomainConfiguration                                                            |

## Cross-Service References

During the migration, the following cross-service entity references must be resolved:

| Reference                    | From Service     | To Service          | Resolution Strategy                    |
|------------------------------|------------------|---------------------|----------------------------------------|
| `Order.restaurant`           | order-service    | restaurant-service  | Replace with `restaurantId` (Long)     |
| `Order.assignedCourier`      | order-service    | courier-service     | Replace with `courierId` (Long)        |
| `Action.order`               | courier-service  | order-service       | Replace with `orderId` (Long)          |

## Shared Libraries

| Library                       | Version | Contents                                                    |
|-------------------------------|---------|-------------------------------------------------------------|
| `shared-libraries/ftgo-common`     | 1.0.0   | Value objects (Money, Address, PersonName), JSON config     |
| `shared-libraries/ftgo-common-jpa` | 1.0.0   | JPA ORM mappings for common value objects (orm.xml)         |
| `shared-libraries/ftgo-domain`     | 1.0.0   | All domain entities, repositories, and configuration        |

## Migration Notes

- All entities currently reside in the shared `ftgo-domain` library for backward compatibility
- As services are extracted, entities will migrate to their owning service module
- Cross-service JPA relationships (`@ManyToOne`) will be replaced with ID references and API calls
- The `DomainConfiguration` class will be split per service during extraction
