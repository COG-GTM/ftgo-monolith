package com.ftgo.tracing;

import com.ftgo.tracing.config.TracingAutoConfiguration;
import com.ftgo.tracing.config.TracingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TracingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TracingAutoConfiguration.class));

    @Test
    void tracingPropertiesBeanIsCreated() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(TracingProperties.class));
    }

    @Test
    void tracingIsEnabledByDefault() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.isEnabled()).isTrue();
        });
    }

    @Test
    void defaultSamplingProbabilityIsOne() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.getSamplingProbability()).isEqualTo(1.0);
        });
    }

    @Test
    void defaultPropagationTypeIsW3C() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.getPropagation().getType())
                    .isEqualTo(TracingProperties.PropagationType.W3C);
        });
    }

    @Test
    void defaultExporterTypeIsZipkin() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.getExporter().getType())
                    .isEqualTo(TracingProperties.ExporterType.ZIPKIN);
        });
    }

    @Test
    void tracingCanBeDisabled() {
        contextRunner
                .withPropertyValues("ftgo.tracing.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(TracingProperties.class));
    }

    @Test
    void customPropertiesAreApplied() {
        contextRunner
                .withPropertyValues(
                        "ftgo.tracing.service-name=order-service",
                        "ftgo.tracing.sampling-probability=0.5",
                        "ftgo.tracing.propagation.type=B3",
                        "ftgo.tracing.exporter.type=OTLP",
                        "ftgo.tracing.exporter.otlp-endpoint=http://jaeger:4317"
                )
                .run(context -> {
                    TracingProperties properties = context.getBean(TracingProperties.class);
                    assertThat(properties.getServiceName()).isEqualTo("order-service");
                    assertThat(properties.getSamplingProbability()).isEqualTo(0.5);
                    assertThat(properties.getPropagation().getType())
                            .isEqualTo(TracingProperties.PropagationType.B3);
                    assertThat(properties.getExporter().getType())
                            .isEqualTo(TracingProperties.ExporterType.OTLP);
                    assertThat(properties.getExporter().getOtlpEndpoint())
                            .isEqualTo("http://jaeger:4317");
                });
    }

    @Test
    void defaultZipkinEndpoint() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.getExporter().getZipkinEndpoint())
                    .isEqualTo("http://localhost:9411/api/v2/spans");
        });
    }

    @Test
    void baggageEnabledByDefault() {
        contextRunner.run(context -> {
            TracingProperties properties = context.getBean(TracingProperties.class);
            assertThat(properties.getPropagation().isBaggageEnabled()).isTrue();
        });
    }
}
