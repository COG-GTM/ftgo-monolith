# JDK 8 behaviour baseline (AB-563)

For current developer setup and operator procedures, see
[Java 21 upgrade and rollback](UPGRADE.md).

Captured on 2026-11-05 from `1fb9c78da64bd0bf7a1562abc990e660f2f0c544`.
At capture time this was both `origin/master` and `origin/devin/ab-561-java21`.
No production code, schema, dependency version or existing test was changed.

## Artifacts

| File | Purpose |
| --- | --- |
| `api-baseline.json` | Actual HTTP `/v2/api-docs` response from the running baseline jar on port 8081; only JSON indentation/key ordering changed. Swagger 2, 24 paths / 25 operations. |
| `golden/*.json` | Strict response fixtures and representative request bodies, loaded as test resources by `ftgo-application`. |
| `test-baseline.csv` | Every original test method's result, elapsed seconds and failure message (22 methods, 6 classes). |
| `verification.json` | Original and post-change counts, post-change per-method results, and database-unavailable verification. |
| `runtime-baseline.json` | Startup, post-warmup heap, raw `jstat` sample and jar size for AB-573. |
| `baseline-log-excerpts.txt` | Relevant original command results and failure evidence, without environment dumps. |

## Results and known failures

Original tests: **18 pass, 4 fail**. New tests: **36 pass**. Combined: **54 pass,
4 fail**. `integrationTest` exists only on `ftgo-order-service` and reports
`NO-SOURCE`; it is not counted as a passing integration test.

The original `build-and-test-all.sh --keep-running` exits 1 at
`:ftgo-application:test`, so its subsequent Docker app build/start and e2e steps
are not reached. We ran the remaining build tasks with `--continue`, started the
JDK 8 jar directly on 8081, and ran the original `run-end-to-end-tests.sh` separately.

Both methods below fail in **both** `FtgoApplicationTest` and `EndToEndTests`:

- `shouldDeliverOrder`
- `shouldCreateReviseAndCancelOrder`

Observed on the unchanged base and again after this change: `POST /orders`
returns HTTP 500 instead of 200; Hibernate reports
`SQLSyntaxErrorException: Unknown column 'latitude' in 'field list'` on the
`orders` insert. The migrations add `delivery_address_latitude` /
`delivery_address_longitude`, but the insert asks for unprefixed `latitude` /
`longitude` too. This ticket records the mismatch; it does not repair it or
weaken/disable the failing tests. The delivery/cancel/revise scenarios therefore
**do not reach their later lifecycle steps** against the real DB.

Severity: **Sev 2** for this observed baseline failure: core order creation is
broken in the local/test deployment; no production impact was investigated.
Later tickets must distinguish these four known failures from new regressions.

Environment friction: the first script attempt failed binding port 3306 because
host MySQL 8 was running. After stopping that host service, the repository's
unmodified MySQL **5.7.13** Docker image built and ran; Flyway V1/V2 succeeded.
The snapshot's earlier image-pull failure did not reproduce here. MySQL 8.4 is
the epic's later target, **not** the database used for this JDK 8 reference.

## Reproduce the original baseline

Run from the repository root on a disposable local database (the original script
recreates its Docker database/volumes):

```sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
export DOCKER_HOST_IP=localhost
export DOCKER_COMPOSE='docker compose'
# Stop host MySQL first if it owns port 3306.
sudo systemctl stop mysql
./build-and-test-all.sh --keep-running
# Expected exit 1 at the two FtgoApplicationTest methods above.
./gradlew --continue cleanTest -x :ftgo-end-to-end-tests:test build integrationTest
# Expected exit 1; continues to the other module tests.
java -Xms64m -Xmx192m -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081
# In another terminal with the same JAVA_HOME/PATH/DOCKER_HOST_IP:
./run-end-to-end-tests.sh
# Expected exit 1 at the two EndToEndTests methods above.
curl -fsS http://localhost:8081/v2/api-docs
```

`docker compose` substitutes for the script's unavailable legacy
`docker-compose` executable. Port 8080 is already occupied in this VM.
The original shell scripts were actually executed; the Gradle/direct-jar
commands above are supplements, not a claim that the original script passed.

## Run only the new safety net (no MySQL required)

```sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:1/no_database?connectTimeout=1000' \
  ./gradlew :ftgo-application:cleanTest :ftgo-application:test --tests '*GoldenTest'
# BUILD SUCCESSFUL: 36 tests, 0 failures.
```

`ApiGoldenTest` uses MockMvc and the actual controllers/advice with mocked
service/repository boundaries. Its ObjectMapper is initialized by the real
`CommonJsonMapperInitializer`, as in `CommonConfiguration`; it does not invent
test-only Money/date serialization rules. It covers every documented operation:
consumers, restaurants, order history and each lifecycle route, couriers and
availability/location/workload, all five tracking routes, and Sentry config.
Request argument checks include Money-string and ISO LocalDateTime parsing.

Strict JSON comparisons reject added/removed/renamed fields, wrong value types
and array order changes, while ignoring object key order. Money remains a JSON
**string** (e.g. `"24.68"`); LocalDateTime remains an ISO string such as
`"2026-01-02T03:24:05"`, never an array or epoch timestamp. Assigned and unassigned
orders cover null fields, action times and estimated delivery time. Workload,
courier plan and tracking-log times are fixed and checked exactly. Empty HTTP
200 lifecycle responses are asserted as empty, not `{}`.

All seven `GlobalExceptionHandler` mappings (including each 404 exception type)
are exercised through controllers with a correlation ID. Only their generated
timestamp is normalized **after** asserting it is a string parseable as
LocalDateTime. Other fields must match exactly; internal exception details must
not leak in the 500 response. Existing controller-local empty 404 responses are
also preserved. One surprising existing detail is preserved intentionally:
`GET /consumers/{id}` includes `consumerId: 0` from its response superclass.

`StaticPathGoldenTest` uses a real embedded servlet container without JPA/DB
auto-configuration: MockMvc alone does not perform the container's `/error`
redispatch. It checks the unknown static path's HTTP 404, JSON body and string
timestamp, not merely an unmapped-controller result. Boot 2's default error
timestamp has an offset (`...+0000`), unlike the domain LocalDateTime fields.
This default body is separate from the advice's error format and has no
`correlationId` field.

Both test configurations live outside `net.chrisrichardson.ftgo` so the existing
application test's component scan cannot accidentally import the mock-only
configuration. No new dependencies are introduced. The only JUnit 4 glue is
`@RunWith(SpringRunner.class)`, `@Before`, and `@Test`: AB-565 can replace these
with `@ExtendWith(SpringExtension.class)`, `@BeforeEach`, and Jupiter `@Test`.
MockMvc, AssertJ, Mockito and the Spring test annotations are available on the
Boot 3.5 path (`@MockBean` is deprecated there but still available). The static
404 test deliberately detects changes to the default error contract at cut-over.

## Runtime comparison protocol (AB-573)

One local sample, **not** a benchmark or CI threshold:

- Spring's `Started FtgoApplicationMain` reports **4.318 s**, JVM uptime **4.657 s**.
- Baseline executable jar: **47,342,837 bytes**.
- Fixed flags: `-Xms64m -Xmx192m`, ParallelGC, JDK 8u504.
- After the original test/e2e attempts, make 100 sequential rounds of
  `GET /config/sentry`, `/api/tracking/stats`, `/consumers/1`, `/restaurants/1`
  (400 successful responses). Retain the same DB and prior test attempts for
  a comparable run; tracking writes are part of the workload.
- Request one full GC with `jcmd PID GC.run`; immediately run
  `jstat -gc PID 1000 1`. Used heap is approximately **38,931,763 bytes**
  (sum `S0U + S1U + EU + OU`, KiB to bytes), committed heap **144,179,200 bytes**.
  These are heap values, not RSS; precision is limited by the printed sample.

Record multiple samples on comparable hardware before drawing performance
conclusions. Swagger's `host` is intentionally the actual capture address
`localhost:8081`; compare semantic operations/schemas rather than that field
when moving to OpenAPI 3 in AB-570. Do not overwrite this JDK 8 reference with
an upgraded runtime's output.
