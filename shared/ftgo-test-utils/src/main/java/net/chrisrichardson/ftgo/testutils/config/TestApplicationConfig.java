package net.chrisrichardson.ftgo.testutils.config;

/**
 * Shared test application configuration constants and utilities.
 *
 * <p>Provides common configuration values used across test suites in all
 * bounded contexts. Use these constants to avoid hardcoded values in tests.
 *
 * <p>Usage in @SpringBootTest:
 * <pre>{@code
 * @SpringBootTest(
 *     webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
 *     properties = {
 *         "spring.datasource.url=" + TestApplicationConfig.MYSQL_JDBC_URL,
 *         "spring.jpa.hibernate.ddl-auto=create-drop"
 *     }
 * )
 * class MyIntegrationTest {
 *     // ...
 * }
 * }</pre>
 */
public final class TestApplicationConfig {

    private TestApplicationConfig() {
        // Utility class - prevent instantiation
    }

    // -------------------------------------------------------------------------
    // Database Configuration
    // -------------------------------------------------------------------------

    /** Default MySQL JDBC URL for Testcontainers. */
    public static final String MYSQL_JDBC_URL = "jdbc:tc:mysql:8.0:///ftgo_test";

    /** Default database username for tests. */
    public static final String DB_USERNAME = "test";

    /** Default database password for tests. */
    public static final String DB_PASSWORD = "test";

    // -------------------------------------------------------------------------
    // Spring Profiles
    // -------------------------------------------------------------------------

    /** Profile for unit tests (no external dependencies). */
    public static final String PROFILE_UNIT_TEST = "unit-test";

    /** Profile for integration tests (real dependencies via Testcontainers). */
    public static final String PROFILE_INTEGRATION_TEST = "integration-test";

    /** Profile for contract tests. */
    public static final String PROFILE_CONTRACT_TEST = "contract-test";

    // -------------------------------------------------------------------------
    // Test Constants
    // -------------------------------------------------------------------------

    /** Default consumer ID used in tests. */
    public static final long DEFAULT_CONSUMER_ID = 1L;

    /** Default restaurant ID used in tests. */
    public static final long DEFAULT_RESTAURANT_ID = 1L;

    /** Default order ID used in tests. */
    public static final long DEFAULT_ORDER_ID = 99L;

    /** Default courier ID used in tests. */
    public static final long DEFAULT_COURIER_ID = 1L;

    // -------------------------------------------------------------------------
    // Timeouts
    // -------------------------------------------------------------------------

    /** Default timeout in seconds for async operations in integration tests. */
    public static final int ASYNC_TIMEOUT_SECONDS = 30;

    /** Default timeout in seconds for container startup. */
    public static final int CONTAINER_STARTUP_TIMEOUT_SECONDS = 120;
}
