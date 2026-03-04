package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.metrics.restaurant.RestaurantMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link RestaurantMetrics}.
 */
class RestaurantMetricsTest {

    private MeterRegistry registry;
    private RestaurantMetrics restaurantMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        restaurantMetrics = new RestaurantMetrics(registry);
    }

    @Test
    void shouldIncrementRestaurantsCreatedCounter() {
        restaurantMetrics.restaurantCreated();

        double count = registry.counter("restaurants.created", "service", "restaurant-service").count();
        assertEquals(1.0, count);
    }

    @Test
    void shouldIncrementMenusUpdatedCounter() {
        restaurantMetrics.menuUpdated();
        restaurantMetrics.menuUpdated();

        double count = registry.counter("menus.updated", "service", "restaurant-service").count();
        assertEquals(2.0, count);
    }
}
