package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Custom business metrics for the Courier Service.
 * Tracks delivery assignment, completion, and average delivery time.
 */
public class CourierMetrics {

    private final Counter deliveriesAssigned;
    private final Counter deliveriesCompleted;
    private final Timer deliveryTime;

    public CourierMetrics(MeterRegistry registry) {
        this.deliveriesAssigned = Counter.builder("deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .register(registry);

        this.deliveriesCompleted = Counter.builder("deliveries.completed")
                .description("Total number of deliveries completed")
                .register(registry);

        this.deliveryTime = Timer.builder("deliveries.average_time")
                .description("Time taken to complete deliveries")
                .register(registry);
    }

    public void recordDeliveryAssigned() {
        deliveriesAssigned.increment();
    }

    public void recordDeliveryCompleted() {
        deliveriesCompleted.increment();
    }

    public Timer getDeliveryTimer() {
        return deliveryTime;
    }
}
