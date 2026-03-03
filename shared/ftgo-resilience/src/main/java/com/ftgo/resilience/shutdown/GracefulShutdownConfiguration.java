package com.ftgo.resilience.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.TimeUnit;

/**
 * Graceful shutdown configuration for zero-downtime deployments.
 * <p>
 * When the application receives a shutdown signal (SIGTERM from Kubernetes),
 * this configuration ensures:
 * <ol>
 *   <li>The readiness probe starts returning DOWN immediately</li>
 *   <li>A configurable delay allows in-flight requests to complete</li>
 *   <li>Active connections are drained before the JVM exits</li>
 * </ol>
 * <p>
 * <b>Kubernetes Integration:</b>
 * <pre>
 *   1. K8s sends SIGTERM to the pod
 *   2. Readiness probe returns DOWN (pod removed from Service endpoints)
 *   3. Pre-shutdown delay allows load balancers to update
 *   4. Active requests complete within the grace period
 *   5. Application context closes
 *   6. If still running after terminationGracePeriodSeconds, K8s sends SIGKILL
 * </pre>
 * <p>
 * Configuration properties:
 * <pre>
 *   # Spring Boot native graceful shutdown (Spring Boot 2.3+)
 *   server.shutdown=graceful
 *   spring.lifecycle.timeout-per-shutdown-phase=30s
 *
 *   # Custom pre-shutdown delay for load balancer deregistration
 *   ftgo.shutdown.pre-wait-seconds=5
 * </pre>
 *
 * @see <a href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-termination">
 *     Kubernetes Pod Termination</a>
 */
@Configuration
public class GracefulShutdownConfiguration implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfiguration.class);

    @Value("${ftgo.shutdown.pre-wait-seconds:5}")
    private int preWaitSeconds;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Graceful shutdown initiated. Waiting {}s for load balancer deregistration...",
                preWaitSeconds);

        try {
            // Allow time for the Kubernetes Service to remove this pod from endpoints
            // and for load balancers to stop routing traffic to this instance
            TimeUnit.SECONDS.sleep(preWaitSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Graceful shutdown pre-wait interrupted");
        }

        log.info("Pre-shutdown wait complete. Draining active connections...");
    }
}
