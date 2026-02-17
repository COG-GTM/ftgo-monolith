package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersCompleted;
    private final Counter ordersCancelled;
    private final Counter ordersFailed;
    private final Timer orderProcessingTime;
    private final DistributionSummary orderTotalAmount;
    private final Counter orderRevisions;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder(FtgoMetricsConstants.PREFIX_ORDER + ".created.total")
                .description("Total number of orders created")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.ordersCompleted = Counter.builder(FtgoMetricsConstants.PREFIX_ORDER + ".completed.total")
                .description("Total number of orders completed")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.ordersCancelled = Counter.builder(FtgoMetricsConstants.PREFIX_ORDER + ".cancelled.total")
                .description("Total number of orders cancelled")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.ordersFailed = Counter.builder(FtgoMetricsConstants.PREFIX_ORDER + ".failed.total")
                .description("Total number of orders that failed")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.orderProcessingTime = Timer.builder(FtgoMetricsConstants.PREFIX_ORDER + ".processing.duration")
                .description("Time taken to process an order")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.orderTotalAmount = DistributionSummary.builder(FtgoMetricsConstants.PREFIX_ORDER + ".total.amount")
                .description("Distribution of order total amounts")
                .baseUnit("dollars")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);

        this.orderRevisions = Counter.builder(FtgoMetricsConstants.PREFIX_ORDER + ".revisions.total")
                .description("Total number of order revisions")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "order-service")
                .register(registry);
    }

    public Counter getOrdersCreated() {
        return ordersCreated;
    }

    public Counter getOrdersCompleted() {
        return ordersCompleted;
    }

    public Counter getOrdersCancelled() {
        return ordersCancelled;
    }

    public Counter getOrdersFailed() {
        return ordersFailed;
    }

    public Timer getOrderProcessingTime() {
        return orderProcessingTime;
    }

    public DistributionSummary getOrderTotalAmount() {
        return orderTotalAmount;
    }

    public Counter getOrderRevisions() {
        return orderRevisions;
    }
}
