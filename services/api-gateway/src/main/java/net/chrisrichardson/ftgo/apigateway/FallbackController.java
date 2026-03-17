package net.chrisrichardson.ftgo.apigateway;

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
 * Fallback controller that handles requests when a circuit breaker is open
 * for a downstream service. Returns a standardized error response.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> ordersFallback() {
        return fallbackResponse("Order Service");
    }

    @GetMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> consumersFallback() {
        return fallbackResponse("Consumer Service");
    }

    @GetMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> restaurantsFallback() {
        return fallbackResponse("Restaurant Service");
    }

    @GetMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> couriersFallback() {
        return fallbackResponse("Courier Service");
    }

    private Mono<Map<String, Object>> fallbackResponse(String serviceName) {
        log.warn("Circuit breaker fallback triggered for {}", serviceName);
        return Mono.just(Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", serviceName + " is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString()
        ));
    }
}
