package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApiTrackingAuthInterceptor implements HandlerInterceptor {

  static final String ADMIN_TOKEN_HEADER = "X-Api-Tracking-Token";

  private final String configuredToken;

  public ApiTrackingAuthInterceptor(String configuredToken) {
    this.configuredToken = configuredToken;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (configuredToken == null || configuredToken.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return false;
    }

    String presentedToken = request.getHeader(ADMIN_TOKEN_HEADER);
    if (presentedToken == null || !constantTimeEquals(configuredToken, presentedToken)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    return true;
  }

  private static boolean constantTimeEquals(String expected, String presented) {
    return MessageDigest.isEqual(sha256(expected), sha256(presented));
  }

  private static byte[] sha256(String value) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required to compare API tracking tokens", e);
    }
  }
}
