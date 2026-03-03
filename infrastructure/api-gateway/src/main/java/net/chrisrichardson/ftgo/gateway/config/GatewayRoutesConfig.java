package net.chrisrichardson.ftgo.gateway.config;

import net.chrisrichardson.ftgo.gateway.filter.CorrelationIdFilter;
import net.chrisrichardson.ftgo.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures all gateway routes for FTGO microservices.
 *
 * <p>Routes support both versioned (/v1/api/**) and non-versioned (/api/**) paths.
 * Each route includes:
 * <ul>
 *   <li>JWT authentication filter</li>
 *   <li>Correlation ID propagation</li>
 *   <li>Circuit breaker with fallback</li>
 *   <li>Rate limiting</li>
 *   <li>Path rewriting</li>
 * </ul>
 */
@Configuration
public class GatewayRoutesConfig {

    @Value("${ftgo.services.order-service.url}")
    private String orderServiceUrl;

    @Value("${ftgo.services.consumer-service.url}")
    private String consumerServiceUrl;

    @Value("${ftgo.services.restaurant-service.url}")
    private String restaurantServiceUrl;

    @Value("${ftgo.services.courier-service.url}")
    private String courierServiceUrl;

    @Bean
    public RouteLocator ftgoRouteLocator(RouteLocatorBuilder builder,
                                          JwtAuthenticationFilter jwtFilter,
                                          CorrelationIdFilter correlationIdFilter) {
        return builder.routes()
                // =====================================================================
                // Order Service Routes
                // =====================================================================
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(null) // Uses default RedisRateLimiter bean
                                        .setDenyEmptyKey(false))
                                .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}")
                        )
                        .uri(orderServiceUrl))

                .route("order-service-versioned", r -> r
                        .path("/v1/api/orders/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .rewritePath("/v1/api/orders/(?<segment>.*)", "/orders/${segment}")
                        )
                        .uri(orderServiceUrl))

                // =====================================================================
                // Consumer Service Routes
                // =====================================================================
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(null)
                                        .setDenyEmptyKey(false))
                                .rewritePath("/api/consumers/(?<segment>.*)", "/consumers/${segment}")
                        )
                        .uri(consumerServiceUrl))

                .route("consumer-service-versioned", r -> r
                        .path("/v1/api/consumers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .rewritePath("/v1/api/consumers/(?<segment>.*)", "/consumers/${segment}")
                        )
                        .uri(consumerServiceUrl))

                // =====================================================================
                // Restaurant Service Routes
                // =====================================================================
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(null)
                                        .setDenyEmptyKey(false))
                                .rewritePath("/api/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                        )
                        .uri(restaurantServiceUrl))

                .route("restaurant-service-versioned", r -> r
                        .path("/v1/api/restaurants/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .rewritePath("/v1/api/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                        )
                        .uri(restaurantServiceUrl))

                // =====================================================================
                // Courier Service Routes
                // =====================================================================
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(null)
                                        .setDenyEmptyKey(false))
                                .rewritePath("/api/couriers/(?<segment>.*)", "/couriers/${segment}")
                        )
                        .uri(courierServiceUrl))

                .route("courier-service-versioned", r -> r
                        .path("/v1/api/couriers/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .rewritePath("/v1/api/couriers/(?<segment>.*)", "/couriers/${segment}")
                        )
                        .uri(courierServiceUrl))

                .build();
    }
}
