# Consumer Service Extraction Plan — FTGO Monolith

Plan for pulling the Consumer Service out of `ftgo-application` into its own deployable, with its own data, while the monolith keeps running. It is based on the code as of `master` (`4823d191`) and follows the same structure as the Order Service extraction plan (`ORDER_SERVICE_EXTRACTION_PLAN.md`, branch `devin/1790695006-order-service-extraction-plan`).

Sections: [1. Current state](#1-current-state) · [2. Target boundary & data ownership](#2-target-boundary--data-ownership) · [3. API surface](#3-api-surface) · [4. Migration order](#4-migration-order) · [5. Running both sides during the transition](#5-running-both-sides-during-the-transition) · [6. Risks & open questions](#6-risks--open-questions) · [Appendix: assumptions](#appendix-assumptions)

---

## 1. Current state

### 1.1 Build and runtime topology

- One Spring Boot process (`FtgoApplicationMain`) imports `ConsumerServiceConfiguration` together with the Order, Restaurant, Courier, and API-tracking configurations. It exposes port 8080 (8081 in `docker-compose.yml`).
- One MySQL schema, `ftgo`, managed by Flyway (`ftgo-flyway`, V1 + V2). The only DB user is `mysqluser`, with `ALL PRIVILEGES ON ftgo.*` (`mysql/schema.sql`).
- The `Consumer` entity and `ConsumerRepository` live in the shared `ftgo-domain` module, not in `ftgo-consumer-service`. `ConsumerConfiguration` pulls them in through `@Import(DomainConfiguration.class)`, which component-scans and entity-scans **every** entity and repository in `ftgo.domain` (Order, Restaurant, Courier included).
- `ftgo-consumer-service` already exists as a Gradle module with its own `main/ConsumerServiceConfiguration` (`@EnableAutoConfiguration`, Swagger). It contains `ConsumerController`, `ConsumerService`, `GetConsumerResponse`, and two exceptions. It has no tests.
- `ftgo-consumer-service-api` contains only `CreateConsumerRequest` / `CreateConsumerResponse`. There is no service contract (interface) and no event types.

### 1.2 Coupling that blocks extraction

Consumer is the **leaf** context: it does not depend on any other service. All coupling is inbound or structural.

| # | Coupling | Where | Kind |
|---|----------|-------|------|
| C1 | `ftgo-order-service` depends on the `ftgo-consumer-service` **implementation** module and injects the concrete `ConsumerService` | `ftgo-order-service/build.gradle`, `OrderConfiguration`, `OrderService` | Build dependency + in-process call |
| C2 | `OrderService.createOrder` calls `consumerService.validateOrderForConsumer(consumerId, total)` inside its own `@Transactional`; `ConsumerService` is also `@Transactional`, so the consumer read joins the order transaction | `OrderService.java` | Synchronous call sharing one DB transaction |
| C3 | `OrderServiceConfiguration` cannot boot without a `ConsumerService` bean; it only resolves because `FtgoApplicationMain` also imports `ConsumerServiceConfiguration` | `OrderConfiguration`, `FtgoApplicationMain` | Implicit Spring wiring |
| C4 | `Consumer` / `ConsumerRepository` live in `ftgo-domain`; `DomainConfiguration` scans all contexts' entities | `ftgo-domain` | Ownership / packaging |
| C5 | `Consumer.id` uses `@GeneratedValue` (AUTO). With Spring Boot 2.0 / Hibernate 5.2 (`new_generator_mappings=true`) on MySQL this resolves to the **table generator on `hibernate_sequence`**. `consumers.id` has no `AUTO_INCREMENT`. | `Consumer.java`, `V1__create_ftgo_db.sql` | Shared ID-generation table |
| C6 | `ConsumerNotFoundException` / `ConsumerVerificationFailedException` have no handler in `GlobalExceptionHandler`, so they fall through to the generic 500 handler | `ftgo-application` | Error contract lives outside the service (and is currently wrong) |
| C7 | `PersonName` (`@Embeddable`) is in `ftgo-common` and shared with `Courier` | `ftgo-common` | Shared value type (fine to keep as a library) |
| C8 | `ApiTrackingInterceptor` writes every request to the shared `api_request_log` table | `ftgo-common` | Shared cross-cutting table |
| C9 | `AbstractEndToEndTests` builds consumer URLs from the same `getApplicationPort()` as orders/restaurants | `ftgo-end-to-end-tests-common` | Test assumes one host |

At the data level Consumer is already decoupled: `orders.consumer_id` is a plain `bigint` with **no FK** to `consumers`, and no other table references `consumers`.

---

## 2. Target boundary & data ownership

### 2.1 Responsibilities of the extracted Consumer Service

The Consumer Service owns the **consumer aggregate**: registration (`create`), lookup (`findById`), and the business rule that decides whether a consumer may place an order of a given total (`Consumer.validateOrderByConsumer`, a no-op today). It is the system of record for consumer identity and name.

The Consumer Service does **not** own:

- Orders, or a consumer's order history. `GET /orders?consumerId=` stays with Order.
- Payment / accounting. `PaymentInformation.paymentToken` stays on `orders`; a future Accounting service is out of scope (see §6).
- Authentication / identity. There is no auth in the app today; if one is added it is a separate concern.

### 2.2 Tables

| Table | Today | After extraction | Notes |
|-------|-------|------------------|-------|
| `consumers` | shared `ftgo` schema | **moves → `ftgo_consumer`**, owned by Consumer Service | Columns stay `id, first_name, last_name`. Add `version bigint` (optimistic locking for the dual-run window) and `created_at datetime`, both nullable, in Flyway V3. No inbound FKs to drop. |
| `hibernate_sequence` | shared, used **only** by `Consumer` (C5) | **moves → `ftgo_consumer`** together with `consumers` | Every other entity uses `IDENTITY`. During Phase 3 both processes allocate from the same row, which is safe. In Phase 4 copy `next_val` with the table. Option (see §6): switch `consumers.id` to `AUTO_INCREMENT` + `IDENTITY` in the new schema and drop the table. |
| `consumer_outbox` *(new)* | — | **new in `ftgo_consumer`** | Transactional outbox for Consumer events (§3.3). |
| monolith `consumer_outbox` *(new, temporary)* | — | **new in `ftgo`**, removed in Phase 5 | Used in Phases 2–3, while the monolith may still be the writer. |
| `orders.consumer_id` | shared | stays with Order | Remains an opaque ID. Order Service does not keep a consumer replica (validation is a synchronous call, §3.2). |
| `api_request_log` | shared | each service writes its own copy | Same approach as Order; see open question 3. |

### 2.3 Code moves

- Move `Consumer` and `ConsumerRepository` out of `ftgo-domain` into `ftgo-consumer-service` under `...consumerservice.domain`.
- Replace `ConsumerConfiguration`'s `@Import(DomainConfiguration.class)` with a consumer-local `@EntityScan` / `@EnableJpaRepositories` on `consumerservice.domain`, plus `@Import(CommonConfiguration.class)`. The Consumer Service must not scan Order/Restaurant/Courier entities.
- Add a contract to `ftgo-consumer-service-api`:
  - `api.ConsumerValidator { void validateOrderForConsumer(long consumerId, Money orderTotal); }` (the same shape as the one on branch `devin/1779201356-decouple-order-from-consumer-impl`).
  - `api.web.GetConsumerResponse` (move it out of the implementation module), `api.web.ValidateOrderRequest {orderTotal}`.
  - `api.events.ConsumerCreated`, `api.events.ConsumerNameChanged` (§3.3).
  - Exceptions the client needs to understand: `ConsumerNotFoundException`, `ConsumerVerificationFailedException`.
- `ConsumerService implements ConsumerValidator`. `ftgo-order-service` depends on `ftgo-consumer-service-api` only.
- Move the consumer exception handlers into an `@ControllerAdvice` inside `ftgo-consumer-service`.
- `ftgo-consumer-service/build.gradle`: drop `ftgo-domain`; depend on `ftgo-common`, `ftgo-common-jpa` (if needed), `common-swagger`, `ftgo-consumer-service-api`. Add `FtgoServicePlugin`, the MySQL driver, and Flyway.

---

## 3. API surface

### 3.1 Public REST API (served by the Consumer Service)

This is the **same contract as today**, so clients (the UI, `AbstractEndToEndTests`) do not change and the gateway can route `/consumers/**` by path.

| Method | Path | Request | Response | Errors |
|--------|------|---------|----------|--------|
| POST | `/consumers` | `CreateConsumerRequest {name: {firstName, lastName}}` | `CreateConsumerResponse {consumerId}` | 400 invalid body |
| GET | `/consumers/{consumerId}` | — | `GetConsumerResponse {consumerId, name}` | 404 |

`GetConsumerResponse` extends `CreateConsumerResponse` but never sets `consumerId`, so today `GET` always returns `"consumerId": 0`. Fix this in Phase 0 (it is a bug, not part of the contract); several unmerged `fix-consumer-id-response` branches already exist.

Error bodies use the existing `ErrorResponse` shape (`status, error, message, path, correlationId`).

Operational endpoints: `/actuator/health`, `/actuator/prometheus` (add a `consumers_created` counter tagged `service=ftgo-consumer-service`), and Swagger via `common-swagger`.

### 3.2 Internal API (called by the Order Service and the monolith)

| Method | Path | Request | Response | Errors |
|--------|------|---------|----------|--------|
| POST | `/consumers/{consumerId}/order-validations` | `ValidateOrderRequest {orderTotal: Money}` | 204 | 404 consumer not found, 422 verification failed |

- This is the remote form of `ConsumerValidator.validateOrderForConsumer`. It keeps the validation rule with Consumer rather than having callers infer validity from `GET /consumers/{id}`. The Order Service plan (§3.2 there) already expects this endpoint.
- Read-only and side-effect-free, so it is safe to retry and safe to shadow.
- It is not exposed through the public gateway route (see §5.1); callers use the internal service URL.

The Consumer Service consumes **no** other service APIs.

### 3.3 Events published by the Consumer Service

Published from `consumer_outbox` to topic `net.chrisrichardson.ftgo.consumerservice.Consumer`.

| Event | Payload | Consumers |
|-------|---------|-----------|
| `ConsumerCreated` | `consumerId, name` | none today; future Accounting (create account), future Order read models (consumer name on order views) |
| `ConsumerNameChanged` | `consumerId, name` | same (only once an update endpoint exists) |

Event rules match the Order Service: every event carries `eventId` (UUID) and `consumerVersion` (= `consumers.version`). Events are optional for this extraction: nothing consumes them yet, and the Consumer Service can ship without them. They are listed so the outbox is in place before a consumer (Accounting) appears.

---

## 4. Migration order

Each phase can be deployed on its own and has a rollback that does not lose data. Every step except Phase 3 is invisible to clients.

**Ordering relative to the Order extraction.** Consumer has no outbound dependencies and no inbound FKs, so it is the lowest-risk service to extract and should go **first**, ahead of the Order Service's Phase 3. Then the Order Service's `ConsumerPort` HTTP adapter targets the real Consumer Service from day one instead of a temporary monolith endpoint. Phase 1 below is the same work as step 1.2 of the Order plan (`ConsumerPort`); do it once.

### Phase 0 — Safety net *(monolith only)*
1. Add `ConsumerControllerTest` (MockMvc) and `ConsumerServiceTest` covering create, get-found, get-404, and validate-not-found.
2. Make `FtgoApplicationTest` / `AbstractEndToEndTests` green against real MySQL; these become the contract suite run against both sides.
3. Fix or pin current behavior that would otherwise look like a regression later:
   - `GET /consumers/{id}` returns `consumerId: 0` (§3.1). **Fix.**
   - `POST /orders` with an unknown consumer returns **500** (C6). Add handlers mapping `ConsumerNotFoundException` → 404 and `ConsumerVerificationFailedException` → 422. **Fix**, and record it as a deliberate contract change.
   - Confirm C5 (IDs come from `hibernate_sequence`, and with what increment) by checking the Hibernate SQL log on `POST /consumers`.

**Rollback:** n/a.

### Phase 1 — Decouple inside the monolith *(no topology change)*
1. Add `ConsumerValidator` to `ftgo-consumer-service-api`; `ConsumerService` implements it. `OrderService` / `OrderConfiguration` depend on the interface. Remove `compile project(":ftgo-consumer-service")` from `ftgo-order-service/build.gradle` (C1, C3).
2. Order-side validation stays in-process for now, but runs **outside** the order write: validate first, then build and save the order. Behavior is unchanged because the check does not write. This removes the reliance on a shared transaction (C2) before it becomes a network call.
3. Move `Consumer` / `ConsumerRepository` to `ftgo-consumer-service`, and give `ConsumerConfiguration` its own scans (C4, §2.3). `DomainConfiguration` no longer sees `Consumer`.
4. Move the consumer exception handlers into the consumer module (C6).
5. Flyway **V3**: `ALTER TABLE consumers ADD COLUMN version BIGINT NULL, ADD COLUMN created_at DATETIME NULL`. Add `@Version` to `Consumer`.

**Exit criteria:** the E2E suite is green; `ftgo-order-service` compiles without any `*-service` implementation module; `ConsumerServiceConfiguration` boots on its own in a test with only the consumer tables.

**Rollback:** a normal code revert. V3 only adds nullable columns.

### Phase 2 — Outbox *(monolith only, optional for this extraction)*
1. Create the monolith `consumer_outbox` in `ftgo`. `ConsumerService.create` writes `ConsumerCreated` in the same transaction.
2. Publish with the same relay chosen for the Order plan (Kafka + polling publisher or Debezium). If the broker is not ready yet, skip this phase; nothing depends on it.

**Rollback:** disable the relay; rows accumulate harmlessly.

### Phase 3 — Stand up the Consumer Service; shared tables, single writer
1. Deploy `ftgo-consumer-service` as its own Spring Boot app, using `ConsumerServiceConfiguration`, a new `Dockerfile`, and a k8s manifest in `ftgo-consumer-service/src/deployment/kubernetes/`. It connects to the **same** `ftgo` schema with a dedicated user, `ftgo_consumer_svc`, granted only on `consumers`, `hibernate_sequence`, `consumer_outbox`, and `api_request_log` (insert).
2. Add `ConsumerValidator` HTTP adapter (`RestConsumerValidator` → `POST /consumers/{id}/order-validations`) in the monolith and the Order Service, behind flag `ftgo.consumer.validator=in-process|http`.
3. Put the edge router in front of both processes (§5.1; shared with the Order plan). Cut over **reads first, then writes**. Each step is a per-route flag:
   1. Shadow `GET /consumers/{id}` to the Consumer Service. Compare and alert on differences.
   2. Serve `GET /consumers/{id}` from the Consumer Service.
   3. Flip `ftgo.consumer.validator=http` in the monolith: order placement now validates through the Consumer Service.
   4. Serve `POST /consumers` from the Consumer Service.
4. After the final flip, disable the monolith's `ConsumerController` with `ftgo.consumers.enabled=false`, but keep the code.

**Rollback:** flip the route or flag back. Both processes use the same rows and the same `hibernate_sequence` row (the table generator takes a row lock), so IDs never collide, and `consumers.version` guards concurrent updates. No data sync is needed.

### Phase 4 — Physically split the data
1. Create the `ftgo_consumer` schema (same MySQL instance first).
2. Move the tables. The Consumer Service is the only writer, so this is a short copy:
   1. Create `consumers`, `hibernate_sequence`, and `consumer_outbox` in `ftgo_consumer` via the service's own Flyway (`ftgo-consumer-service/src/main/resources/db/migration`).
   2. Freeze `POST /consumers` at the gateway (503 + `Retry-After`). Reads and validations keep working, because they only read. `INSERT … SELECT` `consumers`; copy `hibernate_sequence.next_val` (**must be at least `MAX(consumers.id)+1`**). Repoint the datasource and unfreeze. Consumer writes are rare (sign-up only), so the freeze can last seconds.
3. Revoke all `ftgo_consumer_svc` grants on `ftgo`. Revoke `mysqluser`'s access to `ftgo.consumers` (rename the table, see rollback) so any leftover monolith read fails loudly in staging.

**Rollback:** keep the old table as `consumers_frozen_YYYYMMDD` in `ftgo`. To roll back, copy rows with `id > frozen_max_id` back, restore `hibernate_sequence.next_val`, and repoint.

### Phase 5 — Decommission
1. Delete `ConsumerController`, `ConsumerService`, and the consumer entities from the monolith; remove `ConsumerServiceConfiguration` from `FtgoApplicationMain` and `FtgoApplicationTest`. Remove `compile project(":ftgo-consumer-service")` from `ftgo-application/build.gradle`.
2. Flyway V-next in `ftgo-flyway`: drop `consumers_frozen_*`, the monolith `consumer_outbox`, and `hibernate_sequence` (no remaining user in `ftgo`).
3. Point `AbstractEndToEndTests.consumerBaseUrl` at the gateway (C9). Remove the `ftgo.consumer.validator=in-process` adapter.

---

## 5. Running both sides during the transition

### 5.1 Routing (strangler facade)
- Reuse the edge router from the Order plan (Spring Cloud Gateway module or nginx) on port 8081 / the k8s Service. If Consumer goes first, it is the first user of the gateway, so the gateway is introduced in this plan's Phase 3.
- Default route: everything to the monolith. `/consumers/**` is controlled by per-route flags, in the order in §4 Phase 3.3.
- `/consumers/*/order-validations` is **blocked** at the public gateway (internal-only). Internal callers use `http://ftgo-consumer-service:8080` directly.
- Shadow mode mirrors `GET /consumers/{id}` only, never `POST`. A diff job logs mismatches by `X-Correlation-ID`.
- Correlation: the gateway sets `X-Correlation-ID`; both processes already put it in MDC and `api_request_log` through `ApiTrackingInterceptor`, and `RestConsumerValidator` forwards it.

### 5.2 Data consistency across the dual-run window

| Phase | Writer of `consumers` | Readers | Sync mechanism |
|-------|------------------------|---------|----------------|
| 0–2 | monolith | monolith | n/a |
| 3 (shadow / partial flip) | monolith **or** Consumer Service, per route | both | **Shared tables**: no replication. `hibernate_sequence` row lock keeps IDs unique; `version` handles overlap. |
| 4+ | Consumer Service only | Consumer Service; monolith and Order Service through `/order-validations` / `GET` | HTTP (sync). Events (§3.3) for future subscribers. |

Key rule: **exactly one logical writer per row at any time**: the router in Phase 3, DB grants in Phase 4. No dual-write from application code.

### 5.3 Cross-service calls during overlap
- Order placement becomes: Order (monolith or Order Service) → `POST /consumers/{id}/order-validations` → save order. The call is read-only, so there is nothing to compensate if the order save fails afterwards.
- Failure semantics: 404 → `ConsumerNotFoundException` → order API 404; 422 → `ConsumerVerificationFailedException` → order API 422; timeout / 5xx / open circuit → **fail closed** with 503 (do not place orders for unvalidated consumers). Use Resilience4j: 500 ms timeout, 2 retries on connect errors only, circuit breaker per instance.
- The in-process and HTTP adapters call the same `ConsumerService.validateOrderForConsumer`, so the result does not depend on which side handled `POST /orders`.
- Eventual consistency caveat: a consumer created on one side is immediately visible to the other in Phase 3 (shared table). After Phase 4, callers only ever reach the single owner, so there is no replica lag.

### 5.4 Local and CI setup
- `docker-compose.yml`: add `ftgo-consumer-service` and `gateway` (Phase 3). Kafka only if Phase 2 is done.
- CI: run the E2E suite twice per build, once with `/consumers/**` on the monolith and `validator=in-process`, once with `/consumers/**` on the Consumer Service and `validator=http`, until Phase 5.
- Observability: per-route dashboards comparing monolith vs Consumer Service on p50/p99, 4xx/5xx, and `consumers_created`; plus order-placement 5xx rate and validator latency/circuit state (this is where a Consumer outage shows up). Cut over each step only after 24–48h at or better than the monolith.

---

## 6. Risks & open questions

| Risk | Mitigation |
|------|------------|
| Consumer Service becomes a hard runtime dependency of order placement (it was an in-process call) | Fail closed with a clear 503; short timeouts and circuit breaker; run ≥2 replicas. `validateOrderByConsumer` is a no-op today, so a cached "consumer exists" check could be a degraded-mode fallback if the team accepts it. |
| ID collisions if `hibernate_sequence` is mishandled during the Phase 4 copy | Copy `next_val` in the same freeze, assert `next_val > MAX(id)` before unfreezing. Consider switching to `IDENTITY` in `ftgo_consumer`. |
| Behavior change from Phase 0 fixes (`consumerId: 0`, 500 → 404/422) is mistaken for an extraction regression | Land them as separate, announced PRs before any extraction work; pin in tests. |
| `Consumer` moving out of `ftgo-domain` breaks something that relied on `DomainConfiguration` scanning it | Nothing outside `ftgo-consumer-service` references `Consumer` / `ConsumerRepository` today (verified by grep); the Phase 1 exit criterion (service boots alone) catches wiring gaps. |
| Validation moving outside the order transaction | It never wrote anything, so there is no atomicity loss; documented in Phase 1.2. When real rules arrive (credit limits, bans), revisit with a saga (`OrderCreated` → `ConsumerVerified`). |

Open questions for the team:
1. Should consumer extraction go before Order (recommended here), or should the Order Service temporarily call the monolith's `/consumers` endpoints?
2. Keep the `hibernate_sequence` table generator in `ftgo_consumer`, or switch to `AUTO_INCREMENT` during the Phase 4 move?
3. Should `api_request_log` stay per-service, or move to a central log pipeline? (Same question as the Order plan.)
4. Is an update endpoint (`PUT /consumers/{id}`) or delete/GDPR erasure in scope? Neither exists today; adding one would activate `ConsumerNameChanged` and the `version` column.

---

## Appendix: assumptions

- **Scope:** "Consumer Service" = the current `ftgo-consumer-service` module (`ConsumerController`, `ConsumerService`) plus `Consumer` / `ConsumerRepository` from `ftgo-domain`. `PersonName` stays in `ftgo-common` as a shared value type.
- **Shape:** mirrors `ORDER_SERVICE_EXTRACTION_PLAN.md`, which exists only on branch `devin/1790695006-order-service-extraction-plan` (not on `master`). The two plans share the gateway, outbox, and relay decisions; whichever lands first introduces them.
- **Validation contract:** `POST /consumers/{id}/order-validations` with 204/404/422 is assumed as the remote form of `validateOrderForConsumer`, matching the Order plan's §3.2.
- **Messaging:** events are optional for this extraction. Kafka + outbox is assumed only for consistency with the Order plan.
- **Database:** stays MySQL. Phase 4 uses a separate schema on the same instance first.
- **Public API:** backward-compatible paths and payloads, except the two Phase 0 bug fixes. The bundled UI (`static/js/app.js`) uses an in-browser mock API, so it is not a live client.
- **ID generation:** C5 is inferred from `@GeneratedValue` defaults on Spring Boot 2.0.3 / Hibernate 5.2 and the V1 schema; Phase 0.3 verifies it.
- **Environment:** there is no production traffic data in the repo, so durations and freeze windows are estimates.
