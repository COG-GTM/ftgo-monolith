package net.chrisrichardson.ftgo;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SentryConfigControllerTest {

  @Test
  public void sentryConfigUsesDefaultsWithoutBoot() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(SentryConfigController.class);
      context.refresh();

      Map<String, Object> expected = new LinkedHashMap<>();
      expected.put("dsn", "");
      expected.put("environment", "demo");
      expected.put("sendDefaultPii", false);

      Map<String, Object> actual = context.getBean(SentryConfigController.class).sentryConfig();
      assertEquals(expected, actual);
      assertEquals(Arrays.asList("dsn", "environment", "sendDefaultPii"),
              Arrays.asList(actual.keySet().toArray()));
    }
  }

  @Test
  public void sentryConfigUsesOverriddenPropertiesWithoutBoot() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      Map<String, Object> properties = new LinkedHashMap<>();
      properties.put("sentry.dsn", "https://key@o1.ingest.sentry.io/2");
      properties.put("sentry.environment", "prod");
      properties.put("sentry.send-default-pii", "true");
      context.getEnvironment().getPropertySources()
              .addFirst(new MapPropertySource("testProperties", properties));
      context.register(SentryConfigController.class);
      context.refresh();

      Map<String, Object> expected = new LinkedHashMap<>();
      expected.put("dsn", "https://key@o1.ingest.sentry.io/2");
      expected.put("environment", "prod");
      expected.put("sendDefaultPii", Boolean.TRUE);

      Map<String, Object> actual = context.getBean(SentryConfigController.class).sentryConfig();
      assertEquals(expected, actual);
      assertEquals(Arrays.asList("dsn", "environment", "sendDefaultPii"),
              Arrays.asList(actual.keySet().toArray()));
    }
  }

  @Test
  public void getSentryConfigReturnsJson() throws Exception {
    MockMvc mockMvc = mockMvcWithConfiguredController();

    mockMvc.perform(get("/config/sentry"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dsn").value("https://key@o1.ingest.sentry.io/2"))
            .andExpect(jsonPath("$.environment").value("prod"))
            .andExpect(jsonPath("$.sendDefaultPii").value(true));
  }

  @Test
  public void postSentryConfigIsNotAllowed() throws Exception {
    mockMvcWithConfiguredController().perform(post("/config/sentry"))
            .andExpect(status().isMethodNotAllowed());
  }

  private MockMvc mockMvcWithConfiguredController() {
    SentryConfigController controller = new SentryConfigController();
    ReflectionTestUtils.setField(controller, "dsn", "https://key@o1.ingest.sentry.io/2");
    ReflectionTestUtils.setField(controller, "environment", "prod");
    ReflectionTestUtils.setField(controller, "sendDefaultPii", true);
    return MockMvcBuilders.standaloneSetup(controller).build();
  }
}
