package net.chrisrichardson.ftgo.logging;

import org.slf4j.MDC;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent API for setting and clearing MDC (Mapped Diagnostic Context) fields.
 * Implements {@link Closeable} so it can be used in try-with-resources blocks
 * to ensure MDC fields are cleaned up automatically.
 *
 * <p>Usage:
 * <pre>{@code
 * try (LogContext ctx = LogContext.create()
 *         .userId("consumer-123")
 *         .put("orderId", "order-456")
 *         .apply()) {
 *     logger.info("Processing order");
 *     // MDC contains userId and orderId
 * }
 * // MDC fields are automatically removed
 * }</pre>
 */
public final class LogContext implements Closeable {

    private final List<String> keys;

    private LogContext() {
        this.keys = new ArrayList<>();
    }

    /**
     * Creates a new LogContext builder.
     *
     * @return a new LogContext instance
     */
    public static LogContext create() {
        return new LogContext();
    }

    /**
     * Sets an arbitrary key-value pair in the MDC.
     *
     * @param key   the MDC key
     * @param value the value (ignored if null)
     * @return this LogContext for chaining
     */
    public LogContext put(String key, String value) {
        if (key != null && value != null) {
            keys.add(key);
            MDC.put(key, value);
        }
        return this;
    }

    /**
     * Sets the {@code serviceName} MDC field.
     *
     * @param serviceName the service name
     * @return this LogContext for chaining
     */
    public LogContext serviceName(String serviceName) {
        return put(LoggingConstants.MDC_SERVICE_NAME, serviceName);
    }

    /**
     * Sets the {@code userId} MDC field.
     *
     * @param userId the user ID
     * @return this LogContext for chaining
     */
    public LogContext userId(String userId) {
        return put(LoggingConstants.MDC_USER_ID, userId);
    }

    /**
     * Sets the {@code correlationId} MDC field.
     *
     * @param correlationId the correlation ID
     * @return this LogContext for chaining
     */
    public LogContext correlationId(String correlationId) {
        return put(LoggingConstants.MDC_CORRELATION_ID, correlationId);
    }

    /**
     * Sets the {@code traceId} MDC field.
     *
     * @param traceId the distributed trace ID
     * @return this LogContext for chaining
     */
    public LogContext traceId(String traceId) {
        return put(LoggingConstants.MDC_TRACE_ID, traceId);
    }

    /**
     * Sets the {@code spanId} MDC field.
     *
     * @param spanId the distributed span ID
     * @return this LogContext for chaining
     */
    public LogContext spanId(String spanId) {
        return put(LoggingConstants.MDC_SPAN_ID, spanId);
    }

    /**
     * Finalizes the context setup and returns this instance for use
     * in a try-with-resources block. This method exists for readability
     * to signal that the builder phase is complete.
     *
     * @return this LogContext
     */
    public LogContext apply() {
        return this;
    }

    /**
     * Removes all MDC keys that were set by this LogContext instance.
     */
    @Override
    public void close() {
        for (String key : keys) {
            MDC.remove(key);
        }
        keys.clear();
    }

    /**
     * Removes all MDC keys set by this context. Equivalent to {@link #close()}.
     */
    public void clear() {
        close();
    }
}
