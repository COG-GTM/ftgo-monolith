package com.ftgo.common.metrics.restaurant;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Custom business metrics for the Restaurant Service.
 *
 * <p>Metrics provided:</p>
 * <ul>
 *   <li>{@code restaurants.created} - Counter of restaurants created</li>
 *   <li>{@code menus.updated} - Counter of menu updates performed</li>
 * </ul>
 *
 * <p>Activate by setting {@code ftgo.metrics.restaurant.enabled=true} in application properties.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.metrics.restaurant.enabled", havingValue = "true", matchIfMissing = false)
public class RestaurantMetrics {

    private final MeterRegistry meterRegistry;

    private Counter restaurantsCreated;
    private Counter menusUpdated;

    public RestaurantMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        restaurantsCreated = Counter.builder("restaurants.created")
                .description("Total number of restaurants created")
                .tag("service", "restaurant-service")
                .register(meterRegistry);

        menusUpdated = Counter.builder("menus.updated")
                .description("Total number of menu updates performed")
                .tag("service", "restaurant-service")
                .register(meterRegistry);
    }

    /**
     * Records a new restaurant creation.
     */
    public void recordRestaurantCreated() {
        restaurantsCreated.increment();
    }

    /**
     * Records a menu update event.
     */
    public void recordMenuUpdated() {
        menusUpdated.increment();
    }
}
