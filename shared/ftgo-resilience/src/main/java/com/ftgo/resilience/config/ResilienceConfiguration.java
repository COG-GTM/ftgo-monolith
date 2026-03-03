package com.ftgo.resilience.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Default Resilience4j configuration for FTGO microservices.
 * <p>
 * Provides sensible defaults for:
 * <ul>
 *   <li><b>Circuit Breaker</b>: Opens after 5 consecutive failures, half-opens after 30s</li>
 *   <li><b>Retry</b>: 3 attempts with exponential backoff (1s, 2s, 4s)</li>
 *   <li><b>Bulkhead</b>: Limits concurrent calls to 25 per downstream service</li>
 *   <li><b>Rate Limiter</b>: 50 calls per second with 500ms timeout</li>
 * </ul>
 * <p>
 * Services can override these defaults via application.yml properties or by
 * defining their own registry beans.
 *
 * @see <a href="https://resilience4j.readme.io/docs">Resilience4j Documentation</a>
 */
@Configuration
public class ResilienceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfiguration.class);

    // -------------------------------------------------------------------------
    // Circuit Breaker Configuration
    // -------------------------------------------------------------------------

    /**
     * Default circuit breaker configuration.
     * <ul>
     *   <li>Sliding window: count-based, size 10</li>
     *   <li>Failure rate threshold: 50%</li>
     *   <li>Minimum number of calls: 5</li>
     *   <li>Wait duration in open state: 30 seconds</li>
     *   <li>Permitted calls in half-open state: 3</li>
     *   <li>Automatic transition from open to half-open: enabled</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        log.info("Initializing FTGO Circuit Breaker Registry with default configuration");
        return CircuitBreakerRegistry.of(config);
    }

    // -------------------------------------------------------------------------
    // Retry Configuration
    // -------------------------------------------------------------------------

    /**
     * Default retry configuration.
     * <ul>
     *   <li>Max attempts: 3</li>
     *   <li>Wait duration: 1 second (base)</li>
     *   <li>Exponential backoff multiplier: 2 (1s, 2s, 4s)</li>
     *   <li>Retries on: Exception.class</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(Exception.class)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfig config) {
        log.info("Initializing FTGO Retry Registry with default configuration");
        return RetryRegistry.of(config);
    }

    // -------------------------------------------------------------------------
    // Bulkhead Configuration
    // -------------------------------------------------------------------------

    /**
     * Default bulkhead configuration.
     * <ul>
     *   <li>Max concurrent calls: 25</li>
     *   <li>Max wait duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadConfig defaultBulkheadConfig() {
        return BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(BulkheadConfig config) {
        log.info("Initializing FTGO Bulkhead Registry with default configuration");
        return BulkheadRegistry.of(config);
    }

    // -------------------------------------------------------------------------
    // Rate Limiter Configuration
    // -------------------------------------------------------------------------

    /**
     * Default rate limiter configuration.
     * <ul>
     *   <li>Limit for period: 50 calls</li>
     *   <li>Limit refresh period: 1 second</li>
     *   <li>Timeout duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterConfig defaultRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(500))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig config) {
        log.info("Initializing FTGO Rate Limiter Registry with default configuration");
        return RateLimiterRegistry.of(config);
    }
}
