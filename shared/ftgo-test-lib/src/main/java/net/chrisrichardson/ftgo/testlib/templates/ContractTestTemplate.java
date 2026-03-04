package net.chrisrichardson.ftgo.testlib.templates;

/**
 * Template for Spring Cloud Contract tests.
 *
 * <p>This template demonstrates the recommended approach for contract testing
 * between FTGO microservices. Contract tests verify that a service's API
 * contract is maintained between producer and consumer.
 *
 * <h3>Producer (Server) Side</h3>
 * <p>The producer service defines contracts and generates tests:
 * <pre>{@code
 * // 1. Add Spring Cloud Contract dependency in build.gradle:
 * //    testImplementation "org.springframework.cloud:spring-cloud-contract-verifier:4.1.1"
 * //    testImplementation "org.springframework.cloud:spring-cloud-starter-contract-verifier:4.1.1"
 * //
 * // 2. Create contract DSL files under:
 * //    src/test/resources/contracts/order/
 * //
 * // 3. Example contract (shouldReturnOrder.groovy):
 * //    Contract.make {
 * //        description "should return order by id"
 * //        request {
 * //            method GET()
 * //            url "/api/orders/1"
 * //            headers {
 * //                contentType applicationJson()
 * //            }
 * //        }
 * //        response {
 * //            status OK()
 * //            headers {
 * //                contentType applicationJson()
 * //            }
 * //            body([
 * //                orderId: 1,
 * //                state: "APPROVAL_PENDING",
 * //                orderTotal: "29.99"
 * //            ])
 * //        }
 * //    }
 * //
 * // 4. Create a base test class:
 * //    @SpringBootTest(webEnvironment = MOCK)
 * //    @AutoConfigureMockMvc
 * //    public abstract class OrderContractBase {
 * //        @Autowired MockMvc mockMvc;
 * //        @MockBean OrderService orderService;
 * //
 * //        @BeforeEach
 * //        void setUp() {
 * //            RestAssuredMockMvc.mockMvc(mockMvc);
 * //            given(orderService.findById(1L))
 * //                .willReturn(Optional.of(testOrder()));
 * //        }
 * //    }
 * }</pre>
 *
 * <h3>Consumer (Client) Side</h3>
 * <p>The consumer service verifies its expectations against the producer's stubs:
 * <pre>{@code
 * // 1. Add Spring Cloud Contract Stub Runner dependency:
 * //    testImplementation "org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.1.1"
 * //
 * // 2. Write consumer-side test:
 * //    @SpringBootTest
 * //    @AutoConfigureStubRunner(
 * //        ids = "net.chrisrichardson.ftgo:ftgo-order-service:+:stubs:8080",
 * //        stubsMode = StubRunnerProperties.StubsMode.LOCAL
 * //    )
 * //    class OrderClientContractTest {
 * //
 * //        @Autowired
 * //        private OrderServiceClient orderClient;
 * //
 * //        @Test
 * //        void shouldGetOrderFromStub() {
 * //            OrderResponse order = orderClient.getOrder(1L);
 * //            assertThat(order.getOrderId()).isEqualTo(1L);
 * //            assertThat(order.getState()).isEqualTo("APPROVAL_PENDING");
 * //        }
 * //    }
 * }</pre>
 *
 * <h3>Contract Testing Workflow</h3>
 * <ol>
 *   <li>Producer defines contracts in Groovy DSL files</li>
 *   <li>Spring Cloud Contract generates tests from contracts</li>
 *   <li>Producer publishes stubs to a stub repository (local or remote)</li>
 *   <li>Consumer runs Stub Runner to verify its client against the stubs</li>
 *   <li>Both sides are verified independently — no shared running services needed</li>
 * </ol>
 *
 * @see <a href="https://spring.io/projects/spring-cloud-contract">Spring Cloud Contract</a>
 */
public final class ContractTestTemplate {

    private ContractTestTemplate() {
        // Template documentation class — not instantiable
    }
}
