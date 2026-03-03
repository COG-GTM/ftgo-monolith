package com.ftgo.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the FTGO Order Service.
 *
 * <p>Registers and exposes the following metrics:
 * <ul>
 *   <li>{@code ftgo.orders.created} - Counter of orders created</li>
 *   <li>{@code ftgo.orders.cancelled} - Counter of orders cancelled</li>
 *   <li>{@code ftgo.orders.accepted} - Counter of orders accepted by restaurants</li>
 *   <li>{@code ftgo.orders.rejected} - Counter of orders rejected</li>
 *   <li>{@code ftgo.orders.delivered} - Counter of orders delivered</li>
 *   <li>{@code ftgo.order.total.amount} - Distribution summary of order amounts</li>
 *   <li>{@code ftgo.order.processing.time} - Timer for order processing duration</li>
 * </ul>
 */
@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersCancelled;
    private final Counter ordersAccepted;
    private final Counter ordersRejected;
    private final Counter ordersDelivered;
    private final DistributionSummary orderTotalAmount;
    private final Timer orderProcessingTime;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("ftgo.orders.created")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(registry);

        this.ordersCancelled = Counter.builder("ftgo.orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(registry);

        this.ordersAccepted = Counter.builder("ftgo.orders.accepted")
                .description("Total number of orders accepted by restaurants")
                .tag("service", "order-service")
                .register(registry);

        this.ordersRejected = Counter.builder("ftgo.orders.rejected")
                .description("Total number of orders rejected")
                .tag("service", "order-service")
                .register(registry);

        this.ordersDelivered = Counter.builder("ftgo.orders.delivered")
                .description("Total number of orders delivered")
                .tag("service", "order-service")
                .register(registry);

        this.orderTotalAmount = DistributionSummary.builder("ftgo.order.total.amount")
                .description("Distribution of order total amounts in cents")
                .tag("service", "order-service")
                .baseUnit("cents")
                .register(registry);

        this.orderProcessingTime = Timer.builder("ftgo.order.processing.time")
                .description("Time taken to process an order from creation to acceptance")
                .tag("service", "order-service")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreated.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelled.increment();
    }

    public void incrementOrdersAccepted() {
        ordersAccepted.increment();
    }

    public void incrementOrdersRejected() {
        ordersRejected.increment();
    }

    public void incrementOrdersDelivered() {
        ordersDelivered.increment();
    }

    public void recordOrderAmount(double amountInCents) {
        orderTotalAmount.record(amountInCents);
    }

    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start();
    }

    public void stopOrderProcessingTimer(Timer.Sample sample) {
        sample.stop(orderProcessingTime);
    }
}
