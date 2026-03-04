package net.chrisrichardson.ftgo.resilience.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures graceful shutdown for zero-downtime deployments.
 *
 * <p>When a SIGTERM is received (e.g., during K8s pod termination):
 * <ol>
 *   <li>The readiness probe fails (pod removed from Service endpoints)</li>
 *   <li>Active requests are allowed to complete within the timeout</li>
 *   <li>New requests are rejected with 503</li>
 *   <li>After timeout, the application shuts down</li>
 * </ol>
 *
 * <p>This works in conjunction with the K8s {@code preStop} lifecycle hook
 * and {@code terminationGracePeriodSeconds} to ensure zero-downtime deployments.
 *
 * <p>Spring Boot 3.x natively supports graceful shutdown via
 * {@code server.shutdown=graceful} and {@code spring.lifecycle.timeout-per-shutdown-phase}.
 * This configuration adds Tomcat-specific customizations.
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoGracefulShutdownConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoGracefulShutdownConfiguration.class);

    /**
     * Customizes the Tomcat web server for graceful shutdown support.
     * Configures connection timeout and keep-alive settings to support
     * clean connection draining during shutdown.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> gracefulShutdownCustomizer(
            FtgoResilienceProperties properties) {
        return factory -> {
            long timeoutMs = properties.getGracefulShutdown().getTimeout().toMillis();
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("connectionTimeout", String.valueOf(timeoutMs));
            });
            log.info("FTGO Resilience: Tomcat graceful shutdown configured with timeout={}ms", timeoutMs);
        };
    }
}
