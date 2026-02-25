package com.ftgo.common.resilience.retry;

import com.ftgo.common.resilience.config.ResilienceProperties;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Resilience4j retry policies.
 *
 * <p>Provides a default {@link RetryRegistry} with FTGO-standard settings:
 * 3 attempts with exponential backoff (1s, 2s, 4s).</p>
 *
 * <p>Usage with annotations:</p>
 * <pre>
 * &#64;Retry(name = "consumer-service", fallbackMethod = "fallback")
 * public Consumer getConsumer(long consumerId) { ... }
 * </pre>
 */
@Configuration
@ConditionalOnClass(Retry.class)
public class RetryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RetryConfiguration.class);

    /**
     * Creates a default {@link RetryConfig} with exponential backoff.
     *
     * <ul>
     *   <li>Max attempts: 3</li>
     *   <li>Initial interval: 1000ms</li>
     *   <li>Multiplier: 2.0 (backoff: 1s, 2s, 4s)</li>
     *   <li>Retries on: {@link java.io.IOException}, {@link java.util.concurrent.TimeoutException}</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(RetryConfig.class)
    public RetryConfig defaultRetryConfig(ResilienceProperties properties) {
        ResilienceProperties.RetryProperties retryProps = properties.getRetry();

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProps.getMaxAttempts())
                .intervalFunction(io.github.resilience4j.core.IntervalFunction
                        .ofExponentialBackoff(
                                retryProps.getInitialIntervalMillis(),
                                retryProps.getMultiplier()))
                .retryExceptions(
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.client.ResourceAccessException.class)
                .ignoreExceptions(
                        IllegalArgumentException.class)
                .build();

        log.info("FTGO Retry configured: maxAttempts={}, initialInterval={}ms, multiplier={}",
                retryProps.getMaxAttempts(),
                retryProps.getInitialIntervalMillis(),
                retryProps.getMultiplier());

        return config;
    }

    /**
     * Creates a {@link RetryRegistry} with the default configuration.
     */
    @Bean
    @ConditionalOnMissingBean(RetryRegistry.class)
    public RetryRegistry retryRegistry(RetryConfig defaultRetryConfig) {
        return RetryRegistry.of(defaultRetryConfig);
    }
}
