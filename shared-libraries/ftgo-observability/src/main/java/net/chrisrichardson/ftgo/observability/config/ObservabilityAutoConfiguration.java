package net.chrisrichardson.ftgo.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for FTGO observability.
 * Applies service-level tags to all metrics for multi-service Prometheus scraping.
 *
 * JVM and system metric binders (heap, GC, threads, CPU, etc.) are already
 * auto-configured by Spring Boot Actuator and do not need to be registered here.
 */
@Configuration
public class ObservabilityAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(Environment environment) {
        String appName = environment.getProperty("spring.application.name", "unknown");
        return registry -> registry.config()
                .commonTags("application", appName);
    }
}
