package com.ftgo.common.resilience;

import com.ftgo.common.resilience.circuitbreaker.CircuitBreakerConfiguration;
import com.ftgo.common.resilience.config.FtgoResilienceAutoConfiguration;
import com.ftgo.common.resilience.config.ResilienceProperties;
import com.ftgo.common.resilience.discovery.KubernetesServiceRegistry;
import com.ftgo.common.resilience.discovery.ServiceEndpointResolver;
import com.ftgo.common.resilience.shutdown.GracefulShutdownHandler;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FtgoResilienceAutoConfiguration}.
 */
class FtgoResilienceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoResilienceAutoConfiguration.class));

    @Test
    void autoConfigurationCreatesResilienceProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceProperties.class);
        });
    }

    @Test
    void autoConfigurationCreatesCircuitBreakerRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CircuitBreakerConfig.class);
            assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
        });
    }

    @Test
    void autoConfigurationCreatesRetryRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RetryRegistry.class);
        });
    }

    @Test
    void autoConfigurationCreatesBulkheadRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BulkheadRegistry.class);
        });
    }

    @Test
    void autoConfigurationCreatesRateLimiterRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RateLimiterRegistry.class);
        });
    }

    @Test
    void autoConfigurationCreatesServiceDiscovery() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(KubernetesServiceRegistry.class);
            assertThat(context).hasSingleBean(ServiceEndpointResolver.class);
        });
    }

    @Test
    void autoConfigurationCreatesGracefulShutdown() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GracefulShutdownHandler.class);
        });
    }

    @Test
    void autoConfigurationDisabledWhenPropertyFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CircuitBreakerRegistry.class);
                    assertThat(context).doesNotHaveBean(RetryRegistry.class);
                    assertThat(context).doesNotHaveBean(BulkheadRegistry.class);
                    assertThat(context).doesNotHaveBean(RateLimiterRegistry.class);
                });
    }

    @Test
    void circuitBreakerConfigUsesDefaultValues() {
        contextRunner.run(context -> {
            CircuitBreakerConfig config = context.getBean(CircuitBreakerConfig.class);
            assertThat(config.getFailureRateThreshold()).isEqualTo(50f);
            assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
            assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
            assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        });
    }

    @Test
    void circuitBreakerConfigCanBeCustomized() {
        contextRunner
                .withPropertyValues(
                        "ftgo.resilience.circuit-breaker.failure-rate-threshold=75",
                        "ftgo.resilience.circuit-breaker.wait-duration-in-open-state-seconds=60",
                        "ftgo.resilience.circuit-breaker.minimum-number-of-calls=10")
                .run(context -> {
                    CircuitBreakerConfig config = context.getBean(CircuitBreakerConfig.class);
                    assertThat(config.getFailureRateThreshold()).isEqualTo(75f);
                    assertThat(config.getMinimumNumberOfCalls()).isEqualTo(10);
                });
    }

    @Test
    void serviceDiscoveryUsesConfiguredNamespace() {
        contextRunner
                .withPropertyValues(
                        "ftgo.resilience.discovery.namespace=production",
                        "ftgo.resilience.discovery.cluster-domain=svc.cluster.local")
                .run(context -> {
                    KubernetesServiceRegistry registry = context.getBean(KubernetesServiceRegistry.class);
                    assertThat(registry.getNamespace()).isEqualTo("production");
                    assertThat(registry.getClusterDomain()).isEqualTo("svc.cluster.local");
                });
    }

    @Test
    void serviceDiscoveryResolvesUrls() {
        contextRunner.run(context -> {
            ServiceEndpointResolver resolver = context.getBean(ServiceEndpointResolver.class);
            String url = resolver.resolveUrl("order-service", "/api/orders");
            assertThat(url).contains("order-service");
            assertThat(url).endsWith("/api/orders");
        });
    }

    @Test
    void gracefulShutdownUsesConfiguredTimeout() {
        contextRunner
                .withPropertyValues("ftgo.resilience.shutdown.timeout-seconds=60")
                .run(context -> {
                    GracefulShutdownHandler handler = context.getBean(GracefulShutdownHandler.class);
                    assertThat(handler.getTimeoutSeconds()).isEqualTo(60);
                    assertThat(handler.isShuttingDown()).isFalse();
                });
    }
}
