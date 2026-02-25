package com.ftgo.common.resilience.health;

import com.ftgo.common.resilience.config.ResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Health indicator that checks connectivity to downstream services.
 *
 * <p>Services register their downstream dependencies via configuration:</p>
 * <pre>
 * ftgo.resilience.health.downstream-services[0].name=consumer-service
 * ftgo.resilience.health.downstream-services[0].url=http://consumer-service:8082/actuator/health
 * </pre>
 *
 * <p>The indicator attempts an HTTP GET to each downstream service's health
 * endpoint and reports their status.</p>
 */
public class DownstreamServiceHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DownstreamServiceHealthIndicator.class);

    private final ResilienceProperties properties;
    private final CopyOnWriteArrayList<DownstreamService> downstreamServices = new CopyOnWriteArrayList<>();

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    public DownstreamServiceHealthIndicator(ResilienceProperties properties) {
        this.properties = properties;
    }

    /**
     * Registers a downstream service for health checking.
     *
     * @param name the service name
     * @param healthUrl the health endpoint URL
     */
    public void registerDownstreamService(String name, String healthUrl) {
        downstreamServices.add(new DownstreamService(name, healthUrl));
        log.info("Registered downstream service for health check: {} -> {}", name, healthUrl);
    }

    @Override
    public Health health() {
        if (downstreamServices.isEmpty()) {
            return Health.up()
                    .withDetail("downstream", "No downstream services registered")
                    .build();
        }

        Health.Builder builder = Health.up();
        Map<String, Object> details = new LinkedHashMap<>();
        boolean anyDown = false;
        int timeoutSeconds = properties.getHealth().getDownstreamTimeoutSeconds();

        for (DownstreamService service : downstreamServices) {
            Map<String, Object> serviceInfo = new LinkedHashMap<>();
            try {
                URL url = new URL(service.healthUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeoutSeconds * 1000);
                connection.setReadTimeout(timeoutSeconds * 1000);

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                if (responseCode >= 200 && responseCode < 300) {
                    serviceInfo.put("status", "UP");
                    serviceInfo.put("url", service.healthUrl());
                } else {
                    serviceInfo.put("status", "DOWN");
                    serviceInfo.put("url", service.healthUrl());
                    serviceInfo.put("httpStatus", responseCode);
                    anyDown = true;
                }
            } catch (Exception e) {
                serviceInfo.put("status", "DOWN");
                serviceInfo.put("url", service.healthUrl());
                serviceInfo.put("error", e.getMessage());
                anyDown = true;
                log.warn("Downstream service {} health check failed: {}", service.name(), e.getMessage());
            }
            details.put(service.name(), serviceInfo);
        }

        builder.withDetail("downstream", details);

        if (anyDown) {
            return builder.status("DEGRADED").build();
        }

        return builder.build();
    }

    private record DownstreamService(String name, String healthUrl) {
    }
}
