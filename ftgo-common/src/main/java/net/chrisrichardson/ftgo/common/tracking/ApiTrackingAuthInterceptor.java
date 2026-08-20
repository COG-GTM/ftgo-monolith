package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    byte[] expectedBytes = expected.getBytes();
    byte[] presentedBytes = presented.getBytes();
    int result = expectedBytes.length ^ presentedBytes.length;
    for (int i = 0; i < presentedBytes.length; i++) {
      result |= presentedBytes[i] ^ expectedBytes[i % expectedBytes.length];
    }
    return result == 0;
  }
}
