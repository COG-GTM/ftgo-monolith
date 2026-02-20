package com.ftgo.database.migration;

import org.flywaydb.core.api.configuration.FluentConfiguration;

@FunctionalInterface
public interface FlywayConfigurationCustomizer {

    FluentConfiguration customize(FluentConfiguration configuration);
}
