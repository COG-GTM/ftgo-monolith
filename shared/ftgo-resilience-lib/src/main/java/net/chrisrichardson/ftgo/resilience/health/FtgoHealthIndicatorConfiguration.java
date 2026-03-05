package net.chrisrichardson.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Spring Boot Actuator health indicators for FTGO services.
 *
 * <p>Spring Boot Actuator automatically provides the following health indicators
 * when the corresponding dependencies are present:
 * <ul>
 *   <li>{@code db} — DataSource/JDBC health (auto-detected with spring-boot-starter-data-jpa)</li>
 *   <li>{@code diskSpace} — Disk space availability (always active)</li>
 *   <li>{@code ping} — Basic liveness check (always active)</li>
 * </ul>
 *
 * <p>This configuration enables Kubernetes probe groups:
 * <ul>
 *   <li>{@code /actuator/health/liveness} — Kubernetes liveness probe (ping only)</li>
 *   <li>{@code /actuator/health/readiness} — Kubernetes readiness probe (db + downstream services)</li>
 * </ul>
 *
 * <p>The following application properties are expected:
 * <pre>
 * management.endpoint.health.show-details=always
 * management.endpoint.health.probes.enabled=true
 * management.health.livenessstate.enabled=true
 * management.health.readinessstate.enabled=true
 * management.endpoint.health.group.liveness.include=livenessState,ping
 * management.endpoint.health.group.readiness.include=readinessState,db,diskSpace,downstreamServices
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoHealthIndicatorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoHealthIndicatorConfiguration.class);

    public FtgoHealthIndicatorConfiguration() {
        log.info("FTGO Health Indicator configuration loaded. "
                + "Readiness and liveness probe groups configured via application properties.");
    }
}
