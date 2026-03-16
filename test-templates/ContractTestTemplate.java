package net.chrisrichardson.ftgo.<servicename>.contract;

/**
 * Contract Test Template - Consumer-Driven Contract Testing
 *
 * Contract tests verify that service APIs adhere to an agreed-upon contract
 * between a consumer (client) and a provider (server). They are essential
 * during microservices migration to ensure that extracted services remain
 * compatible with their consumers.
 *
 * ========================================================================
 * WHY CONTRACT TESTS?
 * ========================================================================
 *
 * During the FTGO monolith-to-microservices migration, services that were
 * previously co-located will communicate over the network. Contract tests
 * catch breaking API changes early, without requiring a full E2E environment.
 *
 * Test Pyramid placement:
 *
 *       /  E2E Tests  \          <- Fewer (full workflow validation)
 *      / Contract Tests \        <- Between integration and E2E
 *     /  Integration Tests \     <- With real DB / Spring context
 *    /    Unit Tests         \   <- Many (fast, isolated)
 *   /__________________________\
 *
 * ========================================================================
 * APPROACH 1: Spring Cloud Contract (Recommended for Spring Boot projects)
 * ========================================================================
 *
 * Spring Cloud Contract uses Groovy DSL or YAML to define contracts on the
 * provider side. It auto-generates:
 *   - Provider-side verification tests
 *   - Consumer-side stubs (WireMock-based)
 *
 * --- Provider Side (the service exposing the API) ---
 *
 * 1. Add dependency to the service's build.gradle:
 *
 *    buildscript {
 *        dependencies {
 *            classpath "org.springframework.cloud:spring-cloud-contract-gradle-plugin:2.0.2.RELEASE"
 *        }
 *    }
 *    apply plugin: 'spring-cloud-contract'
 *
 *    dependencies {
 *        testCompile 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
 *    }
 *
 * 2. Create contract definitions under src/test/resources/contracts/:
 *
 *    // src/test/resources/contracts/shouldReturnOrderById.groovy
 *    import org.springframework.cloud.contract.spec.Contract
 *
 *    Contract.make {
 *        description "should return order by ID"
 *
 *        request {
 *            method GET()
 *            url "/orders/1"
 *            headers {
 *                contentType applicationJson()
 *            }
 *        }
 *
 *        response {
 *            status 200
 *            headers {
 *                contentType applicationJson()
 *            }
 *            body([
 *                orderId: 1,
 *                state: "APPROVED",
 *                orderTotal: "12.34"
 *            ])
 *        }
 *    }
 *
 * 3. Create a base test class for generated contract verification tests:
 *
 *    // The plugin generates test classes that extend this base class.
 *    // Configure the controller with mocked dependencies, similar to
 *    // the standalone MockMvc pattern in RestApiTestTemplate.
 *
 *    @RunWith(SpringRunner.class)
 *    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
 *    @AutoConfigureMockMvc
 *    public abstract class OrderServiceContractBase {
 *
 *        @Autowired
 *        private WebApplicationContext context;
 *
 *        @MockBean
 *        private OrderRepository orderRepository;
 *
 *        @Before
 *        public void setUp() {
 *            RestAssuredMockMvc.webAppContextSetup(context);
 *
 *            // Set up mock responses matching the contract expectations
 *            Order order = new Order(...);
 *            order.setId(1L);
 *            when(orderRepository.findById(1L))
 *                .thenReturn(Optional.of(order));
 *        }
 *    }
 *
 * 4. Run provider verification:
 *    ./gradlew :services:order-service:contractTest
 *
 * --- Consumer Side (the service calling the API) ---
 *
 * 1. Add stub dependency:
 *
 *    dependencies {
 *        testCompile 'org.springframework.cloud:spring-cloud-starter-contract-stub-runner'
 *    }
 *
 * 2. Write consumer test using auto-configured stubs:
 *
 *    @RunWith(SpringRunner.class)
 *    @SpringBootTest
 *    @AutoConfigureStubRunner(
 *        ids = "net.chrisrichardson.ftgo:ftgo-order-service:+:stubs:8080",
 *        stubsMode = StubRunnerProperties.StubsMode.LOCAL
 *    )
 *    public class OrderServiceClientContractTest {
 *
 *        @Autowired
 *        private OrderServiceClient client;
 *
 *        @Test
 *        public void shouldGetOrderFromStub() {
 *            OrderResponse order = client.getOrder(1L);
 *
 *            assertNotNull(order);
 *            assertEquals("APPROVED", order.getState());
 *            assertEquals("12.34", order.getOrderTotal());
 *        }
 *    }
 *
 * ========================================================================
 * APPROACH 2: Pact (Language-agnostic alternative)
 * ========================================================================
 *
 * Pact is a consumer-driven contract testing framework that works across
 * languages. Use this if FTGO services will be written in different languages.
 *
 * --- Consumer Side (writes the contract) ---
 *
 * 1. Add dependency:
 *
 *    dependencies {
 *        testCompile 'au.com.dius:pact-jvm-consumer-junit:3.6.15'
 *    }
 *
 * 2. Write consumer contract test:
 */

// Example Pact consumer test (JUnit 4):
//
// import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
// import au.com.dius.pact.consumer.junit.PactProviderRule;
// import au.com.dius.pact.consumer.junit.PactVerification;
// import au.com.dius.pact.model.RequestResponsePact;
// import org.junit.Rule;
// import org.junit.Test;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotNull;
//
// public class OrderServicePactConsumerTest {
//
//     @Rule
//     public PactProviderRule provider = new PactProviderRule(
//             "order-service", "localhost", 8080, this);
//
//     @Pact(consumer = "consumer-service")
//     public RequestResponsePact createPact(PactDslWithProvider builder) {
//         return builder
//             .given("an order with ID 1 exists")
//             .uponReceiving("a request to get order 1")
//                 .path("/orders/1")
//                 .method("GET")
//             .willRespondWith()
//                 .status(200)
//                 .body("{\"orderId\":1,\"state\":\"APPROVED\",\"orderTotal\":\"12.34\"}")
//             .toPact();
//     }
//
//     @Test
//     @PactVerification("order-service")
//     public void shouldGetOrder() {
//         // Call the API at http://localhost:8080 (backed by Pact mock)
//         // OrderServiceClient client = new OrderServiceClient("http://localhost:8080");
//         // OrderResponse order = client.getOrder(1L);
//         // assertNotNull(order);
//         // assertEquals("APPROVED", order.getState());
//     }
// }

/**
 * --- Provider Side (verifies the contract) ---
 *
 * 1. Add dependency:
 *
 *    dependencies {
 *        testCompile 'au.com.dius:pact-jvm-provider-junit:3.6.15'
 *        testCompile 'au.com.dius:pact-jvm-provider-spring:3.6.15'
 *    }
 *
 * 2. Write provider verification test:
 *
 *    @RunWith(SpringRestPactRunner.class)
 *    @Provider("order-service")
 *    @PactFolder("pacts")  // or @PactBroker for CI
 *    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 *    public class OrderServicePactProviderTest {
 *
 *        @MockBean
 *        private OrderRepository orderRepository;
 *
 *        @State("an order with ID 1 exists")
 *        public void setupOrderExists() {
 *            Order order = new Order(...);
 *            order.setId(1L);
 *            when(orderRepository.findById(1L))
 *                .thenReturn(Optional.of(order));
 *        }
 *    }
 *
 * ========================================================================
 * CHOOSING BETWEEN APPROACHES
 * ========================================================================
 *
 * | Criteria                     | Spring Cloud Contract | Pact           |
 * |------------------------------|----------------------|----------------|
 * | Language support              | Java/JVM only        | Multi-language |
 * | Contract definition location  | Provider side        | Consumer side  |
 * | Spring Boot integration       | Native               | Via adapter    |
 * | Stub generation               | Automatic (WireMock) | Via Pact Broker|
 * | Recommended for FTGO          | Yes (all Java)       | If polyglot    |
 * | Complexity                    | Lower                | Moderate       |
 *
 * For the FTGO project (all Java, all Spring Boot), Spring Cloud Contract
 * is the recommended approach.
 *
 * ========================================================================
 * CONTRACT TEST WORKFLOW IN CI
 * ========================================================================
 *
 * 1. Provider publishes stubs to local Maven repo or artifact store
 *    ./gradlew :services:order-service:publishStubsPublicationToMavenLocal
 *
 * 2. Consumer tests run against published stubs
 *    ./gradlew :services:consumer-service:contractTest
 *
 * 3. In CI, run provider contract verification before consumer tests
 *    to ensure stubs are up-to-date.
 *
 * ========================================================================
 * COMPATIBILITY NOTES
 * ========================================================================
 *
 * - Spring Cloud Contract 2.0.x is compatible with Spring Boot 2.0.x
 * - Pact JVM 3.6.x is compatible with Java 8
 * - Both approaches work with JUnit 4 (current) and JUnit 5 (future)
 */
public class ContractTestTemplate {
    // This file is documentation-only. See the approaches above for
    // implementation patterns. Actual contract test classes should be
    // created in the respective service modules.
}
