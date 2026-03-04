package net.chrisrichardson.ftgo.tracing.span;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pre-defined custom span operations for the Courier/Delivery Service.
 *
 * <p>Provides convenience methods that create consistently named and tagged
 * spans for delivery-related business operations. These spans appear in
 * Zipkin/Jaeger as child spans within the request trace.
 *
 * <p>Span naming convention: {@code delivery.<operation>}
 * <p>Standard tags:
 * <ul>
 *   <li>{@code delivery.id} — The delivery identifier</li>
 *   <li>{@code delivery.operation} — The operation type</li>
 *   <li>{@code courier.id} — The assigned courier identifier</li>
 * </ul>
 */
@Component
public class DeliveryTracingOperations {

    private static final Logger log = LoggerFactory.getLogger(DeliveryTracingOperations.class);

    private final Tracer tracer;

    public DeliveryTracingOperations(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Starts a span for delivery assignment to a courier.
     *
     * @param deliveryId the delivery identifier
     * @param courierId  the courier being assigned
     * @return the started span
     */
    public Span startDeliveryAssignment(String deliveryId, String courierId) {
        return tracer.nextSpan()
                .name("delivery.assign")
                .tag("delivery.operation", "assign")
                .tag("delivery.id", deliveryId)
                .tag("courier.id", courierId)
                .start();
    }

    /**
     * Starts a span for delivery pickup.
     *
     * @param deliveryId the delivery identifier
     * @return the started span
     */
    public Span startDeliveryPickup(String deliveryId) {
        return tracer.nextSpan()
                .name("delivery.pickup")
                .tag("delivery.operation", "pickup")
                .tag("delivery.id", deliveryId)
                .start();
    }

    /**
     * Starts a span for delivery completion.
     *
     * @param deliveryId the delivery identifier
     * @return the started span
     */
    public Span startDeliveryCompletion(String deliveryId) {
        return tracer.nextSpan()
                .name("delivery.complete")
                .tag("delivery.operation", "complete")
                .tag("delivery.id", deliveryId)
                .start();
    }

    /**
     * Starts a span for courier availability update.
     *
     * @param courierId the courier identifier
     * @param available whether the courier is available
     * @return the started span
     */
    public Span startCourierAvailabilityUpdate(String courierId, boolean available) {
        return tracer.nextSpan()
                .name("courier.availability")
                .tag("delivery.operation", "availability-update")
                .tag("courier.id", courierId)
                .tag("courier.available", String.valueOf(available))
                .start();
    }

    /**
     * Ends the span, recording success.
     *
     * @param span the span to end
     */
    public void endSpan(Span span) {
        if (span != null) {
            span.end();
        }
    }

    /**
     * Ends the span, recording an error.
     *
     * @param span      the span to end
     * @param throwable the error that occurred
     */
    public void endSpanWithError(Span span, Throwable throwable) {
        if (span != null) {
            span.error(throwable);
            span.end();
        }
    }
}
