package net.chrisrichardson.ftgo.testutils.templates;

/**
 * Template and guidelines for writing contract tests with Spring Cloud Contract.
 *
 * <h2>Contract Test Template (Spring Cloud Contract / Pact)</h2>
 *
 * <p>Contract tests verify that the API contract between a provider (service)
 * and its consumers has not been broken. They are crucial for microservices
 * to ensure services can evolve independently.
 *
 * <h3>Provider-Side Contract Test (Spring Cloud Contract):</h3>
 * <pre>{@code
 * // Step 1: Define the contract in src/test/resources/contracts/order/
 * // File: shouldReturnOrderById.groovy
 * //
 * // import org.springframework.cloud.contract.spec.Contract
 * //
 * // Contract.make {
 * //     description "should return order by ID"
 * //     request {
 * //         method GET()
 * //         url "/orders/1"
 * //     }
 * //     response {
 * //         status OK()
 * //         headers {
 * //             contentType applicationJson()
 * //         }
 * //         body([
 * //             orderId: 1,
 * //             state: "APPROVED",
 * //             orderTotal: "12.34"
 * //         ])
 * //     }
 * // }
 *
 * // Step 2: Create the base test class
 * package net.chrisrichardson.ftgo.orderservice.contract;
 *
 * import io.restassured.module.mockmvc.RestAssuredMockMvc;
 * import net.chrisrichardson.ftgo.testutils.builders.OrderBuilder;
 * import org.junit.jupiter.api.BeforeEach;
 * import org.mockito.Mockito;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.boot.test.context.SpringBootTest;
 * import org.springframework.boot.test.mock.bean.MockBean;
 * import org.springframework.web.context.WebApplicationContext;
 *
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
 * abstract class OrderContractBase {
 *
 *     @Autowired
 *     private WebApplicationContext context;
 *
 *     @MockBean
 *     private OrderRepository orderRepository;
 *
 *     @BeforeEach
 *     void setUp() {
 *         RestAssuredMockMvc.webAppContextSetup(context);
 *
 *         // Set up mock responses that satisfy the contracts
 *         Order order = OrderBuilder.anOrder().withOrderId(1L).build();
 *         Mockito.when(orderRepository.findById(1L))
 *             .thenReturn(Optional.of(order));
 *     }
 * }
 * }</pre>
 *
 * <h3>Consumer-Side Contract Test (using stubs):</h3>
 * <pre>{@code
 * package net.chrisrichardson.ftgo.consumer.client;
 *
 * import org.junit.jupiter.api.Test;
 * import org.springframework.boot.test.context.SpringBootTest;
 * import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
 * import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
 *
 * @SpringBootTest
 * @AutoConfigureStubRunner(
 *     ids = "net.chrisrichardson.ftgo:ftgo-order-service:+:stubs:8080",
 *     stubsMode = StubRunnerProperties.StubsMode.LOCAL
 * )
 * class OrderServiceClientContractTest {
 *
 *     @Autowired
 *     private OrderServiceClient orderServiceClient;
 *
 *     @Test
 *     void shouldGetOrderFromStub() {
 *         // The stub server returns responses defined by the contract
 *         OrderResponse response = orderServiceClient.getOrder(1L);
 *         assertThat(response.getOrderId()).isEqualTo(1L);
 *         assertThat(response.getState()).isEqualTo("APPROVED");
 *     }
 * }
 * }</pre>
 *
 * <h3>Alternative: Pact-based Contract Test:</h3>
 * <pre>{@code
 * @ExtendWith(PactConsumerTestExt.class)
 * @PactTestFor(providerName = "order-service", port = "8080")
 * class OrderServicePactTest {
 *
 *     @Pact(consumer = "consumer-service")
 *     RequestResponsePact getOrderPact(PactDslWithProvider builder) {
 *         return builder
 *             .given("an order with ID 1 exists")
 *             .uponReceiving("a request for order 1")
 *                 .method("GET")
 *                 .path("/orders/1")
 *             .willRespondWith()
 *                 .status(200)
 *                 .body(new PactDslJsonBody()
 *                     .integerType("orderId", 1)
 *                     .stringType("state", "APPROVED"))
 *             .toPact();
 *     }
 *
 *     @Test
 *     @PactTestFor(pactMethod = "getOrderPact")
 *     void shouldGetOrder(MockServer mockServer) {
 *         // Test consumer against mock server
 *     }
 * }
 * }</pre>
 *
 * <h3>Key Conventions:</h3>
 * <ul>
 *   <li>Place contracts in {@code src/test/resources/contracts/}</li>
 *   <li>Provider tests mock the service layer, not the database</li>
 *   <li>Consumer tests use generated stubs from the provider</li>
 *   <li>Contract tests run in CI before integration tests</li>
 *   <li>Use Spring Cloud Contract for Spring-to-Spring communication</li>
 *   <li>Consider Pact for cross-language service contracts</li>
 * </ul>
 *
 * @see <a href="https://spring.io/projects/spring-cloud-contract">Spring Cloud Contract</a>
 * @see <a href="https://docs.pact.io/">Pact Documentation</a>
 */
public final class ContractTestTemplate {
    private ContractTestTemplate() {
        // Documentation-only class
    }
}
