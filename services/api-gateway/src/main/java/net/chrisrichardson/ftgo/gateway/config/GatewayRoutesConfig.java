package net.chrisrichardson.ftgo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Order Service routes (v1 and unversioned)
                .route("order-service-v1", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/orders/(?<segment>.*)", "/orders/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://order-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://order-service"))

                // Consumer Service routes
                .route("consumer-service-v1", r -> r
                        .path("/api/v1/consumers/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/consumers/(?<segment>.*)", "/consumers/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCB")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://consumer-service"))
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .rewritePath("/api/consumers/(?<segment>.*)", "/consumers/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCB")
                                        .setFallbackUri("forward:/fallback/consumers"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://consumer-service"))

                // Restaurant Service routes
                .route("restaurant-service-v1", r -> r
                        .path("/api/v1/restaurants/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCB")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://restaurant-service"))
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .rewritePath("/api/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCB")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://restaurant-service"))

                // Courier Service routes
                .route("courier-service-v1", r -> r
                        .path("/api/v1/couriers/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/couriers/(?<segment>.*)", "/couriers/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCB")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://courier-service"))
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .rewritePath("/api/couriers/(?<segment>.*)", "/couriers/${segment}")
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCB")
                                        .setFallbackUri("forward:/fallback/couriers"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://courier-service"))

                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .minimumNumberOfCalls(5)
                        .slowCallRateThreshold(80)
                        .slowCallDurationThreshold(Duration.ofSeconds(3))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                .build());
    }
}
