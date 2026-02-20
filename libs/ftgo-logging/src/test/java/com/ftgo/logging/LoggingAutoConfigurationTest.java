package com.ftgo.logging;

import com.ftgo.logging.config.LoggingAutoConfiguration;
import com.ftgo.logging.config.LoggingProperties;
import com.ftgo.logging.correlation.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void autoConfigurationLoadsWithDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LoggingProperties.class);
            assertThat(context).hasSingleBean(RequestIdFilter.class);
        });
    }

    @Test
    void autoConfigurationCanBeDisabled() {
        contextRunner
                .withPropertyValues("ftgo.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LoggingAutoConfiguration.class);
                });
    }

    @Test
    void propertiesBindCorrectly() {
        contextRunner
                .withPropertyValues(
                        "ftgo.logging.service-name=test-service",
                        "ftgo.logging.json-enabled=true",
                        "ftgo.logging.trace-correlation-enabled=true",
                        "ftgo.logging.logstash.enabled=false",
                        "ftgo.logging.logstash.host=logstash-host",
                        "ftgo.logging.logstash.port=5001"
                )
                .run(context -> {
                    LoggingProperties props = context.getBean(LoggingProperties.class);
                    assertThat(props.getServiceName()).isEqualTo("test-service");
                    assertThat(props.isJsonEnabled()).isTrue();
                    assertThat(props.isTraceCorrelationEnabled()).isTrue();
                    assertThat(props.getLogstash().isEnabled()).isFalse();
                    assertThat(props.getLogstash().getHost()).isEqualTo("logstash-host");
                    assertThat(props.getLogstash().getPort()).isEqualTo(5001);
                });
    }

    @Test
    void traceCorrelationCanBeDisabled() {
        contextRunner
                .withPropertyValues("ftgo.logging.trace-correlation-enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RequestIdFilter.class);
                });
    }
}
