package com.ftgo.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(ObservabilityProperties.class)
public class PrometheusMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MeterRegistryCustomizer<MeterRegistry> commonTagsCustomizer(
            ObservabilityProperties properties, Environment environment) {
        String applicationName = properties.getApplicationName() != null
                ? properties.getApplicationName()
                : environment.getProperty("spring.application.name", "unknown");
        String environmentName = properties.getEnvironment() != null
                ? properties.getEnvironment()
                : environment.getProperty("ftgo.environment", "development");

        return registry -> registry.config()
                .commonTags(
                        "application", applicationName,
                        "env", environmentName
                );
    }

    @Bean
    @ConditionalOnMissingBean(JvmMemoryMetrics.class)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    @ConditionalOnMissingBean(JvmGcMetrics.class)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    @ConditionalOnMissingBean(JvmThreadMetrics.class)
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    @ConditionalOnMissingBean(ClassLoaderMetrics.class)
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    @Bean
    @ConditionalOnMissingBean(ProcessorMetrics.class)
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Bean
    @ConditionalOnMissingBean(UptimeMetrics.class)
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }
}
