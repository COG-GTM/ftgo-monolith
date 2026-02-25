package com.ftgo.common.resilience.shutdown;

import com.ftgo.common.resilience.config.ResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for graceful shutdown to enable zero-downtime deployments.
 *
 * <p>Ensures that in-flight requests are completed before the application
 * terminates. Works in conjunction with Kubernetes preStop hooks and
 * Spring Boot's built-in graceful shutdown support.</p>
 *
 * <h3>Spring Boot Configuration</h3>
 * <pre>
 * server.shutdown=graceful
 * spring.lifecycle.timeout-per-shutdown-phase=30s
 * </pre>
 *
 * <h3>Kubernetes preStop Hook</h3>
 * <pre>
 * lifecycle:
 *   preStop:
 *     exec:
 *       command: ["sh", "-c", "sleep 10"]
 * </pre>
 *
 * <p>The preStop hook gives the load balancer time to remove the pod
 * from service before the application begins shutting down.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.resilience.shutdown.enabled", havingValue = "true", matchIfMissing = true)
public class GracefulShutdownConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfiguration.class);

    /**
     * Creates a {@link GracefulShutdownHandler} that manages the shutdown lifecycle.
     */
    @Bean
    public GracefulShutdownHandler gracefulShutdownHandler(ResilienceProperties properties) {
        int timeoutSeconds = properties.getShutdown().getTimeoutSeconds();
        log.info("FTGO Graceful Shutdown configured: timeout={}s", timeoutSeconds);
        return new GracefulShutdownHandler(timeoutSeconds);
    }
}
