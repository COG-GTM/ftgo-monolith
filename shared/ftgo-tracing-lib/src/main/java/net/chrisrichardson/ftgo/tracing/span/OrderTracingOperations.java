package net.chrisrichardson.ftgo.tracing.span;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pre-defined custom span operations for the Order Service.
 *
 * <p>Provides convenience methods that create consistently named and tagged
 * spans for order-related business operations. These spans appear in Zipkin/Jaeger
 * as child spans within the request trace, making it easy to identify time spent
 * in each business operation.
 *
 * <p>Span naming convention: {@code order.<operation>}
 * <p>Standard tags:
 * <ul>
 *   <li>{@code order.id} — The order identifier</li>
 *   <li>{@code order.operation} — The operation type</li>
 * </ul>
 */
@Component
public class OrderTracingOperations {

    private static final Logger log = LoggerFactory.getLogger(OrderTracingOperations.class);

    private final Tracer tracer;

    public OrderTracingOperations(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Starts a span for order creation. The caller is responsible for ending the span.
     *
     * @return the started span with order creation tags
     */
    public Span startOrderCreation() {
        return tracer.nextSpan()
                .name("order.create")
                .tag("order.operation", "create")
                .start();
    }

    /**
     * Starts a span for order creation with a specific order ID.
     *
     * @param orderId the order identifier
     * @return the started span with order creation tags
     */
    public Span startOrderCreation(String orderId) {
        return tracer.nextSpan()
                .name("order.create")
                .tag("order.operation", "create")
                .tag("order.id", orderId)
                .start();
    }

    /**
     * Starts a span for order cancellation.
     *
     * @param orderId the order identifier
     * @return the started span
     */
    public Span startOrderCancellation(String orderId) {
        return tracer.nextSpan()
                .name("order.cancel")
                .tag("order.operation", "cancel")
                .tag("order.id", orderId)
                .start();
    }

    /**
     * Starts a span for order approval processing.
     *
     * @param orderId the order identifier
     * @return the started span
     */
    public Span startOrderApproval(String orderId) {
        return tracer.nextSpan()
                .name("order.approve")
                .tag("order.operation", "approve")
                .tag("order.id", orderId)
                .start();
    }

    /**
     * Starts a span for order revision.
     *
     * @param orderId the order identifier
     * @return the started span
     */
    public Span startOrderRevision(String orderId) {
        return tracer.nextSpan()
                .name("order.revise")
                .tag("order.operation", "revise")
                .tag("order.id", orderId)
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
