package net.chrisrichardson.ftgo.testutils.builders;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder for creating {@link Restaurant} instances in tests.
 *
 * <p>Usage:
 * <pre>{@code
 * Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *     .withName("Ajanta")
 *     .withAddress(AddressBuilder.anAddress().withCity("Berkeley").build())
 *     .withMenuItems(
 *         MenuItemBuilder.aMenuItem().withName("Chicken Vindaloo").build(),
 *         MenuItemBuilder.aMenuItem().withId("2").withName("Lamb Rogan Josh").build()
 *     )
 *     .build();
 * }</pre>
 *
 * <p>Or with an ID for mock scenarios:
 * <pre>{@code
 * Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *     .withId(1L)
 *     .withName("Ajanta")
 *     .buildWithId();
 * }</pre>
 */
public class RestaurantBuilder {

    private Long id;
    private String name = "Ajanta";
    private Address address = AddressBuilder.anAddress().build();
    private List<MenuItem> menuItems = new ArrayList<>(Collections.singletonList(
            MenuItemBuilder.aMenuItem().build()
    ));

    private RestaurantBuilder() {
    }

    public static RestaurantBuilder aRestaurant() {
        return new RestaurantBuilder();
    }

    public RestaurantBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    public RestaurantBuilder withMenuItems(MenuItem... items) {
        this.menuItems = Arrays.asList(items);
        return this;
    }

    public RestaurantBuilder withMenuItems(List<MenuItem> items) {
        this.menuItems = items;
        return this;
    }

    /**
     * Builds a Restaurant using the (name, address, menu) constructor.
     * Use this when you need a Restaurant without a pre-set ID (JPA will assign one).
     */
    public Restaurant build() {
        return new Restaurant(name, address, new RestaurantMenu(menuItems));
    }

    /**
     * Builds a Restaurant using the (id, name, menu) constructor.
     * Use this for unit tests where you need to control the ID value.
     */
    public Restaurant buildWithId() {
        Long restaurantId = this.id != null ? this.id : 1L;
        return new Restaurant(restaurantId, name, new RestaurantMenu(menuItems));
    }
}
