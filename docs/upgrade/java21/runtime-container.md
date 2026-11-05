# Java 21 container runtime (AB-571)

Verified locally on 2026-11-05: Temurin 21.0.13+9 JRE Alpine, Docker Compose
plugin v5.6.1 (Compose v2 CLI syntax), cgroup v2, MySQL 8.4. Gradle 8.14.4
runs on the host's Ubuntu OpenJDK 21; no JDK 8 fallback is needed.

## Startup and verification

```sh
sudo systemctl stop mysql # host service otherwise owns port 3306
export DOCKER_HOST_IP=localhost
./gradlew :ftgo-application:bootJar
docker compose up -d --build mysql
./gradlew waitForMySql :ftgo-flyway:flywayMigrate
docker compose up -d --build --wait --wait-timeout 180
docker compose ps
docker compose exec -T ftgo-application id
docker compose exec -T ftgo-application ls -la /app
./run-end-to-end-tests.sh
./gradlew -x :ftgo-end-to-end-tests:test build
docker stats --no-stream ftgo-monolith-ftgo-application-1
docker compose down -v
```

`mysql/schema.sql` creates `ftgo` and grants; it does not create application
tables. Flyway runs from the host before starting the app. The MySQL healthcheck
checks a TCP query as the application user against `ftgo`, avoiding the temporary
socket-only initialization server. It does not replace schema migration.
The app healthcheck uses curl on container port 8080; host traffic uses 8081.

Observed: both services healthy; app UID 100 / GID 101 (`ftgo`); `/app` contains
only `ftgo-application.jar` (63,784,734 bytes), not the plain jar. `/proc/1/cmdline`
is `java -XX:MaxRAMPercentage=75 -jar ftgo-application.jar` (JVM receives signals).
E2E: 2 tests, 0 failures/errors/skips. Full build excluding E2E: 65 tests,
0 failures/errors/skips. Both Gradle invocations reported `BUILD SUCCESSFUL`.
GitHub Actions now adds a separate container job, retaining the existing jobs,
with the same migration/health/e2e sequence and always-run logs and cleanup.
The five root startup/build scripts now use `docker compose` by default;
CircleCI and its private setup scripts are removed.

## Heap comparison

No diagnostic JDK tools were added to the JRE image. Heap readings below sum
the `area="heap"` pools in `/actuator/prometheus`, the already-exposed Actuator
Micrometer endpoint (same JVM memory meters as `/actuator/metrics/jvm.memory.*`).
The metrics endpoint itself remains unexposed; no extra management endpoints
are enabled just for measurement.

After the passing E2E suite, repeat AB-563's 100 rounds of `/config/sentry`,
`/api/tracking/stats`, `/consumers/1`, `/restaurants/1`: **400 HTTP 200 responses**.
Then scrape once:

```sh
curl -fsS http://localhost:8081/actuator/prometheus | \
  rg 'jvm_memory_(max|used|committed)_bytes.*area="heap"'
docker stats --no-stream ftgo-monolith-ftgo-application-1
```

| Measurement | AB-563 JDK 8 reference | Java 21 container |
| --- | ---: | ---: |
| Configured maximum | 192 MiB (`-Xmx192m`) | 384 MiB budget (75% of 512 MiB) |
| Reported heap-pool maximum | — | 389,283,840 bytes / 371.250 MiB |
| Used heap | 38,931,763 bytes / ~37.13 MiB | 45,616,416 bytes / 43.503 MiB |
| Committed heap | 144,179,200 bytes / 137.5 MiB | 69,828,608 bytes / 66.594 MiB |
| Docker memory usage / limit | not captured | 302.7 MiB / 512 MiB (59.12%) |

The JVM selected Serial GC under this container's resources; pool maxima exclude
one reserved survivor space, so their sum is below the 384 MiB budget.
This is a single sample, **not a performance benchmark**. Unlike AB-563's
explicit full GC, this sample follows automatic collection only: used heap is
not a like-for-like retained-heap comparison. Framework/collector/DB versions
and the now-passing workload also differ. Docker memory includes native and
other non-heap usage, so it must not be compared directly with heap used.

Existing warnings remain: Gradle deprecated features (future Gradle 9), Mockito
CDS sharing warning, and Flyway's MySQL 8.4 support warning. These did not fail
verification; the environment was checked to use JAVA_HOME and toolchain 21.
