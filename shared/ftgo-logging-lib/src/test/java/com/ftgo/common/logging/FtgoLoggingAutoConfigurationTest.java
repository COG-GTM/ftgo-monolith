package com.ftgo.common.logging;

import com.ftgo.common.logging.appender.LoggingConstants;
import com.ftgo.common.logging.config.FtgoLoggingAutoConfiguration;
import com.ftgo.common.logging.config.LogbackJsonConfiguration;
import com.ftgo.common.logging.config.LoggingProperties;
import com.ftgo.common.logging.filter.CorrelationIdFilter;
import com.ftgo.common.logging.filter.MdcContextFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FtgoLoggingAutoConfiguration}.
 *
 * <p>Verifies that the logging auto-configuration correctly registers
 * beans based on classpath conditions and properties.</p>
 */
class FtgoLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FtgoLoggingAutoConfiguration.class
            ));

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FtgoLoggingAutoConfiguration.class
            ));

    @Test
    @DisplayName("Auto-configuration loads when logging is enabled (default)")
    void autoConfigurationLoadsWhenEnabled() {
        contextRunner
                .withPropertyValues("spring.application.name=test-service")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("Auto-configuration is disabled when ftgo.logging.enabled=false")
    void autoConfigurationDisabledWhenPropertyFalse() {
        contextRunner
                .withPropertyValues(
                        "ftgo.logging.enabled=false",
                        "spring.application.name=test-service"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(FtgoLoggingAutoConfiguration.class);
                });
    }

    @Test
    @DisplayName("CorrelationIdFilter is registered in web application context")
    void correlationIdFilterRegisteredInWebContext() {
        webContextRunner
                .withPropertyValues("spring.application.name=test-service")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CorrelationIdFilter.class);
                });
    }

    @Test
    @DisplayName("MdcContextFilter is registered in web application context")
    void mdcContextFilterRegisteredInWebContext() {
        webContextRunner
                .withPropertyValues("spring.application.name=test-service")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MdcContextFilter.class);
                });
    }

    @Test
    @DisplayName("CorrelationIdFilter is NOT registered when disabled")
    void correlationIdFilterNotRegisteredWhenDisabled() {
        webContextRunner
                .withPropertyValues(
                        "spring.application.name=test-service",
                        "ftgo.logging.correlation-id.enabled=false"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(CorrelationIdFilter.class);
                });
    }

    @Test
    @DisplayName("LoggingProperties is configured with defaults")
    void loggingPropertiesDefaults() {
        LoggingProperties props = new LoggingProperties();
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getJson().isEnabled()).isTrue();
        assertThat(props.getCorrelationId().isEnabled()).isTrue();
        assertThat(props.getCorrelationId().getHeaderName()).isEqualTo("X-Correlation-ID");
        assertThat(props.getCorrelationId().getMdcKey()).isEqualTo("correlationId");
        assertThat(props.getAsync().isEnabled()).isTrue();
        assertThat(props.getAsync().getQueueSize()).isEqualTo(1024);
        assertThat(props.getElasticsearch().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("LoggingConstants defines expected MDC keys")
    void loggingConstantsDefineMdcKeys() {
        assertThat(LoggingConstants.MDC_TRACE_ID).isEqualTo("traceId");
        assertThat(LoggingConstants.MDC_SPAN_ID).isEqualTo("spanId");
        assertThat(LoggingConstants.MDC_CORRELATION_ID).isEqualTo("correlationId");
        assertThat(LoggingConstants.MDC_SERVICE_NAME).isEqualTo("service");
    }

    @Test
    @DisplayName("Console log pattern includes trace context fields")
    void consoleLogPatternIncludesTraceContext() {
        assertThat(LoggingConstants.CONSOLE_LOG_PATTERN).contains("traceId");
        assertThat(LoggingConstants.CONSOLE_LOG_PATTERN).contains("spanId");
        assertThat(LoggingConstants.CONSOLE_LOG_PATTERN).contains("correlationId");
        assertThat(LoggingConstants.CONSOLE_LOG_PATTERN).contains("service");
    }

    @Test
    @DisplayName("Retention days are configured correctly per environment")
    void retentionDaysConfigured() {
        assertThat(LoggingConstants.RETENTION_DEV_DAYS).isEqualTo(7);
        assertThat(LoggingConstants.RETENTION_STAGING_DAYS).isEqualTo(30);
        assertThat(LoggingConstants.RETENTION_PROD_DAYS).isEqualTo(90);
    }
}
