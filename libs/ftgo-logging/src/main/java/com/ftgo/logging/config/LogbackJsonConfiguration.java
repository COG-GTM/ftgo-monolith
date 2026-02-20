package com.ftgo.logging.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.ftgo.logging.encoder.FtgoJsonEncoder;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ftgo.logging", name = "json-enabled", havingValue = "true", matchIfMissing = true)
public class LogbackJsonConfiguration {

    private final LoggingProperties properties;

    public LogbackJsonConfiguration(LoggingProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void configure() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        if (properties.getConsole().isEnabled()) {
            rootLogger.detachAppender("console");
            rootLogger.detachAppender("CONSOLE");

            ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
            consoleAppender.setName("JSON_CONSOLE");
            consoleAppender.setContext(context);

            FtgoJsonEncoder encoder = new FtgoJsonEncoder();
            encoder.setContext(context);
            encoder.setServiceName(properties.getServiceName());
            encoder.setPrettyPrint(properties.getConsole().isPrettyPrint());
            encoder.start();

            consoleAppender.setEncoder(encoder);
            consoleAppender.start();

            rootLogger.addAppender(consoleAppender);
        }
    }
}
