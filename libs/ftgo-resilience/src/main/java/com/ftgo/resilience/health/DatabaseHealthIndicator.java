package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseHealthIndicator implements HealthIndicator {

    private final ResilienceProperties properties;
    private DataSource dataSource;

    public DatabaseHealthIndicator(ResilienceProperties properties) {
        this.properties = properties;
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

        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(properties.getHealthCheck().getTimeoutMs() / 1000);
            long responseTime = System.currentTimeMillis() - startTime;

            if (valid) {
                return Health.up()
                        .withDetail("database", connection.getMetaData().getDatabaseProductName())
                        .withDetail("version", connection.getMetaData().getDatabaseProductVersion())
                        .withDetail("url", connection.getMetaData().getURL())
                        .withDetail("responseTimeMs", responseTime)
                        .build();
            }

            return Health.down()
                    .withDetail("reason", "Connection validation failed")
                    .withDetail("responseTimeMs", responseTime)
                    .build();
        } catch (SQLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorCode", e.getErrorCode())
                    .withDetail("sqlState", e.getSQLState())
                    .withDetail("responseTimeMs", responseTime)
                    .build();
        }
    }
}
