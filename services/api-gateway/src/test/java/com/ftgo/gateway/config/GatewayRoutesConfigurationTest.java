package com.ftgo.gateway.config;

import com.ftgo.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import com.ftgo.gateway.ratelimit.RateLimitGatewayFilterFactory;
import com.ftgo.jwt.JwtProperties;
import com.ftgo.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayRoutesConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void shouldLoadRoutesFromProperties() {
        assertThat(gatewayProperties.getRoutes()).isNotEmpty();
        assertThat(gatewayProperties.getRoutes()).hasSize(4);
    }

    @Test
    void shouldContainOrderServiceRoute() {
        boolean hasOrderRoute = gatewayProperties.getRoutes().stream()
                .anyMatch(r -> "order-service".equals(r.getId()));
        assertThat(hasOrderRoute).isTrue();
    }

    @Test
    void shouldContainConsumerServiceRoute() {
        boolean hasConsumerRoute = gatewayProperties.getRoutes().stream()
                .anyMatch(r -> "consumer-service".equals(r.getId()));
        assertThat(hasConsumerRoute).isTrue();
    }

    @Test
    void shouldContainRestaurantServiceRoute() {
        boolean hasRestaurantRoute = gatewayProperties.getRoutes().stream()
                .anyMatch(r -> "restaurant-service".equals(r.getId()));
        assertThat(hasRestaurantRoute).isTrue();
    }

    @Test
    void shouldContainCourierServiceRoute() {
        boolean hasCourierRoute = gatewayProperties.getRoutes().stream()
                .anyMatch(r -> "courier-service".equals(r.getId()));
        assertThat(hasCourierRoute).isTrue();
    }

    @Test
    void shouldCreateRouteLocator() {
        assertThat(routeLocator).isNotNull();
    }
}
