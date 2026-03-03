package net.chrisrichardson.ftgo.testutils.examples.order;

/**
 * Example API tests for the Order bounded context.
 *
 * <p>Demonstrates Rest-Assured API testing patterns for OrderController.
 *
 * <pre>{@code
 * // In ftgo-order-service/src/test/java/.../OrderControllerApiTest.java:
 * // (JUnit 5 migration of existing OrderControllerTest)
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("Order API Tests")
 * class OrderControllerApiTest {
 *
 *     @Mock
 *     private OrderService orderService;
 *
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     private OrderController orderController;
 *
 *     @BeforeEach
 *     void setUp() {
 *         orderController = new OrderController(orderService, orderRepository);
 *     }
 *
 *     @Nested
 *     @DisplayName("GET /orders/{orderId}")
 *     class GetOrderById {
 *
 *         @Test
 *         @DisplayName("should return order details when found")
 *         void shouldReturnOrderWhenFound() {
 *             Order order = OrderBuilder.anOrder()
 *                 .withOrderId(1L)
 *                 .withConsumerId(42L)
 *                 .build();
 *             when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
 *
 *             given()
 *                 .standaloneSetup(configureControllers(orderController))
 *             .when()
 *                 .get("/orders/1")
 *             .then()
 *                 .statusCode(200)
 *                 .body("orderId", equalTo(1))
 *                 .body("state", equalTo("APPROVED"));
 *         }
 *
 *         @Test
 *         @DisplayName("should return 404 when order not found")
 *         void shouldReturn404WhenNotFound() {
 *             when(orderRepository.findById(999L)).thenReturn(Optional.empty());
 *
 *             given()
 *                 .standaloneSetup(configureControllers(orderController))
 *             .when()
 *                 .get("/orders/999")
 *             .then()
 *                 .statusCode(404);
 *         }
 *     }
 *
 *     @Nested
 *     @DisplayName("POST /orders")
 *     class CreateOrder {
 *
 *         @Test
 *         @DisplayName("should create order and return response")
 *         void shouldCreateOrderAndReturnResponse() {
 *             Order order = OrderBuilder.anOrder().withOrderId(1L).build();
 *             when(orderService.createOrder(anyLong(), anyLong(), anyList()))
 *                 .thenReturn(order);
 *
 *             given()
 *                 .standaloneSetup(configureControllers(orderController))
 *                 .contentType("application/json")
 *                 .body("{\"consumerId\":1,\"restaurantId\":1," +
 *                       "\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}")
 *             .when()
 *                 .post("/orders")
 *             .then()
 *                 .statusCode(200)
 *                 .body("orderId", equalTo(1));
 *         }
 *     }
 *
 *     // Shared helper (migrated from JUnit 4 pattern)
 *     private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
 *         ObjectMapper objectMapper = new ObjectMapper();
 *         objectMapper.registerModule(new MoneyModule());
 *         MappingJackson2HttpMessageConverter converter =
 *             new MappingJackson2HttpMessageConverter(objectMapper);
 *         return MockMvcBuilders.standaloneSetup(controllers)
 *             .setMessageConverters(converter);
 *     }
 * }
 * }</pre>
 */
public final class OrderApiTestExample {
    private OrderApiTestExample() {
        // Documentation-only class
    }
}
