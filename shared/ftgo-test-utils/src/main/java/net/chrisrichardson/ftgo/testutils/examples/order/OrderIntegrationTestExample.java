package net.chrisrichardson.ftgo.testutils.examples.order;

/**
 * Example integration tests for the Order bounded context.
 *
 * <p>Demonstrates Testcontainers usage for repository and service integration tests.
 *
 * <pre>{@code
 * // In ftgo-order-service/src/integration-test/java/.../OrderRepositoryIntegrationTest.java:
 *
 * @SpringBootTest
 * @Testcontainers
 * @ActiveProfiles("integration-test")
 * @DisplayName("Order Repository Integration Tests")
 * class OrderRepositoryIntegrationTest {
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
 *     @DisplayName("should persist and retrieve order with all fields")
 *     void shouldPersistAndRetrieveOrder() {
 *         // Arrange
 *         Order order = OrderBuilder.anOrder()
 *             .withConsumerId(42L)
 *             .withRestaurant(testRestaurant)
 *             .withLineItems(
 *                 OrderLineItemBuilder.anOrderLineItem()
 *                     .withName("Chicken Vindaloo")
 *                     .withPrice("12.34")
 *                     .withQuantity(2)
 *                     .build()
 *             )
 *             .build();
 *
 *         // Act
 *         Order saved = orderRepository.save(order);
 *         Order found = orderRepository.findById(saved.getId()).orElse(null);
 *
 *         // Assert
 *         assertThat(found).isNotNull();
 *         assertOrderBelongsToConsumer(found, 42L);
 *         assertOrderApproved(found);
 *         assertOrderLineItemCount(found, 1);
 *         assertOrderTotal(found, new Money("24.68")); // 12.34 * 2
 *     }
 *
 *     @Test
 *     @DisplayName("should update order state after accept")
 *     void shouldUpdateOrderStateAfterAccept() {
 *         Order order = orderRepository.save(
 *             OrderBuilder.anOrder().withRestaurant(testRestaurant).build()
 *         );
 *
 *         order.acceptTicket(LocalDateTime.now().plusHours(1));
 *         orderRepository.save(order);
 *
 *         Order updated = orderRepository.findById(order.getId()).orElse(null);
 *         assertThat(updated).isNotNull();
 *         assertOrderState(updated, OrderState.ACCEPTED);
 *     }
 *
 *     @Test
 *     @DisplayName("should return empty Optional for non-existent order")
 *     void shouldReturnEmptyForNonExistentOrder() {
 *         assertThat(orderRepository.findById(99999L)).isEmpty();
 *     }
 * }
 * }</pre>
 *
 * <pre>{@code
 * // In ftgo-order-service/src/integration-test/java/.../OrderServiceIntegrationTest.java:
 *
 * @SpringBootTest
 * @Testcontainers
 * @ActiveProfiles("integration-test")
 * @DisplayName("Order Service Integration Tests")
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
 *     @Autowired
 *     private RestaurantRepository restaurantRepository;
 *
 *     @Autowired
 *     private ConsumerRepository consumerRepository;
 *
 *     @Test
 *     @DisplayName("should create order through service layer")
 *     void shouldCreateOrderThroughServiceLayer() {
 *         // Arrange - set up required database records
 *         Consumer consumer = consumerRepository.save(
 *             ConsumerBuilder.aConsumer().build()
 *         );
 *         Restaurant restaurant = restaurantRepository.save(
 *             RestaurantBuilder.aRestaurant().build()
 *         );
 *
 *         // Act
 *         Order order = orderService.createOrder(
 *             consumer.getId(),
 *             restaurant.getId(),
 *             Collections.singletonList(
 *                 new MenuItemIdAndQuantity("1", 2)
 *             )
 *         );
 *
 *         // Assert
 *         assertThat(order.getId()).isNotNull();
 *         assertOrderApproved(order);
 *     }
 * }
 * }</pre>
 */
public final class OrderIntegrationTestExample {
    private OrderIntegrationTestExample() {
        // Documentation-only class
    }
}
