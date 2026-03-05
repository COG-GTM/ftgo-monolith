package net.chrisrichardson.ftgo.testlib.builders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RestaurantBuilder}.
 */
@DisplayName("RestaurantBuilder")
class RestaurantBuilderTest {

    @Test
    @DisplayName("should create restaurant with defaults")
    void shouldCreateRestaurantWithDefaults() {
        Map<String, Object> restaurant = RestaurantBuilder.aRestaurant().build();

        assertThat(restaurant).containsKeys("restaurantId", "name", "address",
                "cuisineType", "createdAt", "menuItems");
        assertThat(restaurant.get("name")).isEqualTo("Ajanta");
        assertThat(restaurant.get("cuisineType")).isEqualTo("Indian");
    }

    @Test
    @DisplayName("should have default menu items")
    @SuppressWarnings("unchecked")
    void shouldHaveDefaultMenuItems() {
        Map<String, Object> restaurant = RestaurantBuilder.aRestaurant().build();

        List<Map<String, Object>> menuItems = (List<Map<String, Object>>) restaurant.get("menuItems");
        assertThat(menuItems).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("should add custom menu items")
    @SuppressWarnings("unchecked")
    void shouldAddCustomMenuItems() {
        Map<String, Object> restaurant = RestaurantBuilder.aRestaurant()
                .addMenuItem("MI-010", "Tandoori Chicken", new BigDecimal("12.99"))
                .build();

        List<Map<String, Object>> menuItems = (List<Map<String, Object>>) restaurant.get("menuItems");
        assertThat(menuItems).hasSizeGreaterThanOrEqualTo(3);
    }
}
