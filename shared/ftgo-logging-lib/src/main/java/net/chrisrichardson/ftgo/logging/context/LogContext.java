package net.chrisrichardson.ftgo.logging.context;

import org.slf4j.MDC;

/**
 * Static utility class for managing SLF4J MDC (Mapped Diagnostic Context) fields
 * across FTGO microservices.
 *
 * <p>Provides a simplified, static API for setting and clearing MDC fields used in
 * structured logging. This class complements {@link net.chrisrichardson.ftgo.logging.mdc.MdcContextLifecycle}
 * by offering direct static access for use in application code without requiring
 * a bean reference.
 *
 * <p>Standard MDC fields:
 * <ul>
 *   <li>{@code service} — service name</li>
 *   <li>{@code userId} — authenticated user ID</li>
 *   <li>{@code requestId} — unique request identifier</li>
 *   <li>{@code traceId} — distributed trace ID (typically set by tracing library)</li>
 *   <li>{@code spanId} — span ID (typically set by tracing library)</li>
 *   <li>{@code correlationId} — request correlation ID</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * LogContext.setUserId("user-123");
 * LogContext.setRequestId("req-456");
 * try {
 *     // ... business logic with enriched MDC
 * } finally {
 *     LogContext.clearRequestContext();
 * }
 * </pre>
 */
public final class LogContext {

    /** MDC key for the service name. */
    public static final String KEY_SERVICE = "service";

    /** MDC key for the authenticated user ID. */
    public static final String KEY_USER_ID = "userId";

    /** MDC key for the unique request identifier. */
    public static final String KEY_REQUEST_ID = "requestId";

    /** MDC key for the distributed trace ID. */
    public static final String KEY_TRACE_ID = "traceId";

    /** MDC key for the span ID. */
    public static final String KEY_SPAN_ID = "spanId";

    /** MDC key for the correlation ID. */
    public static final String KEY_CORRELATION_ID = "correlationId";

    private LogContext() {
        // Utility class — no instantiation
    }

    /**
     * Sets the service name in MDC.
     *
     * @param serviceName the service name
     */
    public static void setServiceName(String serviceName) {
        putIfNotNull(KEY_SERVICE, serviceName);
    }

    /**
     * Sets the user ID in MDC.
     *
     * @param userId the authenticated user ID
     */
    public static void setUserId(String userId) {
        putIfNotNull(KEY_USER_ID, userId);
    }

    /**
     * Sets the request ID in MDC.
     *
     * @param requestId the unique request identifier
     */
    public static void setRequestId(String requestId) {
        putIfNotNull(KEY_REQUEST_ID, requestId);
    }

    /**
     * Sets the trace ID in MDC.
     *
     * @param traceId the distributed trace ID
     */
    public static void setTraceId(String traceId) {
        putIfNotNull(KEY_TRACE_ID, traceId);
    }

    /**
     * Sets the span ID in MDC.
     *
     * @param spanId the span ID
     */
    public static void setSpanId(String spanId) {
        putIfNotNull(KEY_SPAN_ID, spanId);
    }

    /**
     * Sets the correlation ID in MDC.
     *
     * @param correlationId the correlation ID
     */
    public static void setCorrelationId(String correlationId) {
        putIfNotNull(KEY_CORRELATION_ID, correlationId);
    }

    /**
     * Sets all request-scoped MDC fields at once.
     *
     * @param userId        the authenticated user ID (nullable)
     * @param requestId     the unique request identifier (nullable)
     * @param correlationId the correlation ID (nullable)
     */
    public static void setRequestContext(String userId, String requestId, String correlationId) {
        setUserId(userId);
        setRequestId(requestId);
        setCorrelationId(correlationId);
    }

    /**
     * Executes the given {@link Runnable} with the specified MDC context fields set,
     * then clears the request context upon completion.
     *
     * @param action        the action to execute
     * @param userId        the user ID to set (nullable)
     * @param requestId     the request ID to set (nullable)
     */
    public static void withContext(Runnable action, String userId, String requestId) {
        setUserId(userId);
        setRequestId(requestId);
        try {
            action.run();
        } finally {
            clearRequestContext();
        }
    }

    /**
     * Clears all request-scoped MDC fields. The service name is preserved.
     * Call this at the end of request processing to prevent MDC leaking
     * between requests in thread-pooled environments.
     */
    public static void clearRequestContext() {
        MDC.remove(KEY_USER_ID);
        MDC.remove(KEY_REQUEST_ID);
        MDC.remove(KEY_CORRELATION_ID);
        // traceId and spanId are managed by the tracing library
    }

    /**
     * Clears all MDC fields, including the service name.
     */
    public static void clearAll() {
        MDC.clear();
    }

    /**
     * Returns the current value of the specified MDC key, or {@code null} if not set.
     *
     * @param key the MDC key
     * @return the current value, or null
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    private static void putIfNotNull(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }
}
