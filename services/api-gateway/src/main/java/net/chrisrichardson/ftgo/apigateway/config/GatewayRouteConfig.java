package net.chrisrichardson.ftgo.apigateway.config;

import net.chrisrichardson.ftgo.apigateway.filter.CorrelationIdFilter;
import net.chrisrichardson.ftgo.apigateway.filter.RequestLoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private final GatewayServiceProperties serviceProperties;

    public GatewayRouteConfig(GatewayServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           CorrelationIdFilter correlationIdFilter,
                                           RequestLoggingFilter requestLoggingFilter) {
        return builder.routes()
                // ---- API v1 Routes (URL-path versioned) ----

                // Order Service routes
                .route("order-service-v1", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/api/v1/orders/(?<segment>.*)", "/orders/${segment}")
                                .rewritePath("/api/v1/orders$", "/orders"))
                        .uri(serviceProperties.getOrderServiceUrl()))

                // Consumer Service routes
                .route("consumer-service-v1", r -> r
                        .path("/api/v1/consumers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .rewritePath("/api/v1/consumers/(?<segment>.*)", "/consumers/${segment}")
                                .rewritePath("/api/v1/consumers$", "/consumers"))
                        .uri(serviceProperties.getConsumerServiceUrl()))

                // Restaurant Service routes
                .route("restaurant-service-v1", r -> r
                        .path("/api/v1/restaurants/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .rewritePath("/api/v1/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                                .rewritePath("/api/v1/restaurants$", "/restaurants"))
                        .uri(serviceProperties.getRestaurantServiceUrl()))

                // Courier Service routes
                .route("courier-service-v1", r -> r
                        .path("/api/v1/couriers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .rewritePath("/api/v1/couriers/(?<segment>.*)", "/couriers/${segment}")
                                .rewritePath("/api/v1/couriers$", "/couriers"))
                        .uri(serviceProperties.getCourierServiceUrl()))

                // ---- Non-versioned API Routes (default) ----

                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}")
                                .rewritePath("/api/orders$", "/orders"))
                        .uri(serviceProperties.getOrderServiceUrl()))

                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .rewritePath("/api/consumers/(?<segment>.*)", "/consumers/${segment}")
                                .rewritePath("/api/consumers$", "/consumers"))
                        .uri(serviceProperties.getConsumerServiceUrl()))

                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .rewritePath("/api/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                                .rewritePath("/api/restaurants$", "/restaurants"))
                        .uri(serviceProperties.getRestaurantServiceUrl()))

                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(requestLoggingFilter.apply(new RequestLoggingFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .rewritePath("/api/couriers/(?<segment>.*)", "/couriers/${segment}")
                                .rewritePath("/api/couriers$", "/couriers"))
                        .uri(serviceProperties.getCourierServiceUrl()))

                .build();
    }
}
