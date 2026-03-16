package net.chrisrichardson.ftgo.<servicename>.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * REST API Test Template - Rest-Assured with MockMvc (JUnit 4)
 *
 * This template demonstrates REST API endpoint testing using Rest-Assured's
 * MockMvc integration. This is the pattern used in the existing FTGO codebase
 * (see OrderControllerTest).
 *
 * Two approaches are shown:
 *
 *   1. Standalone MockMvc (no Spring context) - preferred for unit-level API tests.
 *      Fast, tests only the controller with mocked services.
 *      Pattern: OrderControllerTest
 *
 *   2. Full Spring Boot Test - for integration-level API tests.
 *      Slower, loads full context, tests end-to-end through the stack.
 *      Pattern: FtgoApplicationTest / AbstractEndToEndTests
 *
 * Conventions:
 *   - Test class name: {Controller}Test or {Controller}ApiTest
 *   - Location: src/test/java/{package}/web/
 *   - Test method prefix: should{ExpectedBehavior}
 *
 * Dependencies:
 *   - Rest-Assured MockMvc module (io.restassured:spring-mock-mvc)
 *   - Mockito for service mocking
 *   - Jackson for JSON serialization (MoneyModule for Money type)
 *
 * JUnit 5 Migration Note:
 *   When migrating to JUnit 5, replace:
 *     - @Before -> @BeforeEach
 *     - @Test (org.junit.Test) -> @Test (org.junit.jupiter.api.Test)
 *   Rest-Assured API remains the same.
 */
public class SomeControllerTest {

    // --- Mocked service dependencies ---

    private SomeService someService;
    private SomeRepository someRepository;

    // --- Controller under test ---

    private SomeController someController;

    // --- Test data constants ---
    // Consider extracting to an Object Mother class for reuse (see OrderDetailsMother)

    private static final long ENTITY_ID = 1L;
    private static final String ENTITY_NAME = "Test Entity";

    @Before
    public void setUp() {
        someService = mock(SomeService.class);
        someRepository = mock(SomeRepository.class);
        someController = new SomeController(someService, someRepository);
    }

    // ---------------------------------------------------------------
    // Approach 1: Standalone MockMvc (no Spring context)
    // ---------------------------------------------------------------

    // --- GET endpoint tests ---

    @Test
    public void shouldReturnEntityWhenFound() {
        // Arrange
        SomeEntity entity = new SomeEntity(ENTITY_ID, ENTITY_NAME);
        when(someRepository.findById(ENTITY_ID)).thenReturn(Optional.of(entity));

        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
        when().
                get("/entities/" + ENTITY_ID).
        then().
                statusCode(200).
                body("id", equalTo((int) ENTITY_ID)).
                body("name", equalTo(ENTITY_NAME));
    }

    @Test
    public void shouldReturn404WhenEntityNotFound() {
        // Arrange
        when(someRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
        when().
                get("/entities/999").
        then().
                statusCode(404);
    }

    // --- POST endpoint tests ---

    @Test
    public void shouldCreateEntityAndReturnId() {
        // Arrange
        SomeEntity created = new SomeEntity(ENTITY_ID, ENTITY_NAME);
        when(someService.create(any())).thenReturn(created);

        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
                contentType("application/json").
                body("{\"name\": \"" + ENTITY_NAME + "\"}").
        when().
                post("/entities").
        then().
                statusCode(200).
                body("id", notNullValue());

        // Verify service was called
        verify(someService).create(any());
    }

    // --- PUT endpoint tests ---

    @Test
    public void shouldUpdateExistingEntity() {
        // Arrange
        SomeEntity existing = new SomeEntity(ENTITY_ID, ENTITY_NAME);
        when(someRepository.findById(ENTITY_ID)).thenReturn(Optional.of(existing));

        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
                contentType("application/json").
                body("{\"name\": \"Updated Name\"}").
        when().
                put("/entities/" + ENTITY_ID).
        then().
                statusCode(200);
    }

    // --- DELETE endpoint tests ---

    @Test
    public void shouldDeleteExistingEntity() {
        // Arrange
        when(someRepository.findById(ENTITY_ID)).thenReturn(
                Optional.of(new SomeEntity(ENTITY_ID, ENTITY_NAME)));

        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
        when().
                delete("/entities/" + ENTITY_ID).
        then().
                statusCode(200);
    }

    // --- Validation error tests ---

    @Test
    public void shouldReturn400ForInvalidInput() {
        // Act & Assert
        given().
                standaloneSetup(configureControllers(someController)).
                contentType("application/json").
                body("{\"name\": \"\"}").
        when().
                post("/entities").
        then().
                statusCode(400);
    }

    // ---------------------------------------------------------------
    // Helper: Configure standalone MockMvc with custom ObjectMapper
    // ---------------------------------------------------------------

    /**
     * Configures MockMvc with custom Jackson ObjectMapper.
     * Register project-specific modules here (e.g., MoneyModule for
     * Money serialization, JavaTimeModule for Java 8 date/time).
     *
     * Pattern from OrderControllerTest.configureControllers().
     */
    private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
        ObjectMapper objectMapper = new ObjectMapper();
        // Register project-specific Jackson modules:
        // objectMapper.registerModule(new MoneyModule());
        // objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        return MockMvcBuilders.standaloneSetup(controllers)
                .setMessageConverters(converter);
    }

    // ---------------------------------------------------------------
    // Approach 2: Full Integration API Test (Spring Boot context)
    //
    // For full-stack API tests, use @RunWith(SpringRunner.class) with
    // @SpringBootTest and inject the port. See FtgoApplicationTest for
    // the pattern, or use IntegrationTestTemplate.java as a base.
    //
    // Example:
    //
    //   @RunWith(SpringRunner.class)
    //   @SpringBootTest(
    //       classes = Config.class,
    //       webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
    //   )
    //   public class SomeControllerIntegrationTest {
    //
    //       @LocalServerPort
    //       private int port;
    //
    //       @Test
    //       public void shouldCreateEntity() {
    //           given().
    //               port(port).
    //               contentType("application/json").
    //               body(new CreateEntityRequest("Test")).
    //           when().
    //               post("/entities").
    //           then().
    //               statusCode(200).
    //               body("id", notNullValue());
    //       }
    //   }
    // ---------------------------------------------------------------
}
