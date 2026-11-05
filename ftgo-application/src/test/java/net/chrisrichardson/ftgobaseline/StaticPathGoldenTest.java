package net.chrisrichardson.ftgobaseline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.chrisrichardson.ftgo.GlobalExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StaticPathGoldenTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StaticPathGoldenTest {

  @Configuration
  @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
          HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
  @Import(GlobalExceptionHandler.class)
  static class Config {
  }

  @Autowired private TestRestTemplate http;

  @Test
  public void unknownStaticPathUsesContainerErrorDispatch() throws Exception {
    ResponseEntity<String> response = http.getForEntity("/this-static-path-does-not-exist.js", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode actual = (ObjectNode) mapper.readTree(response.getBody());
    JsonNode timestamp = actual.get("timestamp");
    assertThat(timestamp.isTextual()).isTrue();
    OffsetDateTime.parse(timestamp.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxx"));
    actual.put("timestamp", "<timestamp>");
    try (java.io.InputStream input = new ClassPathResource("golden/static-not-found.json").getInputStream()) {
      assertThat(actual).isEqualTo(mapper.readTree(input));
    }
  }
}
