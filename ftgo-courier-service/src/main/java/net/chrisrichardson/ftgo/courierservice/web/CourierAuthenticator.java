package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Component
public class CourierAuthenticator {

  private final CourierService courierService;
  private final String operatorToken;

  public CourierAuthenticator(CourierService courierService,
                              @Value("${ftgo.courier.operator-token:}") String operatorToken) {
    this.courierService = courierService;
    this.operatorToken = operatorToken;
  }

  public CourierPrincipal authenticate(String authorizationHeader) {
    if (authorizationHeader == null) {
      throw new CourierAuthenticationException();
    }

    String[] parts = authorizationHeader.trim().split("\\s+");
    if (parts.length != 2 || !"Bearer".equalsIgnoreCase(parts[0]) || parts[1].trim().isEmpty()) {
      throw new CourierAuthenticationException();
    }

    String token = parts[1];
    if (operatorToken != null && !operatorToken.trim().isEmpty()
            && MessageDigest.isEqual(sha256(operatorToken), sha256(token))) {
      return CourierPrincipal.operator();
    }

    Optional<Long> courierId = courierService.findCourierIdByAccessToken(token);
    return courierId.map(CourierPrincipal::courier)
            .orElseThrow(CourierAuthenticationException::new);
  }

  public void requireOperator(CourierPrincipal principal) {
    if (!principal.isOperator()) {
      throw new CourierAccessDeniedException();
    }
  }

  public void requireCanManage(CourierPrincipal principal, long courierId) {
    if (!principal.canManage(courierId)) {
      throw new CourierAccessDeniedException();
    }
  }

  private byte[] sha256(String value) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
