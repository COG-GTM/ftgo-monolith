package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for FTGO observability.
 * Applies common tags (application name, environment) to all metrics
 * so Prometheus/Grafana can filter and aggregate by service.
 */
@Configuration
public class ObservabilityAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(Environment environment) {
        String appName = environment.getProperty("spring.application.name", "unknown");
        return registry -> registry.config()
                .commonTags(
                        "application", appName,
                        "env", environment.getActiveProfiles().length > 0
                                ? environment.getActiveProfiles()[0]
                                : "default"
                );
    }
}
