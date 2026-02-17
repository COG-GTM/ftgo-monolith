package net.chrisrichardson.ftgo.observability;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.observability.metrics.FtgoMetricsNamingConvention;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FtgoMetricsConfiguration {

    @Value("${spring.application.name:ftgo-unknown}")
    private String applicationName;

    @Value("${ftgo.metrics.environment:local}")
    private String environment;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> ftgoCommonTags() {
        return registry -> {
            registry.config()
                    .namingConvention(new FtgoMetricsNamingConvention())
                    .commonTags(
                            "application", applicationName,
                            "environment", environment
                    );
        };
    }
}
