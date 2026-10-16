package net.chrisrichardson.ftgo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes the browser Sentry settings so the dashboard can initialise the
 * Sentry SDK without the DSN being committed to the static assets.
 */
@RestController
public class SentryConfigController {

  @Value("${sentry.dsn:}")
  private String dsn;

  @Value("${sentry.environment:demo}")
  private String environment;

  @Value("${sentry.send-default-pii:false}")
  private boolean sendDefaultPii;

  @GetMapping("/config/sentry")
  public Map<String, Object> sentryConfig() {
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("dsn", dsn);
    config.put("environment", environment);
    config.put("sendDefaultPii", sendDefaultPii);
    return config;
  }
}
