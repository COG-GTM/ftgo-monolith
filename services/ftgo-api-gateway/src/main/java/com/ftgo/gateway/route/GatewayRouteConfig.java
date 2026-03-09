package com.ftgo.gateway.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route configuration for the FTGO API Gateway.
 *
 * <p>Defines routes to all downstream microservices with circuit breaker
 * and path rewriting. Routes configured here supplement (or override)
 * the YAML-based route definitions in application.yml.
 *
 * <p>Route mapping:
 * <ul>
 *   <li>{@code /api/orders/**} -> Order Service</li>
 *   <li>{@code /api/consumers/**} -> Consumer Service</li>
 *   <li>{@code /api/restaurants/**} -> Restaurant Service</li>
 *   <li>{@code /api/couriers/**} -> Courier Service</li>
 * </ul>
 */
@Configuration
public class GatewayRouteConfig {

    @Value("${ftgo.gateway.services.order-service-url:http://localhost:8081}")
    private String orderServiceUrl;

    @Value("${ftgo.gateway.services.consumer-service-url:http://localhost:8082}")
    private String consumerServiceUrl;

    @Value("${ftgo.gateway.services.restaurant-service-url:http://localhost:8083}")
    private String restaurantServiceUrl;

    @Value("${ftgo.gateway.services.courier-service-url:http://localhost:8084}")
    private String courierServiceUrl;

    @Bean
    public RouteLocator ftgoRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ----- Versioned routes (e.g. /api/v1/orders/**) -----
                // These must be defined before non-versioned routes so they match first.
                // The rewritePath strips the version prefix: /api/v1/orders/123 -> /orders/123
                .route("order-service-versioned", r -> r
                        .path("/api/v*/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/api/v[^/]+/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri(orderServiceUrl))

                .route("consumer-service-versioned", r -> r
                        .path("/api/v*/consumers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .rewritePath("/api/v[^/]+/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri(consumerServiceUrl))

                .route("restaurant-service-versioned", r -> r
                        .path("/api/v*/restaurants/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .rewritePath("/api/v[^/]+/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri(restaurantServiceUrl))

                .route("courier-service-versioned", r -> r
                        .path("/api/v*/couriers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .rewritePath("/api/v[^/]+/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri(courierServiceUrl))

                // ----- Non-versioned routes (e.g. /api/orders/**) -----
                // Order Service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .stripPrefix(1)
                        )
                        .uri(orderServiceUrl))

                // Consumer Service routes
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .stripPrefix(1)
                        )
                        .uri(consumerServiceUrl))

                // Restaurant Service routes
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .stripPrefix(1)
                        )
                        .uri(restaurantServiceUrl))

                // Courier Service routes
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .stripPrefix(1)
                        )
                        .uri(courierServiceUrl))

                .build();
    }
}
