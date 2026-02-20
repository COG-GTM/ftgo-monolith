package com.ftgo.database.migration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        FlywayMigrationProperties properties = new FlywayMigrationProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isBaselineOnMigrate()).isTrue();
        assertThat(properties.getBaselineVersion()).isEqualTo("0");
        assertThat(properties.isValidateOnMigrate()).isTrue();
        assertThat(properties.isOutOfOrder()).isFalse();
        assertThat(properties.getLocations()).containsExactly("classpath:db/migration");
        assertThat(properties.getTable()).isEqualTo("flyway_schema_history");
        assertThat(properties.isCleanDisabled()).isTrue();
    }

    @Test
    void shouldAllowCustomValues() {
        FlywayMigrationProperties properties = new FlywayMigrationProperties();
        properties.setEnabled(false);
        properties.setBaselineOnMigrate(false);
        properties.setBaselineVersion("1");
        properties.setValidateOnMigrate(false);
        properties.setOutOfOrder(true);
        properties.setLocations(new String[]{"classpath:db/custom"});
        properties.setTable("custom_history");
        properties.setCleanDisabled(false);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isBaselineOnMigrate()).isFalse();
        assertThat(properties.getBaselineVersion()).isEqualTo("1");
        assertThat(properties.isValidateOnMigrate()).isFalse();
        assertThat(properties.isOutOfOrder()).isTrue();
        assertThat(properties.getLocations()).containsExactly("classpath:db/custom");
        assertThat(properties.getTable()).isEqualTo("custom_history");
        assertThat(properties.isCleanDisabled()).isFalse();
    }
}
