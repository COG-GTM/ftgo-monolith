package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Custom business metrics for the Restaurant Service.
 * Tracks restaurant creation and menu update events.
 */
public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter menusUpdated;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder("restaurants.created")
                .description("Total number of restaurants created")
                .register(registry);

        this.menusUpdated = Counter.builder("menus.updated")
                .description("Total number of menu updates")
                .register(registry);
    }

    public void recordRestaurantCreated() {
        restaurantsCreated.increment();
    }

    public void recordMenuUpdated() {
        menusUpdated.increment();
    }
}
