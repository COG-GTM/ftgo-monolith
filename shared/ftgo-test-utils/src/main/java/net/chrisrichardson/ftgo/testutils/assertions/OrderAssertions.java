package net.chrisrichardson.ftgo.testutils.assertions;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom AssertJ-style assertions for {@link Order} domain objects.
 *
 * <p>Provides fluent, domain-specific assertions that make tests more readable
 * and produce better failure messages than generic assertions.
 *
 * <p>Usage:
 * <pre>{@code
 * import static net.chrisrichardson.ftgo.testutils.assertions.OrderAssertions.*;
 *
 * assertOrderState(order, OrderState.APPROVED);
 * assertOrderTotal(order, new Money("61.70"));
 * assertOrderBelongsToConsumer(order, 1L);
 * }</pre>
 */
public final class OrderAssertions {

    private OrderAssertions() {
        // Utility class - prevent instantiation
    }

    /**
     * Asserts that the order is in the expected state.
     *
     * @param order         the order to check
     * @param expectedState the expected order state
     */
    public static void assertOrderState(Order order, OrderState expectedState) {
        assertThat(order.getOrderState())
                .as("Order %d should be in state %s", order.getId(), expectedState)
                .isEqualTo(expectedState);
    }

    /**
     * Asserts that the order is in APPROVED state.
     */
    public static void assertOrderApproved(Order order) {
        assertOrderState(order, OrderState.APPROVED);
    }

    /**
     * Asserts that the order is in CANCELLED state.
     */
    public static void assertOrderCancelled(Order order) {
        assertOrderState(order, OrderState.CANCELLED);
    }

    /**
     * Asserts that the order total equals the expected amount.
     *
     * @param order         the order to check
     * @param expectedTotal the expected total amount
     */
    public static void assertOrderTotal(Order order, Money expectedTotal) {
        assertThat(order.getOrderTotal())
                .as("Order %d total should be %s", order.getId(), expectedTotal.asString())
                .isEqualTo(expectedTotal);
    }

    /**
     * Asserts that the order belongs to the specified consumer.
     *
     * @param order              the order to check
     * @param expectedConsumerId the expected consumer ID
     */
    public static void assertOrderBelongsToConsumer(Order order, long expectedConsumerId) {
        assertThat(order.getConsumerId())
                .as("Order %d should belong to consumer %d", order.getId(), expectedConsumerId)
                .isEqualTo(expectedConsumerId);
    }

    /**
     * Asserts that the order has the expected number of line items.
     *
     * @param order         the order to check
     * @param expectedCount the expected number of line items
     */
    public static void assertOrderLineItemCount(Order order, int expectedCount) {
        assertThat(order.getLineItems())
                .as("Order %d should have %d line items", order.getId(), expectedCount)
                .hasSize(expectedCount);
    }

    /**
     * Asserts that the order has a non-null restaurant.
     */
    public static void assertOrderHasRestaurant(Order order) {
        assertThat(order.getRestaurant())
                .as("Order %d should have a restaurant", order.getId())
                .isNotNull();
    }

    /**
     * Asserts that the order's restaurant has the expected name.
     */
    public static void assertOrderRestaurantName(Order order, String expectedName) {
        assertThat(order.getRestaurant())
                .as("Order should have a restaurant")
                .isNotNull();
        assertThat(order.getRestaurant().getName())
                .as("Order's restaurant should be named '%s'", expectedName)
                .isEqualTo(expectedName);
    }
}
