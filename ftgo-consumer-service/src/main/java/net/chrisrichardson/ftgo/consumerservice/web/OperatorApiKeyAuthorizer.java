package net.chrisrichardson.ftgo.consumerservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authorizes operator-only endpoints using a shared secret presented as
 * {@code Authorization: Bearer <key>}. When no key is configured every request is
 * rejected, so the endpoints it protects fail closed.
 */
@Component
public class OperatorApiKeyAuthorizer {

  private static final String BEARER_PREFIX = "Bearer ";

  private final byte[] operatorApiKey;

  public OperatorApiKeyAuthorizer(@Value("${ftgo.security.operator-api-key:}") String operatorApiKey) {
    this.operatorApiKey = operatorApiKey == null || operatorApiKey.trim().isEmpty()
            ? null
            : operatorApiKey.trim().getBytes(StandardCharsets.UTF_8);
  }

  public boolean isOperator(HttpServletRequest request) {
    if (operatorApiKey == null) {
      return false;
    }
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      return false;
    }
    byte[] presented = header.substring(BEARER_PREFIX.length()).trim().getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(operatorApiKey, presented);
  }
}
