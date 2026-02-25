package com.ftgo.template.config;

// =============================================================================
// TEMPLATE: Shared Testcontainers Base Class for Integration Tests
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name.
//
// This template demonstrates:
//   - Testcontainers MySQL container setup
//   - @DynamicPropertySource for dynamic configuration
//   - Container reuse across test classes (static container)
//   - Flyway migration support
//
// All integration tests that need a MySQL database should extend this class.
// This avoids duplicating container configuration in every test class.
//
// Dependencies (add to build.gradle):
//   testImplementation libs.testcontainers.mysql
//   testImplementation libs.testcontainers.junit.jupiter
// =============================================================================

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests requiring a MySQL database.
 *
 * <p>Starts a MySQL container via Testcontainers and configures Spring Boot
 * datasource properties dynamically. The container is shared across all test
 * classes that extend this base (one container per JVM, not per test class).
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * @SpringBootTest
 * @ActiveProfiles("integration-test")
 * class OrderRepositoryIntegrationTest extends AbstractMySqlIntegrationTest {
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void shouldPersistOrder() {
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p><b>Location:</b> {@code src/integration-test/java/com/ftgo/{service}/config/}
 */
@Testcontainers
public abstract class AbstractMySqlIntegrationTest {

    /**
     * Shared MySQL container instance.
     *
     * <p>Using a static container ensures it is started once and reused across
     * all test classes. This significantly speeds up integration test execution.
     *
     * <p>The {@code withReuse(true)} flag enables Testcontainers' reuse feature,
     * which keeps the container running between test runs during local development.
     * To enable reuse, add to {@code ~/.testcontainers.properties}:
     * <pre>
     * testcontainers.reuse.enable=true
     * </pre>
     */
    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ftgo")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    /**
     * Dynamically configures Spring Boot datasource properties to point at
     * the Testcontainers MySQL instance.
     *
     * <p>This replaces the need for hardcoded JDBC URLs in test properties files.
     * Testcontainers assigns a random port, and this method injects the actual
     * connection URL into the Spring context.
     *
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Enable Flyway for integration tests to validate migrations
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // JPA settings for integration tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
