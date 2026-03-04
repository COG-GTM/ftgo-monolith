package net.chrisrichardson.ftgo.logging.mdc;

import org.slf4j.MDC;

/**
 * Manages MDC (Mapped Diagnostic Context) lifecycle for FTGO services.
 *
 * <p>Provides utility methods to set and clear MDC fields used across
 * the structured logging pipeline. This ensures consistent MDC key names
 * and simplifies MDC management in application code.
 *
 * <p>Standard MDC fields managed by this class:
 * <ul>
 *   <li>{@code service} — service name</li>
 *   <li>{@code correlationId} — request correlation ID</li>
 *   <li>{@code userId} — authenticated user ID</li>
 *   <li>{@code requestId} — unique request identifier</li>
 *   <li>{@code traceId} — distributed trace ID (set by tracing library)</li>
 *   <li>{@code spanId} — span ID (set by tracing library)</li>
 * </ul>
 */
public class MdcContextLifecycle {

    public static final String MDC_SERVICE = "service";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";

    private final String serviceName;

    /**
     * Creates a new MdcContextLifecycle with the specified service name.
     * The service name is immediately placed into MDC.
     *
     * @param serviceName the name of the service
     */
    public MdcContextLifecycle(String serviceName) {
        this.serviceName = serviceName;
        MDC.put(MDC_SERVICE, serviceName);
    }

    /**
     * Sets the correlation ID in MDC.
     *
     * @param correlationId the correlation ID
     */
    public void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(MDC_CORRELATION_ID, correlationId);
        }
    }

    /**
     * Sets the user ID in MDC.
     *
     * @param userId the authenticated user ID
     */
    public void setUserId(String userId) {
        if (userId != null) {
            MDC.put(MDC_USER_ID, userId);
        }
    }

    /**
     * Sets the request ID in MDC.
     *
     * @param requestId the unique request identifier
     */
    public void setRequestId(String requestId) {
        if (requestId != null) {
            MDC.put(MDC_REQUEST_ID, requestId);
        }
    }

    /**
     * Clears all FTGO-managed MDC fields except the service name.
     * Call this at the end of request processing to prevent MDC leaking.
     */
    public void clearRequestContext() {
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_REQUEST_ID);
        // Note: traceId and spanId are managed by the tracing library
    }

    /**
     * Clears all MDC fields including the service name.
     */
    public void clearAll() {
        MDC.clear();
    }

    /**
     * Returns the configured service name.
     */
    public String getServiceName() {
        return serviceName;
    }
}
