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
 *     masking:
 *       enabled: true
 *     aspect:
 *       enabled: true
 *       include-args: true
 *       include-result: false
 *       slow-threshold-ms: 1000
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

    /**
     * Sensitive data masking configuration.
     */
    private Masking masking = new Masking();

    /**
     * Method entry/exit logging aspect configuration.
     */
    private Aspect aspect = new Aspect();

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

    public Masking getMasking() {
        return masking;
    }

    public void setMasking(Masking masking) {
        this.masking = masking;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public void setAspect(Aspect aspect) {
        this.aspect = aspect;
    }

    /**
     * Configuration for sensitive data masking in log output.
     */
    public static class Masking {

        /**
         * Whether sensitive data masking is enabled. Defaults to {@code true}.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Configuration for the method entry/exit logging aspect.
     */
    public static class Aspect {

        /**
         * Whether the logging aspect is enabled. Defaults to {@code false}.
         */
        private boolean enabled = false;

        /**
         * Whether to include method arguments in entry log. Defaults to {@code true}.
         */
        private boolean includeArgs = true;

        /**
         * Whether to include return value in exit log. Defaults to {@code false}.
         */
        private boolean includeResult = false;

        /**
         * Threshold in milliseconds for slow execution warning. Defaults to {@code 1000}.
         */
        private long slowThresholdMs = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeArgs() {
            return includeArgs;
        }

        public void setIncludeArgs(boolean includeArgs) {
            this.includeArgs = includeArgs;
        }

        public boolean isIncludeResult() {
            return includeResult;
        }

        public void setIncludeResult(boolean includeResult) {
            this.includeResult = includeResult;
        }

        public long getSlowThresholdMs() {
            return slowThresholdMs;
        }

        public void setSlowThresholdMs(long slowThresholdMs) {
            this.slowThresholdMs = slowThresholdMs;
        }
    }
}
