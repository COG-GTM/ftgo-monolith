package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Custom business metrics for the Consumer Service.
 * Tracks consumer registration and validation events.
 */
public class ConsumerMetrics {

    private final Counter consumersRegistered;
    private final Counter consumersValidated;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder("consumers.registered")
                .description("Total number of consumers registered")
                .register(registry);

        this.consumersValidated = Counter.builder("consumers.validated")
                .description("Total number of consumer validations performed")
                .register(registry);
    }

    public void recordConsumerRegistered() {
        consumersRegistered.increment();
    }

    public void recordConsumerValidated() {
        consumersValidated.increment();
    }
}
