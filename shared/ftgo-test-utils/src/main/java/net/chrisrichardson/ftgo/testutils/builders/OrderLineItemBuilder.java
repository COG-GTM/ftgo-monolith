package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.OrderLineItem;

/**
 * Builder for creating {@link OrderLineItem} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * OrderLineItem item = OrderLineItemBuilder.anOrderLineItem()
 *     .withMenuItemId("1")
 *     .withName("Chicken Vindaloo")
 *     .withPrice("12.34")
 *     .withQuantity(2)
 *     .build();
 * }</pre>
 */
public class OrderLineItemBuilder {

    private String menuItemId = "1";
    private String name = "Chicken Vindaloo";
    private Money price = new Money("12.34");
    private int quantity = 1;

    private OrderLineItemBuilder() {
    }

    public static OrderLineItemBuilder anOrderLineItem() {
        return new OrderLineItemBuilder();
    }

    public OrderLineItemBuilder withMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
        return this;
    }

    public OrderLineItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public OrderLineItemBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public OrderLineItemBuilder withPrice(String price) {
        this.price = new Money(price);
        return this;
    }

    public OrderLineItemBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderLineItem build() {
        return new OrderLineItem(menuItemId, name, price, quantity);
    }
}
