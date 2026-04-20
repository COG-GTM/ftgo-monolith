package net.chrisrichardson.ftgo.observability.tracing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import brave.Tracing;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfiguration {

    @Bean
    public AsyncZipkinSpanHandler zipkinSpanHandler(
            @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}") String zipkinEndpoint) {
        return AsyncZipkinSpanHandler.create(
                URLConnectionSender.create(zipkinEndpoint)
        );
    }

    @Bean
    public Tracing braveTracing(
            @Value("${spring.application.name:unknown}") String serviceName,
            AsyncZipkinSpanHandler zipkinSpanHandler) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .addSpanHandler(zipkinSpanHandler)
                .build();
    }
}
