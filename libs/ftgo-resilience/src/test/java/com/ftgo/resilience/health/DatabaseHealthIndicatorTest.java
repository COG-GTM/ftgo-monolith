package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    private DatabaseHealthIndicator healthIndicator;
    private ResilienceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ResilienceProperties();
        healthIndicator = new DatabaseHealthIndicator(properties);
    }

    @Test
    void shouldReturnUnknownWhenNoDataSource() {
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsKey("reason");
    }

    @Test
    void shouldReturnUpWhenDatabaseIsHealthy() throws SQLException {
        healthIndicator.setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0");
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/ftgo");

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "MySQL");
        assertThat(health.getDetails()).containsKey("responseTimeMs");
    }

    @Test
    void shouldReturnDownWhenConnectionValidationFails() throws SQLException {
        healthIndicator.setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(false);

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "Connection validation failed");
    }

    @Test
    void shouldReturnDownWhenSQLExceptionOccurs() throws SQLException {
        healthIndicator.setDataSource(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused", "08001", 1045));

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Connection refused");
        assertThat(health.getDetails()).containsEntry("sqlState", "08001");
    }
}
