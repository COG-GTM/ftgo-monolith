package net.chrisrichardson.ftgo.apigateway.health;

import net.chrisrichardson.ftgo.apigateway.config.GatewayServiceProperties;
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
 * Health indicator that aggregates the health status of all downstream microservices.
 * Checks the /actuator/health endpoint of each service and reports the aggregate status.
 */
@Component
public class DownstreamServicesHealthIndicator implements ReactiveHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DownstreamServicesHealthIndicator.class);
    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final Map<String, String> serviceUrls;

    public DownstreamServicesHealthIndicator(GatewayServiceProperties properties) {
        this.webClient = WebClient.builder().build();
        this.serviceUrls = Map.of(
                "order-service", properties.getOrderServiceUrl(),
                "consumer-service", properties.getConsumerServiceUrl(),
                "restaurant-service", properties.getRestaurantServiceUrl(),
                "courier-service", properties.getCourierServiceUrl()
        );
    }

    @Override
    public Mono<Health> health() {
        return Flux.fromIterable(serviceUrls.entrySet())
                .flatMap(entry -> checkServiceHealth(entry.getKey(), entry.getValue()))
                .collectList()
                .map(results -> {
                    Health.Builder builder = Health.up();
                    boolean allUp = true;

                    for (ServiceHealthResult result : results) {
                        builder.withDetail(result.serviceName(), result.status());
                        if (!"UP".equals(result.status())) {
                            allUp = false;
                        }
                    }

                    if (!allUp) {
                        builder.down();
                    }

                    return builder.build();
                });
    }

    private Mono<ServiceHealthResult> checkServiceHealth(String serviceName, String serviceUrl) {
        return webClient.get()
                .uri(serviceUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(HEALTH_CHECK_TIMEOUT)
                .map(body -> new ServiceHealthResult(serviceName, "UP"))
                .onErrorResume(e -> {
                    log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
                    return Mono.just(new ServiceHealthResult(serviceName, "DOWN"));
                });
    }

    private record ServiceHealthResult(String serviceName, String status) {
    }
}
