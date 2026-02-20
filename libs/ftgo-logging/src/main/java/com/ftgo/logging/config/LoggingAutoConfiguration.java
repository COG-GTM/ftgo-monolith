package com.ftgo.logging.config;

import com.ftgo.logging.correlation.TraceCorrelationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnProperty(prefix = "ftgo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoggingProperties.class)
@Import({
        LogbackJsonConfiguration.class,
        LogstashAppenderConfiguration.class,
        TraceCorrelationConfiguration.class
})
public class LoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoggingAutoConfiguration.class);

    public LoggingAutoConfiguration(LoggingProperties properties) {
        log.info("FTGO Logging auto-configuration enabled: json={}, traceCorrelation={}, logstash={}",
                properties.isJsonEnabled(),
                properties.isTraceCorrelationEnabled(),
                properties.getLogstash().isEnabled());
    }
}
