package net.chrisrichardson.ftgo.resilience.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures K8s-native DNS-based service discovery for FTGO services.
 *
 * <p>In Kubernetes, services are automatically discoverable via cluster DNS.
 * Each K8s Service gets a DNS entry:
 * {@code <service-name>.<namespace>.svc.cluster.local}
 *
 * <p>This configuration provides a centralized registry of service endpoints
 * that can be injected into any component that needs to call other services.
 *
 * <p>For local development, URLs can be overridden via application properties.
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KubernetesServiceDiscoveryProperties.class)
public class FtgoServiceDiscoveryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoServiceDiscoveryConfiguration.class);

    @Bean
    public ServiceResolver ftgoServiceResolver(KubernetesServiceDiscoveryProperties properties) {
        log.info("FTGO Service Discovery: namespace={}, clusterDomain={}, services={}",
                properties.getNamespace(),
                properties.getClusterDomain(),
                properties.getServices().keySet());
        return new ServiceResolver(properties);
    }
}
