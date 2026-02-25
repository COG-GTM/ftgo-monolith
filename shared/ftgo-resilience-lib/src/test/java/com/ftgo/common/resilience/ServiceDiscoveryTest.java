package com.ftgo.common.resilience;

import com.ftgo.common.resilience.discovery.KubernetesServiceRegistry;
import com.ftgo.common.resilience.discovery.ServiceEndpointResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for K8s-native service discovery components.
 */
class ServiceDiscoveryTest {

    private KubernetesServiceRegistry registry;
    private ServiceEndpointResolver resolver;

    @BeforeEach
    void setUp() {
        registry = new KubernetesServiceRegistry("ftgo", "svc.cluster.local");
        resolver = new ServiceEndpointResolver(registry);
    }

    @Test
    void resolvesFqdnCorrectly() {
        String fqdn = registry.getFqdn("order-service");
        assertThat(fqdn).isEqualTo("ftgo-order-service.ftgo.svc.cluster.local");
    }

    @Test
    void resolvesBaseUrlForDefaultService() {
        String url = registry.resolveBaseUrl("order-service");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:8081");
    }

    @Test
    void resolvesFullUrl() {
        String url = resolver.resolveUrl("order-service", "/api/orders");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:8081/api/orders");
    }

    @Test
    void resolvesHealthUrl() {
        String url = resolver.resolveHealthUrl("consumer-service");
        assertThat(url).isEqualTo("http://ftgo-consumer-service.ftgo.svc.cluster.local:8082/actuator/health");
    }

    @Test
    void resolvesReadinessUrl() {
        String url = resolver.resolveReadinessUrl("restaurant-service");
        assertThat(url).contains("/actuator/health/readiness");
    }

    @Test
    void resolvesLivenessUrl() {
        String url = resolver.resolveLivenessUrl("courier-service");
        assertThat(url).contains("/actuator/health/liveness");
    }

    @Test
    void overrideUrlTakesPrecedence() {
        registry.setOverrideUrl("order-service", "http://localhost:8081");
        String url = registry.resolveBaseUrl("order-service");
        assertThat(url).isEqualTo("http://localhost:8081");
    }

    @Test
    void customServiceRegistration() {
        registry.registerService("payment-service", 8085);
        String url = registry.resolveBaseUrl("payment-service");
        assertThat(url).isEqualTo("http://ftgo-payment-service.ftgo.svc.cluster.local:8085");
    }

    @Test
    void throwsForUnknownService() {
        assertThatThrownBy(() -> registry.resolveBaseUrl("unknown-service"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown service");
    }

    @Test
    void defaultServicesAreRegistered() {
        assertThat(registry.isRegistered("order-service")).isTrue();
        assertThat(registry.isRegistered("consumer-service")).isTrue();
        assertThat(registry.isRegistered("restaurant-service")).isTrue();
        assertThat(registry.isRegistered("courier-service")).isTrue();
        assertThat(registry.isRegistered("api-gateway")).isTrue();
    }

    @Test
    void customNamespaceConfiguration() {
        KubernetesServiceRegistry prodRegistry = new KubernetesServiceRegistry(
                "production", "svc.cluster.local");
        ServiceEndpointResolver prodResolver = new ServiceEndpointResolver(prodRegistry);

        String url = prodResolver.resolveUrl("order-service", "/api/orders");
        assertThat(url).contains("production.svc.cluster.local");
    }
}
