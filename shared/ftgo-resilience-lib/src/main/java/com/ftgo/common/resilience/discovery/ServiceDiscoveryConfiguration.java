package com.ftgo.common.resilience.discovery;

import com.ftgo.common.resilience.config.ResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Kubernetes-native service discovery.
 *
 * <p>Provides DNS-based service discovery using Kubernetes internal DNS.
 * Services discover each other via K8s DNS names following the pattern:</p>
 * <pre>
 * {service-name}.{namespace}.svc.cluster.local
 * </pre>
 *
 * <p>For example:</p>
 * <ul>
 *   <li>{@code order-service.ftgo.svc.cluster.local}</li>
 *   <li>{@code consumer-service.ftgo.svc.cluster.local}</li>
 *   <li>{@code restaurant-service.ftgo.svc.cluster.local}</li>
 * </ul>
 *
 * <p>This approach is simpler than using Consul/Eureka and leverages
 * Kubernetes built-in service discovery capabilities.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.resilience.discovery.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceDiscoveryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryConfiguration.class);

    /**
     * Creates a {@link KubernetesServiceRegistry} for DNS-based service discovery.
     */
    @Bean
    public KubernetesServiceRegistry kubernetesServiceRegistry(ResilienceProperties properties) {
        ResilienceProperties.DiscoveryProperties discoveryProps = properties.getDiscovery();
        log.info("FTGO K8s Service Discovery configured: namespace={}, clusterDomain={}",
                discoveryProps.getNamespace(),
                discoveryProps.getClusterDomain());
        return new KubernetesServiceRegistry(
                discoveryProps.getNamespace(),
                discoveryProps.getClusterDomain());
    }

    /**
     * Creates a {@link ServiceEndpointResolver} that resolves service URLs
     * using K8s DNS or fallback configuration.
     */
    @Bean
    public ServiceEndpointResolver serviceEndpointResolver(
            KubernetesServiceRegistry serviceRegistry) {
        return new ServiceEndpointResolver(serviceRegistry);
    }
}
