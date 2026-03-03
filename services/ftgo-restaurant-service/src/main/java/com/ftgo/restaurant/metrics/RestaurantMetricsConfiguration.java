package com.ftgo.restaurant.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for the FTGO Restaurant Service.
 *
 * <p>Configures common tags applied to all metrics emitted by this service,
 * and registers custom business metric beans.
 */
@Configuration
public class RestaurantMetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> restaurantServiceMetricsCustomizer() {
        return registry -> registry.config()
                .commonTags("service", "ftgo-restaurant-service")
                .commonTags("domain", "restaurant");
    }
}
