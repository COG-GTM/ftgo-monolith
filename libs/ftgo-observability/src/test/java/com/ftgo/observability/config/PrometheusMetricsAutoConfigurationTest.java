package com.ftgo.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PrometheusMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PrometheusMetricsAutoConfiguration.class))
            .withUserConfiguration(TestMeterRegistryConfig.class);

    @Test
    void shouldCreateJvmMetricsBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics.class);
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.jvm.JvmGcMetrics.class);
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics.class);
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics.class);
        });
    }

    @Test
    void shouldCreateSystemMetricsBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.system.ProcessorMetrics.class);
            assertThat(context).hasSingleBean(io.micrometer.core.instrument.binder.system.UptimeMetrics.class);
        });
    }

    @Test
    void shouldApplyCommonTags() {
        contextRunner
                .withPropertyValues(
                        "spring.application.name=test-service",
                        "ftgo.observability.application-name=test-service",
                        "ftgo.observability.environment=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MeterRegistry.class);
                });
    }

    @Configuration
    static class TestMeterRegistryConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
