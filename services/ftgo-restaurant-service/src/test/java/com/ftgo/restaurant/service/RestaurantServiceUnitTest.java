package com.ftgo.restaurant.service;

import net.chrisrichardson.ftgo.testlib.builders.RestaurantBuilder;
import net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example unit test for the Restaurant bounded context.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>RestaurantBuilder usage for test data creation</li>
 *   <li>Menu item validation patterns</li>
 *   <li>Nested test organization by behavior</li>
 * </ul>
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Service - Unit Tests")
class RestaurantServiceUnitTest {

    @Nested
    @DisplayName("Restaurant Registration")
    class RestaurantRegistration {

        @Test
        @DisplayName("should create restaurant with default values")
        void shouldCreateRestaurantWithDefaults() {
            Map<String, Object> restaurant = RestaurantBuilder.aRestaurant().build();

            assertThat(restaurant.get("name")).isEqualTo("Ajanta");
            assertThat(restaurant.get("cuisineType")).isEqualTo("Indian");
            assertThat(restaurant.get("restaurantId")).isEqualTo(200L);
        }

        @Test
        @DisplayName("should create restaurant with custom values")
        void shouldCreateRestaurantWithCustomValues() {
            Map<String, Object> restaurant = RestaurantBuilder.aRestaurant()
                    .withName("Thai Palace")
                    .withCuisineType("Thai")
                    .withAddress("100 Thai St, Bangkok")
                    .build();

            assertThat(restaurant.get("name")).isEqualTo("Thai Palace");
            assertThat(restaurant.get("cuisineType")).isEqualTo("Thai");
        }
    }

    @Nested
    @DisplayName("Restaurant Menu")
    class RestaurantMenu {

        @Test
        @DisplayName("should have default menu items")
        @SuppressWarnings("unchecked")
        void shouldHaveDefaultMenuItems() {
            Map<String, Object> restaurant = RestaurantBuilder.aRestaurant().build();

            List<Map<String, Object>> menuItems =
                    (List<Map<String, Object>>) restaurant.get("menuItems");
            assertThat(menuItems).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("should allow adding custom menu items")
        @SuppressWarnings("unchecked")
        void shouldAllowAddingCustomMenuItems() {
            Map<String, Object> restaurant = RestaurantBuilder.aRestaurant()
                    .addMenuItem("MI-100", "Pad Thai", new BigDecimal("13.99"))
                    .build();

            List<Map<String, Object>> menuItems =
                    (List<Map<String, Object>>) restaurant.get("menuItems");
            assertThat(menuItems).hasSizeGreaterThanOrEqualTo(3);

            boolean hasPadThai = menuItems.stream()
                    .anyMatch(item -> "Pad Thai".equals(item.get("name")));
            assertThat(hasPadThai).isTrue();
        }
    }

    @Nested
    @DisplayName("Restaurant Address")
    class RestaurantAddress {

        @Test
        @DisplayName("should have valid address")
        void shouldHaveValidAddress() {
            Map<String, Object> restaurant = RestaurantBuilder.aRestaurant().build();

            FtgoAssertions.assertValidAddress(
                    (String) restaurant.get("address"));
        }
    }
}
