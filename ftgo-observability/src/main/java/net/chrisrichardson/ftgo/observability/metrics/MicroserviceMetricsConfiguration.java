package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MicroserviceMetricsConfiguration {

    @Value("${spring.application.name:ftgo-application}")
    private String applicationName;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return registry -> registry.config()
                .commonTags(Arrays.asList(
                        Tag.of("application", applicationName),
                        Tag.of("framework", "spring-boot")
                ));
    }

    @Bean
    public MeterBinder httpRequestMetrics() {
        return registry -> {
            registry.counter("ftgo.http.requests.total",
                    "type", "all");
            registry.gauge("ftgo.service.health", 1.0);
        };
    }
}
