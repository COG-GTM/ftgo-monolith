package net.chrisrichardson.ftgo.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * Health indicator that reports the status of downstream services
 * based on their circuit breaker state.
 *
 * <p>Maps circuit breaker states to health statuses:
 * <ul>
 *   <li>CLOSED → UP (healthy)</li>
 *   <li>HALF_OPEN → UP with warning details</li>
 *   <li>OPEN → DOWN (unhealthy)</li>
 *   <li>DISABLED → UP (monitoring disabled)</li>
 *   <li>FORCED_OPEN → DOWN (manually forced open)</li>
 * </ul>
 *
 * <p>This indicator contributes to the readiness probe, so an open
 * circuit breaker will mark the service as not ready.
 */
@Component("downstreamServices")
@ConditionalOnBean(CircuitBreakerRegistry.class)
public class DownstreamServiceHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DownstreamServiceHealthIndicator.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public DownstreamServiceHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new TreeMap<>();
        boolean allHealthy = true;
        boolean anyDegraded = false;

        for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            CircuitBreaker.State state = cb.getState();
            CircuitBreaker.Metrics metrics = cb.getMetrics();

            Map<String, Object> cbDetails = new TreeMap<>();
            cbDetails.put("state", state.name());
            cbDetails.put("failureRate", String.format("%.1f%%", metrics.getFailureRate()));
            cbDetails.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
            cbDetails.put("failedCalls", metrics.getNumberOfFailedCalls());
            cbDetails.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
            cbDetails.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());

            switch (state) {
                case OPEN:
                case FORCED_OPEN:
                    allHealthy = false;
                    cbDetails.put("status", "DOWN");
                    break;
                case HALF_OPEN:
                    anyDegraded = true;
                    cbDetails.put("status", "DEGRADED");
                    break;
                case CLOSED:
                case DISABLED:
                default:
                    cbDetails.put("status", "UP");
                    break;
            }

            details.put(cb.getName(), cbDetails);
        }

        if (!allHealthy) {
            return Health.down()
                    .withDetails(details)
                    .build();
        }

        if (anyDegraded) {
            return Health.up()
                    .withDetail("warning", "Some downstream services are in HALF_OPEN state")
                    .withDetails(details)
                    .build();
        }

        return Health.up()
                .withDetails(details)
                .build();
    }
}
