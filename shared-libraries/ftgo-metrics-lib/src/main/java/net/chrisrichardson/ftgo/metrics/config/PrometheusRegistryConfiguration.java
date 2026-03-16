package net.chrisrichardson.ftgo.metrics.config;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus registry configuration for FTGO services.
 * Ensures a PrometheusMeterRegistry is available and configures
 * the /actuator/prometheus endpoint for scraping.
 */
@Configuration
public class PrometheusRegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean(PrometheusMeterRegistry.class)
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
