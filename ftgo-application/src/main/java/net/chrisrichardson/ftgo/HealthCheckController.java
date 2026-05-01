package net.chrisrichardson.ftgo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

  private final DataSource dataSource;

  public HealthCheckController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @RequestMapping(path = "/health", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");

    Map<String, String> dbStatus = new HashMap<>();
    try (Connection connection = dataSource.getConnection()) {
      if (!connection.isValid(2)) {
        throw new RuntimeException("Database connection is not valid");
      }
      dbStatus.put("status", "UP");
    } catch (Exception e) {
      dbStatus.put("status", "DOWN");
      dbStatus.put("error", e.getMessage());
      response.put("status", "DOWN");
      response.put("database", dbStatus);
      return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    response.put("database", dbStatus);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
