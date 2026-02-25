package com.ftgo.common.jpa.migration;

/**
 * Configuration properties for per-service Flyway database migrations.
 *
 * <p>Each microservice configures its own Flyway migration properties
 * through Spring Boot's {@code spring.flyway.*} configuration namespace.
 * This class documents the standard property conventions used across
 * all FTGO microservices.</p>
 *
 * <h3>Standard Properties</h3>
 * <ul>
 *   <li>{@code spring.flyway.enabled} - Enable/disable Flyway (default: true)</li>
 *   <li>{@code spring.flyway.locations} - Migration script locations (default: classpath:db/migration)</li>
 *   <li>{@code spring.flyway.baseline-on-migrate} - Baseline existing databases (default: true for migration)</li>
 *   <li>{@code spring.flyway.table} - Flyway history table name (default: flyway_schema_history)</li>
 * </ul>
 *
 * <h3>Naming Convention</h3>
 * <p>Migration files follow the pattern:
 * {@code V{version}__{description}.sql} where version uses underscores
 * for separators (e.g., {@code V1__create_order_service_schema.sql}).</p>
 *
 * <h3>Per-Service Database Convention</h3>
 * <table>
 *   <tr><th>Service</th><th>Database</th><th>Migration Location</th></tr>
 *   <tr><td>Consumer Service</td><td>ftgo_consumer_service</td><td>classpath:db/migration</td></tr>
 *   <tr><td>Courier Service</td><td>ftgo_courier_service</td><td>classpath:db/migration</td></tr>
 *   <tr><td>Order Service</td><td>ftgo_order_service</td><td>classpath:db/migration</td></tr>
 *   <tr><td>Restaurant Service</td><td>ftgo_restaurant_service</td><td>classpath:db/migration</td></tr>
 * </table>
 */
public final class FlywayMigrationProperties {

    /** Default migration script location on the classpath. */
    public static final String DEFAULT_MIGRATION_LOCATION = "classpath:db/migration";

    /** Flyway schema history table name convention. */
    public static final String SCHEMA_HISTORY_TABLE = "flyway_schema_history";

    // Database name conventions per service
    public static final String CONSUMER_SERVICE_DB = "ftgo_consumer_service";
    public static final String COURIER_SERVICE_DB = "ftgo_courier_service";
    public static final String ORDER_SERVICE_DB = "ftgo_order_service";
    public static final String RESTAURANT_SERVICE_DB = "ftgo_restaurant_service";

    private FlywayMigrationProperties() {
        // Utility class - prevent instantiation
    }
}
