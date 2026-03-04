package net.chrisrichardson.ftgo.metrics.restaurant;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Restaurant Service.
 *
 * <p>Tracks:
 * <ul>
 *   <li>{@code restaurants.created} — Counter of restaurants created</li>
 *   <li>{@code menus.updated} — Counter of menu update operations</li>
 * </ul>
 */
@Component
public class RestaurantMetrics {

    private static final Logger log = LoggerFactory.getLogger(RestaurantMetrics.class);

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

        log.info("Restaurant Service business metrics registered");
    }

    /**
     * Increments the restaurants created counter.
     */
    public void restaurantCreated() {
        restaurantsCreated.increment();
    }

    /**
     * Increments the menus updated counter.
     */
    public void menuUpdated() {
        menusUpdated.increment();
    }
}
