package net.chrisrichardson.ftgo.resilience.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.bulkhead.BulkheadConfigurationProperties;
import net.chrisrichardson.ftgo.resilience.bulkhead.BulkheadFactory;
import net.chrisrichardson.ftgo.resilience.circuitbreaker.CircuitBreakerConfigurationProperties;
import net.chrisrichardson.ftgo.resilience.circuitbreaker.CircuitBreakerFactory;
import net.chrisrichardson.ftgo.resilience.ratelimiter.RateLimiterConfigurationProperties;
import net.chrisrichardson.ftgo.resilience.ratelimiter.RateLimiterFactory;
import net.chrisrichardson.ftgo.resilience.retry.RetryConfigurationProperties;
import net.chrisrichardson.ftgo.resilience.retry.RetryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Auto-configuration for the FTGO resilience library.
 * Creates default registries and factory beans for circuit breakers,
 * retries, bulkheads, and rate limiters using configurable properties.
 *
 * <p>Services can override defaults by providing their own
 * {@link CircuitBreakerConfigurationProperties}, {@link RetryConfigurationProperties},
 * {@link BulkheadConfigurationProperties}, or {@link RateLimiterConfigurationProperties}
 * beans, or by using the factory methods that accept custom properties.</p>
 */
@Configuration
public class ResilienceAutoConfiguration {

    @Value("${ftgo.resilience.circuitbreaker.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${ftgo.resilience.circuitbreaker.ring-buffer-size-closed:10}")
    private int ringBufferSizeInClosedState;

    @Value("${ftgo.resilience.circuitbreaker.ring-buffer-size-half-open:5}")
    private int ringBufferSizeInHalfOpenState;

    @Value("${ftgo.resilience.circuitbreaker.wait-duration-open-ms:30000}")
    private long waitDurationInOpenStateMillis;

    @Value("${ftgo.resilience.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${ftgo.resilience.retry.initial-interval-ms:500}")
    private long retryInitialIntervalMillis;

    @Value("${ftgo.resilience.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Value("${ftgo.resilience.bulkhead.max-concurrent-calls:25}")
    private int bulkheadMaxConcurrentCalls;

    @Value("${ftgo.resilience.bulkhead.max-wait-time-ms:500}")
    private long bulkheadMaxWaitTimeMillis;

    @Value("${ftgo.resilience.ratelimiter.limit-for-period:50}")
    private int rateLimiterLimitForPeriod;

    @Value("${ftgo.resilience.ratelimiter.limit-refresh-period-ms:1000}")
    private long rateLimiterRefreshPeriodMillis;

    @Value("${ftgo.resilience.ratelimiter.timeout-duration-ms:500}")
    private long rateLimiterTimeoutMillis;

    // --- Circuit Breaker ---

    @Bean
    public CircuitBreakerConfigurationProperties circuitBreakerConfigurationProperties() {
        CircuitBreakerConfigurationProperties props = new CircuitBreakerConfigurationProperties();
        props.setFailureRateThreshold(failureRateThreshold);
        props.setRingBufferSizeInClosedState(ringBufferSizeInClosedState);
        props.setRingBufferSizeInHalfOpenState(ringBufferSizeInHalfOpenState);
        props.setWaitDurationInOpenStateMillis(waitDurationInOpenStateMillis);
        return props;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .ringBufferSizeInClosedState(ringBufferSizeInClosedState)
                .ringBufferSizeInHalfOpenState(ringBufferSizeInHalfOpenState)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenStateMillis))
                .build();
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    public CircuitBreakerFactory circuitBreakerFactory(CircuitBreakerRegistry registry) {
        return new CircuitBreakerFactory(registry);
    }

    // --- Retry ---

    @Bean
    public RetryConfigurationProperties retryConfigurationProperties() {
        RetryConfigurationProperties props = new RetryConfigurationProperties();
        props.setMaxAttempts(retryMaxAttempts);
        props.setInitialIntervalMillis(retryInitialIntervalMillis);
        props.setMultiplier(retryMultiplier);
        return props;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(
                retryInitialIntervalMillis, retryMultiplier);

        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .intervalFunction(intervalFunction)
                .build();
        return RetryRegistry.of(defaultConfig);
    }

    @Bean
    public RetryFactory retryFactory(RetryRegistry registry) {
        return new RetryFactory(registry);
    }

    // --- Bulkhead ---

    @Bean
    public BulkheadConfigurationProperties bulkheadConfigurationProperties() {
        BulkheadConfigurationProperties props = new BulkheadConfigurationProperties();
        props.setMaxConcurrentCalls(bulkheadMaxConcurrentCalls);
        props.setMaxWaitTimeMillis(bulkheadMaxWaitTimeMillis);
        return props;
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(bulkheadMaxConcurrentCalls)
                .maxWaitTime(bulkheadMaxWaitTimeMillis)
                .build();
        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public BulkheadFactory bulkheadFactory(BulkheadRegistry registry) {
        return new BulkheadFactory(registry);
    }

    // --- Rate Limiter ---

    @Bean
    public RateLimiterConfigurationProperties rateLimiterConfigurationProperties() {
        RateLimiterConfigurationProperties props = new RateLimiterConfigurationProperties();
        props.setLimitForPeriod(rateLimiterLimitForPeriod);
        props.setLimitRefreshPeriodMillis(rateLimiterRefreshPeriodMillis);
        props.setTimeoutDurationMillis(rateLimiterTimeoutMillis);
        return props;
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(rateLimiterLimitForPeriod)
                .limitRefreshPeriod(Duration.ofMillis(rateLimiterRefreshPeriodMillis))
                .timeoutDuration(Duration.ofMillis(rateLimiterTimeoutMillis))
                .build();
        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public RateLimiterFactory rateLimiterFactory(RateLimiterRegistry registry) {
        return new RateLimiterFactory(registry);
    }
}
