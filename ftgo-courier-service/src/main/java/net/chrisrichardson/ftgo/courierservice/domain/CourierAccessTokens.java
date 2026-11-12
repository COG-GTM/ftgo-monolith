package net.chrisrichardson.ftgo.courierservice.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class CourierAccessTokens {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private CourierAccessTokens() {
  }

  public static String generate() {
    byte[] tokenBytes = new byte[32];
    SECURE_RANDOM.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  public static String hash(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
              .digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte value : digest) {
        hex.append(String.format("%02x", value & 0xff));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }
}
