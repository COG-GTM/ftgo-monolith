# Consumer Service Extraction (Strangler-Fig)

This document describes the extraction of the **Consumer** bounded context out of
the FTGO monolith into a standalone Spring Boot microservice, using the
Strangler-Fig pattern. The monolith continues to function unchanged from the
outside; the only difference is that consumer validation now happens over HTTP
against an independently deployable service.

## Why Consumer was the extraction candidate

A coupling analysis across the monolith's bounded contexts (Order, Restaurant,
Courier, Consumer, Accounting/tracking) found Consumer to be the cleanest seam:

- **Single inbound synchronous dependency.** The only cross-context call into
  Consumer is `OrderService.createOrder()` →
  `ConsumerService.validateOrderForConsumer(consumerId, orderTotal)`. No other
  module calls into the Consumer domain.
- **Isolated data.** The `consumers` table has no foreign keys to or from other
  tables. `orders.consumer_id` is a plain `bigint` column (no FK, no JPA
  relationship) — the Order aggregate stores the consumer id as a primitive, so
  nothing in Order maps the `Consumer` entity.
- **Pre-existing contract surface.** A `ftgo-consumer-service-api` module and a
  REST `ConsumerController` (`POST /consumers`, `GET /consumers/{id}`) already
  existed, so the HTTP seam required no new endpoints.

## The API seam

Callers now depend on an interface in the API module rather than on a concrete
in-process service:

```
net.chrisrichardson.ftgo.consumerservice.api.ConsumerService
    void validateOrderForConsumer(long consumerId, Money orderTotal)
```

There are two implementations of this interface:

| Implementation | Module | Used by |
| --- | --- | --- |
| `ConsumerServiceImpl` | `ftgo-consumer-service` | the standalone Consumer service (in-process, talks to the DB) |
| `ConsumerServiceProxy` | `ftgo-order-service` (`client` package) | the Order service / monolith (HTTP client) |

`OrderService` is constructed with the `ConsumerService` interface and is
unaware of which implementation it receives — exactly the Strangler-Fig seam.

```
Order service (monolith)                     Consumer service (standalone, :8082)
------------------------                     ------------------------------------
OrderService.createOrder()
  -> ConsumerService (interface)
       -> ConsumerServiceProxy   --HTTP-->   ConsumerController
            GET /consumers/{id}                 -> ConsumerServiceImpl
                                                     -> ConsumerRepository -> H2
```

## Migrated entities / classes

Moved **into** `ftgo-consumer-service`:

- `Consumer` entity: `net.chrisrichardson.ftgo.domain.Consumer`
  → `net.chrisrichardson.ftgo.consumerservice.domain.Consumer`
- `ConsumerRepository`: `net.chrisrichardson.ftgo.domain.ConsumerRepository`
  → `net.chrisrichardson.ftgo.consumerservice.domain.ConsumerRepository`

Both were **removed** from `ftgo-domain`; nothing else in the monolith
referenced them, so this is a move (not a duplication). The Consumer bounded
context now owns its data model exclusively.

Moved **into** `ftgo-consumer-service-api` (so callers can catch them without a
dependency on the implementation module):

- `ConsumerVerificationFailedException`
- `ConsumerNotFoundException` (extends `ConsumerVerificationFailedException`)

Renamed:

- `ConsumerService` (concrete class) → `ConsumerServiceImpl`, now
  `implements ConsumerService` (the new API interface).

## New module: `ftgo-consumer-service-application`

A standalone, independently runnable Spring Boot application:

- `ConsumerServiceApplicationMain` — `@EnableAutoConfiguration`, with
  `@EntityScan` / `@EnableJpaRepositories` pointed at
  `net.chrisrichardson.ftgo.consumerservice.domain`, importing the existing
  `ConsumerWebConfiguration` and Swagger config.
- **Own database:** H2 in-memory (`jdbc:h2:mem:consumer;DB_CLOSE_DELAY=-1`),
  schema owned by Flyway (`spring.jpa.hibernate.ddl-auto=none`).
- **Port 8082** (the monolith stays on 8080/8081), so both run together.
- Flyway `V1__create_consumer_db.sql` creates the `consumers` table plus the
  `hibernate_sequence` sequence used by `@GeneratedValue`. MySQL-specific syntax
  (`engine = InnoDB`) is omitted for H2; the monolith's `hibernate_sequence`
  is a MySQL table, so on H2 it is created as a SEQUENCE instead (Hibernate's
  `AUTO` strategy resolves to a sequence on H2).
- Actuator `health` is exposed; the H2 console is enabled for debugging.

## HTTP client (`ConsumerServiceProxy`)

- Calls `GET {consumer.service.url}/consumers/{consumerId}`.
- `consumer.service.url` is configurable (default `http://localhost:8082`; set
  to `http://ftgo-consumer-service:8082` under Docker Compose via the
  `CONSUMER_SERVICE_URL` environment variable).
- `RestTemplate` is built with configurable connect/read timeouts (default
  5000 ms each).
- Status-code → domain-exception mapping:
  - `404 Not Found` → `ConsumerNotFoundException`
  - any other 4xx/5xx → `ConsumerVerificationFailedException`
  - connection failure / timeout (`ResourceAccessException`) →
    `ConsumerVerificationFailedException`
- Logs each validation call and every failure for debugging.

## Behavioral-equivalence notes

- **Same observable behavior.** In the monolith, `validateOrderForConsumer`
  only ever produced one observable effect: it threw `ConsumerNotFoundException`
  when the consumer did not exist (`Consumer.validateOrderByConsumer` is a no-op
  business stub that returns void). The proxy reproduces this exactly: an
  existing consumer returns `200 OK` and validation passes; a missing consumer
  returns `404` and the proxy throws the same `ConsumerNotFoundException`.
- **Transactional expectations preserved.** Validation still happens *before*
  `orderRepository.save(order)` inside `OrderService.createOrder`, which is
  `@Transactional`. If validation fails it throws before the save, so the
  transaction rolls back with no order persisted — identical to the original
  in-process ordering. The cross-service call is a read-only existence check and
  introduces no new write that would need to participate in the order
  transaction.
- **New failure mode, conservatively mapped.** The network introduces a failure
  that did not exist in-process (service unreachable / timeout). It is mapped to
  `ConsumerVerificationFailedException` (the supertype of
  `ConsumerNotFoundException`), so the order is not created when the consumer
  cannot be verified — the safe, fail-closed choice that matches the original
  "don't create an order for an unvalidated consumer" intent.

## Build / run

Build (the project targets Java 8 / Gradle wrapper 4.10.2):

```
./gradlew :ftgo-consumer-service-application:bootJar :ftgo-application:bootJar
```

Run both via the existing Docker Compose setup:

```
docker-compose up --build ftgo-consumer-service ftgo-application
```

- Consumer service: `http://localhost:8082` (`/consumers`, `/actuator/health`,
  `/h2-console`).
- Monolith: `http://localhost:8081`, configured with
  `CONSUMER_SERVICE_URL=http://ftgo-consumer-service:8082`.

## Tests

- `ConsumerServiceProxyTest` (`ftgo-order-service`) uses `MockRestServiceServer`
  to assert the status-code → exception mapping (`200` passes, `404` →
  `ConsumerNotFoundException`, `5xx` → `ConsumerVerificationFailedException`).
- The existing `OrderControllerTest` continues to pass unchanged.

### Pre-existing build limitation (not introduced by this change)

`ftgo-end-to-end-tests-common` (and therefore the `ftgo-application` test source
set and `ftgo-end-to-end-tests`) do not compile on `master` because
`io.eventuate.util:eventuate-util-test:0.1.0.RELEASE` is unresolvable — the
JCenter/Bintray repository that hosted it was shut down and is intentionally
commented out in the root `build.gradle` (commit `8ccaff61`). These end-to-end
modules require a full deployment and are out of scope for this extraction. All
production modules and the touched unit tests compile and pass.
