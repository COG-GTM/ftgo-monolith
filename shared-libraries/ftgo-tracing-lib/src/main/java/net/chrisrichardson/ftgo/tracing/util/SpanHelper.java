package net.chrisrichardson.ftgo.tracing.util;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;

import java.util.concurrent.Callable;

/**
 * Utility for creating and managing custom spans in service code.
 *
 * <p>Wraps Brave's {@link Tracer} to provide a convenient, error-safe API
 * for instrumenting business operations with distributed tracing spans.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * // Simple scoped span (auto-finished)
 * String result = spanHelper.executeInSpan("processOrder", () -> {
 *     return orderService.process(orderId);
 * });
 *
 * // Span with tags
 * spanHelper.executeInSpan("validatePayment", span -> {
 *     span.tag("payment.method", "CREDIT_CARD");
 *     paymentService.validate(payment);
 * });
 * }</pre>
 */
public class SpanHelper {

    private final Tracer tracer;

    public SpanHelper(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Returns the underlying Brave {@link Tracer} for advanced use cases.
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Executes a {@link Callable} within a new child span. The span is
     * automatically finished when the callable completes or throws.
     *
     * @param spanName name of the span
     * @param callable the operation to trace
     * @param <T>      return type
     * @return the result of the callable
     * @throws Exception if the callable throws
     */
    public <T> T executeInSpan(String spanName, Callable<T> callable) throws Exception {
        ScopedSpan span = tracer.startScopedSpan(spanName);
        try {
            return callable.call();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    /**
     * Executes a {@link Runnable} within a new child span. The span is
     * automatically finished when the runnable completes or throws.
     *
     * @param spanName name of the span
     * @param runnable the operation to trace
     */
    public void executeInSpan(String spanName, Runnable runnable) {
        ScopedSpan span = tracer.startScopedSpan(spanName);
        try {
            runnable.run();
        } catch (RuntimeException e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    /**
     * Executes a {@link SpanConsumer} within a new child span, providing
     * access to the span for adding tags and annotations.
     *
     * @param spanName     name of the span
     * @param spanConsumer consumer that receives the span for customization
     */
    public void executeInSpanWithAccess(String spanName, SpanConsumer spanConsumer) {
        Span span = tracer.nextSpan().name(spanName).start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            spanConsumer.accept(span);
        } catch (RuntimeException e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    /**
     * Executes a {@link SpanFunction} within a new child span, providing
     * access to the span for adding tags and returning a result.
     *
     * @param spanName     name of the span
     * @param spanFunction function that receives the span and returns a result
     * @param <T>          return type
     * @return the result of the function
     * @throws Exception if the function throws
     */
    public <T> T executeInSpanWithAccess(String spanName, SpanFunction<T> spanFunction) throws Exception {
        Span span = tracer.nextSpan().name(spanName).start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return spanFunction.apply(span);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    /**
     * Returns the current trace ID, or {@code null} if no trace is active.
     */
    public String currentTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return null;
        }
        return currentSpan.context().traceIdString();
    }

    /**
     * Returns the current span ID, or {@code null} if no span is active.
     */
    public String currentSpanId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return null;
        }
        return Long.toHexString(currentSpan.context().spanId());
    }

    /**
     * Functional interface for operations that consume a span for adding tags/annotations.
     */
    @FunctionalInterface
    public interface SpanConsumer {
        void accept(Span span);
    }

    /**
     * Functional interface for operations that consume a span and return a result.
     *
     * @param <T> return type
     */
    @FunctionalInterface
    public interface SpanFunction<T> {
        T apply(Span span) throws Exception;
    }
}
