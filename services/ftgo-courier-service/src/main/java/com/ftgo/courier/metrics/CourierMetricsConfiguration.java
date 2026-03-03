package com.ftgo.courier.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for the FTGO Courier Service.
 *
 * <p>Configures common tags applied to all metrics emitted by this service,
 * and registers custom business metric beans.
 */
@Configuration
public class CourierMetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> courierServiceMetricsCustomizer() {
        return registry -> registry.config()
                .commonTags("service", "ftgo-courier-service")
                .commonTags("domain", "courier");
    }
}
