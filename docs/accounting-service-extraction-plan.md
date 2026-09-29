# Accounting Service Extraction Plan

Status: proposal
Scope: carve the Accounting bounded context (consumer billing accounts, payment authorization,
capture and reversal for orders) out of `ftgo-application` into an independently deployable
`ftgo-accounting-service` with its own schema, using a strangler-fig approach.

## 0. Starting point: Accounting does not exist yet

Unlike Consumer, Restaurant, Courier and Order, Accounting has no module today. What exists:

| # | Artifact | Where | Notes |
|---|----------|-------|-------|
| A1 | `PaymentInformation { String paymentToken }`, `@Embedded` on `Order` | `ftgo-domain/.../PaymentInformation.java`, `Order.java` | No constructor/getter/setter; nothing ever writes it. |
| A2 | `orders.payment_token VARCHAR(255)` | `ftgo-flyway/.../V1__create_ftgo_db.sql` | Always `NULL` in practice (see A1). |
| A3 | `// TODO - charge a credit card too` | `OrderService.createOrder` | The only payment hook in the order flow. |
| A4 | `ConsumerService.validateOrderForConsumer` -> `Consumer.validateOrderByConsumer` | `ftgo-consumer-service`, `ftgo-domain` | Empty body; the natural place for "can this consumer pay?" today. |
| A5 | `CreateOrderRequest {consumerId, restaurantId, lineItems}` | `ftgo-order-service-api` | Carries no payment data. |
| A6 | `Money { BigDecimal amount }` | `ftgo-common` | No currency. |

Consequence: this is a **build-behind-a-seam, then extract** plan, not a lift-and-shift. There is
no existing accounting data to migrate, so the risk is concentrated in (a) changing the order
write path and (b) the loss of single-transaction atomicity between Order and Accounting.
The plan therefore makes Accounting *behave like a remote service from day one*, even while it
still runs in-process, so the final network cut is a configuration change.

## 1. Assumptions

1. "Accounting" is the FTGO Accounting context from *Microservices Patterns* (ch. 4/13): it owns a
   billing account per consumer and authorizes, revises, reverses and captures the charge for each
   order. It is not general ledger / restaurant payouts / courier pay (out of scope, listed in §8).
2. Card data is tokenized by an external PSP (Stripe-like). FTGO only ever stores opaque tokens;
   no PAN ever enters FTGO, keeping PCI scope to SAQ-A. The PSP sits behind a `PaymentGateway`
   port; a `FakePaymentGateway` is used in dev, CI and the e2e tests.
3. Single currency (USD). The API carries a `currency` field anyway so `Money` can grow one later.
4. The consumer's payment method is registered on the **account**, not per order. This keeps
   `CreateOrderRequest` / the UI / `ftgo-end-to-end-tests` unchanged.
5. No message broker is deployed today (`docker-compose.yml` only runs `mysql` and the app).
   Async work uses a **transactional outbox table + polling relay** that calls the Accounting REST
   API. Swapping the relay for Kafka later does not change the interface.
6. Accounting is extracted **independently of the Order service extraction**: the caller is
   whatever owns `OrderService` at the time (the monolith now, the Order service later). The
   interface below is written so the caller does not care.
7. Same MySQL server, new schema `ftgo_accounting`, separate DB user. A separate server is a later
   ops decision, not a prerequisite.
8. Payment failures are **fail-closed** in enforce mode: if Accounting cannot authorize, the order
   is not created (HTTP 503 / 402), rather than creating an unpaid order.

## 2. Interface

### 2.1 In-process port (new module `ftgo-accounting-service-api`)

Plain Java interface + DTOs, no JPA or Spring types, so it can be implemented locally or by an
HTTP client. `OrderService` and `ConsumerService` depend only on this module.

```java
public interface AccountingService {
  AccountDto createAccount(long consumerId);                       // idempotent on consumerId
  AccountDto getAccount(long consumerId);

  AuthorizationDto authorize(AuthorizeOrderCommand cmd);           // idempotent on orderId
  AuthorizationDto reviseAuthorization(long orderId, MoneyDto newTotal, String idempotencyKey);
  AuthorizationDto reverseAuthorization(long orderId, String reason); // idempotent, no-op if already reversed
  AuthorizationDto capture(long orderId);                          // idempotent, no-op if already captured
  Optional<AuthorizationDto> findAuthorization(long orderId);
}

record AuthorizeOrderCommand(long orderId, long consumerId, MoneyDto orderTotal) {}
record MoneyDto(BigDecimal amount, String currency) {}
record AuthorizationDto(long orderId, long consumerId, MoneyDto authorizedAmount,
                        AuthorizationStatus status, String declineReason) {}
enum AuthorizationStatus { AUTHORIZED, DECLINED, REVERSED, CAPTURED }
```

Exceptions (all in the api module): `AccountNotFoundException`, `AccountDisabledException`,
`PaymentDeclinedException(reason)`, `InvalidAuthorizationStateException`,
`AccountingUnavailableException` (timeouts / 5xx / circuit open).

Implementations:

- `NoopAccountingService` — current behaviour, returns `AUTHORIZED` without persisting. Default.
- `LocalAccountingService` — in-process, real logic, **own DataSource + TransactionManager** on
  `ftgo_accounting` (never joins the monolith's JPA transaction).
- `RestAccountingServiceClient` — HTTP client for the extracted service (timeouts 1s connect /
  2s read, 1 retry only for idempotent calls, circuit breaker).

### 2.2 REST API of the extracted service

Base path `/accounting`. All mutating calls accept `Idempotency-Key`; replays return the original
response.

| Method | Path | Body | Success | Errors |
|--------|------|------|---------|--------|
| POST | `/accounts` | `{consumerId}` | 201 `AccountDto` (200 if exists) | 422 |
| GET | `/accounts/{consumerId}` | — | 200 `AccountDto` | 404 |
| PUT | `/accounts/{consumerId}/payment-method` | `{paymentToken}` | 204 | 404, 422 |
| POST | `/authorizations` | `AuthorizeOrderCommand` | 201 `AuthorizationDto` | 402 declined, 404 account, 409 account disabled |
| GET | `/authorizations/{orderId}` | — | 200 | 404 |
| GET | `/authorizations?status=AUTHORIZED&createdBefore=…` | — | 200 list (for reconciliation) | — |
| POST | `/authorizations/{orderId}/revise` | `{newTotal}` | 200 | 402, 404, 409 |
| POST | `/authorizations/{orderId}/reverse` | `{reason}` | 200 | 404 |
| POST | `/authorizations/{orderId}/capture` | — | 200 | 404, 409 |

Error body is `ftgo-common`'s `ErrorResponse` (status, error, message, path, correlationId), the same shape `GlobalExceptionHandler` returns, so the monolith can map remote
errors to the same exceptions as the local implementation. An OpenAPI spec is published from the
service via `common-swagger`.

### 2.3 Where the monolith calls it

| Order/Consumer operation | Accounting call | Sync / async | Why |
|--------------------------|-----------------|--------------|-----|
| `ConsumerService.create` | `createAccount` | async (outbox) | Consumer creation must not depend on Accounting uptime. |
| `OrderService.createOrder` (replaces the A3 TODO) | `authorize` | **sync** | Caller needs approve/decline before responding. |
| `OrderService.reviseOrder` | `reviseAuthorization` | **sync** | A revision that raises the total can be declined. |
| `OrderService.cancel` | `reverseAuthorization` | async (outbox) | Cancel must always succeed locally; reversal is retried. |
| `OrderService.noteDelivered` | `capture` | async (outbox) | Money moves only once the food is delivered. |

`createOrder` ordering (the order id is `IDENTITY`, so it only exists after insert):

```
tx(ftgo) {
  consumerService.validateOrderForConsumer(...)    // unchanged
  order = save(new Order(...)); flush()            // id assigned
  accounting.authorize(orderId, consumerId, total) // outside Accounting's tx, idempotent on orderId
      -> PaymentDeclinedException           => tx rollback, HTTP 402
      -> AccountingUnavailableException     => tx rollback, HTTP 503
}
```

If the `ftgo` commit fails after a successful `authorize`, an orphan authorization exists. It is
cleaned up by the reconciliation job (§3.4) — not by a distributed transaction. Orphans are cheap
(an un-captured hold that the PSP also expires), whereas a charge without an order is not, which is
why capture happens only on delivery.

### 2.4 Events (published by Accounting via its own outbox)

`AccountCreated`, `OrderAuthorized`, `OrderAuthorizationDeclined`, `AuthorizationReversed`,
`OrderCaptured`. No consumer exists for these initially; they exist so the future Order service can
move to an `APPROVAL_PENDING` saga (see §8) without changing Accounting.

## 3. Data ownership

### 3.1 Owned by Accounting (schema `ftgo_accounting`)

| Table | Key columns | Notes |
|-------|-------------|-------|
| `accounts` | `id` PK, `consumer_id` UNIQUE, `status` (`ACTIVE`/`DISABLED`), `payment_token`, `created_at`, `version` | `payment_token` is the PSP token (assumption 4). `consumer_id` is a soft reference — no FK to `ftgo.consumers`. |
| `authorizations` | `order_id` PK, `consumer_id`, `amount`, `currency`, `status`, `psp_reference`, `decline_reason`, `created_at`, `updated_at`, `version` | One row per order; `order_id` is a soft reference. |
| `ledger_entries` | `id` PK, `order_id`, `type` (`AUTHORIZE`/`REVISE`/`REVERSE`/`CAPTURE`), `amount`, `created_at` | Append-only audit trail; never updated. |
| `idempotency_keys` | `key` PK, `request_hash`, `response_body`, `created_at` | TTL 7 days. |
| `outbox` | `id` PK, `event_type`, `payload`, `created_at`, `published_at` | Accounting's own events. |
| `flyway_schema_history` | — | Accounting's own migration history; migrations live in `ftgo-accounting-service/src/main/resources/db/migration`. |

### 3.2 Owned by the monolith (schema `ftgo`)

| Table / column | Change | Notes |
|----------------|--------|-------|
| `accounting_outbox` (new) | `id`, `command_type` (`CREATE_ACCOUNT`/`REVERSE`/`CAPTURE`), `aggregate_id`, `payload`, `created_at`, `attempts`, `last_error`, `processed_at` | Written in the same `ftgo` tx as the consumer/order change. Relay polls, calls `AccountingService`, marks processed. |
| `orders.payment_token` | **dropped** in the final phase | Never populated (A1/A2); ownership of payment tokens moves to `accounts.payment_token`. |
| `PaymentInformation` / `Order.paymentInformation` | **removed** | Same reason. |
| `orders`, `consumers` | unchanged | Accounting only holds their ids. |

### 3.3 Access rules

- The monolith has **no grants** on `ftgo_accounting` once `LocalAccountingService` is gone
  (Phase 6); until then only the Accounting module's DataSource uses the `ftgo_accounting` user.
- Accounting has **no grants** on `ftgo` at any point. It never reads `orders` or `consumers`;
  everything it needs arrives in the command.
- No cross-schema FKs, joins or views. Enforced by separate DB users in `mysql/schema.sql` and an
  ArchUnit rule that `net.chrisrichardson.ftgo.accountingservice..` is only referenced through
  `..accountingservice.api..`.

### 3.4 Consistency / reconciliation (owned by the monolith, since it owns `orders`)

Scheduled job `AccountingReconciliationJob`, every 5 min, reports and fixes drift:

| Drift | Detection | Fix |
|-------|-----------|-----|
| Orphan authorization (order tx rolled back after `authorize`) | `AUTHORIZED` authorization older than 15 min with no `orders` row | enqueue `REVERSE` |
| Cancelled order still authorized | `orders.order_state = CANCELLED` and authorization `AUTHORIZED` | enqueue `REVERSE` |
| Delivered order not captured | `DELIVERED` > 15 min and authorization not `CAPTURED` | enqueue `CAPTURE` |
| Amount mismatch (revise committed on one side only) | order total != authorized amount for non-terminal orders | re-issue `reviseAuthorization` |
| Consumer without account | `consumers` id with no account | enqueue `CREATE_ACCOUNT` |

Relies on MySQL 8 persisting the auto-increment counter, so rolled-back order ids are not reused.

## 4. Migration steps

Each phase is independently deployable, has an exit criterion, and has a rollback that does not
require restoring a backup. Flags live in `application.properties` / env and are read at startup:

- `ftgo.accounting.mode = off | shadow | enforce` (default `off`)
- `ftgo.accounting.transport = local | remote` (default `local`)

| Phase | Change | Flags after deploy | Exit criterion |
|-------|--------|--------------------|----------------|
| **0. Prep** | Confirm `SELECT COUNT(*) FROM orders WHERE payment_token IS NOT NULL` = 0. Add metrics baseline for `placed_orders`, order-create latency p50/p99. Pick PSP sandbox, get test keys. | — | Baseline recorded; A1/A2 confirmed unused. |
| **1. Seam** | New modules `ftgo-accounting-service-api` (§2.1) and `ftgo-accounting-service` (domain, `LocalAccountingService`, `FakePaymentGateway`, REST controller, Flyway migrations for §3.1). Create schema + user in `mysql/schema.sql` and k8s MySQL init. `OrderService` / `ConsumerService` take an `AccountingService` bean; `NoopAccountingService` is wired when `mode=off`. Add `accounting_outbox` (Flyway `V3`) + relay (disabled when `mode=off`). Unit tests for the domain; contract tests for the REST controller. | `off` / `local` | All existing unit, integration and e2e tests green with no behaviour change. |
| **2. Accounts** | Enable account creation: `ConsumerService.create` writes `CREATE_ACCOUNT` to outbox. One-off, re-runnable backfill job creates accounts for all existing consumers. | `off` (accounts only) / `local` | Reconciliation "consumer without account" = 0 for 24 h. |
| **3. Shadow** | `mode=shadow`: `createOrder`/`reviseOrder` call Accounting, **log and meter** the outcome, but never fail the order (declines and errors are swallowed). Cancel/deliver enqueue reverse/capture. PSP in sandbox. | `shadow` / `local` | 7 days: Accounting error rate < 0.1 %, added p99 latency < 150 ms, reconciliation drift = 0 after auto-fix, no order-create failures attributable to Accounting. |
| **4. Enforce (in-process)** | `mode=enforce`: declines -> 402, unavailability -> 503 (assumption 8). Map new exceptions in `GlobalExceptionHandler`. Add e2e cases (decline token in `FakePaymentGateway`). Switch to PSP live keys. | `enforce` / `local` | 7 days stable; decline rate matches PSP dashboard. |
| **5. Extract** | New Boot app `ftgo-accounting-service-main` (applies `FtgoServicePlugin`) packaging the same `ftgo-accounting-service` module; Dockerfile, `docker-compose.yml` service, k8s Deployment/Service, readiness/liveness probes. Monolith switches to `RestAccountingServiceClient` with `transport=remote`. Roll out canary (one monolith replica remote, rest local — both hit the same `ftgo_accounting` schema, so they are consistent). | `enforce` / `remote` | 7 days: remote error rate and latency within Phase 4 budget; no increase in 503s. |
| **6. Cut** | Remove `ftgo-accounting-service` (impl) from `ftgo-application`'s dependencies; keep only `-api` + REST client. Revoke the monolith's access to `ftgo_accounting` credentials. Accounting gets its own CI pipeline and release cadence. | `enforce` / `remote` (only option) | Monolith artifact contains no `accountingservice.domain` classes (ArchUnit + jar inspection). |
| **7. Contract** | Flyway `V4`: `ALTER TABLE orders DROP COLUMN payment_token`. Remove `PaymentInformation` and `Order.paymentInformation`. | — | Schema diff clean; e2e green. |

Key property: from Phase 1 onward the accounting data lives only in `ftgo_accounting`, and both
the in-process and the remote implementation use it. The Phase 5 "extraction" therefore moves
**no data** — it moves the process boundary.

## 5. Rollback

| Phase | Rollback action | Data impact | Time |
|-------|-----------------|-------------|------|
| 1 | Redeploy previous monolith build. `ftgo_accounting` schema and empty `accounting_outbox` can stay (additive). | None | One deploy |
| 2 | Disable outbox relay; leave accounts in place. | Accounts are harmless without authorizations. | Config change |
| 3 | `mode=off`. Pending outbox rows stay and are drained later or deleted (sandbox only). | None — shadow never affected orders. | Config change |
| 4 | `mode=shadow` (keeps visibility) or `off`. Orders placed while off have no authorization; when re-enabling, reconciliation lists them and ops decides: capture via new `authorize`+`capture`, or write off. | Unpaid orders during the rollback window (explicitly accepted). | Config change |
| 5 | `transport=local`. Works instantly because both transports share the same schema and are idempotent on `orderId`; in-flight outbox rows are simply retried by the local implementation. | None | Config change + restart |
| 6 | Re-add the impl dependency, restore grants, `transport=local` (i.e. go back to Phase 5 state). | None | One deploy |
| 7 | Flyway `V5` re-adds `orders.payment_token VARCHAR(255) NULL`; revert the Java removal. Column was empty (Phase 0 check), so nothing to restore. | None | One deploy |

Rollback triggers (any one, sustained 10 min): order-create 5xx rate > 1 %, order-create p99 >
2x baseline, Accounting error rate > 1 %, outbox backlog > 1,000 rows or oldest row > 15 min,
reconciliation finding a captured amount != order total.

Never done as part of rollback: deleting rows from `authorizations` or `ledger_entries`
(financial audit trail), or reversing captures (that is a refund, handled by ops through the PSP).

## 6. Observability

- Metrics (Micrometer, tag `service`): `accounting_calls_total{op,result}`,
  `accounting_call_duration_seconds{op}`, `accounting_outbox_backlog`,
  `accounting_outbox_oldest_age_seconds`, `accounting_reconciliation_drift{type}`,
  `accounting_declines_total{reason}`.
- The correlation id the monolith already puts in the MDC (`ApiTrackingInterceptor`, `ErrorResponse`)
  must be forwarded as a header by `RestAccountingServiceClient` and logged by the service.
- Never log payment tokens; `api_request_log` must redact `/accounting/accounts/*/payment-method`
  bodies.

## 7. Testing

- Domain unit tests for account/authorization state machine (`AUTHORIZED` (revise keeps it
  `AUTHORIZED` with a new amount) `-> CAPTURED | REVERSED`, idempotent replays, decline paths).
- Contract tests for §2.2 run against both `LocalAccountingService` and the REST controller, so the
  two implementations cannot drift.
- `OrderService` tests with a mocked `AccountingService` for each `mode`.
- Relay tests: retry, poison-message handling (`attempts` cap -> alert), idempotent redelivery.
- `ftgo-end-to-end-tests`: happy path unchanged; add declined-payment and cancel->reverse cases
  using `FakePaymentGateway` magic tokens.
- Failure drill before Phase 4 and Phase 5 exit: kill Accounting mid-traffic, confirm 503s (enforce)
  and that reconciliation clears orphans afterwards.

## 8. Out of scope / follow-ups

- `APPROVAL_PENDING` order state and a choreographed/orchestrated create-order saga: the right
  long-term design once the Order service is extracted, but it changes the order API contract
  (orders are currently created synchronously `APPROVED`). The events in §2.4 are there to enable it.
- Restaurant payouts, courier pay, refunds UI, multi-currency.
- Moving `ftgo_accounting` to its own MySQL server.
- Replacing the polling relay with a broker (Kafka / Eventuate CDC).
