package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Guards the operator-only {@code /api/tracking/**} endpoints. Callers must present the
 * configured key via {@code Authorization: Bearer <key>} or {@code X-Tracking-Api-Key}.
 * When no key is configured the endpoints are disabled entirely.
 */
public class ApiTrackingAccessFilter extends OncePerRequestFilter {

  public static final String API_KEY_HEADER = "X-Tracking-Api-Key";
  private static final String BEARER_PREFIX = "Bearer ";

  private static final Logger logger = LoggerFactory.getLogger(ApiTrackingAccessFilter.class);

  private final byte[] expectedKey;

  public ApiTrackingAccessFilter(String apiKey) {
    this.expectedKey = apiKey == null || apiKey.trim().isEmpty()
            ? null
            : apiKey.trim().getBytes(StandardCharsets.UTF_8);
    if (this.expectedKey == null) {
      logger.warn("ftgo.tracking.api-key is not set; /api/tracking endpoints are disabled");
    }
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    if (expectedKey == null) {
      response.sendError(HttpStatus.NOT_FOUND.value());
      return;
    }

    String presented = presentedKey(request);
    if (presented == null) {
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"ftgo-tracking\"");
      response.sendError(HttpStatus.UNAUTHORIZED.value());
      return;
    }

    if (!MessageDigest.isEqual(expectedKey, presented.getBytes(StandardCharsets.UTF_8))) {
      response.sendError(HttpStatus.FORBIDDEN.value());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static String presentedKey(HttpServletRequest request) {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      String token = authorization.substring(BEARER_PREFIX.length()).trim();
      return token.isEmpty() ? null : token;
    }
    String header = request.getHeader(API_KEY_HEADER);
    if (header != null && !header.trim().isEmpty()) {
      return header.trim();
    }
    return null;
  }
}
