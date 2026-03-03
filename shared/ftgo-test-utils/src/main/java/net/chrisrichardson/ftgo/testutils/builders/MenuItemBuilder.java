package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;

/**
 * Builder for creating {@link MenuItem} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * MenuItem item = MenuItemBuilder.aMenuItem()
 *     .withId("1")
 *     .withName("Chicken Vindaloo")
 *     .withPrice(new Money("12.34"))
 *     .build();
 * }</pre>
 */
public class MenuItemBuilder {

    private String id = "1";
    private String name = "Chicken Vindaloo";
    private Money price = new Money("12.34");

    private MenuItemBuilder() {
    }

    public static MenuItemBuilder aMenuItem() {
        return new MenuItemBuilder();
    }

    public MenuItemBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public MenuItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MenuItemBuilder withPrice(Money price) {
        this.price = price;
        return this;
    }

    public MenuItemBuilder withPrice(String price) {
        this.price = new Money(price);
        return this;
    }

    public MenuItem build() {
        return new MenuItem(id, name, price);
    }
}
