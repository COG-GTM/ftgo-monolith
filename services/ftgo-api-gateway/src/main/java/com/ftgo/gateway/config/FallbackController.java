package com.ftgo.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 * <p>
 * When a downstream service is unavailable and the circuit breaker is
 * open, requests are forwarded to these fallback endpoints which return
 * a user-friendly error response with HTTP 503.
 * </p>
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> orderServiceFallback() {
        return fallbackResponse("Order Service");
    }

    @GetMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> consumerServiceFallback() {
        return fallbackResponse("Consumer Service");
    }

    @GetMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> restaurantServiceFallback() {
        return fallbackResponse("Restaurant Service");
    }

    @GetMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> courierServiceFallback() {
        return fallbackResponse("Courier Service");
    }

    private Mono<Map<String, Object>> fallbackResponse(String serviceName) {
        log.warn("Circuit breaker fallback triggered for {}", serviceName);
        return Mono.just(Map.of(
                "error", "Service Unavailable",
                "message", serviceName + " is currently unavailable. Please try again later.",
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", Instant.now().toString()
        ));
    }
}
