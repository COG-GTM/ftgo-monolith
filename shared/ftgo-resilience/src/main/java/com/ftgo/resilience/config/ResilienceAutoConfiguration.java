package com.ftgo.resilience.config;

import com.ftgo.resilience.health.DatabaseHealthIndicator;
import com.ftgo.resilience.health.DependentServiceHealthIndicator;
import com.ftgo.resilience.health.DiskSpaceHealthIndicator;
import com.ftgo.resilience.health.ServiceBusinessHealthIndicator;
import com.ftgo.resilience.discovery.KubernetesServiceDiscovery;
import com.ftgo.resilience.shutdown.GracefulShutdownConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO Resilience library.
 * <p>
 * Automatically configures:
 * <ul>
 *   <li>Custom health indicators (DB, disk space, business health, dependent services)</li>
 *   <li>Resilience4j circuit breaker, retry, bulkhead, and rate limiter defaults</li>
 *   <li>Kubernetes DNS-based service discovery</li>
 *   <li>Graceful shutdown with connection draining</li>
 *   <li>Micrometer metrics integration for resilience patterns</li>
 * </ul>
 * <p>
 * Include this library as a dependency and it will auto-configure via Spring Boot's
 * spring.factories mechanism.
 */
@Configuration
@Import({
    ResilienceConfiguration.class,
    HealthIndicatorConfiguration.class,
    KubernetesServiceDiscovery.class,
    GracefulShutdownConfiguration.class
})
public class ResilienceAutoConfiguration {
}
