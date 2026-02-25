package com.ftgo.common.jpa.migration;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Utility for validating per-service database migration state.
 *
 * <p>Provides methods to verify that a service's database schema is correctly
 * initialized and that Flyway migrations have been applied. This is useful
 * during the transition from the shared monolith database to per-service
 * databases.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @Autowired
 * private JdbcTemplate jdbcTemplate;
 *
 * DatabaseMigrationValidator validator = new DatabaseMigrationValidator(jdbcTemplate);
 * boolean valid = validator.isSchemaInitialized("flyway_schema_history");
 * }</pre>
 */
public class DatabaseMigrationValidator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Checks whether the Flyway schema history table exists, indicating
     * that migrations have been applied to this database.
     *
     * @param schemaHistoryTable the name of the Flyway history table
     * @return true if the schema history table exists
     */
    public boolean isSchemaInitialized(String schemaHistoryTable) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schemaHistoryTable);
        return count != null && count > 0;
    }

    /**
     * Returns the number of applied Flyway migrations.
     *
     * @param schemaHistoryTable the name of the Flyway history table
     * @return the count of successfully applied migrations, or 0 if table does not exist
     */
    public int getAppliedMigrationCount(String schemaHistoryTable) {
        if (!isSchemaInitialized(schemaHistoryTable)) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM " + schemaHistoryTable + " WHERE success = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Validates that a specific table exists in the current database.
     *
     * @param tableName the table name to check
     * @return true if the table exists
     */
    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    }

    /**
     * Lists all tables in the current database (excluding system tables).
     *
     * @return list of table names
     */
    public List<String> listTables() {
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() ORDER BY table_name";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
