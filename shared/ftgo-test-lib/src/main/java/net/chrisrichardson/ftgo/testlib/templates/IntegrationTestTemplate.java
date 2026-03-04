package net.chrisrichardson.ftgo.testlib.templates;

import net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Template for Spring Boot integration tests with Testcontainers.
 *
 * <p>This template demonstrates the recommended structure for FTGO integration tests:
 * <ul>
 *   <li>{@code @SpringBootTest} for full application context</li>
 *   <li>{@code @Testcontainers} for managed container lifecycle</li>
 *   <li>MySQL container via {@link FtgoMySQLContainer}</li>
 *   <li>{@code @DynamicPropertySource} for runtime property injection</li>
 *   <li>{@code @ActiveProfiles("test")} for test-specific configuration</li>
 *   <li>"integration" tag for CI pipeline filtering</li>
 * </ul>
 *
 * <h3>Copy this template and rename it for your service:</h3>
 * <pre>{@code
 * // Copy to:
 * // services/ftgo-order-service/src/integration-test/java/com/ftgo/order/OrderRepositoryIntegrationTest.java
 * //
 * // Then:
 * // 1. Replace the package and class names
 * // 2. Add @SpringBootTest(classes = YourApplication.class)
 * // 3. Autowire your repositories/services
 * // 4. Add test methods
 * }</pre>
 *
 * <p>Alternatively, extend {@link net.chrisrichardson.ftgo.testlib.config.AbstractIntegrationTest}
 * which pre-configures MySQL Testcontainer and dynamic properties.
 */
@Tag("integration")
@Testcontainers
@ActiveProfiles("test")
// @SpringBootTest(classes = YourApplication.class)  // Uncomment and specify your app class
class IntegrationTestTemplate {

    @Container
    static final MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        FtgoMySQLContainer.registerProperties(registry, mysql);
    }

    // === Autowire your repositories/services ===
    // @Autowired
    // private OrderRepository orderRepository;

    // @Test
    // @DisplayName("should persist and retrieve entity")
    // void shouldPersistAndRetrieveEntity() {
    //     // Arrange
    //     Order order = new Order(/* ... */);
    //
    //     // Act
    //     Order saved = orderRepository.save(order);
    //     Optional<Order> found = orderRepository.findById(saved.getId());
    //
    //     // Assert
    //     assertThat(found).isPresent();
    //     assertThat(found.get().getId()).isEqualTo(saved.getId());
    // }

    // @Test
    // @DisplayName("should apply Flyway migrations")
    // void shouldApplyFlywayMigrations() {
    //     // The database schema is applied via Flyway on startup
    //     // Verify by checking that the table exists
    //     assertThat(orderRepository.count()).isGreaterThanOrEqualTo(0);
    // }
}
