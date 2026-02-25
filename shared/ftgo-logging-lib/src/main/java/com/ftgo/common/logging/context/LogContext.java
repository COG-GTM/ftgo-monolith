package com.ftgo.common.logging.context;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) fields
 * in a consistent manner across all FTGO microservices.
 *
 * <p>This class provides a clean API for setting and clearing standard
 * MDC fields used for structured logging and log correlation.</p>
 *
 * <h3>Standard MDC Fields</h3>
 * <ul>
 *   <li>{@code userId} - Authenticated user identifier</li>
 *   <li>{@code requestId} - Unique request identifier (per-request)</li>
 *   <li>{@code traceId} - Distributed trace ID (set by Micrometer Tracing)</li>
 *   <li>{@code spanId} - Current span ID (set by Micrometer Tracing)</li>
 *   <li>{@code serviceName} - Application/service name</li>
 *   <li>{@code correlationId} - Cross-service correlation ID from API Gateway</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Set context for a request
 * LogContext.setUserId("user-123");
 * LogContext.setRequestId(UUID.randomUUID().toString());
 *
 * try {
 *     // All log statements within this block will include userId and requestId
 *     log.info("Processing order");
 * } finally {
 *     LogContext.clear();
 * }
 *
 * // Or use the builder pattern:
 * LogContext.builder()
 *     .userId("user-123")
 *     .requestId(UUID.randomUUID().toString())
 *     .serviceName("ftgo-order-service")
 *     .apply();
 * }</pre>
 *
 * @see org.slf4j.MDC
 */
public final class LogContext {

    // --- MDC Key Constants ---

    /** MDC key for the authenticated user ID. */
    public static final String KEY_USER_ID = "userId";

    /** MDC key for the unique request ID. */
    public static final String KEY_REQUEST_ID = "requestId";

    /** MDC key for the distributed trace ID. */
    public static final String KEY_TRACE_ID = "traceId";

    /** MDC key for the current span ID. */
    public static final String KEY_SPAN_ID = "spanId";

    /** MDC key for the service/application name. */
    public static final String KEY_SERVICE_NAME = "serviceName";

    /** MDC key for the cross-service correlation ID. */
    public static final String KEY_CORRELATION_ID = "correlationId";

    /** MDC key for the operation being performed. */
    public static final String KEY_OPERATION = "operation";

    private LogContext() {
        // Utility class - prevent instantiation
    }

    // --- Individual Field Setters ---

    /**
     * Sets the userId in the MDC.
     *
     * @param userId the authenticated user identifier
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(KEY_USER_ID, userId);
        }
    }

    /**
     * Sets the requestId in the MDC.
     *
     * @param requestId the unique request identifier
     */
    public static void setRequestId(String requestId) {
        if (requestId != null) {
            MDC.put(KEY_REQUEST_ID, requestId);
        }
    }

    /**
     * Sets the traceId in the MDC.
     *
     * @param traceId the distributed trace identifier
     */
    public static void setTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(KEY_TRACE_ID, traceId);
        }
    }

    /**
     * Sets the spanId in the MDC.
     *
     * @param spanId the current span identifier
     */
    public static void setSpanId(String spanId) {
        if (spanId != null) {
            MDC.put(KEY_SPAN_ID, spanId);
        }
    }

    /**
     * Sets the serviceName in the MDC.
     *
     * @param serviceName the application/service name
     */
    public static void setServiceName(String serviceName) {
        if (serviceName != null) {
            MDC.put(KEY_SERVICE_NAME, serviceName);
        }
    }

    /**
     * Sets the correlationId in the MDC.
     *
     * @param correlationId the cross-service correlation identifier
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(KEY_CORRELATION_ID, correlationId);
        }
    }

    /**
     * Sets the operation name in the MDC.
     *
     * @param operation the business operation being performed
     */
    public static void setOperation(String operation) {
        if (operation != null) {
            MDC.put(KEY_OPERATION, operation);
        }
    }

    /**
     * Sets a custom MDC field.
     *
     * @param key   the MDC key
     * @param value the value (null values are ignored)
     */
    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * Gets the value of an MDC field.
     *
     * @param key the MDC key
     * @return the value, or null if not set
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Removes a specific MDC field.
     *
     * @param key the MDC key to remove
     */
    public static void remove(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }

    /**
     * Clears all standard FTGO MDC fields.
     *
     * <p>This should be called in a {@code finally} block after
     * request processing is complete to prevent MDC leaks.</p>
     */
    public static void clear() {
        MDC.remove(KEY_USER_ID);
        MDC.remove(KEY_REQUEST_ID);
        MDC.remove(KEY_TRACE_ID);
        MDC.remove(KEY_SPAN_ID);
        MDC.remove(KEY_SERVICE_NAME);
        MDC.remove(KEY_CORRELATION_ID);
        MDC.remove(KEY_OPERATION);
    }

    /**
     * Clears the entire MDC context (all fields, including non-standard ones).
     *
     * <p>Use with caution; prefer {@link #clear()} for normal cleanup.</p>
     */
    public static void clearAll() {
        MDC.clear();
    }

    /**
     * Returns a snapshot of the current MDC context as an immutable map.
     *
     * @return the current MDC context map, or null if empty
     */
    public static Map<String, String> snapshot() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Restores the MDC context from a previously captured snapshot.
     *
     * @param contextMap the context map to restore (from {@link #snapshot()})
     */
    public static void restore(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        } else {
            MDC.clear();
        }
    }

    /**
     * Creates a new {@link Builder} for fluent MDC configuration.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for setting multiple MDC fields at once.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * LogContext.builder()
     *     .userId("user-123")
     *     .requestId(UUID.randomUUID().toString())
     *     .serviceName("ftgo-order-service")
     *     .operation("createOrder")
     *     .apply();
     * }</pre>
     */
    public static final class Builder {

        private String userId;
        private String requestId;
        private String traceId;
        private String spanId;
        private String serviceName;
        private String correlationId;
        private String operation;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        /**
         * Applies all configured fields to the MDC.
         */
        public void apply() {
            LogContext.setUserId(userId);
            LogContext.setRequestId(requestId);
            LogContext.setTraceId(traceId);
            LogContext.setSpanId(spanId);
            LogContext.setServiceName(serviceName);
            LogContext.setCorrelationId(correlationId);
            LogContext.setOperation(operation);
        }
    }
}
