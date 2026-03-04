package net.chrisrichardson.ftgo.testlib.builders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test data builder for Order entities.
 *
 * <p>Follows the Builder pattern to create test Order data with sensible defaults.
 * All values can be overridden for specific test scenarios.
 *
 * <p>Usage:
 * <pre>{@code
 * Map<String, Object> order = OrderBuilder.anOrder()
 *     .withConsumerId(42L)
 *     .withRestaurantId(7L)
 *     .withOrderTotal(new BigDecimal("25.99"))
 *     .build();
 * }</pre>
 *
 * @see ConsumerBuilder
 * @see RestaurantBuilder
 */
public final class OrderBuilder {

    private Long orderId = 1L;
    private Long consumerId = 100L;
    private Long restaurantId = 200L;
    private String state = "APPROVAL_PENDING";
    private BigDecimal orderTotal = new BigDecimal("29.99");
    private LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    private LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    private String deliveryAddress = "123 Main St, Springfield, IL 62701";
    private List<Map<String, Object>> lineItems = new ArrayList<>();

    private OrderBuilder() {
        // Add default line item
        Map<String, Object> defaultItem = new HashMap<>();
        defaultItem.put("menuItemId", "MI-001");
        defaultItem.put("name", "Chicken Vindaloo");
        defaultItem.put("quantity", 2);
        defaultItem.put("price", new BigDecimal("14.99"));
        lineItems.add(defaultItem);
    }

    /**
     * Creates a new OrderBuilder with sensible defaults.
     *
     * @return a new OrderBuilder instance
     */
    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderBuilder withConsumerId(Long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public OrderBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public OrderBuilder withOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
        return this;
    }

    public OrderBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OrderBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public OrderBuilder withDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        return this;
    }

    public OrderBuilder withLineItems(List<Map<String, Object>> lineItems) {
        this.lineItems = lineItems;
        return this;
    }

    public OrderBuilder addLineItem(String menuItemId, String name, int quantity, BigDecimal price) {
        Map<String, Object> item = new HashMap<>();
        item.put("menuItemId", menuItemId);
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("price", price);
        this.lineItems.add(item);
        return this;
    }

    /**
     * Creates a builder preset for an approved order.
     *
     * @return a new OrderBuilder with APPROVED state
     */
    public static OrderBuilder anApprovedOrder() {
        return anOrder()
                .withState("APPROVED")
                .withUpdatedAt(LocalDateTime.of(2026, 1, 15, 10, 35, 0));
    }

    /**
     * Creates a builder preset for a cancelled order.
     *
     * @return a new OrderBuilder with CANCELLED state
     */
    public static OrderBuilder aCancelledOrder() {
        return anOrder()
                .withState("CANCELLED")
                .withUpdatedAt(LocalDateTime.of(2026, 1, 15, 11, 0, 0));
    }

    /**
     * Builds the order as a Map representation.
     *
     * @return order data as a Map
     */
    public Map<String, Object> build() {
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("consumerId", consumerId);
        order.put("restaurantId", restaurantId);
        order.put("state", state);
        order.put("orderTotal", orderTotal);
        order.put("createdAt", createdAt);
        order.put("updatedAt", updatedAt);
        order.put("deliveryAddress", deliveryAddress);
        order.put("lineItems", new ArrayList<>(lineItems));
        return order;
    }

    // --- Getters for assertions and verification ---

    public Long getOrderId() {
        return orderId;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getState() {
        return state;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<Map<String, Object>> getLineItems() {
        return lineItems;
    }
}
