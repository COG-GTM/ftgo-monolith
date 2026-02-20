package com.ftgo.resilience.bulkhead;

import com.ftgo.resilience.config.ResilienceAutoConfiguration;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BulkheadConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ResilienceAutoConfiguration.class));

    @Test
    void shouldCreateBulkheadRegistry() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BulkheadRegistry.class);
            assertThat(context).hasSingleBean(BulkheadConfig.class);
        });
    }

    @Test
    void shouldCreateNamedBulkheads() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("orderServiceBulkhead");
            assertThat(context).hasBean("restaurantServiceBulkhead");
            assertThat(context).hasBean("consumerServiceBulkhead");
            assertThat(context).hasBean("courierServiceBulkhead");
            assertThat(context).hasBean("externalPaymentBulkhead");
        });
    }

    @Test
    void shouldApplyDefaultConfiguration() {
        contextRunner.run(context -> {
            BulkheadConfig config = context.getBean(BulkheadConfig.class);
            assertThat(config.getMaxConcurrentCalls()).isEqualTo(25);
        });
    }

    @Test
    void shouldApplyCustomConfiguration() {
        contextRunner
                .withPropertyValues("ftgo.resilience.bulkhead.max-concurrent-calls=50")
                .run(context -> {
                    BulkheadConfig config = context.getBean(BulkheadConfig.class);
                    assertThat(config.getMaxConcurrentCalls()).isEqualTo(50);
                });
    }

    @Test
    void shouldDisableBulkheadWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.bulkhead.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(BulkheadRegistry.class);
                });
    }

    @Test
    void bulkheadShouldLimitConcurrentCalls() {
        contextRunner
                .withPropertyValues("ftgo.resilience.bulkhead.max-concurrent-calls=2")
                .run(context -> {
                    Bulkhead bulkhead = context.getBean("orderServiceBulkhead", Bulkhead.class);
                    assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(2);

                    bulkhead.acquirePermission();
                    assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(1);

                    bulkhead.releasePermission();
                    assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(2);
                });
    }

    @Test
    void externalPaymentBulkheadShouldHaveStricterLimits() {
        contextRunner.run(context -> {
            BulkheadRegistry registry = context.getBean(BulkheadRegistry.class);
            Bulkhead externalBulkhead = registry.bulkhead("externalPayment");
            assertThat(externalBulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(10);
        });
    }
}
