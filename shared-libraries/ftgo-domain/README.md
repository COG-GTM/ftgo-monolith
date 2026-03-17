# ftgo-domain-lib

Shared JPA entities, repositories, and domain model for the FTGO microservices platform.

## Version

**Current version:** `1.0.0`

## Contents

### Entities (`@Entity`)

| Class | Table | Description | Target Service |
|-------|-------|-------------|----------------|
| `Order` | `orders` | Core order entity with state machine, delivery info, line items | Order Service |
| `Consumer` | `consumers` | Consumer entity with PersonName | Consumer Service |
| `Restaurant` | `restaurants` | Restaurant entity with menu and address | Restaurant Service |
| `Courier` | `courier` | Courier entity with availability and delivery plan | Courier Service |

### Value Objects (`@Embeddable`)

| Class | Description | Target Service |
|-------|-------------|----------------|
| `OrderLineItem` | Order line item with quantity, name, price | Order Service |
| `OrderLineItems` | Collection wrapper for order line items | Order Service |
| `MenuItem` | Menu item with id, name, price | Restaurant Service |
| `RestaurantMenu` | Collection wrapper for menu items | Restaurant Service |
| `DeliveryInformation` | Delivery time and address | Order Service |
| `PaymentInformation` | Payment token | Order Service |
| `Plan` | Courier delivery plan with actions | Courier Service |
| `Action` | Individual courier action (pickup/dropoff) | Courier Service |

### Enums

| Class | Description | Target Service |
|-------|-------------|----------------|
| `OrderState` | Order lifecycle states (APPROVED, ACCEPTED, PREPARING, etc.) | Order Service |
| `ActionType` | Courier action types (PICKUP, DROPOFF) | Courier Service |

### Domain Classes

| Class | Description | Target Service |
|-------|-------------|----------------|
| `OrderRevision` | Order revision tracking (delivery info, quantity changes) | Order Service |
| `LineItemQuantityChange` | Tracks order total changes during revision | Order Service |
| `OrderMinimumNotMetException` | Domain exception for order minimum validation | Order Service |

### Repositories

| Interface | Entity | Target Service |
|-----------|--------|----------------|
| `OrderRepository` | `Order` | Order Service |
| `ConsumerRepository` | `Consumer` | Consumer Service |
| `RestaurantRepository` | `Restaurant` | Restaurant Service |
| `CourierRepository` | `Courier` | Courier Service |

### Configuration

| Class | Description |
|-------|-------------|
| `DomainConfiguration` | Spring `@Configuration` with JPA auto-configuration, entity scan, and repository enabling |

## Entity-to-Service Ownership Mapping

| Bounded Context | Entities | Repositories |
|----------------|----------|-------------|
| **Consumer Service** | `Consumer` | `ConsumerRepository` |
| **Restaurant Service** | `Restaurant`, `RestaurantMenu`, `MenuItem` | `RestaurantRepository` |
| **Order Service** | `Order`, `OrderLineItem`, `OrderLineItems`, `OrderState`, `OrderRevision`, `LineItemQuantityChange`, `DeliveryInformation`, `PaymentInformation`, `OrderMinimumNotMetException` | `OrderRepository` |
| **Courier Service** | `Courier`, `Plan`, `Action`, `ActionType` | `CourierRepository` |

## Package

All classes live under:

```
net.chrisrichardson.ftgo.domain
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `ftgo-common` (project) | Shared value objects (`Money`, `Address`, `PersonName`) |
| `ftgo-common-jpa` (project) | JPA ORM mappings for value objects |
| `spring-boot-starter-data-jpa` | Spring Data JPA, Hibernate |
| `jakarta.persistence-api` | JPA annotations |

## Usage

### Gradle dependency (from local Maven repository)

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-domain-lib:1.0.0'
}
```

### Intra-project dependency (within the mono-repo)

```groovy
dependencies {
    implementation project(":shared-libraries:ftgo-domain")
}
```

### Publishing to local repository

```bash
./gradlew :shared-libraries:ftgo-domain:publishToMavenLocal
```

Or publish to the project-level repository:

```bash
./gradlew :shared-libraries:ftgo-domain:publish
```
