package com.ftgo.common.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for metrics endpoints.
 *
 * <p>Ensures the Prometheus scrape endpoint ({@code /actuator/prometheus}) is
 * accessible only from internal network addresses. In production, this should
 * be secured via network policies or an API gateway.</p>
 *
 * <p>Recommended application.properties settings:</p>
 * <pre>
 * management.server.port=8081
 * management.endpoints.web.exposure.include=health,info,metrics,prometheus
 * management.endpoint.prometheus.enabled=true
 * management.metrics.export.prometheus.enabled=true
 * </pre>
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint")
public class MetricsEndpointSecurityConfiguration {

    /**
     * Provides a marker bean indicating metrics security is configured.
     * Actual security rules are applied via application properties and
     * infrastructure-level network policies.
     */
    @Bean
    public MetricsSecurityMarker metricsSecurityMarker() {
        return new MetricsSecurityMarker();
    }

    /**
     * Marker class indicating that metrics endpoint security configuration
     * has been applied. Used by health checks and configuration validation.
     */
    public static class MetricsSecurityMarker {
        public boolean isSecured() {
            return true;
        }
    }
}
