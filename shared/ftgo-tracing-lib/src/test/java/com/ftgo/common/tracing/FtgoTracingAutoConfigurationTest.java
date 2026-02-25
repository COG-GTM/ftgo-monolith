package com.ftgo.common.tracing;

import com.ftgo.common.tracing.config.FtgoTracingAutoConfiguration;
import com.ftgo.common.tracing.config.TracePropagationConfiguration;
import com.ftgo.common.tracing.config.TracingLoggingConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FtgoTracingAutoConfiguration}.
 *
 * <p>Verifies that the tracing auto-configuration correctly registers
 * beans based on classpath conditions and properties.</p>
 */
class FtgoTracingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FtgoTracingAutoConfiguration.class,
                    TracingLoggingConfiguration.class,
                    TracePropagationConfiguration.class
            ));

    @Test
    @DisplayName("Auto-configuration loads when tracing is enabled (default)")
    void autoConfigurationLoadsWhenEnabled() {
        contextRunner
                .withPropertyValues("spring.application.name=test-service")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("Auto-configuration is disabled when ftgo.tracing.enabled=false")
    void autoConfigurationDisabledWhenPropertyFalse() {
        contextRunner
                .withPropertyValues(
                        "ftgo.tracing.enabled=false",
                        "spring.application.name=test-service"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(FtgoTracingAutoConfiguration.class);
                });
    }

    @Test
    @DisplayName("TracingLoggingConfiguration log pattern constants are defined")
    void loggingPatternConstantsDefined() {
        assertThat(TracingLoggingConfiguration.TRACE_LOG_PATTERN).isNotBlank();
        assertThat(TracingLoggingConfiguration.TRACE_LOG_PATTERN).contains("traceId");
        assertThat(TracingLoggingConfiguration.TRACE_LOG_PATTERN).contains("spanId");
    }

    @Test
    @DisplayName("Full log pattern includes timestamp and trace context")
    void fullLogPatternIncludesTraceContext() {
        assertThat(TracingLoggingConfiguration.FULL_LOG_PATTERN).contains("traceId");
        assertThat(TracingLoggingConfiguration.FULL_LOG_PATTERN).contains("spanId");
        assertThat(TracingLoggingConfiguration.FULL_LOG_PATTERN).contains("yyyy-MM-dd");
    }
}
