# Testcontainers Guide: Replacing Docker Compose for Integration Tests

## Overview

This guide documents how FTGO microservices use [Testcontainers](https://www.testcontainers.org/) to replace Docker Compose for integration test infrastructure. Testcontainers provides programmatic, JUnit 5-integrated container lifecycle management that eliminates the need for external `docker-compose.yml` files.

## Why Testcontainers Over Docker Compose?

| Aspect                  | Docker Compose                          | Testcontainers                          |
|-------------------------|------------------------------------------|-----------------------------------------|
| **Lifecycle**           | Manual start/stop before tests           | Automatic — tied to test lifecycle       |
| **CI Integration**      | Requires `docker-compose` installed      | Only requires Docker daemon              |
| **Port Management**     | Fixed ports (risk of conflicts)          | Random ports (no conflicts)              |
| **Test Isolation**      | Shared across all tests                  | Per-test or per-suite isolation           |
| **Configuration**       | External YAML files                      | Java code — type-safe, refactorable      |
| **Cleanup**             | Manual cleanup required                  | Automatic container removal              |
| **IDE Support**         | Run scripts separately                   | Run tests directly from IDE              |
| **Reusability**         | Copy YAML between projects               | Share via library (`ftgo-test-lib`)       |

## Getting Started

### 1. Add Dependency

Testcontainers is already configured in `ftgo-test-lib`. Add it to your service:

```groovy
// services/ftgo-order-service/build.gradle
testCompile project(':shared-ftgo-test-lib')
```

### 2. Use the Pre-Configured MySQL Container

The `ftgo-test-lib` provides `FtgoMySQLContainer` — a singleton MySQL container pre-configured for FTGO:

```java
import net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistOrder() {
        Order saved = orderRepository.save(newOrder());
        assertThat(saved.getId()).isNotNull();
    }
}
```

### 3. Or Extend AbstractIntegrationTest

For the simplest setup, extend the base class which pre-configures everything:

```java
import net.chrisrichardson.ftgo.testlib.config.AbstractIntegrationTest;

class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistOrder() {
        // MySQL container is already running and configured
        Order saved = orderRepository.save(newOrder());
        assertThat(saved.getId()).isNotNull();
    }
}
```

## Container Configuration Details

### FtgoMySQLContainer

The `FtgoMySQLContainer` singleton provides:

- **Image:** `mysql:8.0`
- **Database:** `ftgo_test`
- **Credentials:** `ftgo_test` / `ftgo_test_pass`
- **Character set:** `utf8mb4`
- **Reuse mode:** Enabled (faster local development)

Properties registered automatically:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.driver-class-name`
- `spring.flyway.url`
- `spring.flyway.user`
- `spring.flyway.password`

### Singleton Pattern

Testcontainers uses a singleton pattern to share one container across multiple test classes in the same JVM. This significantly reduces test suite execution time:

```java
// FtgoMySQLContainer.getInstance() always returns the same container
// The container starts once and is reused for all tests
static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();
```

### Reusable Containers (Local Development)

With `withReuse(true)`, Testcontainers keeps containers running between test runs during local development. This requires adding `testcontainers.reuse.enable=true` to `~/.testcontainers.properties`:

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

> **Note:** Reusable containers are automatically disabled in CI environments.

## Migration from Docker Compose

### Before: Docker Compose Setup

```yaml
# docker-compose-test.yml
version: '3'
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_DATABASE: ftgo_test
      MYSQL_USER: ftgo_test
      MYSQL_PASSWORD: ftgo_test_pass
      MYSQL_ROOT_PASSWORD: rootpass
```

```bash
# Before running tests
docker-compose -f docker-compose-test.yml up -d
./gradlew integrationTest
docker-compose -f docker-compose-test.yml down
```

### After: Testcontainers

```java
// No external files needed — everything is in Java code
@Testcontainers
@SpringBootTest
class MyIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
    }

    // Tests run with real MySQL — no manual setup needed
}
```

### Migration Checklist

For each service migrating from Docker Compose to Testcontainers:

- [ ] Add `testCompile project(':shared-ftgo-test-lib')` to `build.gradle`
- [ ] Replace fixed `spring.datasource.*` properties in `application-test.yml` with Testcontainers `@DynamicPropertySource`
- [ ] Update integration test classes to use `@Testcontainers` and `@Container`
- [ ] Remove Docker Compose test files (or keep for E2E tests only)
- [ ] Verify Flyway migrations run correctly against the Testcontainer
- [ ] Update CI pipeline to remove `docker-compose up` pre-step
- [ ] Test locally with `./gradlew integrationTest`

## Advanced Patterns

### Custom Container Initialization

For tests that need specific database state:

```java
@Container
static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance()
    .withInitScript("init-test-data.sql");
```

### Multiple Containers

For tests that need multiple infrastructure services:

```java
@Testcontainers
class FullIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

### Wait Strategies

Testcontainers includes built-in wait strategies to ensure containers are ready:

```java
// Default: waits for the container's exposed port to accept connections
new MySQLContainer<>("mysql:8.0")
    .waitingFor(Wait.forListeningPort());

// Custom: wait for a log message
new GenericContainer<>("custom-service:latest")
    .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

// Custom: wait for HTTP endpoint
new GenericContainer<>("custom-service:latest")
    .waitingFor(Wait.forHttp("/health").forStatusCode(200));
```

### Network for Inter-Container Communication

When containers need to communicate with each other:

```java
static final Network network = Network.newNetwork();

@Container
static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withNetwork(network)
    .withNetworkAliases("mysql");

@Container
static final GenericContainer<?> app = new GenericContainer<>("ftgo-order-service:latest")
    .withNetwork(network)
    .withEnv("DB_HOST", "mysql")
    .dependsOn(mysql);
```

## Troubleshooting

### Container Fails to Start

1. Ensure Docker is running: `docker info`
2. Check available disk space: `df -h`
3. Check Docker memory limits (containers need at least 256MB)
4. Review container logs: Testcontainers prints logs on failure

### Tests Fail with Connection Refused

1. Verify `@DynamicPropertySource` is correctly wiring the container URL
2. Ensure the container is declared as `static final`
3. Check that `@Container` annotation is present
4. Verify `@Testcontainers` is on the test class

### Slow Test Execution

1. Use singleton containers (via `FtgoMySQLContainer.getInstance()`)
2. Enable reusable containers for local development
3. Use `@Transactional` for test isolation instead of recreating the database
4. Run integration tests in a separate Gradle task from unit tests

## Related Documentation

- [Testing Strategy](testing-strategy.md)
- [JUnit 5 Migration Guide](junit5-migration-guide.md)
- [When to Write Which Test](when-to-test.md)
- [Automated Testing Pipeline](automated-testing-pipeline.md)
