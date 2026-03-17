package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Restaurant Service.
 * Tracks restaurant creation and menu update events.
 */
@Component
public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter menusUpdated;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder("restaurants.created")
                .description("Total number of restaurants created")
                .tag("service", "restaurant-service")
                .register(registry);

        this.menusUpdated = Counter.builder("menus.updated")
                .description("Total number of menu updates performed")
                .tag("service", "restaurant-service")
                .register(registry);
    }

    /**
     * Record a new restaurant creation.
     */
    public void recordRestaurantCreated() {
        restaurantsCreated.increment();
    }

    /**
     * Record a menu update event.
     */
    public void recordMenuUpdated() {
        menusUpdated.increment();
    }
}
