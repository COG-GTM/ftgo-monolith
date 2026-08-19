package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Restricts the API tracking endpoints to operators authenticating with HTTP Basic
 * credentials. Access is denied when no credentials are configured.
 */
public class ApiTrackingAuthenticationFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(ApiTrackingAuthenticationFilter.class);
  private static final String BASIC_PREFIX = "Basic ";
  private static final String REALM = "ftgo-api-tracking";

  private final ApiTrackingSecurityProperties properties;

  public ApiTrackingAuthenticationFilter(ApiTrackingSecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
          throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (!properties.isConfigured()) {
      logger.warn("Rejecting {} {}: API tracking credentials are not configured",
              request.getMethod(), request.getRequestURI());
      sendError(response, HttpServletResponse.SC_FORBIDDEN, false);
      return;
    }

    if (!isAuthenticated(request.getHeader("Authorization"))) {
      sendError(response, HttpServletResponse.SC_UNAUTHORIZED, true);
      return;
    }

    chain.doFilter(servletRequest, servletResponse);
  }

  private boolean isAuthenticated(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
      return false;
    }
    String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(authorizationHeader.substring(BASIC_PREFIX.length()).trim()),
              StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return false;
    }
    int separator = decoded.indexOf(':');
    if (separator < 0) {
      return false;
    }
    String username = decoded.substring(0, separator);
    String password = decoded.substring(separator + 1);
    return constantTimeEquals(properties.getUsername(), username)
            & constantTimeEquals(properties.getPassword(), password);
  }

  private static boolean constantTimeEquals(String expected, String provided) {
    return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
            provided.getBytes(StandardCharsets.UTF_8));
  }

  private static void sendError(HttpServletResponse response, int status, boolean challenge) throws IOException {
    if (challenge) {
      response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
    }
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"Access to the API tracking endpoints is restricted\"}");
  }
}
