package net.chrisrichardson.ftgo.metrics.courier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Custom business metrics for the Courier Service.
 *
 * <p>Tracks:
 * <ul>
 *   <li>{@code deliveries.assigned} — Counter of deliveries assigned to couriers</li>
 *   <li>{@code deliveries.completed} — Counter of deliveries completed</li>
 *   <li>{@code deliveries.average_time} — Timer tracking delivery duration</li>
 * </ul>
 */
@Component
public class CourierMetrics {

    private static final Logger log = LoggerFactory.getLogger(CourierMetrics.class);

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
                .description("Time taken for deliveries")
                .tag("service", "courier-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        log.info("Courier Service business metrics registered");
    }

    /**
     * Increments the deliveries assigned counter.
     */
    public void deliveryAssigned() {
        deliveriesAssigned.increment();
    }

    /**
     * Increments the deliveries completed counter.
     */
    public void deliveryCompleted() {
        deliveriesCompleted.increment();
    }

    /**
     * Records a delivery duration.
     *
     * @param duration the time taken for the delivery
     */
    public void recordDeliveryTime(Duration duration) {
        deliveryTime.record(duration);
    }
}
