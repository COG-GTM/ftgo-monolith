package net.chrisrichardson.ftgo.logging.config;

import net.chrisrichardson.ftgo.logging.filter.CorrelationIdFilter;
import net.chrisrichardson.ftgo.logging.mdc.MdcContextLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration for FTGO centralized structured logging.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Registers a servlet filter for correlation ID extraction and MDC population</li>
 *   <li>Provides MDC context lifecycle management for request context propagation</li>
 *   <li>Configures structured JSON logging via Logback programmatically at startup</li>
 * </ul>
 *
 * <p>Activated when {@code ftgo.logging.enabled=true} (default).
 *
 * <p>Structured JSON logging is configured via the included {@code logback-ftgo.xml}
 * configuration file. Services should include it in their {@code logback-spring.xml}:
 * <pre>
 * &lt;configuration&gt;
 *   &lt;include resource="ftgo/logback-ftgo.xml"/&gt;
 * &lt;/configuration&gt;
 * </pre>
 */
@Configuration
@ConditionalOnClass(Logger.class)
@ConditionalOnProperty(prefix = "ftgo.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoLoggingProperties.class)
public class FtgoLoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoLoggingAutoConfiguration.class);

    /**
     * Registers the correlation ID filter as the highest-priority servlet filter.
     * This ensures correlation IDs are extracted from incoming requests before
     * any other processing occurs.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(name = "correlationIdFilterRegistration")
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            FtgoLoggingProperties properties) {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter(properties.getCorrelationIdHeader()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("correlationIdFilter");
        registration.addUrlPatterns("/*");
        log.info("FTGO Logging: Correlation ID filter registered (header={})",
                properties.getCorrelationIdHeader());
        return registration;
    }

    /**
     * Creates the MDC context lifecycle bean for managing request context in MDC.
     */
    @Bean
    @ConditionalOnMissingBean(MdcContextLifecycle.class)
    public MdcContextLifecycle mdcContextLifecycle(FtgoLoggingProperties properties,
                                                    Environment environment) {
        String serviceName = properties.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = environment.getProperty("spring.application.name", "ftgo-service");
        }
        log.info("FTGO Logging: MDC context lifecycle configured for service='{}'", serviceName);
        return new MdcContextLifecycle(serviceName);
    }

    /**
     * Initializes the Logback configuration programmatically at startup.
     * This configures the JSON appender and async wrapper based on properties.
     */
    @Bean
    @ConditionalOnMissingBean(FtgoLogbackInitializer.class)
    public FtgoLogbackInitializer ftgoLogbackInitializer(FtgoLoggingProperties properties,
                                                          Environment environment) {
        String serviceName = properties.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = environment.getProperty("spring.application.name", "ftgo-service");
        }
        FtgoLogbackInitializer initializer = new FtgoLogbackInitializer(properties, serviceName);
        initializer.initialize();
        return initializer;
    }
}
