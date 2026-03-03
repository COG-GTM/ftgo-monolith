package com.ftgo.logging.context;

import com.ftgo.logging.LoggingConstants;
import org.slf4j.MDC;

/**
 * Utility class for managing FTGO logging context via SLF4J MDC (Mapped Diagnostic Context).
 *
 * <p>Provides convenient methods to set and clear standard MDC fields used across
 * all FTGO microservices. This class is the primary API for application code to
 * enrich log entries with contextual information.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * // After authentication, set user context
 * LogContext.setUserId("user-42");
 *
 * // Set request-scoped context
 * LogContext.setRequestId("req-001");
 *
 * // Add custom context
 * LogContext.put("orderId", "order-123");
 *
 * // Clear all FTGO MDC fields when done
 * LogContext.clear();
 * </pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>MDC is inherently thread-local. Each thread has its own MDC map. When using
 * thread pools or async processing, MDC values must be explicitly propagated to
 * child threads. Consider using {@code MDC.getCopyOfContextMap()} and
 * {@code MDC.setContextMap()} for async scenarios.</p>
 */
public final class LogContext {

    private LogContext() {
        // Utility class - prevent instantiation
    }

    // -------------------------------------------------------------------------
    // Standard Field Setters
    // -------------------------------------------------------------------------

    /**
     * Sets the user ID in MDC. Call this after authentication to include
     * the user identifier in all subsequent log entries on this thread.
     *
     * @param userId the authenticated user identifier
     */
    public static void setUserId(String userId) {
        MDC.put(LoggingConstants.MDC_USER_ID, userId);
    }

    /**
     * Sets the request ID in MDC.
     *
     * @param requestId the unique request identifier
     */
    public static void setRequestId(String requestId) {
        MDC.put(LoggingConstants.MDC_REQUEST_ID, requestId);
    }

    /**
     * Sets the correlation ID in MDC. Typically set by {@code CorrelationIdFilter},
     * but can be set manually for non-HTTP contexts (e.g., message consumers).
     *
     * @param correlationId the cross-service correlation identifier
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(LoggingConstants.MDC_CORRELATION_ID, correlationId);
    }

    /**
     * Sets the trace ID in MDC. Typically set by the tracing library,
     * but can be set manually if needed.
     *
     * @param traceId the distributed trace identifier
     */
    public static void setTraceId(String traceId) {
        MDC.put(LoggingConstants.MDC_TRACE_ID, traceId);
    }

    /**
     * Sets the span ID in MDC. Typically set by the tracing library,
     * but can be set manually if needed.
     *
     * @param spanId the span identifier within a trace
     */
    public static void setSpanId(String spanId) {
        MDC.put(LoggingConstants.MDC_SPAN_ID, spanId);
    }

    /**
     * Sets the service name in MDC. Typically set by {@code ServiceNameInitializer},
     * but can be set manually if needed.
     *
     * @param serviceName the service name
     */
    public static void setServiceName(String serviceName) {
        MDC.put(LoggingConstants.MDC_SERVICE_NAME, serviceName);
    }

    // -------------------------------------------------------------------------
    // Generic MDC Operations
    // -------------------------------------------------------------------------

    /**
     * Sets a custom key-value pair in MDC. Use this for application-specific
     * context that should appear in log entries (e.g., orderId, customerId).
     *
     * @param key   the MDC key
     * @param value the MDC value
     */
    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Gets a value from MDC by key.
     *
     * @param key the MDC key
     * @return the value, or null if not set
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Removes a single key from MDC.
     *
     * @param key the MDC key to remove
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    // -------------------------------------------------------------------------
    // Bulk Operations
    // -------------------------------------------------------------------------

    /**
     * Clears all standard FTGO MDC fields. Call this at the end of a request
     * or processing unit to prevent context leaking between operations.
     *
     * <p>This clears: userId, requestId, correlationId, traceId, spanId,
     * serviceName, requestMethod, requestUri, and clientIp.</p>
     */
    public static void clear() {
        MDC.remove(LoggingConstants.MDC_USER_ID);
        MDC.remove(LoggingConstants.MDC_REQUEST_ID);
        MDC.remove(LoggingConstants.MDC_CORRELATION_ID);
        MDC.remove(LoggingConstants.MDC_TRACE_ID);
        MDC.remove(LoggingConstants.MDC_SPAN_ID);
        MDC.remove(LoggingConstants.MDC_SERVICE_NAME);
        MDC.remove(LoggingConstants.MDC_REQUEST_METHOD);
        MDC.remove(LoggingConstants.MDC_REQUEST_URI);
        MDC.remove(LoggingConstants.MDC_CLIENT_IP);
    }

    /**
     * Clears the entire MDC map, including any custom fields.
     * Use with caution as this removes all MDC context, not just FTGO fields.
     */
    public static void clearAll() {
        MDC.clear();
    }

    // -------------------------------------------------------------------------
    // Convenience Getters
    // -------------------------------------------------------------------------

    /**
     * Gets the current user ID from MDC.
     *
     * @return the user ID, or null if not set
     */
    public static String getUserId() {
        return MDC.get(LoggingConstants.MDC_USER_ID);
    }

    /**
     * Gets the current correlation ID from MDC.
     *
     * @return the correlation ID, or null if not set
     */
    public static String getCorrelationId() {
        return MDC.get(LoggingConstants.MDC_CORRELATION_ID);
    }

    /**
     * Gets the current request ID from MDC.
     *
     * @return the request ID, or null if not set
     */
    public static String getRequestId() {
        return MDC.get(LoggingConstants.MDC_REQUEST_ID);
    }

    /**
     * Gets the current trace ID from MDC.
     *
     * @return the trace ID, or null if not set
     */
    public static String getTraceId() {
        return MDC.get(LoggingConstants.MDC_TRACE_ID);
    }

    /**
     * Gets the current span ID from MDC.
     *
     * @return the span ID, or null if not set
     */
    public static String getSpanId() {
        return MDC.get(LoggingConstants.MDC_SPAN_ID);
    }
}
