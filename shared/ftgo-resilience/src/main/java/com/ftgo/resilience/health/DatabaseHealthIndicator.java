package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.sql.DataSource;

/**
 * Custom health indicator for database connectivity.
 * <p>
 * Performs a lightweight validation query to verify database availability.
 * Reports connection pool metrics when available.
 * <p>
 * This indicator is registered under the name "ftgoDatabase" in the health endpoint:
 * <pre>
 *   GET /actuator/health
 *   {
 *     "components": {
 *       "ftgoDatabase": {
 *         "status": "UP",
 *         "details": {
 *           "database": "MySQL",
 *           "validationQuery": "SELECT 1"
 *         }
 *       }
 *     }
 *   }
 * </pre>
 */
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    private static final String VALIDATION_QUERY = "SELECT 1";

    private DataSource dataSource;

    public DatabaseHealthIndicator() {
    }

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        if (dataSource == null) {
            return Health.unknown()
                    .withDetail("reason", "No DataSource configured")
                    .build();
        }

        try {
            long startTime = System.currentTimeMillis();
            try (java.sql.Connection connection = dataSource.getConnection();
                 java.sql.Statement statement = connection.createStatement()) {
                statement.execute(VALIDATION_QUERY);
            }
            long duration = System.currentTimeMillis() - startTime;

            return Health.up()
                    .withDetail("database", "available")
                    .withDetail("validationQuery", VALIDATION_QUERY)
                    .withDetail("responseTimeMs", duration)
                    .build();
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
