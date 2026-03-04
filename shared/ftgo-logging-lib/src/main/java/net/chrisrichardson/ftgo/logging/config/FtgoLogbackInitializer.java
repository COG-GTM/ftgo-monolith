package net.chrisrichardson.ftgo.logging.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.slf4j.LoggerFactory;

/**
 * Programmatic Logback initializer for FTGO structured JSON logging.
 *
 * <p>Configures a {@link LogstashEncoder} with the standard FTGO log fields:
 * <ul>
 *   <li>{@code @timestamp} — ISO-8601 event timestamp</li>
 *   <li>{@code level} — log level (INFO, WARN, ERROR, etc.)</li>
 *   <li>{@code service} — service name from configuration</li>
 *   <li>{@code traceId} — distributed trace ID from MDC</li>
 *   <li>{@code spanId} — span ID from MDC</li>
 *   <li>{@code correlationId} — request correlation ID from MDC</li>
 *   <li>{@code message} — log message</li>
 *   <li>{@code logger} — logger name</li>
 *   <li>{@code thread} — thread name</li>
 * </ul>
 *
 * <p>When async is enabled, wraps the console appender in an {@link AsyncAppender}
 * to avoid blocking the application thread on log I/O.
 */
public class FtgoLogbackInitializer {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FtgoLogbackInitializer.class);

    private final FtgoLoggingProperties properties;
    private final String serviceName;

    public FtgoLogbackInitializer(FtgoLoggingProperties properties, String serviceName) {
        this.properties = properties;
        this.serviceName = serviceName;
    }

    /**
     * Initializes Logback with structured JSON logging configuration.
     * Replaces the root logger's appenders with a JSON console appender
     * (optionally wrapped in an async appender).
     */
    public void initialize() {
        if (!properties.isJsonEnabled()) {
            log.info("FTGO Logging: JSON logging disabled, using default Logback configuration");
            return;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create the Logstash JSON encoder
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setContext(context);
        encoder.setIncludeCallerData(properties.isIncludeCallerData());

        // Configure field names
        LogstashFieldNames fieldNames = encoder.getFieldNames();
        fieldNames.setTimestamp("@timestamp");
        fieldNames.setLevel("level");
        fieldNames.setLogger("logger");
        fieldNames.setThread("thread");
        fieldNames.setMessage("message");
        fieldNames.setStackTrace("stackTrace");

        // Add custom fields (service name)
        encoder.setCustomFields("{\"service\":\"" + serviceName + "\"}");

        encoder.start();

        // Create the JSON console appender
        ConsoleAppender<ILoggingEvent> jsonConsoleAppender = new ConsoleAppender<>();
        jsonConsoleAppender.setContext(context);
        jsonConsoleAppender.setName("FTGO_JSON_CONSOLE");
        jsonConsoleAppender.setEncoder(encoder);
        jsonConsoleAppender.start();

        // Get root logger and replace appenders
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();

        if (properties.isAsyncEnabled()) {
            // Wrap in async appender for non-blocking log writes
            AsyncAppender asyncAppender = new AsyncAppender();
            asyncAppender.setContext(context);
            asyncAppender.setName("FTGO_ASYNC_JSON");
            asyncAppender.setQueueSize(properties.getAsyncQueueSize());
            asyncAppender.setDiscardingThreshold(properties.getAsyncDiscardThreshold());
            asyncAppender.setIncludeCallerData(properties.isIncludeCallerData());
            asyncAppender.setNeverBlock(true);
            asyncAppender.addAppender(jsonConsoleAppender);
            asyncAppender.start();

            rootLogger.addAppender(asyncAppender);
            log.info("FTGO Logging: Async JSON logging initialized for service='{}' (queueSize={}, discardThreshold={})",
                    serviceName, properties.getAsyncQueueSize(), properties.getAsyncDiscardThreshold());
        } else {
            rootLogger.addAppender(jsonConsoleAppender);
            log.info("FTGO Logging: Synchronous JSON logging initialized for service='{}'", serviceName);
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public FtgoLoggingProperties getProperties() {
        return properties;
    }
}
