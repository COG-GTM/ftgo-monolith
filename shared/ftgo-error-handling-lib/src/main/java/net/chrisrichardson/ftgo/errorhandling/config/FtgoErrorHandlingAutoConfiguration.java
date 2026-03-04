package net.chrisrichardson.ftgo.errorhandling.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for FTGO centralized error handling.
 *
 * <p>Registers the {@link GlobalExceptionHandler} as a Spring bean when
 * the application is a web application and {@code ftgo.error-handling.enabled}
 * is {@code true} (default).</p>
 *
 * <p>This configuration is activated automatically via Spring Boot's
 * auto-configuration mechanism using the
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * file.</p>
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "ftgo.error-handling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoErrorHandlingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoErrorHandlingAutoConfiguration.class);

    /**
     * Provides a no-op Tracer if none is configured (e.g., when tracing lib is not on classpath).
     */
    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public Tracer noOpTracer() {
        log.info("FTGO Error Handling: No Tracer found — traceId will be null in error responses");
        return Tracer.NOOP;
    }

    /**
     * Creates the centralized exception handler bean.
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
        log.info("FTGO Error Handling: GlobalExceptionHandler registered");
        return new GlobalExceptionHandler(tracer);
    }
}
