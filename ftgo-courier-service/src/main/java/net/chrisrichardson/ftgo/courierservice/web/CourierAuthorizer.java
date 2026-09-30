package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierAccessDeniedException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierAuthenticationException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CourierAuthorizer {

  public static final String DISPATCHER_API_KEY_HEADER = "X-Dispatcher-Api-Key";
  private static final String BEARER_PREFIX = "Bearer ";

  private final CourierService courierService;
  private final String dispatcherApiKey;

  public CourierAuthorizer(CourierService courierService, String dispatcherApiKey) {
    this.courierService = courierService;
    this.dispatcherApiKey = dispatcherApiKey == null || dispatcherApiKey.isEmpty() ? null : dispatcherApiKey;
  }

  public void requireAccessTo(HttpServletRequest request, long courierId) {
    if (isDispatcher(request)) {
      return;
    }
    Courier caller = courierService.findCourierByAccessToken(bearerToken(request))
            .orElseThrow(CourierAuthenticationException::new);
    if (caller.getId() == null || caller.getId() != courierId) {
      throw new CourierAccessDeniedException(courierId);
    }
  }

  private boolean isDispatcher(HttpServletRequest request) {
    String presented = request.getHeader(DISPATCHER_API_KEY_HEADER);
    if (dispatcherApiKey == null || presented == null) {
      return false;
    }
    return MessageDigest.isEqual(dispatcherApiKey.getBytes(StandardCharsets.UTF_8),
            presented.getBytes(StandardCharsets.UTF_8));
  }

  private String bearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      throw new CourierAuthenticationException();
    }
    String token = header.substring(BEARER_PREFIX.length()).trim();
    if (token.isEmpty()) {
      throw new CourierAuthenticationException();
    }
    return token;
  }
}
