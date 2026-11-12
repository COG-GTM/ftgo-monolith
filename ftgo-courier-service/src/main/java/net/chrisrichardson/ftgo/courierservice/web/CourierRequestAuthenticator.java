package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CourierRequestAuthenticator {

  private static final Logger logger = LoggerFactory.getLogger(CourierRequestAuthenticator.class);

  private final CourierService courierService;
  private final String operatorToken;

  public CourierRequestAuthenticator(CourierService courierService,
                                     @Value("${ftgo.security.operator-token:}") String operatorToken) {
    this.courierService = courierService;
    this.operatorToken = operatorToken;
  }

  @PostConstruct
  public void warnIfOperatorAuthenticationDisabled() {
    if (operatorToken.trim().isEmpty()) {
      logger.warn("Courier operator authentication is disabled because no operator token is configured");
    }
  }

  public CourierPrincipal authenticate(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new CourierAuthenticationException();
    }
    String token = authorizationHeader.substring("Bearer ".length());
    if (token.isEmpty() || !token.equals(token.trim())) {
      throw new CourierAuthenticationException();
    }

    if (!operatorToken.trim().isEmpty() && MessageDigest.isEqual(
            operatorToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
      return CourierPrincipal.operator();
    }

    Courier courier = courierService.findCourierByAccessToken(token)
            .orElseThrow(CourierAuthenticationException::new);
    return CourierPrincipal.courier(courier.getId());
  }

  public void requireOperator(CourierPrincipal principal) {
    if (!principal.isOperator()) {
      throw new CourierAccessDeniedException();
    }
  }

  public void requireCourierOrOperator(CourierPrincipal principal, long courierId) {
    if (!principal.isOperator() && principal.getCourierId() != courierId) {
      throw new CourierAccessDeniedException();
    }
  }
}
