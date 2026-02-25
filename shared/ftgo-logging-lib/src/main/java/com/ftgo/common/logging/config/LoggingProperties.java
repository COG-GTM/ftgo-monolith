package com.ftgo.common.logging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FTGO centralized logging.
 *
 * <p>These properties control the behavior of the FTGO logging library
 * including JSON format, correlation ID propagation, and ELK/EFK integration.</p>
 *
 * <h3>Property Prefix</h3>
 * <pre>ftgo.logging.*</pre>
 *
 * <h3>Example Configuration</h3>
 * <pre>
 * ftgo.logging.enabled=true
 * ftgo.logging.json.enabled=true
 * ftgo.logging.correlation-id.enabled=true
 * ftgo.logging.correlation-id.header-name=X-Correlation-ID
 * ftgo.logging.async.enabled=true
 * ftgo.logging.async.queue-size=1024
 * ftgo.logging.elasticsearch.enabled=false
 * ftgo.logging.elasticsearch.host=elasticsearch
 * ftgo.logging.elasticsearch.port=9200
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.logging")
public class LoggingProperties {

    /**
     * Enable or disable FTGO logging auto-configuration.
     * When disabled, no logging beans are created.
     * Default: true
     */
    private boolean enabled = true;

    private final Json json = new Json();
    private final CorrelationId correlationId = new CorrelationId();
    private final Async async = new Async();
    private final Elasticsearch elasticsearch = new Elasticsearch();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Json getJson() {
        return json;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    public Async getAsync() {
        return async;
    }

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    /**
     * JSON logging format configuration.
     */
    public static class Json {

        /**
         * Enable structured JSON log output via logstash-logback-encoder.
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Include caller information (class, method, line) in JSON logs.
         * Slightly impacts performance; recommended for development only.
         * Default: false
         */
        private boolean includeCallerData = false;

        /**
         * Include MDC fields in JSON log output.
         * Default: true
         */
        private boolean includeMdc = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeCallerData() {
            return includeCallerData;
        }

        public void setIncludeCallerData(boolean includeCallerData) {
            this.includeCallerData = includeCallerData;
        }

        public boolean isIncludeMdc() {
            return includeMdc;
        }

        public void setIncludeMdc(boolean includeMdc) {
            this.includeMdc = includeMdc;
        }
    }

    /**
     * Correlation ID propagation configuration.
     */
    public static class CorrelationId {

        /**
         * Enable correlation ID extraction and propagation.
         * Default: true
         */
        private boolean enabled = true;

        /**
         * HTTP header name for the correlation ID.
         * The API Gateway sets this header on incoming requests.
         * Default: X-Correlation-ID
         */
        private String headerName = "X-Correlation-ID";

        /**
         * MDC key for the correlation ID.
         * Default: correlationId
         */
        private String mdcKey = "correlationId";

        /**
         * Generate a new correlation ID if none is present in the request.
         * Default: true
         */
        private boolean generateIfMissing = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getMdcKey() {
            return mdcKey;
        }

        public void setMdcKey(String mdcKey) {
            this.mdcKey = mdcKey;
        }

        public boolean isGenerateIfMissing() {
            return generateIfMissing;
        }

        public void setGenerateIfMissing(boolean generateIfMissing) {
            this.generateIfMissing = generateIfMissing;
        }
    }

    /**
     * Async logging appender configuration.
     */
    public static class Async {

        /**
         * Enable async logging appender to avoid blocking application threads.
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Queue size for the async appender.
         * Default: 1024
         */
        private int queueSize = 1024;

        /**
         * Discarding threshold. When queue remaining capacity drops below
         * this value, TRACE, DEBUG, and INFO events are discarded.
         * Default: 0 (never discard)
         */
        private int discardingThreshold = 0;

        /**
         * Maximum flush time in milliseconds when the appender is stopped.
         * Default: 5000
         */
        private int maxFlushTime = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public int getDiscardingThreshold() {
            return discardingThreshold;
        }

        public void setDiscardingThreshold(int discardingThreshold) {
            this.discardingThreshold = discardingThreshold;
        }

        public int getMaxFlushTime() {
            return maxFlushTime;
        }

        public void setMaxFlushTime(int maxFlushTime) {
            this.maxFlushTime = maxFlushTime;
        }
    }

    /**
     * Elasticsearch direct shipping configuration.
     * Used when Fluentd/Logstash is not available.
     */
    public static class Elasticsearch {

        /**
         * Enable direct log shipping to Elasticsearch.
         * When disabled, logs are written to stdout only (for Fluentd collection).
         * Default: false (prefer Fluentd collection in K8s)
         */
        private boolean enabled = false;

        /**
         * Elasticsearch host.
         * Default: elasticsearch
         */
        private String host = "elasticsearch";

        /**
         * Elasticsearch port.
         * Default: 9200
         */
        private int port = 9200;

        /**
         * Index name pattern for log entries.
         * Default: ftgo-logs-%d{yyyy.MM.dd}
         */
        private String indexPattern = "ftgo-logs-%d{yyyy.MM.dd}";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getIndexPattern() {
            return indexPattern;
        }

        public void setIndexPattern(String indexPattern) {
            this.indexPattern = indexPattern;
        }
    }
}
