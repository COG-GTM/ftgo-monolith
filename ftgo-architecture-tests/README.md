# ftgo-architecture-tests

ArchUnit tests that enforce the module boundaries of the FTGO monolith. They run as part of
`./gradlew build` / `./gradlew test`, so a boundary violation fails the build. Run them on their own with:

    ./gradlew :ftgo-architecture-tests:test

## Module model

| Service module            | Internal packages (private)           | Public API module / package                                          |
|---------------------------|---------------------------------------|----------------------------------------------------------------------|
| `ftgo-consumer-service`   | `net.chrisrichardson.ftgo.consumerservice..`   | `ftgo-consumer-service-api` / `...consumerservice.api..`    |
| `ftgo-order-service`      | `net.chrisrichardson.ftgo.orderservice..`      | `ftgo-order-service-api` / `...orderservice.api..`          |
| `ftgo-restaurant-service` | `net.chrisrichardson.ftgo.restaurantservice..` | `ftgo-restaurant-service-api` / `...restaurantservice.events..` |
| `ftgo-courier-service`    | `net.chrisrichardson.ftgo.courierservice..`    | `ftgo-courier-service-api` / `...courierservice.api..`      |

Shared modules: `ftgo-common` (`...ftgo.common..`), `ftgo-domain` (`...ftgo.domain..`), `ftgo-test-util`.
`ftgo-application` (root package `net.chrisrichardson.ftgo`) is the composition root.

## Rules (`ModuleBoundariesTest`)

1. **`<service>_internals_are_private`** – code outside a service module may only depend on that service's API
   package, never on its `domain`, `web`, `main`, ... packages.
   *Exception:* the composition root (`ftgo-application`) may reference classes annotated with Spring's
   `@Configuration` in order to wire the modules together.
2. **`api_modules_only_depend_on_common`** – service API packages may only depend on `ftgo-common`, other API
   packages and third-party libraries (no service internals, no `ftgo-domain`).
3. **`shared_modules_do_not_depend_on_services`** – `ftgo-common`, `ftgo-domain` and `ftgo-test-util` must not
   depend on any service module.
4. **`service_modules_are_free_of_cycles`** – no dependency cycles between service modules.

## Violations found when the rules were introduced

| Violation | Resolution |
|-----------|------------|
| `ftgo-order-service` (`OrderService`, `OrderConfiguration`) used `consumerservice.domain.ConsumerService`, and the order service's Gradle build depended on `ftgo-consumer-service`. | **Fixed.** Added `ConsumerValidationService` to `ftgo-consumer-service-api`; `ConsumerService` implements it and the order service depends only on the interface. The `ftgo-order-service -> ftgo-consumer-service` Gradle dependency was removed. |
| `ftgo-application`'s `GlobalExceptionHandler` handles `orderservice.domain.OrderNotFoundException`, `orderservice.domain.RestaurantNotFoundException` and `courierservice.domain.CourierNotFoundException`. | **Documented / allow-listed** in `ModuleBoundariesTest.KNOWN_VIOLATIONS`. Fix by moving these exceptions into the respective API modules (or by giving each service its own `@ControllerAdvice`), then delete the entries. |

## Known architectural debt not covered by these rules

* `ftgo-domain` is a shared kernel: the JPA entities and repositories of *all* services (`Order`, `Consumer`,
  `Courier`, `Restaurant`, ...) live in one module that every service depends on. This is allowed by the rules
  above because it is not another service's internals, but it is the main obstacle to splitting the monolith.
* Intra-module layering is not enforced yet, e.g. `orderservice.domain.OrderService` depends on
  `orderservice.web.MenuItemIdAndQuantity`.

## Adding a known violation

Prefer fixing the dependency. If that is not possible, add a narrowly scoped
`"originClass -> targetClass"` entry to `KNOWN_VIOLATIONS` and document it in the table above.
