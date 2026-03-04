package net.chrisrichardson.ftgo.testlib.builders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test data builder for Restaurant entities.
 *
 * <p>Usage:
 * <pre>{@code
 * Map<String, Object> restaurant = RestaurantBuilder.aRestaurant()
 *     .withName("Ajanta")
 *     .addMenuItem("MI-001", "Chicken Vindaloo", new BigDecimal("14.99"))
 *     .build();
 * }</pre>
 *
 * @see OrderBuilder
 */
public final class RestaurantBuilder {

    private Long restaurantId = 200L;
    private String name = "Ajanta";
    private String address = "456 Restaurant Row, Springfield, IL 62701";
    private String cuisineType = "Indian";
    private LocalDateTime createdAt = LocalDateTime.of(2026, 1, 5, 8, 0, 0);
    private List<Map<String, Object>> menuItems = new ArrayList<>();

    private RestaurantBuilder() {
        // Add default menu items
        addMenuItem("MI-001", "Chicken Vindaloo", new BigDecimal("14.99"));
        addMenuItem("MI-002", "Lamb Biryani", new BigDecimal("16.99"));
    }

    /**
     * Creates a new RestaurantBuilder with sensible defaults.
     *
     * @return a new RestaurantBuilder instance
     */
    public static RestaurantBuilder aRestaurant() {
        return new RestaurantBuilder();
    }

    public RestaurantBuilder withRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public RestaurantBuilder withCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
        return this;
    }

    public RestaurantBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public RestaurantBuilder withMenuItems(List<Map<String, Object>> menuItems) {
        this.menuItems = menuItems;
        return this;
    }

    public RestaurantBuilder addMenuItem(String menuItemId, String name, BigDecimal price) {
        Map<String, Object> item = new HashMap<>();
        item.put("menuItemId", menuItemId);
        item.put("name", name);
        item.put("price", price);
        this.menuItems.add(item);
        return this;
    }

    /**
     * Builds the restaurant as a Map representation.
     *
     * @return restaurant data as a Map
     */
    public Map<String, Object> build() {
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("restaurantId", restaurantId);
        restaurant.put("name", name);
        restaurant.put("address", address);
        restaurant.put("cuisineType", cuisineType);
        restaurant.put("createdAt", createdAt);
        restaurant.put("menuItems", new ArrayList<>(menuItems));
        return restaurant;
    }

    // --- Getters ---

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Map<String, Object>> getMenuItems() {
        return menuItems;
    }
}
