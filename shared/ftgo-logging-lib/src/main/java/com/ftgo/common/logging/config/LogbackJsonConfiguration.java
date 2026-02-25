package com.ftgo.common.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.ConsoleAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Iterator;

/**
 * Programmatic Logback configuration for structured JSON logging.
 *
 * <p>This configuration replaces the default console appender with a
 * logstash-logback-encoder based JSON appender when {@code ftgo.logging.json.enabled=true}.</p>
 *
 * <p>The JSON output includes:</p>
 * <ul>
 *   <li>{@code @timestamp} - ISO 8601 timestamp</li>
 *   <li>{@code level} - Log level (INFO, WARN, ERROR, etc.)</li>
 *   <li>{@code service} - Service name from spring.application.name</li>
 *   <li>{@code traceId} - Distributed trace ID (from Micrometer Tracing)</li>
 *   <li>{@code spanId} - Current span ID</li>
 *   <li>{@code correlationId} - Request correlation ID from API Gateway</li>
 *   <li>{@code message} - Log message</li>
 *   <li>{@code logger_name} - Logger class name</li>
 *   <li>{@code thread_name} - Thread name</li>
 *   <li>{@code stack_trace} - Exception stack trace (if present)</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass({LogstashEncoder.class, LoggerContext.class})
@ConditionalOnProperty(name = "ftgo.logging.json.enabled", havingValue = "true", matchIfMissing = true)
public class LogbackJsonConfiguration {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${ftgo.logging.json.include-caller-data:false}")
    private boolean includeCallerData;

    @Value("${ftgo.logging.async.enabled:true}")
    private boolean asyncEnabled;

    @Value("${ftgo.logging.async.queue-size:1024}")
    private int asyncQueueSize;

    @Value("${ftgo.logging.async.discarding-threshold:0}")
    private int asyncDiscardingThreshold;

    @Value("${ftgo.logging.async.max-flush-time:5000}")
    private int asyncMaxFlushTime;

    /**
     * Configures Logback programmatically to use JSON format with async appender.
     */
    @PostConstruct
    public void configure() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        // Create the JSON console appender
        ConsoleAppender<ILoggingEvent> jsonConsoleAppender = createJsonConsoleAppender(loggerContext);

        // Detach existing appenders
        detachExistingAppenders(rootLogger);

        if (asyncEnabled) {
            // Wrap in async appender for non-blocking log output
            AsyncAppenderBase<ILoggingEvent> asyncAppender = createAsyncAppender(loggerContext, jsonConsoleAppender);
            rootLogger.addAppender(asyncAppender);
        } else {
            rootLogger.addAppender(jsonConsoleAppender);
        }
    }

    private ConsoleAppender<ILoggingEvent> createJsonConsoleAppender(LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("JSON_CONSOLE");

        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setContext(loggerContext);
        encoder.setIncludeCallerData(includeCallerData);

        // Add custom fields
        String customFields = String.format("{\"service\":\"%s\"}", applicationName);
        encoder.setCustomFields(customFields);

        // Configure the timestamp pattern
        encoder.setTimestampPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        encoder.start();
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        return consoleAppender;
    }

    private AsyncAppenderBase<ILoggingEvent> createAsyncAppender(
            LoggerContext loggerContext,
            Appender<ILoggingEvent> delegateAppender) {

        ch.qos.logback.classic.AsyncAppender asyncAppender = new ch.qos.logback.classic.AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC_JSON");
        asyncAppender.setQueueSize(asyncQueueSize);
        asyncAppender.setDiscardingThreshold(asyncDiscardingThreshold);
        asyncAppender.setMaxFlushTime(asyncMaxFlushTime);
        asyncAppender.setIncludeCallerData(includeCallerData);
        asyncAppender.addAppender(delegateAppender);
        asyncAppender.start();

        return asyncAppender;
    }

    private void detachExistingAppenders(Logger rootLogger) {
        Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            Appender<ILoggingEvent> appender = appenderIterator.next();
            appender.stop();
            rootLogger.detachAppender(appender);
        }
    }
}
