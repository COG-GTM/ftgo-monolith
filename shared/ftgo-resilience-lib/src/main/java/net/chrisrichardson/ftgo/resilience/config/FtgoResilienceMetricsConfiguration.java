package net.chrisrichardson.ftgo.resilience.config;

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
 * Configures Micrometer metrics for all Resilience4j components.
 *
 * <p>Exposes resilience metrics via Micrometer so they are automatically
 * available at {@code /actuator/prometheus} when used with the ftgo-metrics-lib.
 *
 * <p>Metrics include:
 * <ul>
 *   <li>Circuit breaker state, failure rate, call counts</li>
 *   <li>Retry attempt counts, success/failure rates</li>
 *   <li>Bulkhead available permits, active calls</li>
 *   <li>Rate limiter available permissions, waiting threads</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class FtgoResilienceMetricsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoResilienceMetricsConfiguration.class);

    @Bean
    @ConditionalOnBean(CircuitBreakerRegistry.class)
    public TaggedCircuitBreakerMetrics taggedCircuitBreakerMetrics(
            CircuitBreakerRegistry circuitBreakerRegistry,
            MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience: Circuit breaker metrics bound to MeterRegistry");
        return metrics;
    }

    @Bean
    @ConditionalOnBean(RetryRegistry.class)
    public TaggedRetryMetrics taggedRetryMetrics(
            RetryRegistry retryRegistry,
            MeterRegistry meterRegistry) {
        TaggedRetryMetrics metrics = TaggedRetryMetrics.ofRetryRegistry(retryRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience: Retry metrics bound to MeterRegistry");
        return metrics;
    }

    @Bean
    @ConditionalOnBean(BulkheadRegistry.class)
    public TaggedBulkheadMetrics taggedBulkheadMetrics(
            BulkheadRegistry bulkheadRegistry,
            MeterRegistry meterRegistry) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience: Bulkhead metrics bound to MeterRegistry");
        return metrics;
    }

    @Bean
    @ConditionalOnBean(RateLimiterRegistry.class)
    public TaggedRateLimiterMetrics taggedRateLimiterMetrics(
            RateLimiterRegistry rateLimiterRegistry,
            MeterRegistry meterRegistry) {
        TaggedRateLimiterMetrics metrics = TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry);
        metrics.bindTo(meterRegistry);
        log.info("FTGO Resilience: Rate limiter metrics bound to MeterRegistry");
        return metrics;
    }
}
