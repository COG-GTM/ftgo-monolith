package com.ftgo.common.resilience.health;

import com.ftgo.common.resilience.config.ResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for FTGO health checks.
 *
 * <p>Configures Spring Boot Actuator health indicators including:</p>
 * <ul>
 *   <li>Custom business health indicators</li>
 *   <li>Downstream service health checks</li>
 *   <li>Readiness and liveness probe group configuration</li>
 * </ul>
 *
 * <p>Standard health indicators (DataSource, DiskSpace) are provided
 * by Spring Boot Actuator auto-configuration.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.resilience.health.enabled", havingValue = "true", matchIfMissing = true)
@Import({ReadinessLivenessConfiguration.class})
public class HealthCheckConfiguration {

    /**
     * Custom business health indicator that checks the overall
     * service readiness including circuit breaker states.
     */
    @Bean
    public ServiceHealthIndicator serviceHealthIndicator() {
        return new ServiceHealthIndicator();
    }

    /**
     * Downstream service health indicator that checks connectivity
     * to dependent services.
     */
    @Bean
    @ConditionalOnProperty(name = "ftgo.resilience.health.downstream-checks-enabled",
            havingValue = "true", matchIfMissing = true)
    public DownstreamServiceHealthIndicator downstreamServiceHealthIndicator(
            ResilienceProperties properties) {
        return new DownstreamServiceHealthIndicator(properties);
    }

    /**
     * Circuit breaker health indicator that reports circuit breaker states.
     */
    @Bean
    public CircuitBreakerHealthIndicator circuitBreakerHealthIndicator() {
        return new CircuitBreakerHealthIndicator();
    }
}
