package com.ftgo.resilience.circuitbreaker;

import com.ftgo.resilience.config.ResilienceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnClass(CircuitBreaker.class)
@ConditionalOnProperty(prefix = "ftgo.resilience.circuit-breaker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CircuitBreakerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ResilienceProperties properties) {
        ResilienceProperties.CircuitBreaker cbProps = properties.getCircuitBreaker();
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofMillis(cbProps.getSlowCallDurationThresholdMs()))
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofMillis(cbProps.getWaitDurationInOpenStateMs()))
                .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "circuitBreakerMetrics")
    @ConditionalOnBean(MeterRegistry.class)
    public TaggedCircuitBreakerMetrics circuitBreakerMetrics(
            CircuitBreakerRegistry registry,
            MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean(name = "orderServiceCircuitBreaker")
    public CircuitBreaker orderServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("orderService");
    }

    @Bean(name = "restaurantServiceCircuitBreaker")
    public CircuitBreaker restaurantServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("restaurantService");
    }

    @Bean(name = "consumerServiceCircuitBreaker")
    public CircuitBreaker consumerServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("consumerService");
    }

    @Bean(name = "courierServiceCircuitBreaker")
    public CircuitBreaker courierServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("courierService");
    }

    @Bean(name = "externalPaymentCircuitBreaker")
    public CircuitBreaker externalPaymentCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig externalConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .slowCallRateThreshold(70)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
        return registry.circuitBreaker("externalPayment", externalConfig);
    }
}
