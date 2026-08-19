package net.chrisrichardson.ftgo.common.security;

import java.security.SecureRandom;
import java.util.Base64;

public class AccessTokens {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 32;

  private AccessTokens() {
  }

  public static String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static String bearerToken(String authorizationHeader) {
    if (authorizationHeader == null)
      return null;
    String header = authorizationHeader.trim();
    if (header.length() <= 7 || !header.regionMatches(true, 0, "Bearer ", 0, 7))
      return null;
    String token = header.substring(7).trim();
    return token.isEmpty() ? null : token;
  }
}
