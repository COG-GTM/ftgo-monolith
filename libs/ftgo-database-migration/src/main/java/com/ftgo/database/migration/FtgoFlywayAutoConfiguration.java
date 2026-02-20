package com.ftgo.database.migration;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@AutoConfiguration(before = FlywayAutoConfiguration.class)
@ConditionalOnClass({Flyway.class, DataSource.class})
@ConditionalOnProperty(prefix = "ftgo.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlywayMigrationProperties.class)
@Import(FlywayMigrationConfiguration.class)
public class FtgoFlywayAutoConfiguration {
}
