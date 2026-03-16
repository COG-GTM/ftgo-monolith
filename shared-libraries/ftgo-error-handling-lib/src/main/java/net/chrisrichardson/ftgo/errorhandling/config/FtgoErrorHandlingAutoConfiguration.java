package net.chrisrichardson.ftgo.errorhandling.config;

import net.chrisrichardson.ftgo.errorhandling.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration that registers the {@link GlobalExceptionHandler}.
 *
 * <p>The handler is only registered when the application is a web application
 * and no other bean of the same type has been defined, allowing services to
 * override with a custom handler if needed.
 */
@Configuration
@ConditionalOnWebApplication
public class FtgoErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
