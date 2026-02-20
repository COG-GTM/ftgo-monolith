package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceRegistryTest {

    private ServiceRegistry serviceRegistry;

    @BeforeEach
    void setUp() {
        ResilienceProperties properties = new ResilienceProperties();
        serviceRegistry = new ServiceRegistry(properties);
    }

    @Test
    void shouldRegisterDefaultServices() {
        assertThat(serviceRegistry.getServiceNames())
                .contains("order-service", "restaurant-service", "consumer-service", "courier-service");
    }

    @Test
    void shouldRegisterAndRetrieveService() {
        serviceRegistry.register("test-service", "test-host", 9090);

        Optional<ServiceRegistry.ServiceInstance> instance = serviceRegistry.getService("test-service");
        assertThat(instance).isPresent();
        assertThat(instance.get().getName()).isEqualTo("test-service");
        assertThat(instance.get().getHost()).isEqualTo("test-host");
        assertThat(instance.get().getPort()).isEqualTo(9090);
    }

    @Test
    void shouldDeregisterService() {
        serviceRegistry.register("temp-service", "temp-host", 8080);
        serviceRegistry.deregister("temp-service");

        assertThat(serviceRegistry.getService("temp-service")).isEmpty();
    }

    @Test
    void shouldResolveServiceUrl() {
        String url = serviceRegistry.resolveServiceUrl("order-service");
        assertThat(url).isEqualTo("http://order-service:8080");
    }

    @Test
    void shouldThrowWhenResolvingUnknownService() {
        assertThatThrownBy(() -> serviceRegistry.resolveServiceUrl("unknown-service"))
                .isInstanceOf(ServiceRegistry.ServiceNotFoundException.class)
                .hasMessageContaining("unknown-service");
    }

    @Test
    void shouldReturnDiscoveryType() {
        assertThat(serviceRegistry.getDiscoveryType()).isEqualTo("kubernetes");
    }

    @Test
    void shouldTrackServiceHealth() {
        serviceRegistry.register("test-service", "test-host", 8080);
        Optional<ServiceRegistry.ServiceInstance> instance = serviceRegistry.getService("test-service");
        assertThat(instance).isPresent();
        assertThat(instance.get().isHealthy()).isTrue();

        instance.get().setHealthy(false);
        assertThat(instance.get().isHealthy()).isFalse();
    }

    @Test
    void shouldReturnAllServices() {
        assertThat(serviceRegistry.getAllServices()).isNotEmpty();
        assertThat(serviceRegistry.getAllServices()).containsKey("order-service");
    }
}
