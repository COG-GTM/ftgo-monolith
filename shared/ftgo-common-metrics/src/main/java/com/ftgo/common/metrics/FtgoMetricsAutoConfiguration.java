package com.ftgo.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for FTGO platform observability.
 *
 * <p>Provides common metric bindings for all FTGO microservices including
 * JVM metrics, processor metrics, and uptime metrics. Custom common tags
 * are applied to all metrics for consistent labeling across services.</p>
 *
 * <p>This configuration is automatically activated when Micrometer is on
 * the classpath via Spring Boot auto-configuration.</p>
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class FtgoMetricsAutoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * Configures common tags applied to all metrics emitted by this service.
     * Tags include the application name and platform identifier.
     */
    @Bean
    public FtgoCommonTagsCustomizer ftgoCommonTagsCustomizer() {
        return new FtgoCommonTagsCustomizer(applicationName);
    }

    /**
     * Binds JVM memory metrics (heap, non-heap, buffer pools).
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Binds JVM garbage collection metrics.
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Binds JVM thread metrics (thread count, daemon threads, peak).
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Binds class loader metrics (loaded, unloaded classes).
     */
    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Binds processor metrics (CPU usage, available processors).
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Binds uptime metrics (process uptime, start time).
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }
}
