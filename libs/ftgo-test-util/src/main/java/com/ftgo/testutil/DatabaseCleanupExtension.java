package com.ftgo.testutil;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCleanupExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        ApplicationContext appContext = SpringExtension.getApplicationContext(context);
        DataSource dataSource = appContext.getBean(DataSource.class);
        cleanDatabase(dataSource);
    }

    public static void cleanDatabase(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            List<String> tableNames = getTableNames(connection);
            if (tableNames.isEmpty()) {
                return;
            }

            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                for (String tableName : tableNames) {
                    if (!isSystemTable(tableName)) {
                        statement.execute("TRUNCATE TABLE " + tableName);
                    }
                }
            } finally {
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
    }

    private static List<String> getTableNames(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(
                connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private static boolean isSystemTable(String tableName) {
        return tableName.startsWith("flyway_")
                || tableName.startsWith("FLYWAY_")
                || tableName.equalsIgnoreCase("schema_version");
    }
}
