package com.ftgo.tracing.exporter;

import com.ftgo.tracing.config.TracingProperties;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TracingExporterConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ZipkinSpanExporter.class)
    @ConditionalOnProperty(prefix = "ftgo.tracing.exporter", name = "type", havingValue = "ZIPKIN", matchIfMissing = true)
    static class ZipkinExporterConfiguration {

        @Bean
        @ConditionalOnMissingBean(SpanExporter.class)
        ZipkinSpanExporter zipkinSpanExporter(TracingProperties properties) {
            return ZipkinSpanExporter.builder()
                    .setEndpoint(properties.getExporter().getZipkinEndpoint())
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OtlpGrpcSpanExporter.class)
    @ConditionalOnProperty(prefix = "ftgo.tracing.exporter", name = "type", havingValue = "OTLP")
    static class OtlpExporterConfiguration {

        @Bean
        @ConditionalOnMissingBean(SpanExporter.class)
        OtlpGrpcSpanExporter otlpSpanExporter(TracingProperties properties) {
            return OtlpGrpcSpanExporter.builder()
                    .setEndpoint(properties.getExporter().getOtlpEndpoint())
                    .build();
        }
    }
}
