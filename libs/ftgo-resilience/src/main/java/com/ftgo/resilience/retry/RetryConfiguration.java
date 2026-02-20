package com.ftgo.resilience.retry;

import com.ftgo.resilience.config.ResilienceProperties;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnClass(Retry.class)
@ConditionalOnProperty(prefix = "ftgo.resilience.retry", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RetryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RetryConfig defaultRetryConfig(ResilienceProperties properties) {
        ResilienceProperties.Retry retryProps = properties.getRetry();
        return RetryConfig.custom()
                .maxAttempts(retryProps.getMaxAttempts())
                .waitDuration(Duration.ofMillis(retryProps.getWaitDurationMs()))
                .retryExceptions(
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.client.ResourceAccessException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class
                )
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfig config) {
        return RetryRegistry.of(config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "retryMetrics")
    @ConditionalOnBean(MeterRegistry.class)
    public TaggedRetryMetrics retryMetrics(
            RetryRegistry registry,
            MeterRegistry meterRegistry) {
        TaggedRetryMetrics metrics = TaggedRetryMetrics.ofRetryRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean(name = "orderServiceRetry")
    public Retry orderServiceRetry(RetryRegistry registry) {
        return registry.retry("orderService");
    }

    @Bean(name = "restaurantServiceRetry")
    public Retry restaurantServiceRetry(RetryRegistry registry) {
        return registry.retry("restaurantService");
    }

    @Bean(name = "consumerServiceRetry")
    public Retry consumerServiceRetry(RetryRegistry registry) {
        return registry.retry("consumerService");
    }

    @Bean(name = "courierServiceRetry")
    public Retry courierServiceRetry(RetryRegistry registry) {
        return registry.retry("courierService");
    }

    @Bean(name = "externalPaymentRetry")
    public Retry externalPaymentRetry(RetryRegistry registry) {
        RetryConfig externalConfig = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class
                )
                .build();
        return registry.retry("externalPayment", externalConfig);
    }
}
