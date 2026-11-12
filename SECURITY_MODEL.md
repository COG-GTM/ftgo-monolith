# ftgo-monolith: threat model

_Source: COG-GTM/ftgo-monolith @ master, plus the owner's answers (2026-11-12). Lines marked **[owner]** come from the system owner; everything else is Devin's reading of the code and moves no finding on its own._

## System overview

FTGO ("Food To Go") is a Spring Boot 2.0 / Java 8 monolith (`ftgo-application/src/main/java/net/chrisrichardson/ftgo/FtgoApplicationMain.java`). One process serves a REST API for consumers, restaurants, orders and couriers, plus a vanilla-JS operations dashboard (`ftgo-application/src/main/resources/static/`), on one port (8080 in the container, 8081 in docker-compose). All state lives in one MySQL database (`ftgo`) whose schema is managed by Flyway (`ftgo-flyway/`). The dashboard reports errors, session replays and user feedback to Sentry SaaS (ADR `docs/adr/0001-sentry-browser-monitoring-for-dashboard.md`).

**Deployment [owner]:** the application runs on an internal network behind an authenticating gateway. The gateway is not in this repository; the repo defines no Ingress, NetworkPolicy or gateway config (`deployment/kubernetes/` has only MySQL and scripts using `kubectl port-forward`).

## Trust boundaries

1. **Gateway → application (HTTP).** The gateway authenticates callers **[owner]**. The application itself has no Spring Security dependency and no authentication or authorization: every controller trusts any request that arrives, and callers choose `consumerId`, `orderId`, `courierId` and `restaurantId` themselves. Whether the gateway enforces per-user authorization is unknown **[owner: not sure]**, so assume it does not.
2. **Internal network → application.** **[owner]** Other internal services/hosts can reach the application port directly, bypassing the gateway; nothing in the repo restricts this.
3. **Application → MySQL.** JDBC with static credentials (`mysqluser`/`mysqlpw` in `ftgo-application/src/main/resources/application.properties`, `docker-compose.yml`, `deployment/kubernetes/stateful-services/ftgo-db-secret.yml`, plain env in `ftgo-mysql-deployment.yml`) and `useSSL=false&allowPublicKeyRetrieval=true`.
4. **Browser → third parties.** Sentry SDK from `browser.sentry-cdn.com` (pinned with SRI); events sent with `sendDefaultPii=true`, unmasked session replay (`maskAllText: false`) at 100% sampling, feedback with screenshots (`static/js/sentry-init.js`).

## Components

| Component | Code it covers |
|---|---|
| **Dashboard** | `ftgo-application/src/main/resources/static/**` (index.html, js/app.js, js/sentry-init.js), `ftgo-application/.../SentryConfigController.java` |
| **Order API** | `ftgo-order-service/**`, `ftgo-order-service-api/**` |
| **Consumer API** | `ftgo-consumer-service/**`, `ftgo-consumer-service-api/**` |
| **Restaurant API** | `ftgo-restaurant-service/**`, `ftgo-restaurant-service-api/**` |
| **Courier API** | `ftgo-courier-service/**`, `ftgo-courier-service-api/**` |
| **API Tracking** | `ftgo-common/src/main/java/net/chrisrichardson/ftgo/common/tracking/**` |
| **Domain & Data** | `ftgo-domain/**`, `ftgo-common/**` (except tracking), `ftgo-common-jpa/**`, `ftgo-flyway/**`, `mysql/**`, `ftgo-application/src/main/**` (except Dashboard files), `common-swagger/**` |
| **Infrastructure** | `docker-compose.yml`, `deployment/**`, `*/Dockerfile`, `buildSrc/**`, `build.gradle`, `*.sh`, `.circleci/**`, `.github/**` |

Test code belongs to the component it tests; the end-to-end suites belong to Infrastructure.

## Entry points (no in-app authentication)

- `POST /orders`; `GET /orders?consumerId=`; `GET /orders/{orderId}`; `POST /orders/{orderId}/{cancel|revise|accept|preparing|ready|pickedup|delivered}` (`OrderController.java`, `TicketController.java`).
- `POST /consumers`, `GET /consumers/{consumerId}` (`ConsumerController.java`).
- `POST /restaurants`, `GET /restaurants/{restaurantId}` (`RestaurantController.java`).
- `POST /couriers`, `GET /couriers/{courierId}`, `POST /couriers/{courierId}/availability`, `POST /couriers/{courierId}/location`, `GET /couriers/{courierId}/workload` (`CourierController.java`).
- `GET /api/tracking/logs`, `/logs/errors`, `/logs/search?uri=`, `/logs/{correlationId}`, `/stats`: the persisted request log of every caller (method, URI, query string, remote IP, User-Agent, exception messages; `ApiTrackingInterceptor.java`, `ApiTrackingController.java`). `X-Correlation-ID` is taken from the client unvalidated and echoed into the response header, MDC and the log.
- `GET /config/sentry`: DSN, environment, PII flag.
- Spring Boot Actuator (Boot 2.0 defaults; Dockerfile healthcheck on `/actuator/health`) and Swagger UI (`common-swagger`).

## Assets and data sensitivity

**[owner]** All assets are equally important; none is ranked above the others.

- Consumer PII: names, delivery addresses with latitude/longitude (`Consumer`, `DeliveryInformation`).
- Order integrity: state machine, totals, line items, assigned courier (`Order`, `OrderState`); `PaymentInformation` in the domain.
- Courier identity, availability and live location.
- The API request log: query strings and client IPs of every caller.
- Secrets: MySQL credentials, Sentry DSN (`SENTRY_DSN`, injected at runtime).

## Attacker profiles

**[owner]** Every attacker matters; none is ruled out.

- **Authenticated gateway user** (consumer, courier, restaurant or operator) acting on other participants' records by changing IDs, or driving order state transitions they should not.
- **Internal-network client** that reaches the application port directly, bypassing the gateway.
- **Insider / co-tenant** with access to MySQL, the cluster or the images.
- **Anonymous internet client**: reaches the app only through the gateway **[owner: behind an authenticating gateway]**. But any internal host can reach the port unauthenticated **[owner]**, so an unauthenticated caller is still a realistic attacker (as an internal client or a compromised internal service). Browser-side attacks (stored XSS in the dashboard fed by API data) still reach operators.
- **Supply chain**: old dependencies (Boot 2.0.3, `openjdk:8u171-jre-alpine`, `msapatterns/mysql:latest`, Gradle 4.10.2) and the CDN-hosted Sentry script.

## Existing controls

- Authenticating gateway in front of the app **[owner]** (not in repo; behaviour unverified).
- JPA/JPQL with bound parameters (e.g. `ApiRequestLogRepository`); no native SQL found in main code.
- SRI hash on the Sentry CDN script; the DSN is not committed (`sentry.dsn=${SENTRY_DSN:}`).
- `GlobalExceptionHandler` in `ftgo-application`.
- Order state transitions guarded by `Order` domain methods.

## Out of scope / accepted risk

**[owner]** Nothing. Missing in-app authentication/authorization is **not** accepted risk; demo DB credentials, Sentry PII/session replay and old pinned dependencies are all in scope.

## Reranking notes

- The owner ruled nothing out and ranked all assets equally, so findings keep the scanner's severity by default.
- The gateway does not lower findings: internal hosts bypass it **[owner]** and per-user authorization at the gateway is unconfirmed, so an unauthenticated internal caller can reach every entry point. A finding moves only if it depends on something the gateway uniquely blocks (an anonymous internet caller with no internal path), which is expected to be rare.

## Finding tags

Component (table above), OWASP category, CVE, Owning team.

## Open questions

1. Does the gateway enforce per-user authorization? (owner: not sure)
2. Which internal services/hosts reach the app port directly, and could a NetworkPolicy restrict that to the gateway?
3. Who owns each component (for the Owning team tag)? The repo has no CODEOWNERS.
