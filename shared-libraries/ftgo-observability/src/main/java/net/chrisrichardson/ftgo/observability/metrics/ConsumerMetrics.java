package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Consumer Service.
 * Tracks consumer registration and validation events.
 * Only activated when spring.application.name is set to consumer-service.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "consumer-service")
public class ConsumerMetrics {

    private final Counter consumersRegistered;
    private final Counter consumersValidated;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder("consumers.registered")
                .description("Total number of consumers registered")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersValidated = Counter.builder("consumers.validated")
                .description("Total number of consumer validations performed")
                .tag("service", "consumer-service")
                .register(registry);
    }

    /**
     * Record a new consumer registration.
     */
    public void recordConsumerRegistered() {
        consumersRegistered.increment();
    }

    /**
     * Record a consumer validation event.
     */
    public void recordConsumerValidated() {
        consumersValidated.increment();
    }
}
