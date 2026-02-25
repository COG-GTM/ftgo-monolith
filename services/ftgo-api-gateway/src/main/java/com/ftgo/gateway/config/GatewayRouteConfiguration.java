package com.ftgo.gateway.config;

import com.ftgo.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route configuration for the FTGO API Gateway.
 * <p>
 * Routes requests to the four FTGO microservices:
 * <ul>
 *   <li>{@code /api/orders/**}      -> Order Service</li>
 *   <li>{@code /api/consumers/**}   -> Consumer Service</li>
 *   <li>{@code /api/restaurants/**} -> Restaurant Service</li>
 *   <li>{@code /api/couriers/**}    -> Courier Service</li>
 * </ul>
 * Each route applies JWT authentication, circuit breaker, and
 * request size limiting filters.
 * </p>
 */
@Configuration
public class GatewayRouteConfiguration {

    private final JwtAuthenticationGatewayFilterFactory jwtFilterFactory;

    @Value("${ftgo.gateway.services.order-service.url:http://ftgo-order-service:8081}")
    private String orderServiceUrl;

    @Value("${ftgo.gateway.services.consumer-service.url:http://ftgo-consumer-service:8082}")
    private String consumerServiceUrl;

    @Value("${ftgo.gateway.services.restaurant-service.url:http://ftgo-restaurant-service:8083}")
    private String restaurantServiceUrl;

    @Value("${ftgo.gateway.services.courier-service.url:http://ftgo-courier-service:8084}")
    private String courierServiceUrl;

    public GatewayRouteConfiguration(JwtAuthenticationGatewayFilterFactory jwtFilterFactory) {
        this.jwtFilterFactory = jwtFilterFactory;
    }

    @Bean
    public RouteLocator ftgoRoutes(RouteLocatorBuilder builder) {
        JwtAuthenticationGatewayFilterFactory.Config jwtConfig =
                new JwtAuthenticationGatewayFilterFactory.Config();

        return builder.routes()
                // ---- Order Service ----
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(orderServiceUrl))

                // ---- Consumer Service ----
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCB")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(consumerServiceUrl))

                // ---- Restaurant Service ----
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCB")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(restaurantServiceUrl))

                // ---- Courier Service ----
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCB")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(courierServiceUrl))

                // ---- API Version v1 routes (explicit versioned paths) ----
                .route("order-service-v1", r -> r
                        .path("/v1/api/orders/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/v1/api/orders/(?<segment>.*)", "/api/orders/${segment}")
                                .addRequestHeader("X-API-Version", "v1")
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(orderServiceUrl))

                .route("consumer-service-v1", r -> r
                        .path("/v1/api/consumers/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCB")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .rewritePath("/v1/api/consumers/(?<segment>.*)", "/api/consumers/${segment}")
                                .addRequestHeader("X-API-Version", "v1")
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(consumerServiceUrl))

                .route("restaurant-service-v1", r -> r
                        .path("/v1/api/restaurants/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCB")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .rewritePath("/v1/api/restaurants/(?<segment>.*)", "/api/restaurants/${segment}")
                                .addRequestHeader("X-API-Version", "v1")
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(restaurantServiceUrl))

                .route("courier-service-v1", r -> r
                        .path("/v1/api/couriers/**")
                        .filters(f -> f
                                .filter(jwtFilterFactory.apply(jwtConfig))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCB")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .rewritePath("/v1/api/couriers/(?<segment>.*)", "/api/couriers/${segment}")
                                .addRequestHeader("X-API-Version", "v1")
                                .addRequestHeader("X-Forwarded-Service", "ftgo-api-gateway"))
                        .uri(courierServiceUrl))

                .build();
    }
}
