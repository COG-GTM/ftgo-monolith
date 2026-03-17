package net.chrisrichardson.ftgo.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/orders")
    public Mono<Map<String, Object>> ordersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("order-service", exchange);
    }

    @RequestMapping("/consumers")
    public Mono<Map<String, Object>> consumersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("consumer-service", exchange);
    }

    @RequestMapping("/restaurants")
    public Mono<Map<String, Object>> restaurantsFallback(ServerWebExchange exchange) {
        return createFallbackResponse("restaurant-service", exchange);
    }

    @RequestMapping("/couriers")
    public Mono<Map<String, Object>> couriersFallback(ServerWebExchange exchange) {
        return createFallbackResponse("courier-service", exchange);
    }

    private Mono<Map<String, Object>> createFallbackResponse(String serviceName, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

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
