package net.chrisrichardson.ftgo.gateway.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DownstreamHealthIndicator implements ReactiveHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DownstreamHealthIndicator.class);

    private final WebClient webClient;
    private final List<ServiceHealthEndpoint> serviceEndpoints;

    public DownstreamHealthIndicator(
            WebClient.Builder webClientBuilder,
            @Value("${gateway.health.order-service-url:http://order-service:8080}") String orderServiceUrl,
            @Value("${gateway.health.consumer-service-url:http://consumer-service:8080}") String consumerServiceUrl,
            @Value("${gateway.health.restaurant-service-url:http://restaurant-service:8080}") String restaurantServiceUrl,
            @Value("${gateway.health.courier-service-url:http://courier-service:8080}") String courierServiceUrl,
            @Value("${gateway.health.timeout-seconds:3}") int timeoutSeconds) {

        this.webClient = webClientBuilder.build();
        this.serviceEndpoints = List.of(
                new ServiceHealthEndpoint("order-service", orderServiceUrl + "/actuator/health", timeoutSeconds),
                new ServiceHealthEndpoint("consumer-service", consumerServiceUrl + "/actuator/health", timeoutSeconds),
                new ServiceHealthEndpoint("restaurant-service", restaurantServiceUrl + "/actuator/health", timeoutSeconds),
                new ServiceHealthEndpoint("courier-service", courierServiceUrl + "/actuator/health", timeoutSeconds)
        );
    }

    @Override
    public Mono<Health> health() {
        return Flux.fromIterable(serviceEndpoints)
                .flatMap(this::checkServiceHealth)
                .collectList()
                .map(results -> {
                    Map<String, Object> details = new LinkedHashMap<>();
                    boolean allHealthy = true;

                    for (ServiceHealthResult result : results) {
                        details.put(result.serviceName(), result.status());
                        if (!"UP".equals(result.status())) {
                            allHealthy = false;
                        }
                    }

                    Health.Builder builder = allHealthy ? Health.up() : Health.down();
                    return builder.withDetails(details).build();
                });
    }

    private Mono<ServiceHealthResult> checkServiceHealth(ServiceHealthEndpoint endpoint) {
        return webClient.get()
                .uri(endpoint.healthUrl())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(endpoint.timeoutSeconds()))
                .map(body -> new ServiceHealthResult(endpoint.serviceName(), "UP"))
                .onErrorResume(ex -> {
                    log.warn("Health check failed for {}: {}", endpoint.serviceName(), ex.getMessage());
                    return Mono.just(new ServiceHealthResult(endpoint.serviceName(), "DOWN"));
                });
    }

    private record ServiceHealthEndpoint(String serviceName, String healthUrl, int timeoutSeconds) {}
    private record ServiceHealthResult(String serviceName, String status) {}
}
