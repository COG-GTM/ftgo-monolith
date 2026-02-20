package com.ftgo.gateway.health;

import com.ftgo.gateway.config.GatewayProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayHealthIndicator implements ReactiveHealthIndicator {

    private final GatewayProperties gatewayProperties;

    public GatewayHealthIndicator(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Health> health() {
        return checkGatewayHealth()
                .onErrorResume(ex -> Mono.just(
                        Health.down()
                                .withException(ex)
                                .build()));
    }

    private Mono<Health> checkGatewayHealth() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("service", "api-gateway");
        details.put("routeCount", gatewayProperties.getRoutes().size());

        for (GatewayProperties.ServiceRoute route : gatewayProperties.getRoutes()) {
            details.put("route." + route.getId(), route.getUri());
        }

        return Mono.just(Health.up().withDetails(details).build());
    }
}
