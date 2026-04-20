package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonMetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(
            @Value("${spring.application.name:unknown}") String serviceName,
            @Value("${ftgo.metrics.environment:development}") String environment) {
        return registry -> registry.config()
                .commonTags(
                        "service", serviceName,
                        "environment", environment
                );
    }
}
