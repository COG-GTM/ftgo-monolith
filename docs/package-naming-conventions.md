# FTGO Package Naming Conventions

## Package Root

All new microservice code uses `com.ftgo` as the package root.

> **Migration Note:** Existing monolith code retains the `net.chrisrichardson.ftgo` package root. A full package migration is planned as a separate task once all services are extracted.

## Package Structure Pattern

```
com.ftgo.<service>.<layer>
```

Where:
- `<service>` is the bounded context name in lowercase without hyphens (e.g., `orderservice`, `consumerservice`)
- `<layer>` is the architectural layer

## Layer Definitions

| Layer          | Package                              | Contents                                                  |
|---------------|--------------------------------------|-----------------------------------------------------------|
| `domain`      | `com.ftgo.<svc>.domain`              | Entities, aggregates, value objects, repository interfaces, domain services, domain exceptions |
| `application` | `com.ftgo.<svc>.application`         | Application services (use cases), command/query handlers   |
| `web`         | `com.ftgo.<svc>.web`                 | REST controllers, request/response DTOs, exception handlers|
| `config`      | `com.ftgo.<svc>.config`              | Spring `@Configuration` classes, bean definitions          |
| `events`      | `com.ftgo.<svc>.events`              | Domain event classes, event publisher interfaces            |
| `messaging`   | `com.ftgo.<svc>.messaging`           | Message channel adapters, async consumer/producer handlers  |

## Service Package Map

### Consumer Service
```
com.ftgo.consumerservice
com.ftgo.consumerservice.domain
com.ftgo.consumerservice.application
com.ftgo.consumerservice.web
com.ftgo.consumerservice.config
com.ftgo.consumerservice.events
com.ftgo.consumerservice.messaging
```

### Order Service
```
com.ftgo.orderservice
com.ftgo.orderservice.domain
com.ftgo.orderservice.application
com.ftgo.orderservice.web
com.ftgo.orderservice.config
com.ftgo.orderservice.events
com.ftgo.orderservice.messaging
```

### Restaurant Service
```
com.ftgo.restaurantservice
com.ftgo.restaurantservice.domain
com.ftgo.restaurantservice.application
com.ftgo.restaurantservice.web
com.ftgo.restaurantservice.config
com.ftgo.restaurantservice.events
com.ftgo.restaurantservice.messaging
```

### Courier Service
```
com.ftgo.courierservice
com.ftgo.courierservice.domain
com.ftgo.courierservice.application
com.ftgo.courierservice.web
com.ftgo.courierservice.config
com.ftgo.courierservice.events
com.ftgo.courierservice.messaging
```

## API Module Packages

API modules contain DTOs and event definitions shared between services.

```
com.ftgo.<service>.api.web        # Request/response DTOs for REST APIs
com.ftgo.<service>.api.events     # Domain events published by the service
```

## Shared Library Packages

| Library          | Package                  |
|-----------------|--------------------------|
| ftgo-common     | `com.ftgo.common`        |
| ftgo-common-jpa | `com.ftgo.common.jpa`    |
| ftgo-domain     | `com.ftgo.domain`        |
| ftgo-test-util  | `com.ftgo.testutil`      |
| common-swagger  | `com.ftgo.common.swagger` |

## Naming Rules

1. **Service names** are concatenated without hyphens: `orderservice`, not `order.service` or `order_service`
2. **No abbreviations** in package names: `consumerservice`, not `consumersvc`
3. **Singular nouns** for layer names: `domain`, not `domains`
4. **API modules** always include `.api.` in the path to distinguish from internal code
5. **Test packages** mirror the main source package structure exactly

## Mapping from Legacy to New Packages

| Legacy Package                                        | New Package                              |
|------------------------------------------------------|------------------------------------------|
| `net.chrisrichardson.ftgo.orderservice.domain`       | `com.ftgo.orderservice.domain`           |
| `net.chrisrichardson.ftgo.orderservice.web`          | `com.ftgo.orderservice.web`              |
| `net.chrisrichardson.ftgo.orderservice.main`         | `com.ftgo.orderservice.config`           |
| `net.chrisrichardson.ftgo.consumerservice.domain`    | `com.ftgo.consumerservice.domain`        |
| `net.chrisrichardson.ftgo.consumerservice.web`       | `com.ftgo.consumerservice.web`           |
| `net.chrisrichardson.ftgo.restaurantservice.domain`  | `com.ftgo.restaurantservice.domain`      |
| `net.chrisrichardson.ftgo.restaurantservice.web`     | `com.ftgo.restaurantservice.web`         |
| `net.chrisrichardson.ftgo.courierservice.domain`     | `com.ftgo.courierservice.domain`         |
| `net.chrisrichardson.ftgo.courierservice.web`        | `com.ftgo.courierservice.web`            |
| `net.chrisrichardson.ftgo.common`                    | `com.ftgo.common`                        |
| `net.chrisrichardson.ftgo.domain`                    | `com.ftgo.domain`                        |
