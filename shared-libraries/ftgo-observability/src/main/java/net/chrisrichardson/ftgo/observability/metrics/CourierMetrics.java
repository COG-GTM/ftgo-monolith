package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Custom business metrics for the Courier Service.
 * Tracks delivery assignment, completion, and average delivery time.
 */
@Component
public class CourierMetrics {

    private final Counter deliveriesAssigned;
    private final Counter deliveriesCompleted;
    private final Timer deliveryTime;

    public CourierMetrics(MeterRegistry registry) {
        this.deliveriesAssigned = Counter.builder("deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesCompleted = Counter.builder("deliveries.completed")
                .description("Total number of deliveries completed")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveryTime = Timer.builder("deliveries.average_time")
                .description("Time taken to complete deliveries")
                .tag("service", "courier-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * Record a delivery being assigned to a courier.
     */
    public void recordDeliveryAssigned() {
        deliveriesAssigned.increment();
    }

    /**
     * Record a delivery completion.
     */
    public void recordDeliveryCompleted() {
        deliveriesCompleted.increment();
    }

    /**
     * Record the time taken for a delivery.
     *
     * @param duration the delivery duration
     */
    public void recordDeliveryTime(Duration duration) {
        deliveryTime.record(duration);
    }
}
