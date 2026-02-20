package com.ftgo.resilience.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ResilienceAutoConfiguration.class));

    @Test
    void shouldLoadAutoConfigurationByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceProperties.class);
        });
    }

    @Test
    void shouldDisableAutoConfigurationWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ResilienceProperties.class);
                });
    }

    @Test
    void shouldBindDefaultProperties() {
        contextRunner.run(context -> {
            ResilienceProperties properties = context.getBean(ResilienceProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getCircuitBreaker().isEnabled()).isTrue();
            assertThat(properties.getRetry().isEnabled()).isTrue();
            assertThat(properties.getBulkhead().isEnabled()).isTrue();
            assertThat(properties.getHealthCheck().isEnabled()).isTrue();
            assertThat(properties.getDiscovery().isEnabled()).isTrue();
        });
    }

    @Test
    void shouldBindCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "ftgo.resilience.circuit-breaker.failure-rate-threshold=30",
                        "ftgo.resilience.retry.max-attempts=5",
                        "ftgo.resilience.bulkhead.max-concurrent-calls=50",
                        "ftgo.resilience.health-check.timeout-ms=10000",
                        "ftgo.resilience.discovery.type=eureka"
                )
                .run(context -> {
                    ResilienceProperties properties = context.getBean(ResilienceProperties.class);
                    assertThat(properties.getCircuitBreaker().getFailureRateThreshold()).isEqualTo(30);
                    assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
                    assertThat(properties.getBulkhead().getMaxConcurrentCalls()).isEqualTo(50);
                    assertThat(properties.getHealthCheck().getTimeoutMs()).isEqualTo(10000);
                    assertThat(properties.getDiscovery().getType()).isEqualTo("eureka");
                });
    }
}
