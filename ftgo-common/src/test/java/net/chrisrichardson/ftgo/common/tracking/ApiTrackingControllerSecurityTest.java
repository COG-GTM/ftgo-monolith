package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.security.user.name=operator",
        "spring.security.user.password=operator-test-password",
        "spring.security.user.roles=OPERATOR"
})
@AutoConfigureMockMvc
public class ApiTrackingControllerSecurityTest {

  @Configuration
  @EnableAutoConfiguration(exclude = {
          DataSourceAutoConfiguration.class,
          DataSourceTransactionManagerAutoConfiguration.class,
          HibernateJpaAutoConfiguration.class
  })
  @Import(ApiTrackingConfiguration.class)
  static class TestConfig {

    @Bean
    public ApiRequestLogRepository apiRequestLogRepository() {
      ApiRequestLogRepository repository = mock(ApiRequestLogRepository.class);
      when(repository.findRecentLogs(any())).thenReturn(Collections.emptyList());
      when(repository.findErrorsSince(any())).thenReturn(Collections.emptyList());
      when(repository.findByRequestUri(any())).thenReturn(Collections.emptyList());
      when(repository.findByCorrelationId(any())).thenReturn(null);
      return repository;
    }

    @Bean
    public ApiTrackingController apiTrackingController(ApiRequestLogRepository repository) {
      return new ApiTrackingController(repository);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  private static final String[] TRACKING_PATHS = {
          "/api/tracking/logs",
          "/api/tracking/logs/errors",
          "/api/tracking/logs/search?uri=/orders",
          "/api/tracking/logs/some-correlation-id",
          "/api/tracking/stats"
  };

  @Test
  public void anonymousRequestsAreRejected() throws Exception {
    for (String path : TRACKING_PATHS) {
      mockMvc.perform(get(path))
              .andExpect(status().isUnauthorized())
              .andExpect(header().exists("WWW-Authenticate"));
    }
  }

  @Test
  public void wrongCredentialsAreRejected() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").header("Authorization", basic("operator", "wrong")))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void operatorCanReadLogs() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").header("Authorization", basic("operator", "operator-test-password")))
            .andExpect(status().isOk());
    mockMvc.perform(get("/api/tracking/stats").header("Authorization", basic("operator", "operator-test-password")))
            .andExpect(status().isOk());
  }

  private static String basic(String user, String password) {
    return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
  }
}
