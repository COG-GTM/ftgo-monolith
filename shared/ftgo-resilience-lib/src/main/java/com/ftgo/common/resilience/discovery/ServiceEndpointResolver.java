package com.ftgo.common.resilience.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves service endpoints using K8s DNS-based service discovery.
 *
 * <p>Provides a simple API for resolving service URLs and health endpoints:</p>
 * <pre>
 * String orderUrl = resolver.resolveUrl("order-service", "/api/orders");
 * // Returns: http://ftgo-order-service.ftgo.svc.cluster.local:8081/api/orders
 *
 * String healthUrl = resolver.resolveHealthUrl("order-service");
 * // Returns: http://ftgo-order-service.ftgo.svc.cluster.local:8081/actuator/health
 * </pre>
 */
public class ServiceEndpointResolver {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpointResolver.class);

    private static final String HEALTH_PATH = "/actuator/health";

    private final KubernetesServiceRegistry serviceRegistry;

    public ServiceEndpointResolver(KubernetesServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Resolves a full URL for a service endpoint.
     *
     * @param serviceName the target service name (e.g., "order-service")
     * @param path the API path (e.g., "/api/orders")
     * @return the full URL
     */
    public String resolveUrl(String serviceName, String path) {
        String baseUrl = serviceRegistry.resolveBaseUrl(serviceName);
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return baseUrl + normalizedPath;
    }

    /**
     * Resolves the health endpoint URL for a service.
     *
     * @param serviceName the target service name
     * @return the health endpoint URL
     */
    public String resolveHealthUrl(String serviceName) {
        return resolveUrl(serviceName, HEALTH_PATH);
    }

    /**
     * Resolves the readiness probe URL for a service.
     *
     * @param serviceName the target service name
     * @return the readiness endpoint URL
     */
    public String resolveReadinessUrl(String serviceName) {
        return resolveUrl(serviceName, HEALTH_PATH + "/readiness");
    }

    /**
     * Resolves the liveness probe URL for a service.
     *
     * @param serviceName the target service name
     * @return the liveness endpoint URL
     */
    public String resolveLivenessUrl(String serviceName) {
        return resolveUrl(serviceName, HEALTH_PATH + "/liveness");
    }

    /**
     * Returns the underlying service registry.
     */
    public KubernetesServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
