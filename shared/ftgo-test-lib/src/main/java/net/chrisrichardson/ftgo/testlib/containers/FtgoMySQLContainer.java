package net.chrisrichardson.ftgo.testlib.containers;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Pre-configured MySQL Testcontainer for FTGO integration tests.
 *
 * <p>Provides a singleton MySQL container that is shared across all integration
 * tests in a test suite, reducing container startup overhead.
 *
 * <p>Usage:
 * <pre>{@code
 * @Testcontainers
 * class MyIntegrationTest {
 *
 *     @Container
 *     static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();
 *
 *     @DynamicPropertySource
 *     static void mysqlProperties(DynamicPropertyRegistry registry) {
 *         FtgoMySQLContainer.registerProperties(registry, mysql);
 *     }
 * }
 * }</pre>
 *
 * <p>Or using the singleton pattern for shared containers:
 * <pre>{@code
 * class MyIntegrationTest extends AbstractIntegrationTest {
 *     // MySQL container is automatically configured
 * }
 * }</pre>
 *
 * @see net.chrisrichardson.ftgo.testlib.config.AbstractIntegrationTest
 */
public final class FtgoMySQLContainer {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String DATABASE_NAME = "ftgo_test";
    private static final String USERNAME = "ftgo_test";
    private static final String PASSWORD = "ftgo_test_pass";

    private static MySQLContainer<?> instance;

    private FtgoMySQLContainer() {
        // Utility class
    }

    /**
     * Returns a singleton MySQL container instance.
     *
     * <p>The container is configured with:
     * <ul>
     *   <li>MySQL 8.0 image</li>
     *   <li>Database name: ftgo_test</li>
     *   <li>Reusable mode enabled (for faster local development)</li>
     *   <li>Standard FTGO test credentials</li>
     * </ul>
     *
     * @return the singleton MySQLContainer instance
     */
    @SuppressWarnings("resource")
    public static MySQLContainer<?> getInstance() {
        if (instance == null) {
            instance = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withReuse(true)
                    .withCommand("--character-set-server=utf8mb4",
                            "--collation-server=utf8mb4_unicode_ci",
                            "--default-authentication-plugin=mysql_native_password");
        }
        return instance;
    }

    /**
     * Registers MySQL container connection properties with Spring's DynamicPropertyRegistry.
     *
     * <p>This method sets the following properties:
     * <ul>
     *   <li>spring.datasource.url</li>
     *   <li>spring.datasource.username</li>
     *   <li>spring.datasource.password</li>
     *   <li>spring.datasource.driver-class-name</li>
     *   <li>spring.flyway.url</li>
     *   <li>spring.flyway.user</li>
     *   <li>spring.flyway.password</li>
     * </ul>
     *
     * @param registry the Spring DynamicPropertyRegistry
     * @param mysql    the MySQL container
     */
    public static void registerProperties(
            org.springframework.test.context.DynamicPropertyRegistry registry,
            MySQLContainer<?> mysql) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
    }
}
