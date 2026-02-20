package com.ftgo.resilience.circuitbreaker;

import com.ftgo.resilience.config.ResilienceAutoConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ResilienceAutoConfiguration.class));

    @Test
    void shouldCreateCircuitBreakerRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
            assertThat(context).hasSingleBean(CircuitBreakerConfig.class);
        });
    }

    @Test
    void shouldCreateNamedCircuitBreakers() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("orderServiceCircuitBreaker");
            assertThat(context).hasBean("restaurantServiceCircuitBreaker");
            assertThat(context).hasBean("consumerServiceCircuitBreaker");
            assertThat(context).hasBean("courierServiceCircuitBreaker");
            assertThat(context).hasBean("externalPaymentCircuitBreaker");
        });
    }

    @Test
    void shouldApplyDefaultConfiguration() {
        contextRunner.run(context -> {
            CircuitBreakerConfig config = context.getBean(CircuitBreakerConfig.class);
            assertThat(config.getFailureRateThreshold()).isEqualTo(50.0f);
            assertThat(config.getSlidingWindowSize()).isEqualTo(10);
            assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
        });
    }

    @Test
    void shouldApplyCustomConfiguration() {
        contextRunner
                .withPropertyValues(
                        "ftgo.resilience.circuit-breaker.failure-rate-threshold=30",
                        "ftgo.resilience.circuit-breaker.sliding-window-size=20"
                )
                .run(context -> {
                    CircuitBreakerConfig config = context.getBean(CircuitBreakerConfig.class);
                    assertThat(config.getFailureRateThreshold()).isEqualTo(30.0f);
                    assertThat(config.getSlidingWindowSize()).isEqualTo(20);
                });
    }

    @Test
    void shouldDisableCircuitBreakerWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.circuit-breaker.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CircuitBreakerRegistry.class);
                });
    }

    @Test
    void circuitBreakerShouldTransitionThroughStates() {
        contextRunner.run(context -> {
            CircuitBreaker cb = context.getBean("orderServiceCircuitBreaker", CircuitBreaker.class);
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

            for (int i = 0; i < 10; i++) {
                cb.onError(0, java.util.concurrent.TimeUnit.MILLISECONDS,
                        new RuntimeException("test"));
            }

            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        });
    }

    @Test
    void externalPaymentCircuitBreakerShouldHaveStricterConfig() {
        contextRunner.run(context -> {
            CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
            CircuitBreaker externalCb = registry.circuitBreaker("externalPayment");
            CircuitBreakerConfig config = externalCb.getCircuitBreakerConfig();
            assertThat(config.getFailureRateThreshold()).isEqualTo(30.0f);
            assertThat(config.getSlidingWindowSize()).isEqualTo(20);
            assertThat(config.getMinimumNumberOfCalls()).isEqualTo(10);
        });
    }
}
