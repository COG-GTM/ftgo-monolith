package net.chrisrichardson.ftgo.resilience.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.bulkhead.FtgoBulkheadConfiguration;
import net.chrisrichardson.ftgo.resilience.circuitbreaker.FtgoCircuitBreakerConfiguration;
import net.chrisrichardson.ftgo.resilience.discovery.KubernetesServiceDiscoveryProperties;
import net.chrisrichardson.ftgo.resilience.health.DownstreamServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.ratelimiter.FtgoRateLimiterConfiguration;
import net.chrisrichardson.ftgo.resilience.retry.FtgoRetryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO resilience patterns.
 *
 * <p>Configures:
 * <ul>
 *   <li>Circuit breaker (Resilience4j) with configurable thresholds</li>
 *   <li>Retry with exponential backoff</li>
 *   <li>Bulkhead for concurrent call isolation</li>
 *   <li>Rate limiter for downstream protection</li>
 *   <li>Health indicators for downstream services</li>
 *   <li>K8s-native service discovery via DNS</li>
 *   <li>Resilience metrics exposed via Micrometer</li>
 * </ul>
 *
 * <p>Activated when {@code ftgo.resilience.enabled=true} (default).
 */
@Configuration
@ConditionalOnClass({CircuitBreakerRegistry.class, RetryRegistry.class})
@ConditionalOnProperty(prefix = "ftgo.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({FtgoResilienceProperties.class, KubernetesServiceDiscoveryProperties.class})
@Import({
        FtgoCircuitBreakerConfiguration.class,
        FtgoRetryConfiguration.class,
        FtgoBulkheadConfiguration.class,
        FtgoRateLimiterConfiguration.class,
        FtgoResilienceMetricsConfiguration.class,
        DownstreamServiceHealthIndicator.class
})
public class FtgoResilienceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoResilienceAutoConfiguration.class);

    public FtgoResilienceAutoConfiguration(FtgoResilienceProperties properties) {
        log.info("FTGO Resilience auto-configuration initialized. "
                        + "Circuit breaker failure threshold={}%, "
                        + "Retry max attempts={}, "
                        + "Bulkhead max concurrent={}, "
                        + "Rate limiter limit/period={}",
                properties.getCircuitBreaker().getFailureRateThreshold(),
                properties.getRetry().getMaxAttempts(),
                properties.getBulkhead().getMaxConcurrentCalls(),
                properties.getRateLimiter().getLimitForPeriod());
    }
}
