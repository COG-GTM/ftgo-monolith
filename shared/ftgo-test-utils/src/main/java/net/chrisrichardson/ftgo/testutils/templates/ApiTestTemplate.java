package net.chrisrichardson.ftgo.testutils.templates;

/**
 * Template and guidelines for writing API tests with Rest-Assured.
 *
 * <h2>API Test Template (Rest-Assured + MockMvc)</h2>
 *
 * <p>API tests verify REST endpoints including serialization, HTTP status codes,
 * content types, and response structure. They use Rest-Assured's fluent API
 * for readable test assertions.
 *
 * <h3>MockMvc API Test (no server startup - fast):</h3>
 * <pre>{@code
 * package net.chrisrichardson.ftgo.orderservice.web;
 *
 * import io.restassured.module.mockmvc.RestAssuredMockMvc;
 * import net.chrisrichardson.ftgo.testutils.builders.OrderBuilder;
 * import org.junit.jupiter.api.*;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import org.mockito.junit.jupiter.MockitoExtension;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
 * import org.springframework.boot.test.mock.bean.MockBean;
 * import org.springframework.test.web.servlet.MockMvc;
 *
 * import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
 * import static org.hamcrest.CoreMatchers.equalTo;
 *
 * @WebMvcTest(OrderController.class)
 * @DisplayName("OrderController API Tests")
 * class OrderControllerApiTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @MockBean
 *     private OrderService orderService;
 *
 *     @MockBean
 *     private OrderRepository orderRepository;
 *
 *     @BeforeEach
 *     void setUp() {
 *         RestAssuredMockMvc.mockMvc(mockMvc);
 *     }
 *
 *     @Nested
 *     @DisplayName("GET /orders/{id}")
 *     class GetOrder {
 *
 *         @Test
 *         @DisplayName("should return 200 with order details")
 *         void shouldReturnOrderWhenFound() {
 *             Order order = OrderBuilder.anOrder().withOrderId(1L).build();
 *             when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
 *
 *             given()
 *                 .contentType("application/json")
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
 *                 .contentType("application/json")
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
 *         @DisplayName("should return 200 when order created")
 *         void shouldCreateOrder() {
 *             // Setup mock
 *             Order order = OrderBuilder.anOrder().withOrderId(1L).build();
 *             when(orderService.createOrder(anyLong(), anyLong(), anyList()))
 *                 .thenReturn(order);
 *
 *             given()
 *                 .contentType("application/json")
 *                 .body("{\"consumerId\":1,\"restaurantId\":1," +
 *                       "\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}")
 *             .when()
 *                 .post("/orders")
 *             .then()
 *                 .statusCode(200)
 *                 .body("orderId", equalTo(1));
 *         }
 *
 *         @Test
 *         @DisplayName("should return 400 for invalid request")
 *         void shouldReturn400ForInvalidRequest() {
 *             given()
 *                 .contentType("application/json")
 *                 .body("{}")
 *             .when()
 *                 .post("/orders")
 *             .then()
 *                 .statusCode(400);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Standalone API Test (no Spring context - fastest):</h3>
 * <pre>{@code
 * // JUnit 4 pattern (existing monolith) - see OrderControllerTest
 * // JUnit 5 equivalent:
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("OrderController Standalone API Tests")
 * class OrderControllerStandaloneApiTest {
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
 *     @Test
 *     void shouldFindOrder() {
 *         Order order = OrderBuilder.anOrder().withOrderId(1L).build();
 *         when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
 *
 *         given()
 *             .standaloneSetup(orderController)
 *         .when()
 *             .get("/orders/1")
 *         .then()
 *             .statusCode(200)
 *             .body("orderId", equalTo(1));
 *     }
 * }
 * }</pre>
 *
 * <h3>Key Conventions:</h3>
 * <ul>
 *   <li>Use {@code @WebMvcTest} for controller-layer tests (faster than @SpringBootTest)</li>
 *   <li>Use Rest-Assured's given/when/then syntax for readability</li>
 *   <li>Mock service layer dependencies with {@code @MockBean}</li>
 *   <li>Test all HTTP status codes (200, 400, 404, 500)</li>
 *   <li>Verify response body structure and content type</li>
 *   <li>Group tests by endpoint using {@code @Nested}</li>
 * </ul>
 *
 * @see <a href="https://rest-assured.io/">Rest-Assured Documentation</a>
 */
public final class ApiTestTemplate {
    private ApiTestTemplate() {
        // Documentation-only class
    }
}
