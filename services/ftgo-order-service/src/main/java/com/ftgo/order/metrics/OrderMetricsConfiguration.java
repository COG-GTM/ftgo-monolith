package com.ftgo.order.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for the FTGO Order Service.
 *
 * <p>Configures common tags applied to all metrics emitted by this service,
 * and registers custom business metric beans.
 */
@Configuration
public class OrderMetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> orderServiceMetricsCustomizer() {
        return registry -> registry.config()
                .commonTags("service", "ftgo-order-service")
                .commonTags("domain", "order");
    }
}
