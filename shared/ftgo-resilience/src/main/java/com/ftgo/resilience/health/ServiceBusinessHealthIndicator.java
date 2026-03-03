package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Custom health indicator for business-level service health.
 * <p>
 * Provides an extensible base for service-specific business health checks.
 * By default, reports UP status. Services should extend or replace this bean
 * to add domain-specific health checks (e.g., order processing backlog,
 * payment gateway availability, menu sync status).
 * <p>
 * This indicator is registered under the name "ftgoServiceBusiness" in the health endpoint:
 * <pre>
 *   GET /actuator/health
 *   {
 *     "components": {
 *       "ftgoServiceBusiness": {
 *         "status": "UP",
 *         "details": {
 *           "service": "ftgo-service",
 *           "description": "Business health checks passed"
 *         }
 *       }
 *     }
 *   }
 * </pre>
 */
public class ServiceBusinessHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusinessHealthIndicator.class);

    private volatile boolean healthy = true;
    private volatile String statusMessage = "Business health checks passed";

    @Override
    public Health health() {
        if (healthy) {
            return Health.up()
                    .withDetail("description", statusMessage)
                    .build();
        } else {
            return Health.down()
                    .withDetail("description", statusMessage)
                    .build();
        }
    }

    /**
     * Set the business health status programmatically.
     * Services can call this to signal degraded business functionality.
     *
     * @param healthy       whether the service is healthy
     * @param statusMessage description of the current status
     */
    public void setHealthStatus(boolean healthy, String statusMessage) {
        this.healthy = healthy;
        this.statusMessage = statusMessage;
        if (!healthy) {
            log.warn("Business health status changed to DOWN: {}", statusMessage);
        } else {
            log.info("Business health status changed to UP: {}", statusMessage);
        }
    }
}
