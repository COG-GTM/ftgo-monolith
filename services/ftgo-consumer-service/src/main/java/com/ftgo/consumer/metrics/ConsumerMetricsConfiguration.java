package com.ftgo.consumer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for the FTGO Consumer Service.
 *
 * <p>Configures common tags applied to all metrics emitted by this service,
 * and registers custom business metric beans.
 */
@Configuration
public class ConsumerMetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> consumerServiceMetricsCustomizer() {
        return registry -> registry.config()
                .commonTags("service", "ftgo-consumer-service")
                .commonTags("domain", "consumer");
    }
}
