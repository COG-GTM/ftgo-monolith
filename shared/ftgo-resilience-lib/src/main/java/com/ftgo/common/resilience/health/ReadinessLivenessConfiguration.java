package com.ftgo.common.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpointGroup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Kubernetes readiness and liveness probe support.
 *
 * <p>Configures Spring Boot Actuator health groups for Kubernetes probes:</p>
 * <ul>
 *   <li>{@code /actuator/health/readiness} - Readiness probe (includes DB, downstream services)</li>
 *   <li>{@code /actuator/health/liveness} - Liveness probe (basic application health)</li>
 * </ul>
 *
 * <p>These endpoints are configured via application properties:</p>
 * <pre>
 * management.endpoint.health.probes.enabled=true
 * management.health.livenessstate.enabled=true
 * management.health.readinessstate.enabled=true
 * management.endpoint.health.group.readiness.include=readinessState,db,downstreamService
 * management.endpoint.health.group.liveness.include=livenessState
 * </pre>
 *
 * <p>The actual probe group configuration is done via properties in
 * {@code application-resilience.properties} since Spring Boot requires
 * property-based health group configuration.</p>
 */
@Configuration
@ConditionalOnClass(HealthEndpointGroup.class)
public class ReadinessLivenessConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReadinessLivenessConfiguration.class);

    /**
     * Logs that readiness/liveness probe configuration is active.
     * The actual configuration is in application-resilience.properties.
     */
    public ReadinessLivenessConfiguration() {
        log.info("FTGO Readiness/Liveness probe configuration active. "
                + "Endpoints: /actuator/health/readiness, /actuator/health/liveness");
    }
}
