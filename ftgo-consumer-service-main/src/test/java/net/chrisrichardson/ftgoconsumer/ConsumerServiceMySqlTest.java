package net.chrisrichardson.ftgoconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.chrisrichardson.ftgo.FtgoApplicationMain;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = ConsumerServiceMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.config.location=classpath:/application.yml")
class ConsumerServiceMySqlTest {

  @Container
  static final MySQLContainer CONSUMER_MYSQL = new MySQLContainer("mysql:8.4")
      .withDatabaseName("ftgo_consumer")
      .withUsername("ftgo_consumer_user");

  @Container
  static final MySQLContainer MONOLITH_MYSQL = new MySQLContainer("mysql:8.4")
      .withDatabaseName("ftgo");

  @DynamicPropertySource
  static void consumerDataSource(DynamicPropertyRegistry properties) {
    properties.add("spring.datasource.url", CONSUMER_MYSQL::getJdbcUrl);
    properties.add("spring.datasource.username", CONSUMER_MYSQL::getUsername);
    properties.add("spring.datasource.password", CONSUMER_MYSQL::getPassword);
  }

  @LocalServerPort
  private int consumerPort;
  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private ObjectMapper mapper;

  private final HttpClient http = HttpClient.newHttpClient();

  @Test
  void migratesEmptySchemaAndServesTheSamePublicJsonAsTheMonolith() throws Exception {
    Flyway.configure()
        .dataSource(MONOLITH_MYSQL.getJdbcUrl(), MONOLITH_MYSQL.getUsername(), MONOLITH_MYSQL.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();

    try (var context = new SpringApplicationBuilder(FtgoApplicationMain.class).run(
        "--spring.config.location=classpath:/application.properties",
        "--spring.datasource.url=" + MONOLITH_MYSQL.getJdbcUrl(),
        "--spring.datasource.username=" + MONOLITH_MYSQL.getUsername(),
        "--spring.datasource.password=" + MONOLITH_MYSQL.getPassword(),
        "--spring.flyway.enabled=false",
        "--sentry.dsn=",
        "--server.port=0")) {
      int monolithPort = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
      assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).isEqualTo("ftgo_consumer");
      assertThat(jdbc.queryForList("SHOW TABLES", String.class))
          .containsExactlyInAnyOrder("consumers", "hibernate_sequence", "flyway_schema_history");

      String name = "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}";
      assertPublicParity(monolithPort, "/consumers", "{\"name\":" + name + "}", 200, "{\"consumerId\":1}");
      assertPublicParity(monolithPort, "/consumers/1", null, 200, "{\"consumerId\":0,\"name\":" + name + "}");
      assertPublicParity(monolithPort, "/consumers/1/", null, 200, "{\"consumerId\":0,\"name\":" + name + "}");
      assertPublicParity(monolithPort, "/consumers/2", null, 404, "");
      assertMalformedCreateParity(monolithPort);
      assertMethodNotAllowedParity(monolithPort);
    }

    assertValidation(1, "{\"orderTotal\":\"24.68\"}", 204);
    assertValidation(2, "{\"orderTotal\":\"24.68\"}", 404);
    assertValidation(1, "{}", 400);
  }

  private void assertPublicParity(int monolithPort, String path, String request, int status, String expected)
      throws Exception {
    HttpResponse<byte[]> monolith = send(monolithPort, path, request);
    HttpResponse<byte[]> consumer = send(consumerPort, path, request);
    assertThat(monolith.statusCode()).isEqualTo(status);
    assertThat(consumer.statusCode()).isEqualTo(monolith.statusCode());
    assertThat(consumer.body()).containsExactly(monolith.body());
    assertThat(consumer.body()).containsExactly(expected.getBytes(UTF_8));
  }

  private void assertMalformedCreateParity(int monolithPort) throws Exception {
    HttpResponse<byte[]> monolith = send(monolithPort, "/consumers", "{");
    HttpResponse<byte[]> consumer = send(consumerPort, "/consumers", "{");
    assertThat(monolith.statusCode()).isEqualTo(400);
    assertThat(consumer.statusCode()).isEqualTo(monolith.statusCode());
    assertThat(monolith.headers().firstValue("X-Correlation-ID")).contains("consumer-parity");
    assertThat(consumer.headers().firstValue("X-Correlation-ID")).contains("consumer-parity");
    assertThat(withoutTimestamp(consumer.body())).containsExactly(withoutTimestamp(monolith.body()));
  }

  private void assertMethodNotAllowedParity(int monolithPort) throws Exception {
    HttpResponse<byte[]> monolith = deleteConsumer(monolithPort);
    HttpResponse<byte[]> consumer = deleteConsumer(consumerPort);
    assertThat(monolith.statusCode()).isEqualTo(405);
    assertThat(consumer.statusCode()).isEqualTo(monolith.statusCode());
    assertThat(monolith.headers().firstValue("X-Correlation-ID")).isEmpty();
    assertThat(consumer.headers().firstValue("X-Correlation-ID")).isEmpty();
    assertThat(withoutTimestamp(consumer.body())).containsExactly(withoutTimestamp(monolith.body()));
  }

  private HttpResponse<byte[]> deleteConsumer(int port) throws Exception {
    return http.send(request(port, "/consumers/1").DELETE().build(), HttpResponse.BodyHandlers.ofByteArray());
  }

  private byte[] withoutTimestamp(byte[] body) throws Exception {
    ObjectNode error = (ObjectNode) mapper.readTree(body);
    assertThat(error.remove("timestamp").asText()).isNotBlank();
    return mapper.writeValueAsBytes(error);
  }

  private void assertValidation(long consumerId, String request, int status) throws Exception {
    HttpResponse<byte[]> response = send(consumerPort, "/consumers/" + consumerId + "/validate-order", request);
    assertThat(response.statusCode()).isEqualTo(status);
    assertThat(response.body()).isEmpty();
  }

  private HttpResponse<byte[]> send(int port, String path, String json) throws Exception {
    HttpRequest.Builder request = request(port, path);
    if (json != null) {
      request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json));
    }
    return http.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
  }

  private HttpRequest.Builder request(int port, String path) {
    return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
        .header("X-Correlation-ID", "consumer-parity");
  }
}
