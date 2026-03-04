package net.chrisrichardson.ftgo.testlib.templates;

import net.chrisrichardson.ftgo.testlib.config.AbstractApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Template for Rest-Assured API tests.
 *
 * <p>This template demonstrates the recommended structure for FTGO API tests:
 * <ul>
 *   <li>Extend {@link AbstractApiTest} for Rest-Assured and Spring Boot configuration</li>
 *   <li>Use Rest-Assured's given/when/then BDD-style API</li>
 *   <li>Group tests by HTTP method or resource</li>
 *   <li>Test both success and error paths</li>
 *   <li>Verify response status codes, headers, and body</li>
 * </ul>
 *
 * <h3>Copy this template and rename it for your service:</h3>
 * <pre>{@code
 * // Copy to:
 * // services/ftgo-order-service/src/test/java/com/ftgo/order/api/OrderApiTest.java
 * //
 * // Then:
 * // 1. Replace the package and class names
 * // 2. Add @SpringBootTest(classes = YourApplication.class) if needed
 * // 3. Add your test methods
 * }</pre>
 */
// @DisplayName("Order API")  // Uncomment and rename
class ApiTestTemplate extends AbstractApiTest {

    // === Example: GET endpoint tests ===

    // @Nested
    // @DisplayName("GET /api/orders/{id}")
    // class GetOrder {
    //
    //     @Test
    //     @DisplayName("should return order when found")
    //     void shouldReturnOrderWhenFound() {
    //         given()
    //             .contentType("application/json")
    //         .when()
    //             .get("/api/orders/1")
    //         .then()
    //             .statusCode(200)
    //             .body("orderId", equalTo(1))
    //             .body("state", notNullValue());
    //     }
    //
    //     @Test
    //     @DisplayName("should return 404 when order not found")
    //     void shouldReturn404WhenOrderNotFound() {
    //         given()
    //             .contentType("application/json")
    //         .when()
    //             .get("/api/orders/999")
    //         .then()
    //             .statusCode(404);
    //     }
    // }

    // === Example: POST endpoint tests ===

    // @Nested
    // @DisplayName("POST /api/orders")
    // class CreateOrder {
    //
    //     @Test
    //     @DisplayName("should create order with valid request")
    //     void shouldCreateOrderWithValidRequest() {
    //         String requestBody = TestJsonHelper.toJson(
    //             OrderBuilder.anOrder().build()
    //         );
    //
    //         given()
    //             .contentType("application/json")
    //             .body(requestBody)
    //         .when()
    //             .post("/api/orders")
    //         .then()
    //             .statusCode(201)
    //             .body("orderId", notNullValue())
    //             .body("state", equalTo("APPROVAL_PENDING"));
    //     }
    //
    //     @Test
    //     @DisplayName("should return 400 for invalid request")
    //     void shouldReturn400ForInvalidRequest() {
    //         given()
    //             .contentType("application/json")
    //             .body("{}")
    //         .when()
    //             .post("/api/orders")
    //         .then()
    //             .statusCode(400);
    //     }
    // }
}
