package com.ftgo.order.api;

import net.chrisrichardson.ftgo.testlib.builders.OrderBuilder;
import net.chrisrichardson.ftgo.testlib.config.TestJsonHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example API test for the Order bounded context.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Rest-Assured BDD-style API testing patterns</li>
 *   <li>Test data builders for request bodies</li>
 *   <li>JSON serialization with TestJsonHelper</li>
 *   <li>Testing both success and error responses</li>
 * </ul>
 *
 * <p><strong>Note:</strong> These are structural examples. In a real service,
 * you would extend {@code AbstractApiTest} and have a running Spring Boot context.
 * For now, these demonstrate the patterns and use the test library utilities.
 */
@Tag("api")
@DisplayName("Order API - Example Tests")
class OrderApiExampleTest {

    @Nested
    @DisplayName("Request Body Construction")
    class RequestBodyConstruction {

        @Test
        @DisplayName("should serialize order to JSON for API request")
        void shouldSerializeOrderToJson() {
            Map<String, Object> order = OrderBuilder.anOrder()
                    .withConsumerId(42L)
                    .withRestaurantId(7L)
                    .build();

            String json = TestJsonHelper.toJson(order);

            assertThat(json).contains("\"consumerId\":42");
            assertThat(json).contains("\"restaurantId\":7");
            assertThat(json).contains("\"state\":\"APPROVAL_PENDING\"");
        }

        @Test
        @DisplayName("should round-trip order through JSON serialization")
        void shouldRoundTripOrderThroughJson() {
            Map<String, Object> original = OrderBuilder.anOrder()
                    .withOrderTotal(new BigDecimal("29.99"))
                    .build();

            String json = TestJsonHelper.toJson(original);
            Map<String, Object> deserialized = TestJsonHelper.fromJson(json);

            assertThat(deserialized).containsKey("orderId");
            assertThat(deserialized).containsKey("state");
            assertThat(deserialized.get("state")).isEqualTo("APPROVAL_PENDING");
        }
    }

    @Nested
    @DisplayName("Response Validation Patterns")
    class ResponseValidationPatterns {

        @Test
        @DisplayName("should validate order response structure")
        void shouldValidateOrderResponseStructure() {
            // Simulate a response body
            Map<String, Object> response = OrderBuilder.anApprovedOrder()
                    .withOrderId(123L)
                    .build();

            String json = TestJsonHelper.toJson(response);
            Map<String, Object> parsed = TestJsonHelper.fromJson(json);

            assertThat(parsed).containsKeys("orderId", "state", "orderTotal");
        }
    }
}
