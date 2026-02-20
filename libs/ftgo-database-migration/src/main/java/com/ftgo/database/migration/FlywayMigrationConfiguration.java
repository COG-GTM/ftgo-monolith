package com.ftgo.database.migration;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationConfiguration {

    @Bean
    public FlywayConfigurationCustomizer ftgoFlywayConfigurationCustomizer(FlywayMigrationProperties properties) {
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
