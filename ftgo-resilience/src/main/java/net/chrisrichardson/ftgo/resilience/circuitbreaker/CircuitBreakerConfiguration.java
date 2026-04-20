package net.chrisrichardson.ftgo.resilience.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .ringBufferSizeInClosedState(10)
                .ringBufferSizeInHalfOpenState(3)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultCircuitBreakerConfig) {
        return CircuitBreakerRegistry.of(defaultCircuitBreakerConfig);
    }

    @Bean
    public CircuitBreaker consumerServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("consumerService");
    }

    @Bean
    public CircuitBreaker restaurantServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("restaurantService");
    }

    @Bean
    public CircuitBreaker deliveryServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("deliveryService");
    }
}
