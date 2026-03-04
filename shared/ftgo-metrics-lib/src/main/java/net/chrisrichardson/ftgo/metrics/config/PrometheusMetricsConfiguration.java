package net.chrisrichardson.ftgo.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Prometheus meter registry.
 *
 * <p>Spring Boot Actuator auto-configures this when the Prometheus registry
 * is on the classpath. This configuration serves as a fallback and ensures
 * the registry is available for non-Boot applications or test contexts.
 */
@Configuration
@ConditionalOnClass(PrometheusMeterRegistry.class)
public class PrometheusMetricsConfiguration {

    /**
     * Creates a PrometheusMeterRegistry if one is not already present.
     * In a Spring Boot application, Actuator auto-configuration typically
     * provides this bean.
     */
    @Bean
    @ConditionalOnMissingBean(PrometheusMeterRegistry.class)
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
