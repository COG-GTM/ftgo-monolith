package net.chrisrichardson.ftgo.testlib.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for FTGO REST API tests using Rest-Assured.
 *
 * <p>Provides standard configuration for API tests:
 * <ul>
 *   <li>Full Spring Boot context with a random port</li>
 *   <li>Rest-Assured pre-configured with the server's base URI</li>
 *   <li>"api" tag for test filtering</li>
 *   <li>"test" profile activation</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * class OrderApiTest extends AbstractApiTest {
 *
 *     @Test
 *     void shouldCreateOrder() {
 *         given()
 *             .contentType(ContentType.JSON)
 *             .body(OrderBuilder.anOrder().build())
 *         .when()
 *             .post("/api/orders")
 *         .then()
 *             .statusCode(201)
 *             .body("orderId", notNullValue());
 *     }
 * }
 * }</pre>
 *
 * @see AbstractIntegrationTest
 */
@Tag("api")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractApiTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
