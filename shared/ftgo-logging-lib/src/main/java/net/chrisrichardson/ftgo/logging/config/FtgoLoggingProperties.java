package net.chrisrichardson.ftgo.logging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO centralized logging.
 *
 * <p>Properties are bound from {@code ftgo.logging.*} in application configuration.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo:
 *   logging:
 *     enabled: true
 *     json-enabled: true
 *     async-enabled: true
 *     async-queue-size: 1024
 *     async-discard-threshold: 0
 *     include-caller-data: false
 *     service-name: ftgo-order-service
 *     correlation-id-header: X-Correlation-ID
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.logging")
public class FtgoLoggingProperties {

    /**
     * Whether FTGO logging auto-configuration is enabled. Defaults to {@code true}.
     */
    private boolean enabled = true;

    /**
     * Whether to enable structured JSON logging output. Defaults to {@code true}.
     * When disabled, standard Logback pattern layout is used.
     */
    private boolean jsonEnabled = true;

    /**
     * Whether to wrap the JSON appender in an async appender for non-blocking
     * log writes. Defaults to {@code true}. Recommended for production to avoid
     * log I/O impacting service performance.
     */
    private boolean asyncEnabled = true;

    /**
     * Queue size for the async appender. Defaults to {@code 1024}.
     * Larger values reduce the risk of log loss under high throughput.
     */
    private int asyncQueueSize = 1024;

    /**
     * Discarding threshold for the async appender. When the remaining queue
     * capacity falls below this threshold, TRACE, DEBUG, and INFO events are
     * discarded. Defaults to {@code 0} (no discarding).
     */
    private int asyncDiscardThreshold = 0;

    /**
     * Whether to include caller data (class, method, line) in log events.
     * Defaults to {@code false} for performance. Enable in development for debugging.
     */
    private boolean includeCallerData = false;

    /**
     * Service name included in structured log output. If not set, defaults to
     * {@code spring.application.name}.
     */
    private String serviceName;

    /**
     * HTTP header name used for correlation ID propagation from API Gateway.
     * Defaults to {@code X-Correlation-ID}.
     */
    private String correlationIdHeader = "X-Correlation-ID";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isJsonEnabled() {
        return jsonEnabled;
    }

    public void setJsonEnabled(boolean jsonEnabled) {
        this.jsonEnabled = jsonEnabled;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public int getAsyncQueueSize() {
        return asyncQueueSize;
    }

    public void setAsyncQueueSize(int asyncQueueSize) {
        this.asyncQueueSize = asyncQueueSize;
    }

    public int getAsyncDiscardThreshold() {
        return asyncDiscardThreshold;
    }

    public void setAsyncDiscardThreshold(int asyncDiscardThreshold) {
        this.asyncDiscardThreshold = asyncDiscardThreshold;
    }

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCorrelationIdHeader() {
        return correlationIdHeader;
    }

    public void setCorrelationIdHeader(String correlationIdHeader) {
        this.correlationIdHeader = correlationIdHeader;
    }
}
