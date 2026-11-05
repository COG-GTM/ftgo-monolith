package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ApiTrackingAccessInterceptor implements HandlerInterceptor {

  private final String operatorToken;

  public ApiTrackingAccessInterceptor(String operatorToken) {
    this.operatorToken = operatorToken;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
          throws IOException {
    if (operatorToken == null || operatorToken.trim().isEmpty()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
              "API tracking endpoints are disabled: no operator token configured");
      return false;
    }

    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")
            || !MessageDigest.isEqual(
                    authorization.substring("Bearer ".length()).getBytes(StandardCharsets.UTF_8),
                    operatorToken.getBytes(StandardCharsets.UTF_8))) {
      response.setHeader("WWW-Authenticate", "Bearer realm=\"api-tracking\"");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    return true;
  }
}
