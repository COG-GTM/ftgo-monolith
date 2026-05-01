package net.chrisrichardson.ftgo;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HealthCheckControllerTest {

  private DataSource dataSource;
  private Connection connection;
  private HealthCheckController controller;

  @Before
  public void setUp() throws Exception {
    dataSource = mock(DataSource.class);
    connection = mock(Connection.class);
    controller = new HealthCheckController(dataSource);
  }

  @Test
  public void shouldReturnUpWhenDatabaseIsHealthy() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.isValid(2)).thenReturn(true);

    ResponseEntity<Map<String, Object>> response = controller.health();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("UP", response.getBody().get("status"));

    @SuppressWarnings("unchecked")
    Map<String, String> dbStatus = (Map<String, String>) response.getBody().get("database");
    assertEquals("UP", dbStatus.get("status"));

    verify(connection).close();
  }

  @Test
  public void shouldReturnDownWhenDatabaseIsUnhealthy() throws Exception {
    when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

    ResponseEntity<Map<String, Object>> response = controller.health();

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals("DOWN", response.getBody().get("status"));

    @SuppressWarnings("unchecked")
    Map<String, String> dbStatus = (Map<String, String>) response.getBody().get("database");
    assertEquals("DOWN", dbStatus.get("status"));
    assertEquals("Connection refused", dbStatus.get("error"));
  }
}
