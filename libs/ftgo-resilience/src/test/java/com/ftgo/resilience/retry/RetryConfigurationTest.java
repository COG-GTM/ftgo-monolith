package com.ftgo.resilience.retry;

import com.ftgo.resilience.config.ResilienceAutoConfiguration;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ResilienceAutoConfiguration.class));

    @Test
    void shouldCreateRetryRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RetryRegistry.class);
            assertThat(context).hasSingleBean(RetryConfig.class);
        });
    }

    @Test
    void shouldCreateNamedRetries() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("orderServiceRetry");
            assertThat(context).hasBean("restaurantServiceRetry");
            assertThat(context).hasBean("consumerServiceRetry");
            assertThat(context).hasBean("courierServiceRetry");
            assertThat(context).hasBean("externalPaymentRetry");
        });
    }

    @Test
    void shouldApplyDefaultConfiguration() {
        contextRunner.run(context -> {
            RetryConfig config = context.getBean(RetryConfig.class);
            assertThat(config.getMaxAttempts()).isEqualTo(3);
        });
    }

    @Test
    void shouldApplyCustomConfiguration() {
        contextRunner
                .withPropertyValues("ftgo.resilience.retry.max-attempts=5")
                .run(context -> {
                    RetryConfig config = context.getBean(RetryConfig.class);
                    assertThat(config.getMaxAttempts()).isEqualTo(5);
                });
    }

    @Test
    void shouldDisableRetryWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.retry.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RetryRegistry.class);
                });
    }

    @Test
    void retryShouldExecuteMultipleAttempts() {
        contextRunner.run(context -> {
            Retry retry = context.getBean("orderServiceRetry", Retry.class);
            AtomicInteger attempts = new AtomicInteger(0);

            assertThatThrownBy(() -> {
                Retry.decorateRunnable(retry, () -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("test failure");
                }).run();
            }).isInstanceOf(RuntimeException.class);

            assertThat(attempts.get()).isEqualTo(3);
        });
    }

    @Test
    void externalPaymentRetryShouldHaveMoreAttempts() {
        contextRunner.run(context -> {
            RetryRegistry registry = context.getBean(RetryRegistry.class);
            Retry externalRetry = registry.retry("externalPayment");
            assertThat(externalRetry.getRetryConfig().getMaxAttempts()).isEqualTo(5);
        });
    }
}
