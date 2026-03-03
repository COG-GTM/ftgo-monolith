package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom health indicator for dependent service availability.
 * <p>
 * Checks the health of downstream services by calling their health endpoints.
 * Services register their dependencies at startup, and this indicator periodically
 * verifies that all downstream services are reachable.
 * <p>
 * In Kubernetes, services are discovered via DNS:
 * <pre>
 *   http://ftgo-order-service.ftgo.svc.cluster.local:8080/actuator/health
 * </pre>
 * <p>
 * This indicator is registered under the name "ftgoDependentService" in the health endpoint:
 * <pre>
 *   GET /actuator/health
 *   {
 *     "components": {
 *       "ftgoDependentService": {
 *         "status": "UP",
 *         "details": {
 *           "order-service": "UP",
 *           "consumer-service": "UP"
 *         }
 *       }
 *     }
 *   }
 * </pre>
 */
public class DependentServiceHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DependentServiceHealthIndicator.class);

    private final Map<String, String> dependentServices = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;

    public DependentServiceHealthIndicator() {
        this.restTemplate = new RestTemplate();
    }

    public DependentServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Register a dependent service for health monitoring.
     *
     * @param serviceName  logical name of the dependent service
     * @param healthUrl    full URL to the service's health endpoint
     */
    public void registerDependency(String serviceName, String healthUrl) {
        dependentServices.put(serviceName, healthUrl);
        log.info("Registered dependent service for health monitoring: {} -> {}", serviceName, healthUrl);
    }

    /**
     * Remove a dependent service from health monitoring.
     *
     * @param serviceName logical name of the dependent service
     */
    public void removeDependency(String serviceName) {
        dependentServices.remove(serviceName);
        log.info("Removed dependent service from health monitoring: {}", serviceName);
    }

    @Override
    public Health health() {
        if (dependentServices.isEmpty()) {
            return Health.up()
                    .withDetail("dependencies", "none registered")
                    .build();
        }

        Map<String, Object> details = new LinkedHashMap<>();
        boolean allHealthy = true;

        for (Map.Entry<String, String> entry : dependentServices.entrySet()) {
            String serviceName = entry.getKey();
            String healthUrl = entry.getValue();

            try {
                restTemplate.getForObject(healthUrl, String.class);
                details.put(serviceName, "UP");
            } catch (Exception e) {
                details.put(serviceName, "DOWN: " + e.getMessage());
                allHealthy = false;
                log.warn("Dependent service {} is unavailable: {}", serviceName, e.getMessage());
            }
        }

        Health.Builder builder = allHealthy ? Health.up() : Health.down();
        details.forEach(builder::withDetail);
        return builder.build();
    }
}
