package com.ftgo.gateway.health;

import com.ftgo.gateway.config.GatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayHealthIndicatorTest {

    @Test
    void shouldReturnUpStatusWithRouteDetails() {
        GatewayProperties properties = new GatewayProperties();

        GatewayProperties.ServiceRoute route = new GatewayProperties.ServiceRoute();
        route.setId("order-service");
        route.setUri("http://localhost:8081");
        properties.setRoutes(List.of(route));

        GatewayHealthIndicator indicator = new GatewayHealthIndicator(properties);

        StepVerifier.create(indicator.health())
                .assertNext(health -> {
                    assertThat(health.getStatus()).isEqualTo(Status.UP);
                    assertThat(health.getDetails()).containsKey("service");
                    assertThat(health.getDetails().get("service")).isEqualTo("api-gateway");
                    assertThat(health.getDetails()).containsKey("routeCount");
                    assertThat(health.getDetails().get("routeCount")).isEqualTo(1);
                    assertThat(health.getDetails()).containsKey("route.order-service");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroRoutesWhenEmpty() {
        GatewayProperties properties = new GatewayProperties();
        GatewayHealthIndicator indicator = new GatewayHealthIndicator(properties);

        StepVerifier.create(indicator.health())
                .assertNext(health -> {
                    assertThat(health.getStatus()).isEqualTo(Status.UP);
                    assertThat(health.getDetails().get("routeCount")).isEqualTo(0);
                })
                .verifyComplete();
    }
}
