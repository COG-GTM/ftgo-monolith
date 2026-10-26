package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Requires requests addressed to a specific courier to carry that courier's bearer token.
 */
public class CourierAuthenticationInterceptor implements HandlerInterceptor {

  public static final String[] PATH_PATTERNS = {"/couriers/{courierId}", "/couriers/{courierId}/**"};

  private static final String BEARER_PREFIX = "Bearer ";

  private final CourierService courierService;

  public CourierAuthenticationInterceptor(CourierService courierService) {
    this.courierService = courierService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    Long courierId = courierIdFromPath(request);
    String accessToken = bearerToken(request);
    if (courierId == null || accessToken == null || !courierService.authenticate(courierId, accessToken)) {
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    return true;
  }

  private static Long courierIdFromPath(HttpServletRequest request) {
    Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(attribute instanceof Map)) {
      return null;
    }
    Object courierId = ((Map<?, ?>) attribute).get("courierId");
    if (!(courierId instanceof String)) {
      return null;
    }
    try {
      return Long.valueOf((String) courierId);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static String bearerToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      return null;
    }
    String token = header.substring(BEARER_PREFIX.length()).trim();
    return token.isEmpty() ? null : token;
  }
}
