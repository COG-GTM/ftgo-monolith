package net.chrisrichardson.ftgo.testlib.assertions;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom domain-specific assertions for FTGO entities.
 *
 * <p>Provides high-level assertion methods for verifying FTGO domain objects
 * represented as Maps (as produced by test data builders).
 *
 * <p>Usage:
 * <pre>{@code
 * FtgoAssertions.assertOrder(orderMap)
 *     .hasState("APPROVED")
 *     .hasConsumerId(42L);
 *
 * FtgoAssertions.assertAddress(addressString)
 *     .isNotBlank();
 * }</pre>
 */
public final class FtgoAssertions {

    private FtgoAssertions() {
        // Utility class
    }

    /**
     * Creates an order assertion object for fluent assertions.
     *
     * @param order the order data map
     * @return an OrderAssert instance
     */
    public static OrderAssert assertOrder(Map<String, Object> order) {
        return new OrderAssert(order);
    }

    /**
     * Asserts that an address string is valid (non-null, non-blank).
     *
     * @param address the address string
     */
    public static void assertValidAddress(String address) {
        assertThat(address)
                .as("Address")
                .isNotNull()
                .isNotBlank();
    }

    /**
     * Asserts that a list of line items is non-empty and each item has required fields.
     *
     * @param lineItems the list of line item maps
     */
    @SuppressWarnings("unchecked")
    public static void assertValidLineItems(List<Map<String, Object>> lineItems) {
        assertThat(lineItems)
                .as("Line items")
                .isNotNull()
                .isNotEmpty();

        for (Map<String, Object> item : lineItems) {
            assertThat(item).containsKeys("menuItemId", "name", "quantity", "price");
        }
    }

    /**
     * Fluent assertion class for Order data.
     */
    public static class OrderAssert {

        private final Map<String, Object> order;

        OrderAssert(Map<String, Object> order) {
            assertThat(order).as("Order").isNotNull();
            this.order = order;
        }

        public OrderAssert hasState(String expectedState) {
            assertThat(order.get("state"))
                    .as("Order state")
                    .isEqualTo(expectedState);
            return this;
        }

        public OrderAssert hasConsumerId(Long expectedConsumerId) {
            assertThat(order.get("consumerId"))
                    .as("Order consumerId")
                    .isEqualTo(expectedConsumerId);
            return this;
        }

        public OrderAssert hasRestaurantId(Long expectedRestaurantId) {
            assertThat(order.get("restaurantId"))
                    .as("Order restaurantId")
                    .isEqualTo(expectedRestaurantId);
            return this;
        }

        public OrderAssert hasOrderId(Long expectedOrderId) {
            assertThat(order.get("orderId"))
                    .as("Order orderId")
                    .isEqualTo(expectedOrderId);
            return this;
        }

        @SuppressWarnings("unchecked")
        public OrderAssert hasLineItemCount(int expectedCount) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("lineItems");
            assertThat(items)
                    .as("Order line items count")
                    .hasSize(expectedCount);
            return this;
        }

        public OrderAssert hasDeliveryAddress(String expectedAddress) {
            assertThat(order.get("deliveryAddress"))
                    .as("Order delivery address")
                    .isEqualTo(expectedAddress);
            return this;
        }
    }
}
