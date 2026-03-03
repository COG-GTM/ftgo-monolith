package com.ftgo.errorhandling.config;

import com.ftgo.errorhandling.handler.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for FTGO centralized error handling.
 *
 * <p>Automatically configures the {@link GlobalExceptionHandler} when this library
 * is on the classpath. The handler is only registered for web applications and can
 * be disabled via configuration property.</p>
 *
 * <p>Services only need to add this library as a dependency to enable centralized
 * error handling. No additional configuration is required:</p>
 * <pre>
 * dependencies {
 *     compile project(":shared:ftgo-error-handling")
 * }
 * </pre>
 *
 * <p>To disable auto-configuration (e.g., for custom error handling):</p>
 * <pre>
 * ftgo.error-handling.enabled=false
 * </pre>
 *
 * <p>To include stack traces in error responses (development only):</p>
 * <pre>
 * ftgo.error-handling.include-stacktrace=true
 * </pre>
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "ftgo.error-handling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoErrorHandlingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoErrorHandlingAutoConfiguration.class);

    /**
     * Registers the global exception handler bean.
     *
     * <p>Uses {@link ConditionalOnMissingBean} to allow services to provide
     * their own custom exception handler if needed.</p>
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        log.info("FTGO Error Handling auto-configuration enabled: registering GlobalExceptionHandler");
        return new GlobalExceptionHandler();
    }
}
