package com.ftgo.common.resilience.config;

import com.ftgo.common.resilience.circuitbreaker.CircuitBreakerConfiguration;
import com.ftgo.common.resilience.retry.RetryConfiguration;
import com.ftgo.common.resilience.bulkhead.BulkheadConfiguration;
import com.ftgo.common.resilience.ratelimiter.RateLimiterConfiguration;
import com.ftgo.common.resilience.health.HealthCheckConfiguration;
import com.ftgo.common.resilience.discovery.ServiceDiscoveryConfiguration;
import com.ftgo.common.resilience.shutdown.GracefulShutdownConfiguration;
import com.ftgo.common.resilience.metrics.ResilienceMetricsConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO resilience patterns.
 *
 * <p>Activates when {@code ftgo.resilience.enabled=true} (default).
 * Imports all resilience sub-configurations including circuit breakers,
 * retries, bulkheads, rate limiters, health checks, service discovery,
 * graceful shutdown, and metrics.</p>
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *   <li>{@code ftgo.resilience.enabled} - Enable/disable resilience (default: true)</li>
 *   <li>{@code ftgo.resilience.circuit-breaker.*} - Circuit breaker settings</li>
 *   <li>{@code ftgo.resilience.retry.*} - Retry settings</li>
 *   <li>{@code ftgo.resilience.bulkhead.*} - Bulkhead settings</li>
 *   <li>{@code ftgo.resilience.rate-limiter.*} - Rate limiter settings</li>
 *   <li>{@code ftgo.resilience.health.*} - Health check settings</li>
 *   <li>{@code ftgo.resilience.discovery.*} - Service discovery settings</li>
 *   <li>{@code ftgo.resilience.shutdown.*} - Graceful shutdown settings</li>
 * </ul>
 *
 * @see CircuitBreakerConfiguration
 * @see RetryConfiguration
 * @see BulkheadConfiguration
 * @see RateLimiterConfiguration
 * @see HealthCheckConfiguration
 * @see ServiceDiscoveryConfiguration
 * @see GracefulShutdownConfiguration
 * @see ResilienceMetricsConfiguration
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.resilience.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ResilienceProperties.class)
@Import({
    CircuitBreakerConfiguration.class,
    RetryConfiguration.class,
    BulkheadConfiguration.class,
    RateLimiterConfiguration.class,
    HealthCheckConfiguration.class,
    ServiceDiscoveryConfiguration.class,
    GracefulShutdownConfiguration.class,
    ResilienceMetricsConfiguration.class
})
public class FtgoResilienceAutoConfiguration {
}
