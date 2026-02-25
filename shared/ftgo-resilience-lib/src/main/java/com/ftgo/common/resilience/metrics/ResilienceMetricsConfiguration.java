package com.ftgo.common.resilience.metrics;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for exposing Resilience4j metrics via Micrometer.
 *
 * <p>Registers tagged metrics for all resilience patterns:</p>
 * <ul>
 *   <li>Circuit breaker metrics: state, failure rate, slow call rate, buffered calls</li>
 *   <li>Retry metrics: successful/failed calls with/without retry</li>
 *   <li>Bulkhead metrics: available concurrent calls, max allowed</li>
 *   <li>Rate limiter metrics: available permissions, waiting threads</li>
 * </ul>
 *
 * <p>Metrics are automatically exposed via the {@code /actuator/prometheus}
 * endpoint when Micrometer Prometheus registry is on the classpath.</p>
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class ResilienceMetricsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceMetricsConfiguration.class);

    /**
     * Binds circuit breaker metrics to Micrometer.
     */
    @Bean
    @ConditionalOnBean({CircuitBreakerRegistry.class, MeterRegistry.class})
    public TaggedCircuitBreakerMetrics taggedCircuitBreakerMetrics(
            CircuitBreakerRegistry circuitBreakerRegistry,
            MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics metrics = TaggedCircuitBreakerMetrics
                .ofCircuitBreakerRegistry(circuitBreakerRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience metrics: Circuit breaker metrics bound to Micrometer");
        return metrics;
    }

    /**
     * Binds retry metrics to Micrometer.
     */
    @Bean
    @ConditionalOnBean({RetryRegistry.class, MeterRegistry.class})
    public TaggedRetryMetrics taggedRetryMetrics(
            RetryRegistry retryRegistry,
            MeterRegistry meterRegistry) {
        TaggedRetryMetrics metrics = TaggedRetryMetrics.ofRetryRegistry(retryRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience metrics: Retry metrics bound to Micrometer");
        return metrics;
    }

    /**
     * Binds bulkhead metrics to Micrometer.
     */
    @Bean
    @ConditionalOnBean({BulkheadRegistry.class, MeterRegistry.class})
    public TaggedBulkheadMetrics taggedBulkheadMetrics(
            BulkheadRegistry bulkheadRegistry,
            MeterRegistry meterRegistry) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics
                .ofBulkheadRegistry(bulkheadRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience metrics: Bulkhead metrics bound to Micrometer");
        return metrics;
    }

    /**
     * Binds rate limiter metrics to Micrometer.
     */
    @Bean
    @ConditionalOnBean({RateLimiterRegistry.class, MeterRegistry.class})
    public TaggedRateLimiterMetrics taggedRateLimiterMetrics(
            RateLimiterRegistry rateLimiterRegistry,
            MeterRegistry meterRegistry) {
        TaggedRateLimiterMetrics metrics = TaggedRateLimiterMetrics
                .ofRateLimiterRegistry(rateLimiterRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience metrics: Rate limiter metrics bound to Micrometer");
        return metrics;
    }
}
