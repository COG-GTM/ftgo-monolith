package net.chrisrichardson.ftgo.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller that handles requests when a circuit breaker is open
 * for a downstream service. Returns a standardized error response.
 * Handles all HTTP methods so that POST/PUT/DELETE also receive fallback JSON.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE,
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Map<String, Object>> ordersFallback() {
        return fallbackResponse("Order Service");
    }

    @RequestMapping(value = "/consumers", produces = MediaType.APPLICATION_JSON_VALUE,
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Map<String, Object>> consumersFallback() {
        return fallbackResponse("Consumer Service");
    }

    @RequestMapping(value = "/restaurants", produces = MediaType.APPLICATION_JSON_VALUE,
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Map<String, Object>> restaurantsFallback() {
        return fallbackResponse("Restaurant Service");
    }

    @RequestMapping(value = "/couriers", produces = MediaType.APPLICATION_JSON_VALUE,
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH})
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
