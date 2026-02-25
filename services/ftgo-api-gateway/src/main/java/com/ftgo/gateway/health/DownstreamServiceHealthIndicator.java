package com.ftgo.gateway.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Reactive health indicator that aggregates health status from all
 * downstream microservices.
 * <p>
 * Checks the {@code /actuator/health} endpoint of each service
 * and reports the aggregate status. If any service is unhealthy,
 * the gateway reports as degraded (not DOWN, since the gateway
 * itself is still functional).
 * </p>
 */
@Component("downstreamServices")
public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DownstreamServiceHealthIndicator.class);

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;

    private static final Map<String, String> SERVICE_HEALTH_URLS = Map.of(
            "order-service", "${ftgo.gateway.services.order-service.url:http://ftgo-order-service:8081}/actuator/health",
            "consumer-service", "${ftgo.gateway.services.consumer-service.url:http://ftgo-consumer-service:8082}/actuator/health",
            "restaurant-service", "${ftgo.gateway.services.restaurant-service.url:http://ftgo-restaurant-service:8083}/actuator/health",
            "courier-service", "${ftgo.gateway.services.courier-service.url:http://ftgo-courier-service:8084}/actuator/health"
    );

    public DownstreamServiceHealthIndicator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Health> health() {
        return Flux.fromIterable(SERVICE_HEALTH_URLS.entrySet())
                .flatMap(entry -> checkServiceHealth(entry.getKey(), entry.getValue()))
                .collectList()
                .map(results -> {
                    Health.Builder builder = Health.up();
                    boolean anyDown = false;

                    for (ServiceHealthResult result : results) {
                        builder.withDetail(result.serviceName(), Map.of(
                                "status", result.status(),
                                "url", result.url()
                        ));
                        if (!"UP".equals(result.status())) {
                            anyDown = true;
                        }
                    }

                    if (anyDown) {
                        // Report as degraded but not DOWN (gateway is still operational)
                        builder.status("DEGRADED");
                    }

                    return builder.build();
                });
    }

    private Mono<ServiceHealthResult> checkServiceHealth(String serviceName, String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(HEALTH_CHECK_TIMEOUT)
                .map(body -> new ServiceHealthResult(serviceName, "UP", url))
                .onErrorResume(ex -> {
                    log.debug("Health check failed for {}: {}", serviceName, ex.getMessage());
                    return Mono.just(new ServiceHealthResult(serviceName, "DOWN", url));
                });
    }

    private record ServiceHealthResult(String serviceName, String status, String url) {
    }
}
