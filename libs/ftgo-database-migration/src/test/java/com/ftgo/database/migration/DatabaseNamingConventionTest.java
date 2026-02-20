package com.ftgo.database.migration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseNamingConventionTest {

    @Test
    void shouldGenerateDatabaseNameFromServiceName() {
        assertThat(DatabaseNamingConvention.databaseName("order-service"))
                .isEqualTo("ftgo_order_service_db");
    }

    @Test
    void shouldHandleUnderscoresInServiceName() {
        assertThat(DatabaseNamingConvention.databaseName("consumer_service"))
                .isEqualTo("ftgo_consumer_service_db");
    }

    @Test
    void shouldReturnDefaultSchemaHistoryTable() {
        assertThat(DatabaseNamingConvention.schemaHistoryTable("order-service"))
                .isEqualTo("flyway_schema_history");
    }

    @Test
    void shouldReturnDefaultMigrationLocation() {
        assertThat(DatabaseNamingConvention.migrationLocation("order-service"))
                .isEqualTo("classpath:db/migration");
    }

    @Test
    void shouldDefineServiceDatabaseConstants() {
        assertThat(DatabaseNamingConvention.ORDER_SERVICE_DB).isEqualTo("ftgo_order_service_db");
        assertThat(DatabaseNamingConvention.CONSUMER_SERVICE_DB).isEqualTo("ftgo_consumer_service_db");
        assertThat(DatabaseNamingConvention.COURIER_SERVICE_DB).isEqualTo("ftgo_courier_service_db");
        assertThat(DatabaseNamingConvention.RESTAURANT_SERVICE_DB).isEqualTo("ftgo_restaurant_service_db");
    }
}
