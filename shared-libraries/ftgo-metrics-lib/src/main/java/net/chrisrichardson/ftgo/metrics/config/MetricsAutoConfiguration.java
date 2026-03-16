package net.chrisrichardson.ftgo.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import net.chrisrichardson.ftgo.metrics.helper.BusinessMetricsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for FTGO metrics library.
 * Configures Micrometer with Prometheus registry, JVM metrics bindings,
 * and common tags for service identification.
 */
@Configuration
public class MetricsAutoConfiguration {

    @Value("${spring.application.name:ftgo-application}")
    private String serviceName;

    @Value("${ftgo.metrics.environment:local}")
    private String environment;

    /**
     * Adds common tags to all metrics for service identification.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return registry -> registry.config()
                .commonTags("application", serviceName)
                .commonTags("environment", environment);
    }

    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Provides a helper bean for recording business-level metrics.
     */
    @Bean
    public BusinessMetricsHelper businessMetricsHelper(MeterRegistry meterRegistry) {
        return new BusinessMetricsHelper(meterRegistry);
    }
}
