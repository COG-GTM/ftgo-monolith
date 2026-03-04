package net.chrisrichardson.ftgo.testlib.builders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OrderBuilder}.
 */
@DisplayName("OrderBuilder")
class OrderBuilderTest {

    @Test
    @DisplayName("should create order with defaults")
    void shouldCreateOrderWithDefaults() {
        Map<String, Object> order = OrderBuilder.anOrder().build();

        assertThat(order).containsKeys("orderId", "consumerId", "restaurantId", "state",
                "orderTotal", "createdAt", "updatedAt", "deliveryAddress", "lineItems");
        assertThat(order.get("state")).isEqualTo("APPROVAL_PENDING");
        assertThat(order.get("orderId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("should create approved order preset")
    void shouldCreateApprovedOrderPreset() {
        Map<String, Object> order = OrderBuilder.anApprovedOrder().build();

        assertThat(order.get("state")).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("should create cancelled order preset")
    void shouldCreateCancelledOrderPreset() {
        Map<String, Object> order = OrderBuilder.aCancelledOrder().build();

        assertThat(order.get("state")).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("should override defaults with custom values")
    void shouldOverrideDefaults() {
        Map<String, Object> order = OrderBuilder.anOrder()
                .withOrderId(42L)
                .withConsumerId(99L)
                .withOrderTotal(new BigDecimal("50.00"))
                .build();

        assertThat(order.get("orderId")).isEqualTo(42L);
        assertThat(order.get("consumerId")).isEqualTo(99L);
        assertThat(order.get("orderTotal")).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("should add line items")
    @SuppressWarnings("unchecked")
    void shouldAddLineItems() {
        Map<String, Object> order = OrderBuilder.anOrder()
                .addLineItem("MI-003", "Naan Bread", 3, new BigDecimal("3.99"))
                .build();

        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) order.get("lineItems");
        assertThat(lineItems).hasSizeGreaterThan(1);
    }
}
