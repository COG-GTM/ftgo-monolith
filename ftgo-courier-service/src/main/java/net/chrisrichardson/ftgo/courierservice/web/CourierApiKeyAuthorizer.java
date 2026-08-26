package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CourierApiKeyAuthorizer {

  private final String configuredApiKey;

  public CourierApiKeyAuthorizer(@Value("${ftgo.courier.api-key:}") String configuredApiKey) {
    this.configuredApiKey = configuredApiKey;
  }

  public boolean isAuthorized(String providedApiKey) {
    if (configuredApiKey == null || configuredApiKey.isEmpty() || providedApiKey == null) {
      return false;
    }
    return MessageDigest.isEqual(configuredApiKey.getBytes(StandardCharsets.UTF_8),
            providedApiKey.getBytes(StandardCharsets.UTF_8));
  }
}
