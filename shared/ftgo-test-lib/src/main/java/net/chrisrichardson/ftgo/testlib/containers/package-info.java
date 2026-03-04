/**
 * Testcontainers configuration for FTGO integration tests.
 *
 * <p>Provides pre-configured container definitions that replace Docker Compose
 * for integration test infrastructure. Containers use the singleton pattern
 * to minimize startup overhead across test suites.
 *
 * <h3>Migration from Docker Compose</h3>
 * <p>Instead of relying on external {@code docker-compose.yml} files,
 * integration tests use Testcontainers to programmatically manage
 * infrastructure dependencies (MySQL, Kafka, etc.). This approach:
 * <ul>
 *   <li>Eliminates manual container lifecycle management</li>
 *   <li>Ensures tests run in CI without Docker Compose installed</li>
 *   <li>Provides per-test or per-suite isolation</li>
 *   <li>Integrates with JUnit 5 lifecycle annotations</li>
 * </ul>
 *
 * @see net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer
 */
package net.chrisrichardson.ftgo.testlib.containers;
