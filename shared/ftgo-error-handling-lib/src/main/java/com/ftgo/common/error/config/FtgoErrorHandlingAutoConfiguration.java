package com.ftgo.common.error.config;

import com.ftgo.common.error.handler.GlobalExceptionHandler;
import com.ftgo.common.error.handler.MicrometerTraceIdProvider;
import com.ftgo.common.error.handler.NoOpTraceIdProvider;
import com.ftgo.common.error.handler.TraceIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO centralized error handling.
 *
 * <p>Activates when Spring Web is on the classpath and provides:</p>
 * <ul>
 *   <li>{@link GlobalExceptionHandler} - centralized @ControllerAdvice</li>
 *   <li>{@link TraceIdProvider} - trace ID extraction (Micrometer or no-op)</li>
 * </ul>
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *   <li>{@code ftgo.error-handling.enabled} - Enable/disable error handling (default: true)</li>
 * </ul>
 *
 * @see GlobalExceptionHandler
 * @see TraceIdProvider
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.error-handling.enabled", havingValue = "true", matchIfMissing = true)
@Import(FtgoErrorHandlingProperties.class)
public class FtgoErrorHandlingAutoConfiguration {

    /**
     * No-op trace ID provider fallback.
     * Always registered as the default; overridden when a Tracer bean is available.
     */
    @Bean
    @ConditionalOnMissingBean(TraceIdProvider.class)
    public TraceIdProvider noOpTraceIdProvider() {
        return new NoOpTraceIdProvider();
    }

    /**
     * Provides the {@link GlobalExceptionHandler} as a bean.
     * Services can override this by defining their own @ControllerAdvice.
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler(TraceIdProvider traceIdProvider) {
        return new GlobalExceptionHandler(traceIdProvider);
    }

    /**
     * Micrometer Tracing-aware trace ID provider.
     * Activated when both the Tracer class is on the classpath AND a Tracer bean exists.
     */
    @Configuration
    @ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
    @ConditionalOnBean(type = "io.micrometer.tracing.Tracer")
    static class MicrometerTracingConfiguration {

        @Bean
        public TraceIdProvider micrometerTraceIdProvider(io.micrometer.tracing.Tracer tracer) {
            return new MicrometerTraceIdProvider(tracer);
        }
    }
}
