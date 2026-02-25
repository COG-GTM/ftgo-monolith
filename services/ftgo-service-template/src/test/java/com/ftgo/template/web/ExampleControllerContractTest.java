package com.ftgo.template.web;

// =============================================================================
// TEMPLATE: Contract Test Base Class (Spring Cloud Contract)
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "Example" with your resource.
//
// This template demonstrates:
//   - Spring Cloud Contract verifier base class setup
//   - MockMvc configuration for contract verification
//   - Mocked service dependencies for predictable contract responses
//   - How contracts are verified against the actual controller
//
// Contract testing flow:
//   1. Consumer defines the expected API contract (Groovy DSL or YAML)
//   2. Contract files are stored in shared/ftgo-{service}-service-api/
//   3. This base class sets up the Spring context for verification
//   4. Spring Cloud Contract generates JUnit tests from the contracts
//   5. Generated tests run against this base class configuration
//
// For the full contract testing strategy, see: docs/api-contract-testing.md
//
// Dependencies (add to build.gradle when ready):
//   testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
// =============================================================================

import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Spring Cloud Contract verification tests.
 *
 * <p>Spring Cloud Contract auto-generates test classes from contract definitions
 * (stored in {@code shared/ftgo-{service}-service-api/src/contractTest/resources/contracts/}).
 * Those generated tests extend this base class, which provides the MockMvc and
 * mock service configuration.
 *
 * <p><b>Contract file location:</b>
 * <pre>
 * shared/ftgo-{service}-service-api/
 *   src/contractTest/resources/contracts/
 *     {resource}/
 *       shouldReturnResourceById.groovy
 *       shouldCreateResource.groovy
 *       shouldReturn404ForNonExistentResource.groovy
 * </pre>
 *
 * <p><b>Example Groovy Contract:</b>
 * <pre>{@code
 * Contract.make {
 *     description "should return example by ID"
 *     request {
 *         method GET()
 *         url "/api/v1/examples/1"
 *         headers { accept(applicationJson()) }
 *     }
 *     response {
 *         status OK()
 *         headers { contentType(applicationJson()) }
 *         body([
 *             status: "success",
 *             data: [
 *                 id: 1,
 *                 name: "Test Entity",
 *                 status: "ACTIVE"
 *             ]
 *         ])
 *     }
 * }
 * }</pre>
 *
 * <p><b>Gradle configuration (in service build.gradle):</b>
 * <pre>{@code
 * plugins {
 *     id 'org.springframework.cloud.contract' version '4.1.0'
 * }
 *
 * contracts {
 *     testFramework = TestFramework.JUNIT5
 *     baseClassForTests = 'com.ftgo.template.web.ExampleControllerContractTest'
 *     contractsPath = "contracts"
 * }
 * }</pre>
 *
 * <p><b>Location:</b> {@code src/test/java/com/ftgo/{service}/web/}
 */
// TODO: Uncomment when adding Spring Cloud Contract
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
// @AutoConfigureMockMvc
public class ExampleControllerContractTest {

    // @Autowired
    // private MockMvc mockMvc;

    // @MockBean
    // private ExampleService exampleService;

    @BeforeEach
    void setUp() {
        // Configure RestAssuredMockMvc for Spring Cloud Contract
        // RestAssuredMockMvc.mockMvc(mockMvc);

        // Set up stubs for contract verification.
        // Each contract test expects specific responses from the service layer.
        // The stubs here should match what the contracts define.

        // Example: stub for "shouldReturnExampleById" contract
        // var entity = ExampleEntityTestBuilder.anEntity()
        //     .withId(1L)
        //     .withName("Test Entity")
        //     .build();
        // when(exampleService.findById(1L)).thenReturn(entity);

        // Example: stub for "shouldReturn404ForNonExistentExample" contract
        // when(exampleService.findById(999999L))
        //     .thenThrow(new EntityNotFoundException("Example", 999999L));
    }

    // -------------------------------------------------------------------------
    // Consumer-side stub testing (for services that CONSUME other APIs)
    // -------------------------------------------------------------------------
    //
    // If this service consumes APIs from other services, create a separate test
    // class that uses @AutoConfigureStubRunner to test against generated stubs:
    //
    // @SpringBootTest
    // @AutoConfigureStubRunner(
    //     ids = "com.ftgo:ftgo-restaurant-service-api:+:stubs:8090",
    //     stubsMode = StubRunnerProperties.StubsMode.LOCAL
    // )
    // class RestaurantServiceClientContractTest {
    //
    //     @Autowired
    //     private RestaurantServiceClient restaurantClient;
    //
    //     @Test
    //     void shouldGetRestaurantFromStub() {
    //         // The stub runner provides a WireMock server on port 8090
    //         // that responds according to the published contracts
    //         var restaurant = restaurantClient.getRestaurant(1L);
    //         assertThat(restaurant).isNotNull();
    //         assertThat(restaurant.getName()).isNotBlank();
    //     }
    // }
    // -------------------------------------------------------------------------
}
