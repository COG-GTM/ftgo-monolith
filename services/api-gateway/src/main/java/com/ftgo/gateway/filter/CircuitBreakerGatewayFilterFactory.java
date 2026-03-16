package com.ftgo.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Factory that creates gateway filters backed by Resilience4j circuit breakers.
 *
 * <p>Each downstream service gets its own named circuit breaker. When a service
 * becomes unavailable, the circuit opens and the gateway returns
 * {@code 503 Service Unavailable} immediately, preventing resource exhaustion
 * and cascading failures.
 */
@Component
public class CircuitBreakerGatewayFilterFactory {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerGatewayFilterFactory.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerGatewayFilterFactory(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public GatewayFilter apply(String serviceName) {
        return new CircuitBreakerGatewayFilter(serviceName);
    }

    private class CircuitBreakerGatewayFilter implements GatewayFilter, Ordered {

        private final String serviceName;
        private final CircuitBreaker circuitBreaker;

        CircuitBreakerGatewayFilter(String serviceName) {
            this.serviceName = serviceName;
            this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .onErrorResume(throwable -> {
                        log.error("Circuit breaker [{}] triggered for {}: {}",
                                serviceName,
                                exchange.getRequest().getURI().getPath(),
                                throwable.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        return exchange.getResponse().setComplete();
                    });
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 4;
        }
    }
}
