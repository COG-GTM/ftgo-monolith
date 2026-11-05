package net.chrisrichardson.ftgo.courierservice.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class CourierAccessTokens {

  private static final SecureRandom secureRandom = new SecureRandom();
  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  private CourierAccessTokens() {
  }

  public static String generate() {
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  public static String hash(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
              .digest(token.getBytes(StandardCharsets.UTF_8));
      char[] hex = new char[digest.length * 2];
      for (int i = 0; i < digest.length; i++) {
        int value = digest[i] & 0xff;
        hex[i * 2] = HEX_DIGITS[value >>> 4];
        hex[i * 2 + 1] = HEX_DIGITS[value & 0x0f];
      }
      return new String(hex);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
