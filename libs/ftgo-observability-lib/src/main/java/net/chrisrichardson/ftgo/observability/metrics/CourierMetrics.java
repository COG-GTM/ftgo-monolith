package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class CourierMetrics {

    private final Counter couriersRegistered;
    private final Counter deliveriesAssigned;
    private final Counter deliveriesCompleted;
    private final Counter deliveriesFailed;
    private final Timer deliveryTime;
    private final Counter courierAvailabilityChanges;
    private final DistributionSummary deliveryDistance;

    public CourierMetrics(MeterRegistry registry) {
        this.couriersRegistered = Counter.builder(FtgoMetricsConstants.PREFIX_COURIER + ".registered.total")
                .description("Total number of couriers registered")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.deliveriesAssigned = Counter.builder(FtgoMetricsConstants.PREFIX_COURIER + ".deliveries.assigned.total")
                .description("Total number of deliveries assigned to couriers")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.deliveriesCompleted = Counter.builder(FtgoMetricsConstants.PREFIX_COURIER + ".deliveries.completed.total")
                .description("Total number of deliveries completed")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.deliveriesFailed = Counter.builder(FtgoMetricsConstants.PREFIX_COURIER + ".deliveries.failed.total")
                .description("Total number of deliveries that failed")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.deliveryTime = Timer.builder(FtgoMetricsConstants.PREFIX_COURIER + ".delivery.duration")
                .description("Time taken to complete a delivery")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.courierAvailabilityChanges = Counter.builder(FtgoMetricsConstants.PREFIX_COURIER + ".availability.changes.total")
                .description("Total number of courier availability status changes")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);

        this.deliveryDistance = DistributionSummary.builder(FtgoMetricsConstants.PREFIX_COURIER + ".delivery.distance")
                .description("Distribution of delivery distances")
                .baseUnit("kilometers")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "courier-service")
                .register(registry);
    }

    public Counter getCouriersRegistered() {
        return couriersRegistered;
    }

    public Counter getDeliveriesAssigned() {
        return deliveriesAssigned;
    }

    public Counter getDeliveriesCompleted() {
        return deliveriesCompleted;
    }

    public Counter getDeliveriesFailed() {
        return deliveriesFailed;
    }

    public Timer getDeliveryTime() {
        return deliveryTime;
    }

    public Counter getCourierAvailabilityChanges() {
        return courierAvailabilityChanges;
    }

    public DistributionSummary getDeliveryDistance() {
        return deliveryDistance;
    }
}
