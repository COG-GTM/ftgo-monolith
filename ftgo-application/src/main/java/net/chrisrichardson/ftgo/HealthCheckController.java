package net.chrisrichardson.ftgo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/health")
public class HealthCheckController {

  private final DataSource dataSource;

  public HealthCheckController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = new LinkedHashMap<>();
    health.put("status", "UP");

    Map<String, String> dbStatus = new LinkedHashMap<>();
    try (Connection conn = dataSource.getConnection()) {
      dbStatus.put("status", conn.isValid(2) ? "UP" : "DOWN");
      dbStatus.put("database", conn.getMetaData().getDatabaseProductName());
    } catch (Exception e) {
      dbStatus.put("status", "DOWN");
      dbStatus.put("error", e.getMessage());
      health.put("status", "DOWN");
      health.put("db", dbStatus);
      return ResponseEntity.status(503).body(health);
    }

    health.put("db", dbStatus);
    return ResponseEntity.ok(health);
  }
}
