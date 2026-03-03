package com.ftgo.resilience.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kubernetes DNS-based service discovery for FTGO microservices.
 * <p>
 * In Kubernetes, services are automatically assigned DNS names following the pattern:
 * <pre>
 *   {service-name}.{namespace}.svc.cluster.local
 * </pre>
 * <p>
 * This class provides a centralized registry of FTGO service URLs that other components
 * (health indicators, REST clients, circuit breakers) can use to locate downstream services.
 * <p>
 * <b>DNS Resolution Examples:</b>
 * <ul>
 *   <li>Order Service:      {@code ftgo-order-service.ftgo.svc.cluster.local:8080}</li>
 *   <li>Consumer Service:   {@code ftgo-consumer-service.ftgo.svc.cluster.local:8080}</li>
 *   <li>Restaurant Service: {@code ftgo-restaurant-service.ftgo.svc.cluster.local:8080}</li>
 *   <li>Courier Service:    {@code ftgo-courier-service.ftgo.svc.cluster.local:8080}</li>
 *   <li>API Gateway:        {@code ftgo-api-gateway.ftgo.svc.cluster.local:8080}</li>
 * </ul>
 * <p>
 * Configuration properties:
 * <pre>
 *   ftgo.service-discovery.namespace=ftgo
 *   ftgo.service-discovery.cluster-domain=cluster.local
 *   ftgo.service-discovery.default-port=8080
 * </pre>
 */
@Configuration
public class KubernetesServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(KubernetesServiceDiscovery.class);

    @Value("${ftgo.service-discovery.namespace:ftgo}")
    private String namespace;

    @Value("${ftgo.service-discovery.cluster-domain:cluster.local}")
    private String clusterDomain;

    @Value("${ftgo.service-discovery.default-port:8080}")
    private int defaultPort;

    /** Registry of known FTGO service names. */
    private static final String[] FTGO_SERVICES = {
            "ftgo-order-service",
            "ftgo-consumer-service",
            "ftgo-restaurant-service",
            "ftgo-courier-service",
            "ftgo-api-gateway"
    };

    private final Map<String, String> serviceUrls = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        for (String serviceName : FTGO_SERVICES) {
            String url = buildServiceUrl(serviceName);
            serviceUrls.put(serviceName, url);
            log.info("Registered service discovery entry: {} -> {}", serviceName, url);
        }
    }

    /**
     * Get the base URL for a service by its name.
     *
     * @param serviceName the Kubernetes service name (e.g., "ftgo-order-service")
     * @return the full base URL (e.g., "http://ftgo-order-service.ftgo.svc.cluster.local:8080")
     */
    public String getServiceUrl(String serviceName) {
        return serviceUrls.computeIfAbsent(serviceName, this::buildServiceUrl);
    }

    /**
     * Get the health endpoint URL for a service.
     *
     * @param serviceName the Kubernetes service name
     * @return the health endpoint URL
     */
    public String getHealthUrl(String serviceName) {
        return getServiceUrl(serviceName) + "/actuator/health";
    }

    /**
     * Get all registered service URLs.
     *
     * @return unmodifiable map of service name to base URL
     */
    public Map<String, String> getAllServiceUrls() {
        return java.util.Collections.unmodifiableMap(serviceUrls);
    }

    /**
     * Build the Kubernetes DNS-based URL for a service.
     * <p>
     * Format: http://{service-name}.{namespace}.svc.{cluster-domain}:{port}
     */
    private String buildServiceUrl(String serviceName) {
        return String.format("http://%s.%s.svc.%s:%d",
                serviceName, namespace, clusterDomain, defaultPort);
    }

    /**
     * Register a custom service URL (useful for testing or non-standard services).
     *
     * @param serviceName the service name
     * @param url         the service URL
     */
    public void registerServiceUrl(String serviceName, String url) {
        serviceUrls.put(serviceName, url);
        log.info("Custom service URL registered: {} -> {}", serviceName, url);
    }
}
