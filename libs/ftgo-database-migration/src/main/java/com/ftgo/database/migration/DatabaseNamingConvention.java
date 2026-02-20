package com.ftgo.database.migration;

public final class DatabaseNamingConvention {

    private static final String DATABASE_PREFIX = "ftgo_";
    private static final String DATABASE_SUFFIX = "_db";

    private DatabaseNamingConvention() {
    }

    public static String databaseName(String serviceName) {
        return DATABASE_PREFIX + sanitize(serviceName) + DATABASE_SUFFIX;
    }

    public static String schemaHistoryTable(String serviceName) {
        return "flyway_schema_history";
    }

    public static String migrationLocation(String serviceName) {
        return "classpath:db/migration";
    }

    private static String sanitize(String name) {
        return name.replace("-", "_").toLowerCase();
    }

    public static final String ORDER_SERVICE_DB = "ftgo_order_service_db";
    public static final String CONSUMER_SERVICE_DB = "ftgo_consumer_service_db";
    public static final String COURIER_SERVICE_DB = "ftgo_courier_service_db";
    public static final String RESTAURANT_SERVICE_DB = "ftgo_restaurant_service_db";
}
