package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "ftgo-api-gateway"))
                        .uri("lb://order-service"))

                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "ftgo-api-gateway"))
                        .uri("lb://consumer-service"))

                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "ftgo-api-gateway"))
                        .uri("lb://restaurant-service"))

                .route("delivery-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "ftgo-api-gateway"))
                        .uri("lb://delivery-service"))

                .route("monolith-fallback", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "ftgo-api-gateway"))
                        .uri("lb://ftgo-monolith"))

                .build();
    }
}
