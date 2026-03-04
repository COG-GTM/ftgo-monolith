package net.chrisrichardson.ftgo.tracing.span;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Utility class for creating custom spans around business operations.
 *
 * <p>Provides convenience methods for wrapping operations in named spans
 * with tags, enabling fine-grained tracing of key business flows like
 * order creation, delivery assignment, etc.
 *
 * <p>Usage:
 * <pre>
 * // Wrap a void operation
 * FtgoSpanUtils.executeInSpan(tracer, "order.create", () -> {
 *     orderService.createOrder(request);
 * });
 *
 * // Wrap an operation that returns a value
 * Order order = FtgoSpanUtils.executeInSpan(tracer, "order.create", () -> {
 *     return orderService.createOrder(request);
 * });
 * </pre>
 */
public final class FtgoSpanUtils {

    private static final Logger log = LoggerFactory.getLogger(FtgoSpanUtils.class);

    private FtgoSpanUtils() {
        // Utility class — not instantiable
    }

    /**
     * Executes a {@link Runnable} within a new span.
     *
     * @param tracer   the Micrometer Tracer
     * @param spanName the name for the new span
     * @param action   the operation to execute
     */
    public static void executeInSpan(Tracer tracer, String spanName, Runnable action) {
        Span span = tracer.nextSpan().name(spanName).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            action.run();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Executes a {@link Supplier} within a new span and returns its result.
     *
     * @param tracer   the Micrometer Tracer
     * @param spanName the name for the new span
     * @param supplier the operation to execute
     * @param <T>      the return type
     * @return the result of the supplier
     */
    public static <T> T executeInSpan(Tracer tracer, String spanName, Supplier<T> supplier) {
        Span span = tracer.nextSpan().name(spanName).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            return supplier.get();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Executes a {@link Runnable} within a new span with additional tags.
     *
     * @param tracer   the Micrometer Tracer
     * @param spanName the name for the new span
     * @param tagKey   the tag key
     * @param tagValue the tag value
     * @param action   the operation to execute
     */
    public static void executeInSpan(Tracer tracer, String spanName,
                                     String tagKey, String tagValue,
                                     Runnable action) {
        Span span = tracer.nextSpan().name(spanName).tag(tagKey, tagValue).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            action.run();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Executes a {@link Supplier} within a new span with additional tags.
     *
     * @param tracer   the Micrometer Tracer
     * @param spanName the name for the new span
     * @param tagKey   the tag key
     * @param tagValue the tag value
     * @param supplier the operation to execute
     * @param <T>      the return type
     * @return the result of the supplier
     */
    public static <T> T executeInSpan(Tracer tracer, String spanName,
                                      String tagKey, String tagValue,
                                      Supplier<T> supplier) {
        Span span = tracer.nextSpan().name(spanName).tag(tagKey, tagValue).start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            return supplier.get();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
