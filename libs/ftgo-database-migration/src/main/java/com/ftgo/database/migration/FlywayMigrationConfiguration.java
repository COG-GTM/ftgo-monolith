package com.ftgo.database.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayMigrationConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(FlywayMigrationProperties properties) {
        return flyway -> {
            if (!properties.isEnabled()) {
                return;
            }
            flyway.migrate();
        };
    }

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer(FlywayMigrationProperties properties) {
        return configuration -> configuration
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .baselineVersion(properties.getBaselineVersion())
                .validateOnMigrate(properties.isValidateOnMigrate())
                .outOfOrder(properties.isOutOfOrder())
                .locations(properties.getLocations())
                .table(properties.getTable())
                .cleanDisabled(properties.isCleanDisabled());
    }
}
