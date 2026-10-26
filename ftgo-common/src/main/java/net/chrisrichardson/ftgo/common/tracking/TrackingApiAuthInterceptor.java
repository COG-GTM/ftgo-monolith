package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Guards the tracking API endpoints. Access requires the {@value #API_KEY_HEADER}
 * header to match the key configured via {@code ftgo.tracking.api.key}. When no
 * key is configured, all access is denied.
 */
public class TrackingApiAuthInterceptor implements HandlerInterceptor {

  static final String API_KEY_HEADER = "X-Tracking-Api-Key";

  private final String apiKey;

  public TrackingApiAuthInterceptor(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    if (apiKey == null || apiKey.isEmpty() || !constantTimeEquals(apiKey, request.getHeader(API_KEY_HEADER))) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    return true;
  }

  private static boolean constantTimeEquals(String expected, String actual) {
    if (actual == null) {
      return false;
    }
    return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
  }
}
