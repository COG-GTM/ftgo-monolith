package net.chrisrichardson.ftgo.consumerservice.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class ConsumerApiKeys {

  private static final int KEY_LENGTH_BYTES = 32;

  private static final SecureRandom RANDOM = new SecureRandom();

  private ConsumerApiKeys() {
  }

  public static String generateApiKey() {
    byte[] key = new byte[KEY_LENGTH_BYTES];
    RANDOM.nextBytes(key);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
  }

  public static String hash(String apiKey) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(apiKey.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
