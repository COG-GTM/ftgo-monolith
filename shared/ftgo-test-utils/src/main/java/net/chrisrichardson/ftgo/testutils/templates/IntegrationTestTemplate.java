package net.chrisrichardson.ftgo.testutils.templates;

/**
 * Template and guidelines for writing integration tests with Testcontainers.
 *
 * <h2>Integration Test Template (@SpringBootTest + Testcontainers)</h2>
 *
 * <p>Integration tests verify that multiple components work together correctly
 * using real external dependencies (database, message broker, etc.) via Testcontainers.
 *
 * <h3>Repository Integration Test Template:</h3>
 * <pre>{@code
 * package net.chrisrichardson.ftgo.orderservice.repository;
 *
 * import net.chrisrichardson.ftgo.testutils.containers.MySqlTestContainer;
 * import net.chrisrichardson.ftgo.testutils.builders.OrderBuilder;
 * import net.chrisrichardson.ftgo.testutils.builders.RestaurantBuilder;
 * import org.junit.jupiter.api.*;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.boot.test.context.SpringBootTest;
 * import org.springframework.test.context.ActiveProfiles;
 * import org.springframework.test.context.DynamicPropertyRegistry;
 * import org.springframework.test.context.DynamicPropertySource;
 * import org.testcontainers.containers.MySQLContainer;
 * import org.testcontainers.junit.jupiter.Container;
 * import org.testcontainers.junit.jupiter.Testcontainers;
 *
 * import static org.assertj.core.api.Assertions.assertThat;
 *
 * @SpringBootTest
 * @Testcontainers
 * @ActiveProfiles("integration-test")
 * @DisplayName("OrderRepository Integration Tests")
 * class OrderRepositoryIntegrationTest {
 *
 *     // Use shared MySQL container from ftgo-test-utils
 *     @Container
 *     static MySQLContainer<?> mysql = MySqlTestContainer.getInstance();
 *
 *     // Register container properties with Spring
 *     @DynamicPropertySource
 *     static void configureProperties(DynamicPropertyRegistry registry) {
 *         MySqlTestContainer.registerProperties(registry, mysql);
 *     }
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Autowired
 *     private RestaurantRepository restaurantRepository;
 *
 *     private Restaurant testRestaurant;
 *
 *     @BeforeEach
 *     void setUp() {
 *         orderRepository.deleteAll();
 *         testRestaurant = restaurantRepository.save(
 *             RestaurantBuilder.aRestaurant().build()
 *         );
 *     }
 *
 *     @Test
 *     @DisplayName("should save and retrieve order")
 *     void shouldSaveAndRetrieveOrder() {
 *         // Arrange
 *         Order order = OrderBuilder.anOrder()
 *             .withRestaurant(testRestaurant)
 *             .build();
 *
 *         // Act
 *         Order saved = orderRepository.save(order);
 *         Order found = orderRepository.findById(saved.getId()).orElse(null);
 *
 *         // Assert
 *         assertThat(found).isNotNull();
 *         assertThat(found.getConsumerId()).isEqualTo(order.getConsumerId());
 *         assertThat(found.getOrderState()).isEqualTo(OrderState.APPROVED);
 *     }
 *
 *     @Test
 *     @DisplayName("should return empty when order not found")
 *     void shouldReturnEmptyWhenNotFound() {
 *         assertThat(orderRepository.findById(999L)).isEmpty();
 *     }
 * }
 * }</pre>
 *
 * <h3>Service Integration Test Template:</h3>
 * <pre>{@code
 * @SpringBootTest
 * @Testcontainers
 * @ActiveProfiles("integration-test")
 * @DisplayName("OrderService Integration Tests")
 * class OrderServiceIntegrationTest {
 *
 *     @Container
 *     static MySQLContainer<?> mysql = MySqlTestContainer.getInstance();
 *
 *     @DynamicPropertySource
 *     static void configureProperties(DynamicPropertyRegistry registry) {
 *         MySqlTestContainer.registerProperties(registry, mysql);
 *     }
 *
 *     @Autowired
 *     private OrderService orderService;
 *
 *     @Test
 *     @DisplayName("should create order end-to-end with real database")
 *     void shouldCreateOrderEndToEnd() {
 *         // Tests the full service -> repository -> database flow
 *     }
 * }
 * }</pre>
 *
 * <h3>Key Conventions:</h3>
 * <ul>
 *   <li>Use {@code @Testcontainers} and {@code @Container} for lifecycle management</li>
 *   <li>Use {@code @DynamicPropertySource} to inject container URLs into Spring</li>
 *   <li>Use {@code @ActiveProfiles("integration-test")} to load test-specific config</li>
 *   <li>Clean up test data in {@code @BeforeEach} to ensure test isolation</li>
 *   <li>Place integration tests in {@code src/integration-test/java} (not src/test/java)</li>
 *   <li>Use the shared {@code MySqlTestContainer} from ftgo-test-utils</li>
 * </ul>
 *
 * @see net.chrisrichardson.ftgo.testutils.containers.MySqlTestContainer
 * @see net.chrisrichardson.ftgo.testutils.config.TestApplicationConfig
 */
public final class IntegrationTestTemplate {
    private IntegrationTestTemplate() {
        // Documentation-only class
    }
}
