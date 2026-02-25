package com.ftgo.common.jpa.migration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FlywayMigrationPropertiesTest {

    @Test
    public void shouldDefineDefaultMigrationLocation() {
        assertEquals("classpath:db/migration", FlywayMigrationProperties.DEFAULT_MIGRATION_LOCATION);
    }

    @Test
    public void shouldDefineSchemaHistoryTable() {
        assertEquals("flyway_schema_history", FlywayMigrationProperties.SCHEMA_HISTORY_TABLE);
    }

    @Test
    public void shouldDefineConsumerServiceDatabase() {
        assertEquals("ftgo_consumer_service", FlywayMigrationProperties.CONSUMER_SERVICE_DB);
    }

    @Test
    public void shouldDefineCourierServiceDatabase() {
        assertEquals("ftgo_courier_service", FlywayMigrationProperties.COURIER_SERVICE_DB);
    }

    @Test
    public void shouldDefineOrderServiceDatabase() {
        assertEquals("ftgo_order_service", FlywayMigrationProperties.ORDER_SERVICE_DB);
    }

    @Test
    public void shouldDefineRestaurantServiceDatabase() {
        assertEquals("ftgo_restaurant_service", FlywayMigrationProperties.RESTAURANT_SERVICE_DB);
    }
}
