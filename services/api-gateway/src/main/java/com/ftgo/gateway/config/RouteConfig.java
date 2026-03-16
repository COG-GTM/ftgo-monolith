package com.ftgo.gateway.config;

import com.ftgo.gateway.filter.CircuitBreakerGatewayFilterFactory;
import com.ftgo.gateway.filter.CorrelationIdFilter;
import com.ftgo.gateway.filter.JwtValidationFilter;
import com.ftgo.gateway.filter.RateLimitingFilter;
import com.ftgo.gateway.filter.RequestResponseLoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures gateway routes for all FTGO microservices.
 *
 * <p>Each route maps a public API path to the corresponding internal service.
 * All routes apply JWT validation, rate limiting, circuit breaker,
 * and correlation ID filters.
 */
@Configuration
public class RouteConfig {

    private final JwtValidationFilter jwtValidationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final CircuitBreakerGatewayFilterFactory circuitBreakerFilterFactory;
    private final CorrelationIdFilter correlationIdFilter;
    private final RequestResponseLoggingFilter requestResponseLoggingFilter;
    private final FtgoGatewayProperties gatewayProperties;

    public RouteConfig(JwtValidationFilter jwtValidationFilter,
                       RateLimitingFilter rateLimitingFilter,
                       CircuitBreakerGatewayFilterFactory circuitBreakerFilterFactory,
                       CorrelationIdFilter correlationIdFilter,
                       RequestResponseLoggingFilter requestResponseLoggingFilter,
                       FtgoGatewayProperties gatewayProperties) {
        this.jwtValidationFilter = jwtValidationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.circuitBreakerFilterFactory = circuitBreakerFilterFactory;
        this.correlationIdFilter = correlationIdFilter;
        this.requestResponseLoggingFilter = requestResponseLoggingFilter;
        this.gatewayProperties = gatewayProperties;
    }

    @Bean
    public RouteLocator ftgoRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Order Service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(correlationIdFilter)
                                .filter(requestResponseLoggingFilter)
                                .filter(jwtValidationFilter)
                                .filter(rateLimitingFilter)
                                .filter(circuitBreakerFilterFactory.apply("order-service"))
                                .stripPrefix(1)
                        )
                        .uri(gatewayProperties.getServices().getOrderServiceUrl()))

                // Consumer Service routes
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter)
                                .filter(requestResponseLoggingFilter)
                                .filter(jwtValidationFilter)
                                .filter(rateLimitingFilter)
                                .filter(circuitBreakerFilterFactory.apply("consumer-service"))
                                .stripPrefix(1)
                        )
                        .uri(gatewayProperties.getServices().getConsumerServiceUrl()))

                // Restaurant Service routes
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .filter(correlationIdFilter)
                                .filter(requestResponseLoggingFilter)
                                .filter(jwtValidationFilter)
                                .filter(rateLimitingFilter)
                                .filter(circuitBreakerFilterFactory.apply("restaurant-service"))
                                .stripPrefix(1)
                        )
                        .uri(gatewayProperties.getServices().getRestaurantServiceUrl()))

                // Courier Service routes
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter)
                                .filter(requestResponseLoggingFilter)
                                .filter(jwtValidationFilter)
                                .filter(rateLimitingFilter)
                                .filter(circuitBreakerFilterFactory.apply("courier-service"))
                                .stripPrefix(1)
                        )
                        .uri(gatewayProperties.getServices().getCourierServiceUrl()))

                .build();
    }
}
