package net.chrisrichardson.ftgo.metrics.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Consumer Service.
 *
 * <p>Tracks:
 * <ul>
 *   <li>{@code consumers.registered} — Counter of new consumer registrations</li>
 *   <li>{@code consumers.validated} — Counter of successful consumer validations</li>
 * </ul>
 */
@Component
public class ConsumerMetrics {

    private static final Logger log = LoggerFactory.getLogger(ConsumerMetrics.class);

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

        log.info("Consumer Service business metrics registered");
    }

    /**
     * Increments the consumers registered counter.
     */
    public void consumerRegistered() {
        consumersRegistered.increment();
    }

    /**
     * Increments the consumers validated counter.
     */
    public void consumerValidated() {
        consumersValidated.increment();
    }
}
