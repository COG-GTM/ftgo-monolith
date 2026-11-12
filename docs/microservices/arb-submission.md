# ARB submission: FTGO microservices decomposition (epic AB-585)

- **ARB ticket:** TO BE CREATED — there is no ARB Jira project yet. When one exists, file this body as the ticket,
  then replace "TO BE CREATED" here, in [ADR 0003](../adr/0003-microservices-decomposition.md) and in the epic PR's
  **Architecture review** section with the `ARB-xxxx` key.
- **ADR:** [docs/adr/0003-microservices-decomposition.md](../adr/0003-microservices-decomposition.md) (Status: Proposed)
- **Decomposition detail:** [docs/microservices/README.md](README.md)
- **Jira:** [AB-586](https://cog-gtm.atlassian.net/browse/AB-586), epic [AB-585](https://cog-gtm.atlassian.net/browse/AB-585)
- **Repository:** `COG-GTM/ftgo-monolith`, integration branch `devin/ab-585-microservices` (based on `devin/ab-561-java21`)
- **Requested by:** Abhay Aggarwal. **Owning team:** TBD — owner to confirm before ARB.
- **Gate:** the epic PR from `devin/ab-585-microservices` to the default branch does not merge until this ARB ticket
  is approved. Work inside the epic branch proceeds in the meantime.

## Triage

Done manually against `rules/arb-triggers.md` (org `arb-review` plugin) on the planned change set of the epic.

```
ARB triage: ARB_REQUIRED
Triggers:
- T1 New deployable services: ftgo-consumer-service-main, ftgo-restaurant-service-main, ftgo-courier-service-main, ftgo-order-service-main, ftgo-api-gateway (AB-587..AB-592)
- T2 New data stores: schemas ftgo_consumer, ftgo_restaurant, ftgo_courier, ftgo_order with per-service users and Flyway histories; data copied out of ftgo (AB-588..AB-592, AB-595). Classification unchanged
- T4 New sync cross-domain contracts: order -> consumer validate-order, order -> restaurant menu read model, order -> courier assignment (AB-588..AB-590, AB-592); cross-aggregate FKs removed from service schemas
- T6 Network boundary change: gateway becomes the front door (8088), new service ports 8082-8085, unauthenticated service-to-service HTTP (accepted interim, follow-up)
- T7 Framework introduction: Spring Cloud 2025.0.x / Spring Cloud Gateway (AB-587, AB-591)
- T8 New shared platform library: ftgo-service-runtime used by all five deployables (AB-587)
Not triggered: T3 (no external vendor/SaaS; Spring Cloud and Flyway are libraries; Sentry unchanged), T5 (no cloud infrastructure; no approved-infra policy in repo), T9 ($0 new recurring spend)
Required next steps: ADR (docs/adr/0003-microservices-decomposition.md), ARB ticket (TO BE CREATED), C4 diagram, NFRs, cost estimate; epic PR to the default branch waits on ARB approval
```

## Ticket body

**Summary.** Decompose `ftgo-monolith` (Spring Boot 3.5, Java 21, one MySQL schema) into four independently
deployable services — consumer, restaurant, courier, order — behind a Spring Cloud Gateway, using the strangler-fig
pattern. Each service owns a schema (`ftgo_<context>`) on the existing MySQL 8.4 server, with its own DB user and
Flyway migrations, and references other aggregates by id only. Services integrate over synchronous REST behind ports
with an in-process (monolith) and a remote adapter. The gateway routes each context to the monolith or its service
via `ftgo.routing.<context>=monolith|service` (default `monolith`), so every cut-over is a config flip that can be
reverted. Public REST paths and JSON shapes do not change; the AB-563 golden JSON and e2e suite prove parity in
monolith, gateway-all-monolith and gateway-all-services modes.

**Why now.** The service modules already exist as libraries; three cross-aggregate JPA links (`Order.restaurant`,
`Order.assignedCourier`, `Action.order`) and the shared schema are what prevent independent deployment. The epic
demonstrates an incremental, reversible extraction on top of the Java 21 / Boot 3.5 baseline (ADR 0002).

**Alternatives considered.** Stay a modular monolith (kept as the fallback state after every wave); big-bang rewrite
(rejected: no incremental rollback); shared database across services (rejected: schema stays a hidden shared
contract); async-first with Kafka/Eventuate (deferred: adds broker + CDC infrastructure and changes the synchronous
contract). Details in ADR 0003.

**Architecture.** C4 container diagram in ADR 0003 (gateway 8088 → monolith 8081 / consumer 8082 / restaurant 8083 /
courier 8084 / order 8085; order → consumer, restaurant, courier over REST; five schemas on one MySQL 8.4 server).

**NFRs.** Functional parity is the hard gate (golden JSON diff empty, e2e green in all three modes). Added latency of
the gateway hop and order's downstream calls is measured in AB-595 against the monolith baseline. Availability SLO,
p95 target, peak load and rollback RTO: TBD — owner to confirm before ARB. Cut-over RPO 0 per context (write freeze
+ verified copy).

**Security.** No new data categories (demo data: names, addresses/coordinates, `orders.payment_token`). Gateway and
service-to-service HTTP stay **unauthenticated, as the monolith API is today** — accepted as an interim state with a
follow-up epic for gateway auth + service identity, which must also block the new internal endpoints
(`POST /consumers/{consumerId}/validate-order`, `POST /couriers/assignments`) at the gateway. Per-service DB users
restricted to their schema (least-privilege improvement). Credentials from env, none committed.
`api_request_log` stops seeing service-served requests (follow-up: gateway-level tracking).

**Cost.** $0 new recurring spend: local Docker Compose and GitHub Actions on existing plans, open-source libraries,
same MySQL server. More CI minutes from the AB-594 matrix.

**Operations.** Health/prometheus actuator endpoints on every deployable; correlation-id propagation via
`ftgo-service-runtime`; per-context cut-over and rollback runbook in AB-595. Owning team and on-call: TBD — owner to
confirm before ARB.

**Policy exceptions.** None.

**Follow-ups** (filed by AB-595): async messaging/outbox; gateway + service-to-service auth and TLS; Helm/Kubernetes
(relates to AB-402); retire service code from the monolith; split `ftgo-domain`; gateway-level request tracking;
circuit breakers; separate MySQL servers if needed.

## Reviewers needed

- **Architecture / Platform:** new deployables, gateway, shared `ftgo-service-runtime`, Spring Cloud introduction (T1, T7, T8).
- **Data:** four new schemas, per-service users, data copy and FK removal (T2).
- **Security:** unauthenticated internal traffic as an interim state, new network boundary and ports (T6).

## Questions for the ARB

1. Is unauthenticated gateway and service-to-service traffic acceptable for the duration of the epic, given the
   monolith API is unauthenticated today? Should internal endpoints be blocked at the gateway in this epic?
2. Is one MySQL server with schema + user isolation acceptable, or is a server per service required before any
   hosted deployment?
3. Who is the owning team and on-call for the five deployables, and what availability / p95 targets apply?
