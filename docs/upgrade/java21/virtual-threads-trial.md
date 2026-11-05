# AB-574 virtual threads trial

## Decision

Keep `spring.threads.virtual.enabled=false` explicitly in `application.properties`.
Spring Boot 3.5 already supplies the opt-in Tomcat/application executor support;
no custom executor or preview JVM option is necessary on Java 21. Override with
`--spring.threads.virtual.enabled=true`. This is a trial, not approval to enable
virtual threads in production. Revisit after the JDBC driver/pinning issue below
and the subsequent regression gate have been addressed.

## Environment and method

- Local Ubuntu x86_64, Linux `6.8.0-1060-aws`, 8 logical Xeon CPUs, 31 GiB RAM.
- OpenJDK `21.0.12.1+1-1-22.04.4-Ubuntu`; Spring Boot 3.5.16;
  Tomcat 10.1.55; HikariCP 6.3.3; **Connector/J 8.4.0, not 9.x**.
  The explicit version is in `gradle/libs.versions.toml`; the packaged jar confirms it.
  No dependency upgrade was folded into this executor experiment.
- Docker Compose MySQL 8.4.13, migrated database, host `localhost`; application
  port 8081. Database and application are on the same host as the load generator.
- Same jar throughout both modes and all repetitions:
  SHA-256 `87b9cfb66b24f2401ed5d3ab6c966378432602d520c14a42995c0f791fc080ab`.
- Default Hikari maximum pool size **10**, Tomcat platform maximum 200;
  neither was tuned. Default SQL DEBUG and API tracking INFO logging retained.
  API tracking persists another database write after each API request.
- `load-test.py` uses Python standard-library HTTP/1.1 keep-alive clients, one
  synchronous client per worker, with 30-second request timeouts. It seeds a real
  consumer, restaurant/menu and order through the API; the same seed file/IDs are
  reused across modes. Requests rotate equally across GET consumer, GET restaurant,
  GET order and POST order. Order creation and tracking data are not rolled back.
- Each run has 15 seconds of unmeasured warmup then 30 seconds of measurement,
  at concurrency 50 and 200, twice per mode. Order: platform-1, virtual-1,
  platform-2, virtual-2; each process executes c50 then c200. Each process is
  restarted from the same jar; the database is not reset. Tail latency includes
  the complete response read; throughput uses actual elapsed time including
  draining requests that started before the deadline. Percentiles use nearest rank.
- Two complete matrices were run: one with pin tracing for diagnosis, then one
  without tracing for performance. Tracing produces large synchronous diagnostics
  and cannot be used as an unbiased performance comparison. Each matrix uses
  identical JVM options between modes except the Boot virtual-thread flag.
- Closed-loop, single-host results are not production capacity estimates; JIT,
  database growth, logging, and run-order effects remain. Python client CPU was
  about 37% of one CPU in a sampled c50 platform run, versus app ~347% and
  MySQL ~56%; this sample does not prove the generator never bottlenecks.

## Reproduction

Use a disposable local database: this workload creates many orders and tracking
rows. Keep the seed and logs outside the source tree. There is no need to install
an extra load-test dependency.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
export DOCKER_HOST_IP=localhost
sudo systemctl stop mysql
docker compose up -d --build mysql
./gradlew waitForMySql :ftgo-flyway:flywayMigrate
./gradlew -x :ftgo-end-to-end-tests:test build
mkdir -p "$HOME/ab574-vt-trial"
```

Start one process at a time (stop its exact PID before starting the other mode):

```bash
# Chosen default: no virtual-thread command-line override.
java -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081

# Pinning diagnostic process; stdout AND stderr must be captured.
java -Djdk.tracePinnedThreads=full \
  -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081 \
  --spring.threads.virtual.enabled=true > "$HOME/ab574-vt-trial/virtual-app.log" 2>&1

# Performance process: omit tracePinnedThreads, otherwise same options.
java -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081 \
  --spring.threads.virtual.enabled=true
```

In another terminal, wait for `/actuator/health`, then run both E2E flows (create,
revise, cancel; and create, courier assignment, accept, prepare, ready, pickup,
deliver). Run these once in default/platform mode and once in VT diagnostic mode:

```bash
curl --retry 30 --retry-connrefused --retry-delay 1 --fail localhost:8081/actuator/health
./run-end-to-end-tests.sh
for c in 50 200; do
  python3 docs/upgrade/java21/load-test.py --concurrency "$c" \
    --warmup 15 --duration 30 --seed "$HOME/ab574-vt-trial/seed.json" \
    --label virtual-1 | tee -a "$HOME/ab574-vt-trial/results.jsonl"
done
rg -n 'reason:MONITOR|<== monitors:' "$HOME/ab574-vt-trial/virtual-app.log"
rg -n synchronized --glob '*.java' .
curl --silent localhost:8081/actuator/prometheus | rg '^hikaricp_connections_(active|idle|max|pending)'
```

Repeat with labels platform-1, virtual-1, platform-2, virtual-2 and the appropriate
flag, restarting between labels. Repeat the entire matrix without pin tracing,
saving that output separately as `performance.jsonl`.

## Pinning findings

Observed diagnostic trace headers: **46,877** in virtual-1 and **60,806** in
virtual-2 (startup health, E2E where run, warmup and measured traffic combined).
These are printed trace counts, not an exhaustive JFR event count or pin duration.
Every printed trace contains the Connector/J read monitor:

```text
VirtualThread[#50,tomcat-handler-0]/runnable@ForkJoinPool-1-worker-1 reason:MONITOR
    java.base/java.lang.VirtualThread$VThreadContinuation.onPinned(VirtualThread.java:199)
    ...
    java.base/sun.nio.ch.NioSocketImpl.timedRead(NioSocketImpl.java:280)
    ...
    com.mysql.cj.protocol.ReadAheadInputStream.read(ReadAheadInputStream.java:171) <== monitors:1
```

The loaded 8.4.0 jar's bytecode (`javap -p`) independently confirms
`ReadAheadInputStream.read(byte[], int, int)` is synchronized. Unlike the expected
newer ReentrantLock-based driver, this actual driver performs blocking socket
reads while holding intrinsic monitors. Other monitor-owning frames include
`ConnectionImpl.setAutoCommit`, `commit`, `isValid`, and prepared-statement query
and update execution. These are third-party dependencies, not safe repo-local
`synchronized` replacements.

Reachability is established by actual traces, not just library inspection:
`tomcat-handler-*` virtual threads traverse `OrderController.getOrder` and
`OrderController.create`, consumer/restaurant controllers, Spring transactions/
Hibernate, then Connector/J and socket reads. Tracking persistence also reaches
the same path through `ApiTrackingInterceptor.afterCompletion`. The first trace
is a health-check connection-validation path through
`HikariPool.getConnection -> PoolBase.isConnectionDead -> ConnectionImpl.isValid`;
business-flow frames above separately rule out health-check-only pinning.

Checked the competing Hikari hypothesis: no `<== monitors:...` frame belongs to
Hikari in either VT log. `ConcurrentBag.borrow` uses `SynchronousQueue.poll`
(verified in bytecode), although pool lifecycle methods have synchronization.
Hikari calls the driver and can wait for a connection; these traces do not show
its own monitor causing the socket-read pin. This does not prove all possible
Hikari paths are pin-free.

Three virtual-1 traces also show Hibernate's
`ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(...:439)` holding a
monitor; these also contain the driver read monitor. No such frame appeared in
virtual-2. Removing driver pinning alone has not been tested and must not be
assumed to eliminate all pinning. Repository Java source search for
`synchronized` returned **no matches**, so no repo-local lock rewrite was made.

## Pool bottleneck and interpretation

An in-flight performance c50 platform sample returned:

```text
hikaricp_connections_active{pool="HikariPool-1",service="ftgo-application"} 10.0
hikaricp_connections_idle{pool="HikariPool-1",service="ftgo-application"} 0.0
hikaricp_connections_max{pool="HikariPool-1",service="ftgo-application"} 10.0
hikaricp_connections_pending{pool="HikariPool-1",service="ftgo-application"} 34.0
```

The pool is demonstrably saturated at that instant; virtual threads do not add
database connections. An earlier traced platform sample also had 10 active and
33 pending. Post-run metrics return to idle and are not evidence of peak usage.
No pool-size tuning was included: that would confound the executor comparison.
Pinning is confirmed; its isolated contribution to throughput is not quantified.

## Results

Results tables are recorded below. All error counts include HTTP non-200 and
client transport errors; warmup errors are reported separately by the script.


### Performance (pin tracing disabled)

| Mode / repetition | Concurrency | Requests/s | p50 ms | p95 ms | p99 ms | Errors |
|---|---:|---:|---:|---:|---:|---:|
| platform-1 | 50 | 1530.71 | 28.756 | 70.570 | 95.055 | 0 |
| platform-1 | 200 | 1457.22 | 115.205 | 333.271 | 484.986 | 0 |
| virtual-1 | 50 | 1432.15 | 31.228 | 69.434 | 95.264 | 0 |
| virtual-1 | 200 | 1422.10 | 110.151 | 343.326 | 502.317 | 0 |
| platform-2 | 50 | 1775.31 | 24.661 | 60.891 | 81.075 | 0 |
| platform-2 | 200 | 1530.73 | 104.020 | 321.055 | 464.414 | 0 |
| virtual-2 | 50 | 1395.20 | 31.848 | 71.719 | 99.733 | 0 |
| virtual-2 | 200 | 1370.27 | 112.216 | 354.687 | 528.451 | 0 |

All measured and warmup errors were zero. Across the two repetitions:

| Concurrency | Platform mean req/s | VT mean req/s | VT / platform | Platform mean run p99 ms | VT mean run p99 ms | p99 increase | Gate |
|---|---:|---:|---:|---:|---:|---:|---|
| 50 | 1653.01 | 1413.68 | 85.5% | 88.065 | 97.499 | 10.7% | FAIL |
| 200 | 1493.97 | 1396.18 | 93.5% | 474.700 | 515.384 | 8.6% | FAIL |

The gate requires >=95% throughput, <=10% p99 increase and zero errors.
The p99 comparison above averages each run's p99, not pooled request samples.
At c50 both individual throughput ratios also fall below 95%; at c200 the second
repetition fails throughput and p99. Thus there is no basis to change the default,
even after removing diagnostic overhead. This is evidence of a regression in this
local trial, not proof that pinning alone caused all of it.

### Diagnostic matrix (full pin tracing enabled)

| Mode / repetition | Concurrency | Requests/s | p50 ms | p95 ms | p99 ms | Errors |
|---|---:|---:|---:|---:|---:|---:|
| platform-1 | 50 | 1445.05 | 30.141 | 75.574 | 103.109 | 0 |
| platform-1 | 200 | 1622.82 | 107.801 | 284.483 | 396.045 | 0 |
| virtual-1 | 50 | 333.79 | 128.234 | 306.336 | 429.728 | 0 |
| virtual-1 | 200 | 305.83 | 558.964 | 1411.363 | 1987.339 | 0 |
| platform-2 | 50 | 993.24 | 40.748 | 123.403 | 179.582 | 0 |
| platform-2 | 200 | 1380.99 | 119.868 | 342.753 | 487.918 | 0 |
| virtual-2 | 50 | 614.80 | 74.486 | 147.329 | 197.454 | 0 |
| virtual-2 | 200 | 617.50 | 302.670 | 621.746 | 855.508 | 0 |

All diagnostic warmup errors were zero too. Do not interpret this matrix as
production performance: VT logs were 562/742 MiB versus platform 199/146 MiB.
The much lower VT diagnostic throughput shows why the uninstrumented matrix is
reported separately. Baseline SQL/API logs remain enabled in both matrices.

## Verification and artifacts

No Java source, dependency, pool sizing, golden test, or golden fixture changes
were needed for this trial. Only the explicit opt-in property, this document and
the standard-library load script were added. Script CLI checks rejected invalid
concurrency and unsupported URL schemes before sending requests.

`DOCKER_HOST_IP=localhost ./gradlew -x :ftgo-end-to-end-tests:test build`:

```text
BUILD SUCCESSFUL in 20s
50 actionable tasks: 5 executed, 45 up-to-date
```

E2E with the chosen default value (`--spring.threads.virtual.enabled=false`):

```text
BUILD SUCCESSFUL in 5s
17 actionable tasks: 2 executed, 15 up-to-date
```

E2E with `--spring.threads.virtual.enabled=true` and full pin tracing:

```text
BUILD SUCCESSFUL in 5s
17 actionable tasks: 2 executed, 15 up-to-date
```

Both E2E suites exercised both lifecycle flows. Raw local evidence is retained in
`$HOME/ab574-vt-trial/`: `results.jsonl` (traced), `performance.jsonl` (untraced),
`seed.json`, `{platform,virtual}-{1,2}-app.log` (traced), corresponding
`*-perf-app.log` (untraced), `platform-1-e2e.log`, `virtual-1-e2e.log`, and
`inflight-pool.txt`. Build output: `$HOME/ab574-vt-build.log`.
Logs had no `ERROR` or `Failed to persist API request log` matches. Raw logs are
not committed because of their size. Processes were stopped by their exact PID
at the end of each label; MySQL remains running. Dashboard visuals were not
retested: this change is executor configuration only, with existing static/API
contracts exercised by the build.

Next experiment: separately upgrade and verify a supported ReentrantLock-based
Connector/J version, repeat pin tracing (including Hibernate's cold query-plan
path), then rerun this same matrix before considering a default-on decision.
