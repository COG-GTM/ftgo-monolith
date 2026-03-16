package net.chrisrichardson.ftgo.tracing.config;

import brave.Tracing;
import net.chrisrichardson.ftgo.tracing.util.SpanHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for the FTGO distributed tracing library.
 *
 * <p>Integrates Spring Cloud Sleuth with Zipkin/Jaeger for distributed tracing.
 * Provides B3 context propagation, configurable sampling, and custom span
 * creation utilities.</p>
 *
 * <p>Tracing can be disabled entirely by setting {@code ftgo.tracing.enabled=false}.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.tracing.enabled", havingValue = "true", matchIfMissing = true)
@Import({TracingLogCorrelationConfiguration.class, TracingWebConfiguration.class})
public class TracingAutoConfiguration {

    /**
     * Provides a {@link SpanHelper} bean for creating custom spans in service code.
     */
    @Bean
    @ConditionalOnMissingBean
    public SpanHelper spanHelper(Tracing tracing) {
        return new SpanHelper(tracing.tracer());
    }
}
