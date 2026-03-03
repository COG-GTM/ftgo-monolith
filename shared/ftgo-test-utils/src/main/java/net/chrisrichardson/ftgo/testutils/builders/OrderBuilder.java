package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for creating {@link Order} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * Order order = OrderBuilder.anOrder()
 *     .withConsumerId(1L)
 *     .withRestaurant(RestaurantBuilder.aRestaurant().buildWithId())
 *     .withLineItems(
 *         new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2)
 *     )
 *     .build();
 * }</pre>
 *
 * <p>Or using defaults for quick test setup:
 * <pre>{@code
 * Order order = OrderBuilder.anOrder().build();
 * }</pre>
 */
public class OrderBuilder {

    private long consumerId = 1L;
    private Long orderId;
    private Restaurant restaurant;
    private List<OrderLineItem> lineItems;

    private OrderBuilder() {
    }

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder withConsumerId(long consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public OrderBuilder withOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderBuilder withRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        return this;
    }

    public OrderBuilder withLineItems(OrderLineItem... items) {
        this.lineItems = new ArrayList<>();
        Collections.addAll(this.lineItems, items);
        return this;
    }

    public OrderBuilder withLineItems(List<OrderLineItem> items) {
        this.lineItems = items;
        return this;
    }

    public Order build() {
        Restaurant orderRestaurant = this.restaurant != null
                ? this.restaurant
                : RestaurantBuilder.aRestaurant().buildWithId();

        List<OrderLineItem> orderLineItems = this.lineItems != null
                ? this.lineItems
                : defaultLineItems();

        Order order = new Order(consumerId, orderRestaurant, orderLineItems);

        if (orderId != null) {
            order.setId(orderId);
        }

        return order;
    }

    private List<OrderLineItem> defaultLineItems() {
        MenuItem defaultItem = MenuItemBuilder.aMenuItem().build();
        OrderLineItem lineItem = new OrderLineItem(
                defaultItem.getId(),
                defaultItem.getName(),
                defaultItem.getPrice(),
                1
        );
        return Collections.singletonList(lineItem);
    }
}
