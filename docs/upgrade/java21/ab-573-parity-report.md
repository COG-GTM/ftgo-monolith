# AB-573: Java 21 runtime parity

Captured 2026-11-05 from `6285695255541ebb4dccb40b4f36588c40bcabbb`, Java 21.0.12.1, Boot 3.5.16, Gradle 8.14.4, repository MySQL 8.4.13. This is a stack comparison, not an isolated JDK benchmark. No production code changed. Full raw live bodies, every method result and every changed JSON field are in [ab-573-parity.json](ab-573-parity.json).

## Summary

| Check | Result |
|---|---|
| Full script with host-jar recovery | exit 0; build/test, integrationTest NO-SOURCE, e2e reached |
| Tests | 67 PASS, 0 FAIL, 0 SKIP; baseline 54 PASS / 4 FAIL |
| Original methods | 22 present, 22 PASS; four known failures repaired by earlier Hibernate cut-over |
| Golden tests | ApiGoldenTest 37/37, StaticPathGoldenTest 1/1 |
| OpenAPI | PASS: 24 paths, 25 operations, 26 schemas; 8 request bodies / 25 response schemas |
| Error probes | unknown order 404 empty; state conflict 409; no courier 503; static path 404 |
| Runtime | startup +18.57%, used heap +7.99%, jar +34.73%; see exact table |

## Commands and script recovery

```sh
sudo systemctl stop mysql
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH" DOCKER_HOST_IP=localhost DOCKER_COMPOSE="docker compose"
./build-and-test-all.sh --keep-running > /home/ubuntu/ab-573-evidence/full-build.log 2>&1
# In another shell, after the Docker app exited with UnsupportedClassVersionError:
DOCKER_HOST_IP=localhost "$JAVA_HOME/bin/java" -Xms64m -Xmx192m -XX:+UseParallelGC \
  -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081 \
  > /home/ubuntu/ab-573-evidence/app.log 2>&1
curl -fsS localhost:8081/v3/api-docs -o /home/ubuntu/ab-573-evidence/openapi.json
python3 docs/upgrade/java21/compare-openapi.py /home/ubuntu/ab-573-evidence/openapi.json
curl -sSI localhost:8081/swagger-ui.html
curl -sS -L -o /home/ubuntu/ab-573-evidence/swagger.html -w "%{http_code} %{url_effective}\n" localhost:8081/swagger-ui.html
```

The script is unchanged. Compose already maps host 8081 to container 8080; nginx on host 8080 needs no workaround. However the app Dockerfile still uses Java 8 (AB-571 deferred scope), and exits: `UnsupportedClassVersionError ... class file version 61.0 ... recognizes ... up to 52.0`. The wait script kept polling until the host Java 21 jar was started on 8081; then the original e2e command executed and passed. Thus **not** a claim that the Docker app works. MySQL remains up and host app PID 12972 remains running. Compose `version is obsolete` warnings were inspected: they concern the existing Compose file, not an unloaded environment. The repository blueprint Java 8 instruction is stale; build.gradle explicitly requires toolchain/release 21.

## Per-method differences

JUnit XML from every `*/build/test-results/test/TEST-*.xml` was parsed; Jupiter display suffix `()` removed only for method identity. Against both original CSV and verification.json after_change, the only result changes are:

- `net.chrisrichardson.ftgo.FtgoApplicationTest#shouldDeliverOrder`: FAIL → PASS
- `net.chrisrichardson.ftgo.FtgoApplicationTest#shouldCreateReviseAndCancelOrder`: FAIL → PASS
- `net.chrisrichardson.ftgo.endtoendtests.EndToEndTests#shouldDeliverOrder`: FAIL → PASS
- `net.chrisrichardson.ftgo.endtoendtests.EndToEndTests#shouldCreateReviseAndCancelOrder`: FAIL → PASS

Missing methods: none. New methods versus verification.json:

- `net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfigurationTest#describesFtgoApi`: PASS
- `net.chrisrichardson.ftgo.JpaSchemaValidationTest#shouldUseAndAdvanceTheExistingSequenceValueByOne`: PASS
- `net.chrisrichardson.ftgo.JpaSchemaValidationTest#shouldSearchRequestUrisWithPortableJpql`: PASS
- `net.chrisrichardson.ftgo.OpenApiDocumentationTest#unwrapsResponseAndAsyncContainers`: PASS
- `net.chrisrichardson.ftgobaseline.ApiGoldenTest#springMvcExceptionsAreClientErrors`: PASS
- `net.chrisrichardson.ftgobaseline.ApiGoldenTest#errorPathKeepsTrailingSlash`: PASS
- `net.chrisrichardson.ftgobaseline.MySqlDateTimeTest#shouldPreserveLocalDateTimeInStoredDatetimeAcrossSessionTimeZones`: PASS
- `net.chrisrichardson.ftgo.common.tracking.ApiTrackingInterceptorTest#recordsRequestUriOtherwise`: PASS
- `net.chrisrichardson.ftgo.common.tracking.ApiTrackingInterceptorTest#recordsOriginalUriWhenRequestWasRewritten`: PASS

Module counts: common-swagger: 1, ftgo-application: 44, ftgo-common: 9, ftgo-domain: 9, ftgo-end-to-end-tests: 2, ftgo-order-service: 2.

## OpenAPI and golden/live differences

The comparison script printed both PASS lines above. Swagger entry returns 302, Location `/swagger-ui/index.html`; following it returns 200 with Swagger UI HTML. Browser rendering is not yet checked (browser test follows). Approved differences: `/v3/api-docs` replaces `/v2/api-docs`, schema references/components and documentation metadata differ as described in AB-566. Trailing slash GET `/orders/`, GET `/orders/{id}/`, POST `/orders/` all returned 200 live.

All 30 golden files are accounted for below. “Strict” means passing deterministic golden tests, not exact equality to unrelated live data. The machine artifact lists **every changed field and both values**; live differences are not schema regressions.

- **consumer-created.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.consumerId`.
- **consumer-request.json** — strict golden test; live request. Request fixture sent unchanged to the live POST endpoint.
- **consumer.json** — live + strict golden test. Exact live match.
- **courier-created.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.id`.
- **courier-request.json** — strict golden test; live request. Request fixture sent unchanged to the live POST endpoint.
- **courier-workload.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.courierId, $.lastLocationUpdate`.
- **courier.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Action.makePickup constructs a null time; dropoff time derives from the live readyBy. Changed fields: `$.id, $.lastLocationUpdate, $.plan.actions[0].time, $.plan.actions[1].time`.
- **error-bad-request.json** — strict golden test; no direct live fixture comparison. Strict golden test passes; invalid-location exception is mocked and was not independently reproduced live.
- **error-conflict.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.path, $.timestamp`.
- **error-courier.json** — live + strict golden test. Existing Optional.get() in CourierService.findCourierById throws NoSuchElementException and maps to 500; identical source at AB-563, not a Java 21 regression. Fixture mocks CourierNotFoundException -> 404. Changed fields: `$.error, $.message, $.path, $.status, $.timestamp`.
- **error-internal.json** — live + strict golden test. The real unknown-courier failure exercises the internal-error contract; only generated timestamp and requested courier ID/path differ. Changed fields: `$.path, $.timestamp`.
- **error-order.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.message, $.path, $.timestamp`.
- **error-restaurant.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.message, $.timestamp`.
- **error-unavailable.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Real assignment failure occurs at /accept, not the mocked /ready fixture. Changed fields: `$.path, $.timestamp`.
- **order-assigned.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Real assignment requires ACCEPTED, unlike the mocked APPROVED fixture. Action.makePickup constructs a null time; DROPOFF is calculated from readyBy/location. Changed fields: `$.assignedCourier, $.courierActions[0].time, $.courierActions[1].time, $.estimatedDeliveryTime, $.orderId, $.state`.
- **order-cancelled.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Total is 37.02 because this order was revised to quantity 3 before cancellation; fixture cancels quantity 2. Changed fields: `$.orderId, $.orderTotal`.
- **order-created.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.orderId`.
- **order-history.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$[0].orderId`.
- **order-request.json** — strict golden test; live request. Request sent live with generated consumer/restaurant IDs replacing 1/2.
- **order-revised.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.orderId`.
- **order.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.orderId`.
- **restaurant-created.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.id`.
- **restaurant-request.json** — strict golden test; live request. Request fixture sent unchanged to the live POST endpoint.
- **restaurant.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.id`.
- **sentry.json** — live + strict golden test. Exact live match.
- **static-not-found.json** — live + strict golden test. IDs and timestamps reflect live persisted data; see individual fields. Changed fields: `$.timestamp`.
- **tracking-empty-stats.json** — live + strict golden test. Empty live window selected with minutesBack=0 instead of mocked empty 60-minute window; periodMinutes is the only difference. Changed fields: `$.periodMinutes`.
- **tracking-log.json** — live + strict golden test. Live traffic, IDs, correlation, paths, timings, user agent and request counts differ from the synthetic single-failed-request fixture; exact field differences below. Changed fields: `$.correlationId, $.durationMs, $.errorMessage, $.id, $.queryString, $.requestTimestamp, $.requestUri, $.responseStatus, $.userAgent`.
- **tracking-logs.json** — live + strict golden test. Live traffic, IDs, correlation, paths, timings, user agent and request counts differ from the synthetic single-failed-request fixture; exact field differences below. Changed fields: `$[0].durationMs, $[0].errorMessage, $[0].id, $[0].queryString, $[0].requestTimestamp, $[0].requestUri, $[0].responseStatus, $[0].userAgent, $[1]`.
- **tracking-stats.json** — live + strict golden test. Live traffic, IDs, correlation, paths, timings, user agent and request counts differ from the synthetic single-failed-request fixture; exact field differences below. Changed fields: `$.avgDurationMs, $.errorCount, $.errorRate, $.p95DurationMs, $.statusCodeDistribution.200, $.statusCodeDistribution.302, $.statusCodeDistribution.404, $.statusCodeDistribution.409, $.statusCodeDistribution.500, $.topEndpoints.GET /config/sentry, $.topEndpoints.GET /consumers/1, $.topEndpoints.GET /consumers/5, $.topEndpoints.GET /couriers/1, $.topEndpoints.GET /couriers/17, $.topEndpoints.GET /couriers/18, $.topEndpoints.GET /couriers/18/workload, $.topEndpoints.GET /couriers/999999999, $.topEndpoints.GET /health, $.topEndpoints.GET /orders, $.topEndpoints.GET /orders/, $.topEndpoints.GET /orders/1, $.topEndpoints.GET /orders/2, $.topEndpoints.GET /orders/3, $.topEndpoints.GET /orders/4, $.topEndpoints.GET /orders/5/, $.topEndpoints.GET /orders/6, $.topEndpoints.GET /orders/999999999, $.topEndpoints.GET /restaurants/1, $.topEndpoints.GET /restaurants/2, $.topEndpoints.GET /restaurants/3, $.topEndpoints.GET /restaurants/4, $.topEndpoints.GET /restaurants/5, $.topEndpoints.GET /swagger-ui.html, $.topEndpoints.GET /swagger-ui/index.html, $.topEndpoints.GET /this-static-path-does-not-exist.js, $.topEndpoints.GET /v3/api-docs, $.topEndpoints.HEAD /swagger-ui.html, $.topEndpoints.POST /consumers, $.topEndpoints.POST /couriers, $.topEndpoints.POST /couriers/1/availability, $.topEndpoints.POST /couriers/17/availability, $.topEndpoints.POST /couriers/18/availability, $.topEndpoints.POST /couriers/18/location, $.topEndpoints.POST /couriers/2/availability, $.topEndpoints.POST /orders, $.topEndpoints.POST /orders/, $.topEndpoints.POST /orders/1/accept, $.topEndpoints.POST /orders/1/delivered, $.topEndpoints.POST /orders/1/pickedup, $.topEndpoints.POST /orders/1/preparing, $.topEndpoints.POST /orders/1/ready, $.topEndpoints.POST /orders/2/cancel, $.topEndpoints.POST /orders/2/revise, $.topEndpoints.POST /orders/3/accept, $.topEndpoints.POST /orders/3/delivered, $.topEndpoints.POST /orders/3/pickedup, $.topEndpoints.POST /orders/3/preparing, $.topEndpoints.POST /orders/3/ready, $.topEndpoints.POST /orders/4/cancel, $.topEndpoints.POST /orders/4/revise, $.topEndpoints.POST /orders/5/accept, $.topEndpoints.POST /orders/5/cancel, $.topEndpoints.POST /orders/5/preparing, $.topEndpoints.POST /orders/5/revise, $.topEndpoints.POST /orders/6/accept, $.topEndpoints.POST /orders/999999999/preparing, $.topEndpoints.POST /restaurants, $.totalRequests`.

## Error paths (curl status and body)

All calls use `curl -sS -w "\n%{http_code}" -H "X-Correlation-ID: golden-correlation"`; POST uses `-X POST` or `-H "Content-Type: application/json" -d BODY`. See machine artifact for all paths and sent fixture descriptions above.

### unknown-order: `/orders/999999999` → 404
```json
""
```

### error-order: `/orders/999999999/preparing` → 404
```json
{
  "timestamp": "2026-11-05T21:34:13.419748282",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found999999999",
  "path": "/orders/999999999/preparing",
  "correlationId": "golden-correlation"
}
```

### error-conflict: `/orders/5/preparing` → 409
```json
{
  "timestamp": "2026-11-05T21:34:13.399428262",
  "status": 409,
  "error": "State Transition Error",
  "message": "current state: APPROVED",
  "path": "/orders/5/preparing",
  "correlationId": "golden-correlation"
}
```

### error-unavailable: `/orders/7/accept` → 503
```json
{
  "timestamp": "2026-11-05T21:34:25.313439978",
  "status": 503,
  "error": "No Courier Available",
  "message": "No courier available for assignment",
  "path": "/orders/7/accept",
  "correlationId": "golden-correlation"
}
```

### static-not-found: `/this-static-path-does-not-exist.js` → 404
```json
{
  "timestamp": "2026-11-05T21:34:13.437+0000",
  "status": 404,
  "error": "Not Found",
  "message": "No message available",
  "path": "/this-static-path-does-not-exist.js"
}
```

GET unknown order intentionally has no body (existing controller-local 404); `error-order.json` describes POST preparing advice, tested separately. Timestamp, generated IDs in path/message, and the actual /accept assignment route are the only expected differences for these error fixtures. 503 required disabling **all** available persisted couriers (IDs 1,17,18); first attempt missed 17 and returned 200, so a fresh APPROVED order was used after correcting setup. No direct SQL mutations were used.

Additional exploratory findings: unknown courier 500 is not a new regression: runtime stack shows Optional.get in CourierService.findCourierById, identical source at AB-563. Reusing golden-correlation on many requests caused a tracking lookup 500 (NonUniqueResultException, 28 results); unique `ab573-unique` lookup succeeded. These mock/live mismatches are not changed in this parity ticket.

## Runtime

Measured after original tests/e2e and exactly 100 sequential rounds GET `/config/sentry`, `/api/tracking/stats`, `/consumers/1`, `/restaurants/1`: **400 HTTP 200**. Then `jcmd 12972 GC.run` printed `Command executed successfully`, immediately followed by `jstat -gc 12972 1000 1`. Explicit ParallelGC matches JDK 8 (JDK 21 default would be G1). Startup log: `Started FtgoApplicationMain in 5.12 seconds (process running for 5.542)`.

| Measurement | JDK 8 baseline | Java 21 | Delta |
|---|---:|---:|---:|
| startup_reported_seconds | 4.318 | 5.12 | +18.57% |
| jvm_uptime_at_started_seconds | 4.657 | 5.542 | +19.00% |
| jar_size_bytes | 47342837 | 63784734 | +34.73% |
| heap_used_after_warmup_and_gc_bytes_approx | 38931763 | 42043904 | +7.99% |
| heap_committed_bytes | 144179200 | 110624768 | -23.27% |

```text
    S0C         S1C         S0U         S1U          EC           EU           OC           OU          MC         MU       CCSC      CCSU     YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT   
     5120.0      4608.0         0.0         0.0      53760.0        202.4      44544.0      40856.1   102272.0   101140.3   14016.0   13510.5     31     0.144     5     0.358     -         -     0.502
```

Used heap sums S0U+S1U+EU+OU in KiB × 1024 (approximate); committed sums S0C+S1C+EC+OC. Jar measured with `stat -c %s ftgo-application/build/libs/ftgo-application.jar`. Flags and warmup match; DB necessarily differs (baseline 5.7.13, target 8.4.13), and the previously failing lifecycle tests now complete. Startup, used heap and jar size increases are **flagged regressions in these local measurements**, not causal attribution to Java 21. Committed heap decreased. Repeat samples on comparable hardware before performance conclusions.

## Open items / scope

- AB-571: Docker application image still runs Java 8 and cannot run this jar; full script completed only after starting Java 21 host jar.
- Single local runtime sample: startup, heap used and jar size increases need repeated comparable measurements.
- Live unknown courier returns preexisting 500 rather than mocked 404.
- Reused correlation ID causes preexisting single-result query failure; unique correlation tested successfully.
- Mock-only error-bad-request not directly reproduced live; empty tracking stats were checked with a zero-minute window.
- No code fix in AB-573: observed baseline SQL failures already repaired by AB-570; Docker runtime upgrade remains AB-571.
- No PR or Jira changes. No browser/visual verification in this step. App and MySQL intentionally left up.
- Raw logs and capture scripts are persistent local evidence under `/home/ubuntu/ab-573-evidence/`; portable responses/methods/runtime are committed in the JSON report.

## Browser smoke (dashboard)

Run in Chrome against the same Java 21 host jar on 8081 + MySQL 8.4, recorded (recording attached to AB-573 / PR).

- The dashboard (`static/js/app.js`) runs CRUD and order lifecycle on in-browser mock/localStorage data: API tracking recorded no browser POSTs. Its **Add Consumer / Add Restaurant / Add Courier** buttons don't open a form. `static/` is unchanged since the AB-563 base `1fb9c78d`, so this is **pre-existing UI behaviour, not a Java 21 regression**. Because of it, the full flow could not be driven from the dashboard.
- Real REST flow instead (the browser showed each persisted result): consumer 7, restaurant 6, courier 19, order 8 (total 24.68). accept → preparing → ready → pickedup → delivered each returned 200, and GETs confirmed `APPROVED → ACCEPTED → PREPARING → READY_FOR_PICKUP → PICKED_UP → DELIVERED` with courier 19 assigned. The API tracking page lists every lifecycle POST, including `/orders/8/delivered` 200.
- `GET /consumers/7` returns the right name but `consumerId: 0`. This was already the case before the upgrade (noted during AB-566); it is not a regression.
- `/swagger-ui.html` → 302 → `/swagger-ui/index.html` 200 (renders). `/nope.js` → 404. `/config/sentry` → 200 (`dsn: ""`, `environment: demo`), and the Sentry SDK + `sentry-init.js` load. With an empty DSN the feedback widget is disabled by design, so submitting feedback was not exercised.
