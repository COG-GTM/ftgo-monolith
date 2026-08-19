package net.chrisrichardson.ftgo.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authenticates restaurant/courier operators with the shared token configured via
 * {@code ftgo.staff.api-token}. Access is denied when no token is configured.
 */
public class StaffAuthenticator {

  public static final String STAFF_TOKEN_HEADER = "X-Ftgo-Staff-Token";

  private final String configuredToken;

  public StaffAuthenticator(String configuredToken) {
    this.configuredToken = configuredToken == null ? "" : configuredToken.trim();
  }

  public AuthenticatedStaff authenticate(String presentedToken) {
    if (configuredToken.isEmpty())
      throw new UnauthenticatedException("Staff API access is not configured");

    String presented = presentedToken == null ? "" : presentedToken.trim();
    if (!MessageDigest.isEqual(presented.getBytes(StandardCharsets.UTF_8),
            configuredToken.getBytes(StandardCharsets.UTF_8)))
      throw new UnauthenticatedException("A valid staff token is required");

    return new AuthenticatedStaff();
  }
}
