package net.chrisrichardson.ftgo.testlib.config;

import net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for FTGO integration tests with Testcontainers.
 *
 * <p>Provides standard configuration for integration tests:
 * <ul>
 *   <li>Full Spring Boot application context</li>
 *   <li>MySQL Testcontainer (shared singleton instance)</li>
 *   <li>"test" profile activation</li>
 *   <li>"integration" tag for test filtering</li>
 *   <li>Dynamic property registration for datasource configuration</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void shouldPersistOrder() {
 *         // test with real database
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Note:</strong> Subclasses must have a Spring Boot application class
 * discoverable via component scanning or explicitly specified via
 * {@code @SpringBootTest(classes = ...)}.
 *
 * @see AbstractUnitTest
 * @see FtgoMySQLContainer
 */
@Tag("integration")
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
    }
}
