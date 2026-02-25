package com.ftgo.common.resilience.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles graceful shutdown lifecycle for FTGO services.
 *
 * <p>When a shutdown signal is received:</p>
 * <ol>
 *   <li>Marks the service as shutting down (readiness probe fails)</li>
 *   <li>Waits for in-flight requests to complete</li>
 *   <li>Closes connections and releases resources</li>
 * </ol>
 *
 * <p>Works with Spring Boot's {@code server.shutdown=graceful} and
 * Kubernetes preStop hooks for zero-downtime deployments.</p>
 */
public class GracefulShutdownHandler {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownHandler.class);

    private final int timeoutSeconds;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public GracefulShutdownHandler(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Returns whether the application is in the process of shutting down.
     *
     * @return true if shutdown has been initiated
     */
    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    /**
     * Handles the Spring context closed event to initiate graceful shutdown.
     */
    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        if (shuttingDown.compareAndSet(false, true)) {
            log.info("FTGO Graceful shutdown initiated. Timeout: {}s. "
                    + "Waiting for in-flight requests to complete...", timeoutSeconds);
        }
    }

    /**
     * Returns the configured shutdown timeout in seconds.
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
