package com.ftgo.gateway.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 *
 * <p>When a downstream service circuit breaker opens, requests are forwarded
 * to these fallback endpoints which return a standardized error response.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> ordersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Order Service", exchange);
    }

    @RequestMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> consumersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Consumer Service", exchange);
    }

    @RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> restaurantsFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Restaurant Service", exchange);
    }

    @RequestMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> couriersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Courier Service", exchange);
    }

    private Mono<Map<String, Object>> createFallbackResponse(String serviceName,
                                                              ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered for {} [correlationId={}]",
                serviceName,
                exchange.getRequest().getHeaders().getFirst("X-Correlation-ID"));

        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

        return Mono.just(Map.of(
                "error", "Service Unavailable",
                "message", serviceName + " is currently unavailable. Please try again later.",
                "status", 503
        ));
    }
}
