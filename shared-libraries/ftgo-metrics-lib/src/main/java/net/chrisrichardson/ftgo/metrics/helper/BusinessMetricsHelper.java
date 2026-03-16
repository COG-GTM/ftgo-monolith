package net.chrisrichardson.ftgo.metrics.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for recording business-level metrics in FTGO services.
 * Provides convenient methods for tracking orders, deliveries,
 * and other domain-specific events.
 */
public class BusinessMetricsHelper {

    private final MeterRegistry meterRegistry;

    public BusinessMetricsHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Increments a counter for a business event.
     *
     * @param name   metric name (e.g., "orders_created")
     * @param tags   key-value pairs for metric dimensions
     */
    public void incrementCounter(String name, String... tags) {
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a timed business operation.
     *
     * @param name     metric name (e.g., "order_processing_duration")
     * @param duration the duration value
     * @param unit     the time unit
     * @param tags     key-value pairs for metric dimensions
     */
    public void recordTimer(String name, long duration, TimeUnit unit, String... tags) {
        Timer.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .record(duration, unit);
    }

    /**
     * Records a gauge value for a business metric.
     *
     * @param name  metric name (e.g., "active_orders")
     * @param value the current gauge value
     * @param tags  key-value pairs for metric dimensions
     */
    public void recordGauge(String name, double value, String... tags) {
        meterRegistry.gauge(name, io.micrometer.core.instrument.Tags.of(tags),
                value);
    }

    // --- Domain-Specific Convenience Methods ---

    /**
     * Records an order creation event.
     *
     * @param restaurantId the restaurant identifier
     */
    public void orderCreated(String restaurantId) {
        incrementCounter("ftgo_orders_created_total",
                "restaurant_id", restaurantId);
    }

    /**
     * Records an order state transition.
     *
     * @param fromState the previous order state
     * @param toState   the new order state
     */
    public void orderStateChanged(String fromState, String toState) {
        incrementCounter("ftgo_order_state_transitions_total",
                "from_state", fromState,
                "to_state", toState);
    }

    /**
     * Records a delivery assignment event.
     *
     * @param courierId the courier identifier
     */
    public void deliveryAssigned(String courierId) {
        incrementCounter("ftgo_deliveries_assigned_total",
                "courier_id", courierId);
    }

    /**
     * Records a delivery completion event.
     *
     * @param courierId  the courier identifier
     * @param durationMs delivery time in milliseconds
     */
    public void deliveryCompleted(String courierId, long durationMs) {
        incrementCounter("ftgo_deliveries_completed_total",
                "courier_id", courierId);
        recordTimer("ftgo_delivery_duration_milliseconds",
                durationMs, TimeUnit.MILLISECONDS,
                "courier_id", courierId);
    }

    /**
     * Records a consumer registration event.
     */
    public void consumerRegistered() {
        incrementCounter("ftgo_consumers_registered_total");
    }

    /**
     * Records a restaurant registration event.
     */
    public void restaurantRegistered() {
        incrementCounter("ftgo_restaurants_registered_total");
    }

    /**
     * Records a menu revision event.
     *
     * @param restaurantId the restaurant identifier
     */
    public void menuRevised(String restaurantId) {
        incrementCounter("ftgo_menu_revisions_total",
                "restaurant_id", restaurantId);
    }

    /**
     * Records a payment processing event.
     *
     * @param status "success" or "failure"
     */
    public void paymentProcessed(String status) {
        incrementCounter("ftgo_payments_processed_total",
                "status", status);
    }

    /**
     * Returns the underlying MeterRegistry for advanced use cases.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
