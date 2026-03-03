package net.chrisrichardson.ftgo.testutils.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared MySQL Testcontainer configuration for integration tests.
 *
 * <p>Provides a singleton MySQL container that is reused across test classes
 * within the same JVM to reduce startup overhead. The container is started
 * once and stopped when the JVM shuts down.
 *
 * <h3>Usage with JUnit 5 and @Testcontainers (recommended):</h3>
 * <pre>{@code
 * @SpringBootTest
 * @Testcontainers
 * class MyRepositoryIntegrationTest {
 *
 *     @Container
 *     static MySQLContainer<?> mysql = MySqlTestContainer.getInstance();
 *
 *     // For Spring Boot 2.2+ with @DynamicPropertySource:
 *     @DynamicPropertySource
 *     static void configureProperties(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", mysql::getJdbcUrl);
 *         registry.add("spring.datasource.username", mysql::getUsername);
 *         registry.add("spring.datasource.password", mysql::getPassword);
 *     }
 *
 *     // For Spring Boot 2.0.x, use ApplicationContextInitializer instead:
 *     // See getSpringProperties() method documentation below.
 * }
 * }</pre>
 *
 * <h3>Usage with shared singleton (for faster test suites):</h3>
 * <pre>{@code
 * @SpringBootTest
 * class MyRepositoryIntegrationTest {
 *
 *     static MySQLContainer<?> mysql = MySqlTestContainer.getSharedInstance();
 *
 *     // Use MySqlTestContainer.getSpringProperties(mysql) for property map
 * }
 * }</pre>
 *
 * <h3>Usage with ApplicationContextInitializer (Spring Boot 2.0.x compatible):</h3>
 * <pre>{@code
 * public class TestContainerInitializer
 *         implements ApplicationContextInitializer<ConfigurableApplicationContext> {
 *
 *     static MySQLContainer<?> mysql = MySqlTestContainer.getSharedInstance();
 *
 *     public void initialize(ConfigurableApplicationContext ctx) {
 *         Map<String, String> props = MySqlTestContainer.getSpringProperties(mysql);
 *         TestPropertyValues.of(
 *             props.entrySet().stream()
 *                 .map(e -> e.getKey() + "=" + e.getValue())
 *                 .toArray(String[]::new)
 *         ).applyTo(ctx.getEnvironment());
 *     }
 * }
 *
 * @SpringBootTest
 * @ContextConfiguration(initializers = TestContainerInitializer.class)
 * class MyIntegrationTest { ... }
 * }</pre>
 *
 * @see org.testcontainers.containers.MySQLContainer
 */
public final class MySqlTestContainer {

    private static final Logger log = LoggerFactory.getLogger(MySqlTestContainer.class);

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String DATABASE_NAME = "ftgo_test";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";

    private static MySQLContainer<?> sharedInstance;

    private MySqlTestContainer() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new MySQL container instance for use with {@code @Container}.
     * Each call returns a new container.
     *
     * @return a configured but not-yet-started MySQLContainer
     */
    @SuppressWarnings("resource")
    public static MySQLContainer<?> getInstance() {
        return new MySQLContainer<>(MYSQL_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("mysql"))
                .withReuse(false);
    }

    /**
     * Returns a shared singleton MySQL container that is reused across test classes.
     * The container is started on first access and stopped when the JVM exits.
     *
     * <p>This approach significantly reduces integration test execution time
     * when multiple test classes need a MySQL database.
     *
     * @return a running shared MySQLContainer
     */
    @SuppressWarnings("resource")
    public static synchronized MySQLContainer<?> getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MySQLContainer<>(MYSQL_IMAGE)
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("mysql-shared"))
                    .withReuse(true);
            sharedInstance.start();
            log.info("Started shared MySQL container: {}", sharedInstance.getJdbcUrl());
        }
        return sharedInstance;
    }

    /**
     * Returns a map of Spring datasource properties derived from a running MySQL container.
     *
     * <p>This method is compatible with all Spring Boot versions. Use the returned map
     * with an {@code ApplicationContextInitializer} or pass properties directly to
     * {@code @SpringBootTest(properties = {...})}.
     *
     * <p>For Spring Boot 2.2+ with {@code @DynamicPropertySource}, you can call
     * container methods directly instead:
     * <pre>{@code
     * @DynamicPropertySource
     * static void configureProperties(DynamicPropertyRegistry registry) {
     *     registry.add("spring.datasource.url", mysql::getJdbcUrl);
     *     registry.add("spring.datasource.username", mysql::getUsername);
     *     registry.add("spring.datasource.password", mysql::getPassword);
     *     registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
     *     registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
     * }
     * }</pre>
     *
     * @param mysql the running MySQL container
     * @return a map of Spring datasource property key-value pairs
     */
    public static Map<String, String> getSpringProperties(MySQLContainer<?> mysql) {
        Map<String, String> properties = new HashMap<>();
        properties.put("spring.datasource.url", mysql.getJdbcUrl());
        properties.put("spring.datasource.username", mysql.getUsername());
        properties.put("spring.datasource.password", mysql.getPassword());
        properties.put("spring.datasource.driver-class-name", mysql.getDriverClassName());
        properties.put("spring.jpa.hibernate.ddl-auto", "create-drop");
        return properties;
    }
}
