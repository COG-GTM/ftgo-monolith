package net.chrisrichardson.ftgo.gateway.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 *
 * <p>When a downstream service is unavailable and the circuit breaker is open,
 * requests are forwarded to these fallback endpoints. Returns a standardized
 * error response with service unavailability information.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> orderServiceFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Order Service", exchange);
    }

    @GetMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> consumerServiceFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Consumer Service", exchange);
    }

    @GetMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> restaurantServiceFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Restaurant Service", exchange);
    }

    @GetMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> courierServiceFallback(ServerWebExchange exchange) {
        return createFallbackResponse("Courier Service", exchange);
    }

    private Mono<Map<String, Object>> createFallbackResponse(String serviceName,
                                                               ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        log.warn("Circuit breaker fallback triggered for {} (correlationId={})", serviceName, correlationId);

        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

        return Mono.just(Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", serviceName + " is currently unavailable. Please try again later.",
                "service", serviceName,
                "timestamp", Instant.now().toString(),
                "correlationId", correlationId != null ? correlationId : "unknown"
        ));
    }
}
