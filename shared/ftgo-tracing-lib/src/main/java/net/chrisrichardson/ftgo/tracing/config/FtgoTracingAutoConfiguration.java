package net.chrisrichardson.ftgo.tracing.config;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Auto-configuration for FTGO distributed tracing using Micrometer Tracing
 * with Brave bridge and Zipkin reporter.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Sets up Brave as the tracing backend with Micrometer Tracing bridge</li>
 *   <li>Configures B3 propagation for Zipkin compatibility</li>
 *   <li>Injects traceId and spanId into SLF4J MDC for log correlation</li>
 *   <li>Reports spans to a Zipkin collector via HTTP</li>
 *   <li>Provides a configurable sampling strategy</li>
 * </ul>
 *
 * <p>Activated when {@code ftgo.tracing.enabled=true} (default).
 */
@Configuration
@ConditionalOnClass({Tracer.class, Tracing.class})
@ConditionalOnProperty(prefix = "ftgo.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoTracingProperties.class)
public class FtgoTracingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoTracingAutoConfiguration.class);

    /**
     * Creates a Zipkin span reporter that sends spans to the configured endpoint.
     */
    @Bean
    @ConditionalOnMissingBean(BytesMessageSender.class)
    public URLConnectionSender zipkinSender(FtgoTracingProperties properties) {
        log.info("FTGO Tracing: Zipkin sender configured to send spans to {}", properties.getZipkinEndpoint());
        return URLConnectionSender.create(properties.getZipkinEndpoint());
    }

    /**
     * Creates the async Zipkin span handler that bridges Brave spans to Zipkin.
     * Uses {@link AsyncZipkinSpanHandler} which accepts a {@link BytesMessageSender}
     * directly and handles async batching of span reports.
     */
    @Bean
    @ConditionalOnMissingBean(AsyncZipkinSpanHandler.class)
    public AsyncZipkinSpanHandler zipkinSpanHandler(BytesMessageSender sender) {
        return AsyncZipkinSpanHandler.create(sender);
    }

    /**
     * Creates the Brave {@link Tracing} instance with B3 propagation,
     * MDC-based trace context for logging, and configurable sampling.
     */
    @Bean
    @ConditionalOnMissingBean(Tracing.class)
    public Tracing braveTracing(FtgoTracingProperties properties,
                                AsyncZipkinSpanHandler zipkinSpanHandler,
                                Environment environment) {
        String serviceName = properties.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = environment.getProperty("spring.application.name", "ftgo-service");
        }

        float samplingProbability = properties.getSamplingProbability();

        // Configure MDC correlation: inject traceId and spanId into SLF4J MDC
        ThreadLocalCurrentTraceContext currentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();

        // B3 propagation for Zipkin compatibility
        Propagation.Factory b3Factory = B3Propagation.newFactoryBuilder()
                .injectFormat(B3Propagation.Format.MULTI)
                .build();

        // Custom baggage field for FTGO cross-service correlation
        BaggageField ftgoTraceField = BaggageField.create("x-ftgo-trace");

        Tracing tracing = Tracing.newBuilder()
                .localServiceName(serviceName)
                .propagationFactory(BaggagePropagation.newFactoryBuilder(b3Factory)
                        .add(BaggagePropagationConfig.SingleBaggageField.remote(ftgoTraceField))
                        .build())
                .currentTraceContext(currentTraceContext)
                .sampler(Sampler.create(samplingProbability))
                .addSpanHandler(zipkinSpanHandler)
                .build();

        log.info("FTGO Tracing: Brave tracing configured for service='{}', sampling={}, propagation={}",
                serviceName, samplingProbability, properties.getPropagationType());

        return tracing;
    }

    /**
     * Creates the Micrometer Tracing {@link Tracer} backed by Brave.
     */
    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public Tracer micrometerTracer(Tracing braveTracing) {
        brave.Tracer braveTracer = braveTracing.tracer();
        BraveCurrentTraceContext braveCurrentTraceContext =
                new BraveCurrentTraceContext(braveTracing.currentTraceContext());
        BraveBaggageManager baggageManager = new BraveBaggageManager();

        log.info("FTGO Tracing: Micrometer Tracer (Brave bridge) initialized");
        return new BraveTracer(braveTracer, braveCurrentTraceContext, baggageManager);
    }

}
