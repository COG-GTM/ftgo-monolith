package com.ftgo.logging.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.ftgo.logging.encoder.FtgoJsonEncoder;
import jakarta.annotation.PostConstruct;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ftgo.logging.logstash", name = "enabled", havingValue = "true")
public class LogstashAppenderConfiguration {

    private final LoggingProperties properties;

    public LogstashAppenderConfiguration(LoggingProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void configure() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        LogstashTcpSocketAppender logstashAppender = new LogstashTcpSocketAppender();
        logstashAppender.setName("LOGSTASH");
        logstashAppender.setContext(context);
        logstashAppender.addDestination(
                properties.getLogstash().getHost() + ":" + properties.getLogstash().getPort());

        FtgoJsonEncoder encoder = new FtgoJsonEncoder();
        encoder.setContext(context);
        encoder.setServiceName(properties.getServiceName());
        encoder.start();

        logstashAppender.setEncoder(encoder);
        logstashAppender.setIncludeCallerData(properties.getLogstash().isIncludeCallerData());
        logstashAppender.start();

        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName("ASYNC_LOGSTASH");
        asyncAppender.setContext(context);
        asyncAppender.setQueueSize(properties.getLogstash().getQueueSize());
        asyncAppender.setDiscardingThreshold(0);
        asyncAppender.setIncludeCallerData(properties.getLogstash().isIncludeCallerData());
        asyncAppender.addAppender(logstashAppender);
        asyncAppender.start();

        rootLogger.addAppender(asyncAppender);
    }
}
